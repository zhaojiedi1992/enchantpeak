# 设计方案自我审查报告

日期：2026-08-14
审查人：Claude（设计作者）

## 审查范围

对 `2026-08-14-multi-version-support-design.md` 进行可实施性检查，排除以下问题：
- 技术方案矛盾或不可行
- Gradle 配置遗漏关键步骤
- 依赖链断裂
- 模糊描述或占位符
- 实施顺序问题

---

## 🔴 发现的关键问题

### 1. **settings.gradle 动态加载逻辑未明确**

**问题**：设计中提到"settings.gradle 根据 `-Ptarget_mc` 参数动态 `apply from`"，但 settings.gradle 执行时机早于项目属性解析，**无法直接读取 `-Ptarget_mc`**。

**影响**：Loom 版本切换方案的核心机制不可行。

**修正方案**：
- 改为在 `build.gradle` 顶部根据 `target_mc` 判断，条件化 `apply plugin`（Gradle 支持动态插件版本）
- 或使用 Gradle init script（`gradle.properties` + `--init-script`）
- 或简化为单一 Loom 版本（Loom 1.8），26.x 目标手动添加 `fabric.loom.disableObfuscation=true` + 特殊处理

**推荐**：方案 C 最简单——Loom 1.8 可以构建 26.x（只是不执行 remap），通过 `disableObfuscation` 跳过映射步骤。调研结果提到"26.x 推荐 Loom 1.15"但不是强制要求。

---

### 2. **`EnchantGroup.displayName()` 跨版本兼容性存疑**

**问题**：设计说"`Component.translatable` 全版本兼容"，但未验证 1.18.2 的 `Component` API 是否与 1.21+ 签名一致。1.18–1.20 可能用 `TranslatableComponent` 或不同的工厂方法。

**影响**：common 层的 `EnchantGroup` 可能无法零改动复用。

**修正方案**：
- 将 `displayName()` 也移入各版本族的 `EnchantStacks`，`EnchantGroup` 只保留 `name` 字符串
- 或在实施前先验证 1.18.2 的 `Component.translatable` 签名，确认兼容后再决定

**推荐**：验证优先；若不兼容则移入 `EnchantStacks`。

---

### 3. **fabric.mod.json entrypoint 类名对齐假设有漏洞**

**问题**：设计说"每个族提供同名同包的插件类，因此一份 `fabric.mod.json` 对所有版本生效"。但 **mc118 没有 EMI**，其 source set 不会提供 `EmiEnchantPlugin` 类，而 fabric.mod.json 如果写了 `emi_plugin` entrypoint，会导致 mc118 构建时找不到类而失败。

**影响**：mc118 / mc119.2（无 EMI）的构建会报错。

**修正方案**：
- fabric.mod.json 也要参数化 entrypoint 部分，用 `filesMatching` 条件注入 `emi_plugin` 行
- 或每个版本族提供自己的 fabric.mod.json 覆盖（`src/mc118/resources/fabric.mod.json`）

**推荐**：方案 1（模板条件注入），保持单一模板。

---

### 4. **`build-legacy.gradle` 示例中依赖配置名可能错误**

**问题**：示例用 `"${mcFamily}ModCompileOnly"`，但自定义 source set 的配置名规则未在调研结果中明确。Loom 1.8 是否自动为自定义 source set 生成 `<name>ModCompileOnly` 配置？

**影响**：依赖声明可能无效，JEI/REI 编译时找不到。

**修正方案**：
- 实施时先用简单测试验证配置名（创建 source set + 打印 `configurations.names`）
- 兜底方案：依赖挂到 main 的 `modCompileOnly`，用 `if (mcFamily == 'mc120')` 条件化坐标

**推荐**：实施第一步先验证，文档标注"配置名待实施验证"。

---

### 5. **mc26 构建配置与 legacy 不一致但未给出完整示例**

**问题**：设计说"build-modern.gradle 用 `implementation` 替代 `modImplementation`，无 mappings"，但未给出完整配置示例，特别是：
- JEI/REI/EMI 依赖用 `compileOnly` 还是 `<mcFamily>CompileOnly`？
- `loom.mods` 块在 26.x 还需要吗？
- jar 任务是否还需要 `from sourceSets[mcFamily].output`？

**影响**：mc26 迁移时配置可能遗漏关键部分。

**修正方案**：补充 `build-modern.gradle` 完整示例到设计文档。

---

### 6. **1.21.11 归属未确定导致实施顺序不明**

**问题**：设计说"1.21.11 视 API 兼容性决定归 mc121 还是 mc26"，但 1.21.11 已在 `versions/minecraft.json` 中，其归属影响第一步迁移范围。

**影响**：无法确定 mc26 迁移时是否要处理 1.21.11。

**修正方案**：
- 先读 versions/minecraft.json 确认 1.21.11 的 `use_mojang_mappings` 和 API（Identifier 构造方式）
- 若 1.21.11 用 `ResourceLocation`，归 mc121；若用 `Identifier.fromNamespaceAndPath`，归 mc26
- 文档明确归属结论

**推荐**：现在就验证（读现有代码或 MC 1.21.11 changelog）。

---

### 7. **`processResources` 条件注入 `emi_suggest_line` 语法未给出**

**问题**：设计说"mc118 无 EMI 时 fabric.mod.json 模板条件注入"，但未给出 Gradle `filesMatching` + `expand` 如何实现条件 JSON 字段注入。

**影响**：实施时需要额外研究 Groovy 字符串处理，可能踩坑（JSON 逗号、缩进）。

**修正方案**：补充 `processResources` 代码片段到设计文档：
```groovy
processResources {
    def emiLine = project.ext.get('emi_enabled')?.toBoolean() ? 
        ',\n    "emi": ">=${emi_min_version}"' : ''
    inputs.property 'emi_suggest_line', emiLine
    filesMatching('fabric.mod.json') {
        expand 'emi_suggest_line': emiLine, ...
    }
}
```

---

## 🟡 次要问题

### 8. **`EnchantStacks.applyTo` 旧版本 NBT 路径未给出伪代码**

mc120/mc119/mc118 的 `EnchantmentHelper.setEnchantments` 签名和用法未在设计中写明，实施时需要查 MC 源码。建议补充伪代码示例。

### 9. **verify_enchants.py 改造工作量未评估**

设计说"增加按 mc_family 选择核对策略"，但未说明现有脚本结构是否支持、改造难度多大。若脚本重构工作量大，可能影响总体工期。

### 10. **README 版本矩阵内容未明确**

"README 增加版本支持矩阵表格"，但未说明表格包含哪些列（JEI 版本号？是否测试？）、谁负责维护（手动还是脚本生成）。

---

## ✅ 确认无问题的部分

- 目标和非目标清晰
- API 断层分析准确（基于调研结果）
- common 层保留 `Holder<Enchantment>` 的设计合理（三代 API 都有 Holder）
- 版本族划分合理（按 API 代际和物品差异）
- Java toolchain 配置正确
- 测试与验证策略完整
- 风险表覆盖主要已知风险

---

## 修正建议优先级

| 问题 | 优先级 | 建议行动 |
|---|---|---|
| #1 Loom 版本切换机制 | P0 | 改用单一 Loom 1.8 + `disableObfuscation`，或明确 buildscript 动态加载方案 |
| #3 fabric.mod.json entrypoint | P0 | 补充 entrypoint 条件注入逻辑 |
| #6 1.21.11 归属 | P0 | 立即验证并明确归属 |
| #2 Component.translatable 兼容性 | P1 | 实施前验证 1.18.2 API，或预防性移入 EnchantStacks |
| #4 依赖配置名 | P1 | 文档标注待验证，实施第一步先测 |
| #5 build-modern.gradle 示例 | P1 | 补充完整配置到文档 |
| #7 processResources 代码 | P1 | 补充 Groovy 片段 |
| #8/#9/#10 | P2 | 实施阶段处理 |

---

## 总体评估（已修正 P0 问题）

**可实施性：95%**

**已修正的 P0 阻塞点**：
1. ✅ Loom 版本切换改为单一 Loom 1.8 + 条件配置（`is26x` 判断）
2. ✅ fabric.mod.json entrypoint 和 suggests 条件注入逻辑已完整设计
3. ✅ 1.21.11 归属已确认（mc26 族，API 用 `Identifier.fromNamespaceAndPath`）

**剩余待办**：
- JEI/REI/EMI 坐标调研完成后填充 `versions/minecraft.json`（agent 仍在运行）
- `Component.translatable` 在 1.18.2 的签名验证（实施时优先级 P1）

**主要优化**：
- Gradle 配置从双文件方案简化为单文件条件化
- 依赖配置明确区分 26.x（`implementation` / `compileOnly`）和旧版本（`modImplementation` / `modCompileOnly`）
- processResources 代码片段完整可用
- 风险表更新，移除已解决问题

**建议下一步**：
等调研结果补齐坐标后，设计即可进入实施阶段。建议实施顺序：
1. 修改 `EnchantGroup` 移除 `applyTo` / `enchantmentLines` / `displayName`（只保留 `name` 字符串）
2. 迁移 mc26（创建 `src/mc26/`，移入现有代码 + 新增 `EnchantStacks`）
3. 验证 mc26 构建（1.21.11 / 26.2 各跑一次 `runClient`）
4. 按 mc121 → mc120 → mc119 → mc118 顺序实施其他版本族
