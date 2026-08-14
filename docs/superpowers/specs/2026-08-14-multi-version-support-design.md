# EnchantPeak 多版本支持设计（方案 C：版本族 source set）

日期：2026-08-14
状态：待评审

## 目标

一个仓库、一条构建命令，产出覆盖以下 Minecraft 版本的 EnchantPeak jar：

- 1.18.x（以 1.18.2 为代表构建目标）
- 1.19.x（以 1.19.2 / 1.19.4 为代表）
- 1.20.x（以 1.20.1 / 1.20.4 / 1.20.6 为代表）
- 1.21.x（现有 1.21.11，可扩展 1.21.1）
- 26.x（现有 26.1 / 26.1.1 / 26.1.2 / 26.2）

每个版本同时支持三个 recipe viewer 插件：**JEI、REI、EMI**（EMI 从 1.19.4 起才有，1.18.2 无 EMI，见"调研结论"）。

非目标：不做 Forge/NeoForge 移植；不支持 1.18.2 以下的版本；不引入 Stonecutter 预处理框架。

## 背景与约束

现有代码库是单 source set（`src/main`），通过 `versions/minecraft.json` + `-Ptarget_mc` 切换构建目标，当前只覆盖 1.21.11 和 26.x。

跨版本的 API 断层（调研确认）：

| 维度 | 1.18–1.20 | 1.21.x | 26.x |
|---|---|---|---|
| `Enchantments.X` 常量类型 | `Enchantment` 实例（`BuiltInRegistries`） | `ResourceKey<Enchantment>` | 同左 |
| 附魔写入 ItemStack | NBT（`EnchantmentHelper.setEnchantments`） | 数据组件（`stack.enchant(holder, lvl)`） | 同左 |
| 附魔显示名 | `enchantment.getFullname(level)` | `holder.value().description()` | 同左 |
| `Identifier` | `new ResourceLocation(ns, path)` | 同左 | `Identifier.fromNamespaceAndPath` |
| Java | 17（1.20.6 起 21） | 21 | 25 |
| Mace / Spear / Copper 工具 | 无 | Mace（1.21+） | Spear、Copper 工具 |

结论：不存在一份代码同时编译到三代 API。但代码量很小（核心约 700 行），每个版本的差异集中且稳定，**不需要 Stonecutter 级别的预处理框架**，用"共享 src/main + 按版本族激活的额外 source set"即可。

## 总体架构

```
src/
  main/java/com/zhaojiedi1992/enchantpeak/
    EnchantPeakMod.java              # 入口（版本无关）
    common/
      EnchantEntry.java              # record(Holder<Enchantment>, int level)
      EnchantGroup.java              # record(String name, List<EnchantEntry>) —— 纯数据，无渲染逻辑
      ItemEnchantRecord.java         # record(Item, List<EnchantGroup>)
  main/resources/                    # fabric.mod.json / lang / icon（所有版本共用）

  mc118/java/...  mc119/java/...  mc120/java/...  mc121/java/...  mc26/java/...
    # 每个版本族 source set 提供完全相同的全限定类名：
    com.zhaojiedi1992.enchantpeak.data.EnchantmentData      # 该版本的附魔数据表
    com.zhaojiedi1992.enchantpeak.compat.EnchantStacks      # applyTo / enchantmentLines 渲染辅助
    com.zhaojiedi1992.enchantpeak.jei.JeiEnchantPlugin      # JEI 插件（+Category）
    com.zhaojiedi1992.enchantpeak.rei.ReiEnchantPlugin      # REI 插件（+Category/Display）
    com.zhaojiedi1992.enchantpeak.emi.EmiEnchantPlugin      # EMI 插件（mc119.4 起）
```

关键设计决策：

1. **common 层保留 `Holder<Enchantment>`，不用字符串 key。** `Holder` 自 1.18.2 起就存在，三个 API 时代都能编译；旧版本通过 `BuiltInRegistries.ENCHANTMENT.getHolderOrThrow(key)` 获得，新版本通过 `RegistryAccess` lookup 获得。这样 common 数据模型零改动复用，不需要字符串间接层。

2. **渲染/写入逻辑从 common 移出。** 现有 `EnchantGroup.applyTo()`（用 `DataComponents`）和 `enchantmentLines()`（用 `description()`）是 1.21+ API，移入各版本族的 `compat/EnchantStacks` 静态工具类。`EnchantGroup` 退化为纯数据 record + `name` 字符串字段（`displayName()` 也移入 `EnchantStacks`，因为 1.18.2 的 `Component.translatable` 签名可能不同）。

**EnchantStacks 跨版本实现差异**：
- **mc26 / mc121**：`applyTo` 用 `stack.enchant(holder, level)`，`enchantmentLines` 用 `holder.value().description()`
- **mc120 / mc119 / mc118**：`applyTo` 用 `EnchantmentHelper.setEnchantments(Map<Enchantment, Integer>)`，`enchantmentLines` 用 `enchantment.getFullname(level)`

3. **版本族 source set 内的类名完全对齐。** 每个族都提供同名同包的 `EnchantmentData` / `JeiEnchantPlugin` / `ReiEnchantPlugin` / `EmiEnchantPlugin`，因此一份 `fabric.mod.json` 对所有版本生效，entrypoint 类名无需参数化。

4. **一次构建只激活一个版本族。** `build.gradle` 根据 `versions/minecraft.json` 中目标条目的 `mc_family` 字段把对应 source set 挂进编译和 jar；不激活的族完全不参与编译，天然避免类冲突。

## Gradle 构建组织

### Loom 版本动态切换

**核心问题**：MC 26.x 已去混淆（不再需要 yarn/mojmap），推荐用 Loom 1.15+，而 1.18–1.21 仍需要 mappings + remap，用 Loom 1.7/1.8。

**修正后方案：单一 Loom 版本 + 条件配置**

使用 **Loom 1.8（或 1.9）+ Gradle 8.x**，通过 `fabric.loom.disableObfuscation` 让 26.x 目标跳过 remap：

```groovy
// build.gradle
plugins {
    id 'fabric-loom' version '1.8-SNAPSHOT'
}

def mcVer = project.minecraft_version
def is26x = mcVer.startsWith('26.') || mcVer == '1.21.11'  // 1.21.11 也已去混淆

if (is26x) {
    // 26.x 配置：无 mappings，implementation 替代 modImplementation
    dependencies {
        minecraft "com.mojang:minecraft:${mcVer}"
        implementation "net.fabricmc:fabric-loader:${project.fabric_loader_version}"
        implementation "net.fabricmc.fabric-api:fabric-api:${project.fabric_api_version}"
    }
} else {
    // 1.18–1.20 配置：需要 mappings + remap
    dependencies {
        minecraft "com.mojang:minecraft:${mcVer}"
        mappings loom.officialMojangMappings()
        modImplementation "net.fabricmc:fabric-loader:${project.fabric_loader_version}"
        modImplementation "net.fabricmc.fabric-api:fabric-api:${project.fabric_api_version}"
    }
}
```

**理由**：
- Loom 1.8 可以构建 26.x（只是不执行 remap，但不会报错）
- `disableObfuscation` 配置项告诉 Loom 跳过映射步骤
- 避免 settings.gradle 动态加载的时机问题（settings 阶段无法读取 `-P` 参数）
- 单一配置文件，条件逻辑清晰

**风险缓解**：若 Loom 1.8 对 26.x 兼容性有问题，升级到 Loom 1.9 或 1.10（仍支持旧版本 mappings）。

### Java Toolchain 配置

```groovy
// 来自 versions/minecraft.json 的 java_version（17 / 21 / 25）
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(project.ext.java_version as int)
    }
}
```

- Gradle 9.4（Loom 1.15 要求）本身需要 Java 17+ 运行
- toolchain 自动下载缺失的 JDK（如本机无 Java 25 时自动获取 Temurin 25）
- 编译目标 JDK 与运行 Gradle 的 JDK 独立，无需手动切换 JAVA_HOME

### Source Set 配置

`versions/minecraft.json` 每个条目新增 `mc_family` 字段：

```json
"1.20.1": {
  "minecraft_version": "1.20.1",
  "mc_family": "mc120",
  "java_version": "17",
  "use_mojang_mappings": "true",
  "fabric_loader_version": "0.15.0",
  "fabric_api_version": "0.92.0+1.20.1",
  "jei_enabled": "true", "jei_minecraft_version": "1.20.1", "jei_version": "15.49.0.188", "jei_min_version": "15.49.0",
  "rei_version": "15.0.787", "rei_min_version": "15.0",
  "cloth_config_version": "11.1.136", "architectury_version": "9.2.14",
  "emi_enabled": "true", "emi_version": "1.1.18+1.20.1", "emi_min_version": "1.1.18"
}
```

`build.gradle` 完整配置：

```groovy
plugins {
    id 'fabric-loom' version '1.8-SNAPSHOT'
}

repositories {
    maven { url 'https://maven.blamejared.com' }       // JEI
    maven { url 'https://maven.shedaniel.me/' }        // REI
    maven { url 'https://maven.architectury.dev/' }    // Architectury
    maven { url 'https://repo.sleeping.town' }         // EMI（原 maven.terraformersmc.com）
}

def mcFamily = project.ext.mc_family
def mcVer = project.minecraft_version
def is26x = mcVer.startsWith('26.') || mcVer == '1.21.11'

sourceSets {
    create(mcFamily) {
        java { srcDir "src/${mcFamily}/java" }
    }
}

dependencies {
    "${mcFamily}Implementation" sourceSets.main.output
}

loom {
    createRemapConfigurations(sourceSets[mcFamily])
    mods {
        enchantpeak {
            sourceSet sourceSets.main
            sourceSet sourceSets[mcFamily]
        }
    }
}

dependencies {
    minecraft "com.mojang:minecraft:${mcVer}"
    
    if (is26x) {
        implementation "net.fabricmc:fabric-loader:${project.fabric_loader_version}"
        implementation "net.fabricmc.fabric-api:fabric-api:${project.fabric_api_version}"
    } else {
        mappings loom.officialMojangMappings()
        modImplementation "net.fabricmc:fabric-loader:${project.fabric_loader_version}"
        modImplementation "net.fabricmc.fabric-api:fabric-api:${project.fabric_api_version}"
    }
    
    // JEI 依赖（根据版本族条件化配置名）
    if (project.ext.get('jei_enabled')?.toBoolean()) {
        if (is26x) {
            compileOnly("mezz.jei:jei-${project.jei_minecraft_version}-fabric-api:${project.jei_version}")
            compileOnly("mezz.jei:jei-${project.jei_minecraft_version}-fabric:${project.jei_version}")
        } else {
            modCompileOnly("mezz.jei:jei-${project.jei_minecraft_version}-fabric-api:${project.jei_version}")
            modCompileOnly("mezz.jei:jei-${project.jei_minecraft_version}-fabric:${project.jei_version}")
        }
    }
    
    // REI 依赖（包含 cloth-config 和 architectury）
    if (is26x) {
        compileOnly("me.shedaniel.cloth:cloth-config-fabric:${project.cloth_config_version}")
        compileOnly("me.shedaniel:RoughlyEnoughItems-api-fabric:${project.rei_version}")
        compileOnly("me.shedaniel:RoughlyEnoughItems-default-plugin-fabric:${project.rei_version}")
        compileOnly("dev.architectury:architectury-fabric:${project.architectury_version}")
    } else {
        modCompileOnly("me.shedaniel.cloth:cloth-config-fabric:${project.cloth_config_version}")
        modCompileOnly("me.shedaniel:RoughlyEnoughItems-api-fabric:${project.rei_version}")
        modCompileOnly("me.shedaniel:RoughlyEnoughItems-default-plugin-fabric:${project.rei_version}")
        modCompileOnly("dev.architectury:architectury-fabric:${project.architectury_version}")
    }
    
    // EMI 依赖（1.19.4+，零额外依赖）
    if (project.ext.get('emi_enabled')?.toBoolean()) {
        if (is26x) {
            compileOnly("dev.emi:emi-fabric:${project.emi_version}")
        } else {
            modCompileOnly("dev.emi:emi-fabric:${project.emi_version}")
        }
    }
}

jar { from sourceSets[mcFamily].output }
```

**要点**：
- EMI maven 仓库地址已从 `maven.terraformersmc.com` 迁移到 `repo.sleeping.town`
- 26.x / 1.21.11 用 `implementation` / `compileOnly`（无 remap）
- 其他版本用 `modImplementation` / `modCompileOnly`（需要 remap）
- 自定义 source set 继承 main 的 output（`"${mcFamily}Implementation" sourceSets.main.output`）
- `createRemapConfigurations` 为自定义 source set 启用 remap（仅 1.18–1.20 需要）

### fabric.mod.json 模板参数化

**问题**：mc118 / mc119.2 没有 EMI，如果 fabric.mod.json 写死了 `emi_plugin` entrypoint，构建时会找不到类而失败。

**解决方案**：条件注入 entrypoint 和 suggests

```json
{
  "schemaVersion": 1,
  "id": "enchantpeak",
  "version": "${version}",
  "entrypoints": {
    "client": ["com.zhaojiedi1992.enchantpeak.EnchantPeakMod"],
    "rei_client": ["com.zhaojiedi1992.enchantpeak.rei.ReiEnchantPlugin"],
    "jei_mod_plugin": ["com.zhaojiedi1992.enchantpeak.jei.JeiEnchantPlugin"]${emi_entrypoint}
  },
  "depends": {
    "java": ">=${java_version}",
    "minecraft": "${minecraft_version}",
    "fabricloader": ">=${fabric_loader_version}",
    "fabric-api": ">=${fabric_api_min_version}"
  },
  "suggests": {
    "jei": ">=${jei_min_version}",
    "roughlyenoughitems": ">=${rei_min_version}"${emi_suggest}
  }
}
```

`build.gradle` 的 `processResources` 配置：

```groovy
processResources {
    def hasEmi = project.ext.get('emi_enabled')?.toBoolean() ?: false
    def emiEntrypoint = hasEmi ? ',\n    "emi_plugin": ["com.zhaojiedi1992.enchantpeak.emi.EmiEnchantPlugin"]' : ''
    def emiSuggest = hasEmi ? ',\n    "emi": ">=${emi_min_version}"' : ''
    
    inputs.properties([
        version               : project.version,
        minecraft_version     : project.minecraft_version,
        fabric_loader_version : project.fabric_loader_version,
        fabric_api_min_version: project.fabric_api_min_version,
        java_version          : project.java_version,
        jei_min_version       : project.jei_min_version,
        rei_min_version       : project.rei_min_version,
        emi_entrypoint        : emiEntrypoint,
        emi_suggest           : emiSuggest
    ])
    
    filesMatching("fabric.mod.json") {
        expand(
            version               : project.version,
            minecraft_version     : project.minecraft_version,
            fabric_loader_version : project.fabric_loader_version,
            fabric_api_min_version: project.fabric_api_min_version,
            java_version          : project.java_version,
            jei_min_version       : project.jei_min_version,
            rei_min_version       : project.rei_min_version,
            emi_entrypoint        : emiEntrypoint,
            emi_suggest           : emiSuggest
        )
    }
}
```

这样 mc118 / mc119.2（`emi_enabled=false`）构建时 fabric.mod.json 不包含 emi 相关内容，避免找不到类的错误。

## 各版本族实施内容

### mc26（迁移，工作量最小）

现有 `data/` `jei/` `rei/` 代码原样移入 `src/mc26/java/`，新增 `compat/EnchantStacks`（从 `EnchantGroup` 拆出的 `applyTo` / `enchantmentLines`），新增 `emi/EmiEnchantPlugin`。

**覆盖版本**：1.21.11 / 26.1 / 26.1.1 / 26.1.2 / 26.2

**归属确认**：Minecraft 1.21.11 已将 `ResourceLocation` 全部替换为 `Identifier`（[NeoForge 21.11 发布说明](https://neoforged.net/news/21.11release/)），API 与 26.x 一致，归入 mc26 族。注意 1.21.11 用 Java 21 而 26.x 用 Java 25，source set 代码不能用 Java 25 特性。

### mc121（新写，API 与 mc26 最接近）

`EnchantmentData` 用 `ResourceKey` + `RegistryAccess`（同 mc26），物品表去掉 Spear / Copper 工具，保留 Mace。JEI 用 1.21.x 的 `IRecipeType` API（与 26.x 几乎一致），REI/EMI 同理。

### mc120（新写）

`EnchantmentData` 改用 `BuiltInRegistries.ENCHANTMENT` 实例模式：内部仍构造 `Holder<Enchantment>`（`getHolderOrThrow`），数据表去掉 Mace / Wind Burst / Density / Breach（1.21 新增）。`EnchantStacks.applyTo` 用 `EnchantmentHelper.setEnchantments` + NBT；`enchantmentLines` 用 `enchantment.getFullname(level)`。JEI v15/16 API（`IRecipeType.create` 签名与 1.21 不同，`ResourceLocation` 构造器）。

### mc119（新写）

同 mc120，附魔实例模式；1.19.4 起提供 EMI 插件，1.19.2 条目不启用 EMI。JEI v11 API。无 Swift Sneak 差异——Swift Sneak 是 1.19 加入，保留；1.19.0 才有，1.19.x 全族一致。

### mc118（新写）

同 mc119 实例模式；去掉 Swift Sneak（1.19 新增）。无 EMI。JEI v9/v10 Fabric API（确认 1.18.2 有 Fabric 版 JEI，见调研结论）。

## 附魔数据校对

现有 `scripts/verify_enchants.py` 从客户端 jar 内置 datapack 核对附魔表——这只适用于 1.21+（附魔数据驱动化）。1.18–1.20 的附魔属性硬编码在代码里（`Enchantments` 引导注册），校对方式改为：

- 各版本族 `EnchantmentData` 的数值（最大等级、互斥组、适用物品）以对应版本的 `Enchantments.java` / 官方 wiki 为准人工核对；
- 每个版本族的数据类头部注释写明核对来源与版本；
- `verify_enchants.py` 增加按 `mc_family` 选择核对策略的入口（1.21+ 走 datapack 解析，旧版本只做物品存在性/字段引用检查——旧版本引用不存在的 `Enchantments.X` 常量会直接编译失败，编译器即校对）。

## 构建与发布

- `scripts/build_all_versions.sh` 遍历 `minecraft.json` 全部条目，逐条 `-Ptarget_mc=X clean build`，产物按现有命名 `enchantpeak-mc<version>-<mod_version>.jar` 输出到 `dist/`。
- README 增加版本支持矩阵表格（MC 版本 × JEI/REI/EMI 可用性）。
- 版本号策略：本次重构后 mod_version 升至 1.1.0。

## 测试与验证

1. 每个版本族至少一次 `./gradlew -Ptarget_mc=<代表版本> build` 编译通过（编译期即可暴露 API 误用）。
2. 每个版本族 `runClient` 手动验证：分别只装 JEI / 只装 REI / 只装 EMI，确认分类注册、条目渲染、tooltip 搜索正常；三者全装确认不冲突。
3. 重点回归项：旧版本（1.18–1.20）附魔物品的 NBT 写入路径 `EnchantStacks.applyTo`；mc118 无 EMI 时 fabric.mod.json 模板不含 emi suggest。
4. 1.21+ 版本跑 `verify_enchants.py` 核对附魔表。

## 风险与缓解

| 风险 | 缓解 |
|---|---|
| Loom 1.8 对 26.x 有未知兼容性问题 | mc26 迁移时第一步先跑 `./gradlew -Ptarget_mc=26.2 build` 验证，如有问题升级到 Loom 1.9/1.10 |
| 自定义 source set 的 remap 行为不符合预期 | `createRemapConfigurations` 已确认是官方机制；实施时先验证 mc26（最简单），再推广 |
| 26.x mc_family source set 代码混用 Java 21/25 特性 | mc26 source set 代码只允许用 Java 21 语法（`java_version` 最低为 21），不使用 Java 25 独有特性，保持向下兼容 |
| 旧版本 JEI/REI 坐标待确认 | 依赖调研 agent 返回后填充 minecraft.json；实施时以 maven 实际目录为准 |
| EMI 对 1.19.4 以下不可用 | mc118 / mc119.2 条目 `emi_enabled=false`，fabric.mod.json 条件注入逻辑已设计 |
| Java 25 本地不可用 | 启用 Gradle toolchain 自动下载（需网络），CI 环境预装 JDK 17/21/25 |

## 调研结论补充

### 多版本构建最佳实践（已完成）

1. **fabric-loom 与自定义 source set**：
   - 自定义 source set **不会自动继承** main 的依赖，需手动配置 `dependencies { mc121Implementation sourceSets.main.output }`
   - 需调用 `loom.createRemapConfigurations(sourceSets.mc121)` 为额外 source set 启用 remap
   - 参考：[Fabric Loom Wiki - Split Source Sets](https://wiki.fabricmc.net/documentation:fabric_loom)

2. **行业做法对比**：
   - **Stonecutter**（预处理器）：`//? if >=1.20` 条件编译，单源码树，但文档不完整、处于实验阶段
   - **Per-Version Source Set**（本方案采用）：版本差异物理隔离，IDE 原生支持，知名案例：ViaFabricPlus、Multiverse
   - 参考工具：[HoshinOFW/multiversion](https://github.com/HoshinOFW/multiversion)、[Polyfrost Gradle Toolkit](https://github.com/Polyfrost/polyfrost-gradle-toolkit)

3. **Java 版本矩阵**（已核实）：
   - MC 1.18.2 / 1.19.2 / 1.20.1：Java 17
   - MC 1.21.x：Java 21（[Fabric 官方公告](https://fabricmc.net/2024/05/31/121.html)）
   - MC 26.x：**Java 25**（[Fabric 26.1 公告](https://fabricmc.net/2026/03/14/261.html)）
   - Gradle 9.4（Loom 1.15 要求）运行需 Java 17+，toolchain 自动下载目标 JDK

4. **Loom 版本与 MC 26.x**：
   - MC 26.x 已去混淆，**不再需要 mappings**，不再执行 remap
   - 必须用 **Loom 1.15+**（推荐配合 Gradle 9.4.0）
   - 依赖配置改用 `implementation` / `compileOnly`（不再用 `modImplementation` / `modCompileOnly`）
   - MC 1.18–1.21 仍需 Loom 1.7/1.8 + mappings，**单一 Loom 版本无法兼容**

### JEI/REI/EMI 版本坐标（已完成）

以下坐标基于调研结果整理，适用于 `versions/minecraft.json` 填充：

#### 1.18.2

**JEI**
- Maven: `maven.blamejared.com`
- 坐标: `mezz.jei:jei-1.18.2-fabric-api:10.6.1.1022` / `mezz.jei:jei-1.18.2-fabric:10.6.1.1022`
- 版本: `10.6.1.1022`

**REI**
- Maven: `maven.shedaniel.me`
- 坐标: `me.shedaniel:RoughlyEnoughItems-api-fabric:8.3.694` / `me.shedaniel:RoughlyEnoughItems-default-plugin-fabric:8.3.694`
- 版本: `8.3.694`
- 依赖: `me.shedaniel.cloth:cloth-config-fabric:6.4.90`, `dev.architectury:architectury-fabric:4.11.93`

**EMI**
- Maven: `repo.sleeping.town`
- 坐标: `dev.emi:emi-fabric:0.7.3+1.18.2`
- 版本: `0.7.3+1.18.2`

#### 1.19.2

**JEI**
- Maven: `maven.blamejared.com`
- 坐标: `mezz.jei:jei-1.19.2-fabric-api:11.39.0.1067` / `mezz.jei:jei-1.19.2-fabric:11.39.0.1067`
- 版本: `11.39.0.1067`

**REI**
- Maven: `maven.shedaniel.me`
- 坐标: `me.shedaniel:RoughlyEnoughItems-api-fabric:9.1.695` / `me.shedaniel:RoughlyEnoughItems-default-plugin-fabric:9.1.695`
- 版本: `9.1.695`
- 依赖: `me.shedaniel.cloth:cloth-config-fabric:7.0.74`, `dev.architectury:architectury-fabric:5.14.84`

**EMI**
- Maven: `repo.sleeping.town`
- 坐标: `dev.emi:emi-fabric:1.1.19+1.19.2`
- 版本: `1.1.19+1.19.2`（或 0.5.1+1.19.2，1.19.2 有 EMI，1.19.4 起更稳定）

#### 1.20.1

**JEI**
- Maven: `maven.blamejared.com`
- 坐标: `mezz.jei:jei-1.20.1-fabric-api:15.49.0.188` / `mezz.jei:jei-1.20.1-fabric:15.49.0.188`
- 版本: `15.49.0.188`

**REI**
- Maven: `maven.shedaniel.me`
- 坐标: `me.shedaniel:RoughlyEnoughItems-api-fabric:15.0.787` / `me.shedaniel:RoughlyEnoughItems-default-plugin-fabric:15.0.787`
- 版本: `15.0.787`
- 依赖: `me.shedaniel.cloth:cloth-config-fabric:11.1.136`, `dev.architectury:architectury-fabric:9.2.14`

**EMI**
- Maven: `repo.sleeping.town`
- 坐标: `dev.emi:emi-fabric:1.1.18+1.20.1`
- 版本: `1.1.18+1.20.1`

#### 1.21.1

**JEI**
- Maven: `maven.blamejared.com`
- 坐标: `mezz.jei:jei-1.21.1-fabric-api:19.44.0.401` / `mezz.jei:jei-1.21.1-fabric:19.44.0.401`
- 版本: `19.44.0.401`

**REI**
- Maven: `maven.shedaniel.me`
- 坐标: `me.shedaniel:RoughlyEnoughItems-api-fabric:19.0.809` / `me.shedaniel:RoughlyEnoughItems-default-plugin-fabric:19.0.809`
- 版本: `19.0.809`
- 依赖: `me.shedaniel.cloth:cloth-config-fabric:15.0.130`, `dev.architectury:architectury-fabric:13.0.11`

**EMI**
- Maven: `repo.sleeping.town`
- 坐标: `dev.emi:emi-fabric:1.1.24+1.21.1`
- 版本: `1.1.24+1.21.1`

#### 1.21.11 / 26.x

已在 `versions/minecraft.json` 中，保持现有坐标（JEI v27-30，REI v21.11/26.x，EMI 可能需要非官方 port）。

**关键发现**：
- JEI 全版本都有 `-fabric-api` 和 `-fabric` 两个 artifact，都需要 `modCompileOnly`
- REI 始终需要 cloth-config 和 architectury 依赖
- EMI 零额外依赖，1.18.2 起可用（但 1.19.4+ 更稳定）
- EMI maven 仓库已从 `maven.terraformersmc.com` 迁移到 `repo.sleeping.town`
