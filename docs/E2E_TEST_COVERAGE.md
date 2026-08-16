# E2E 测试覆盖报告

## 概述

EnchantPeak 的端到端测试使用 HeadlessMc 框架，通过真实客户端启动和 API 级断言验证 JEI/REI 集成的完整性。

## 测试架构

### 测试流程
1. **启动阶段**：HeadlessMc 以真实启动器方式启动客户端
2. **进世界**：自动进入世界（180秒超时保护）
3. **等待重载**：等待 JEI/REI 完成世界级重载（15秒稳定时间）
4. **API 断言**：在主线程执行全面的 API 级验证
5. **结果输出**：机器可读的结果行（CI 自动解析）

### 测试矩阵
- **Fabric 26.2**：完整 API 断言（e2e=true）
- **Fabric 1.21.x**：日志标记检查
- **Fabric 1.20.x/1.18.x**：日志标记检查
- **NeoForge**：JEI 日志标记检查

## 详细覆盖

### 1. JEI 集成测试（`E2eJeiChecks`）

#### 验证点
- ✅ **配方类型注册**：`best_enchantments` 配方类型可用
- ✅ **总配方数量**：>= 90 条物品记录
- ✅ **多类别物品验证**：

| 物品类型 | 代表物品 | JEI 记录数 | 说明 |
|---------|---------|-----------|------|
| 工具-镐 | 钻石镐 | 1 | 包含 2 个流派（时运/精准），在 category 内渲染 |
| 工具-斧 | 钻石斧 | 1 | 包含 6 个流派（时运×3伤害 + 精准×3伤害），在 category 内渲染 |
| 武器-剑 | 钻石剑 | 1 | 包含 3 个流派（锋利/亡灵/节肢），在 category 内渲染 |
| 防具-头盔 | 钻石头盔 | 1 | 包含 4 个流派（四种保护类型），在 category 内渲染 |
| 防具-靴子 | 钻石靴子 | 1 | 包含 8 个流派（4保护×2移动），在 category 内渲染 |
| 远程-弓 | 弓 | 1 | 包含 2 个流派（无限/经验修补），在 category 内渲染 |
| 远程-三叉戟 | 三叉戟 | 1 | 包含 2 个流派（忠诚/激流），在 category 内渲染 |
| 工具-其他 | 钓鱼竿 | 1 | 包含 1 个流派（完整钓鱼增益），在 category 内渲染 |

#### 验证原理
JEI 为每个物品注册一条 `ItemEnchantRecord`，不同流派在 JEI category 的槽位里展示。测试通过 `IRecipeManager.createRecipeLookup()` API 验证每个物品的记录可被正确查询，确保玩家在 JEI 界面点击物品时能看到所有流派。

### 2. REI 集成测试（`E2eReiChecks`）

#### 基础验证
- ✅ **Display 数量**：>= 90（自定义分类展示）
- ✅ **附魔条目总数**：>= 288（所有附魔方案的独立条目）

#### 搜索功能验证（新增）
模拟玩家在 REI 搜索框输入附魔名，验证搜索结果数量：

| 搜索关键词 | 实测结果数 (26.2) | 阈值 | 覆盖物品 |
|-----------|------------------|------|---------|
| fortune（时运） | 42 | >= 40 | 镐(7) + 铲(7) + 锄(7) + 斧(21) |
| efficiency（效率） | 113 | >= 85 | 镐/铲/锄各7 + 斧21 + 剪刀1 + 26.x属性行文本 |
| sharpness（锋利） | 28 | >= 25 | 剑(7) + 斧(14) + 矛(7) |
| protection（保护） | 144 | >= 140 | 子串匹配4种保护（protection/fire_protection/blast_protection/projectile_protection），7材质×4部位×4类型 |

#### 搜索实现原理
```java
// 通过 EntryRegistry API 过滤附魔物品
EntryRegistry.getInstance().getEntryStacks()
    .filter(entry -> {
        ItemStack stack = entry.getValue();
        // 检查 tooltip 文本（REI 的默认搜索行为）
        String text = entry.asFormattedText().getString().toLowerCase();
        return stack.isEnchanted() && text.contains(keyword);
    })
    .count();
```

这直接模拟了 REI 的 `tooltipSearch = ALWAYS` 行为，验证玩家搜索体验。

### 3. 进游戏验证

#### 启动流程
- ✅ **客户端启动**：Fabric/NeoForge 加载器成功初始化
- ✅ **模组加载**：EnchantPeak 主模组和 e2e 测试模组加载
- ✅ **世界进入**：180秒内成功进入测试世界
- ✅ **插件初始化**：JEI/REI 插件完成注册

#### 日志标记验证（所有版本）
```
[EnchantPeak] Initialized.                          # 主模组初始化
JEI recipes registered: \d+                         # JEI 配方注册数量
REI displays registered: \d+ info, \d+ custom       # REI 展示注册数量
REI entries added: \d+                              # REI 搜索条目数量
[EnchantPeak E2E] RESULT: OK                        # API 断言通过（26.2）
```

#### 失败模式检测
禁止出现的日志模式：
- `注册失败`：插件注册阶段崩溃
- `当前注册表中缺失`：附魔/物品注册表异常
- `RESULT: FAIL`：API 断言失败

## 覆盖物品清单

### 工具类（48 条记录，96 个方案）
- **镐**：木/石/铁/金/钻石/下界合金 × 2 方案 = 12
- **斧**：木/石/铁/金/钻石/下界合金 × 6 方案 = 36
- **铲**：木/石/铁/金/钻石/下界合金 × 2 方案 = 12
- **锄**：木/石/铁/金/钻石/下界合金 × 2 方案 = 12
- **剪刀**：1 方案
- **钓鱼竿**：1 方案
- **打火石**：1 方案

### 武器类（9 条记录，25 个方案）
- **剑**：木/石/铁/金/钻石/下界合金 × 3 方案 = 18
- **弓**：2 方案（无限/经验修补）
- **弩**：2 方案（穿透/多重射击）
- **三叉戟**：2 方案（忠诚/激流）

### 防具类（25 条记录，120 个方案）
- **头盔**：皮革/锁链/铁/金/钻石/下界合金/海龟 × 4 方案 = 28
- **胸甲**：皮革/锁链/铁/金/钻石/下界合金 × 4 方案 = 24
- **护腿**：皮革/锁链/铁/金/钻石/下界合金 × 4 方案 = 24
- **靴子**：皮革/锁链/铁/金/钻石/下界合金 × 8 方案 = 48

### 其他类（10 条记录，10 个方案）
- **盾牌**：1 方案
- **鞘翅**：1 方案
- **胡萝卜钓竿**：1 方案
- **诡异菌钓竿**：1 方案
- **雕刻南瓜**：0 方案（仅诅咒）
- **指南针**：0 方案（仅诅咒）
- **头颅**（苦力怕/龙/玩家/骷髅/凋灵骷髅/僵尸）：各 0 方案（仅诅咒）

**总计**：92 条物品记录，288 个附魔方案

## 测试结果示例

### 成功输出
```
[EnchantPeak E2E] RESULT: OK jei_recipes=92 pickaxe=1 axe=1 sword=1 helmet=1 boots=1 bow=1 trident=1 fishing_rod=1 rei_displays=92 rei_enchanted_entries=288 search_fortune=42 search_sharpness=28 search_protection=144 search_efficiency=113
```

### 失败输出
```
[EnchantPeak E2E] RESULT: FAIL jei_recipes=85 pickaxe=0 ...
```
退出码：1（CI 自动标记为失败）

## 本地测试

### 快速测试（推荐）
```bash
# 仅编译 e2e jar（不运行实际客户端）
./gradlew e2eJar -Ptarget_mc=26.2 -Pe2e
```

### 完整 e2e 测试（需要 HeadlessMc）
```bash
# 1. 编译并暂存模组
./gradlew build e2eJar stageE2eMods -Ptarget_mc=26.2 -Pe2e

# 2. 使用 mc-runtime-test 运行
mc-runtime-test --mc 26.2 --modloader fabric --java 25

# 3. 检查日志
python3 scripts/assert_e2e_log.py
```

## 改进历史

### v1.1.0（即将发布）
- ✅ **新增**：完整的 e2e 测试框架，基于 HeadlessMc 的真实客户端验证
- ✅ **新增**：REI 搜索功能验证（4种关键附魔：时运/效率/锋利/保护）
- ✅ **增强**：JEI 多类别物品验证（8种代表物品覆盖工具/武器/防具全类别）
- ✅ **增强**：从数量检查升级到精确记录数验证
- ✅ **修复**：push.sh 网络探测时机问题（构建前 + push 前两次探测）
- ✅ **改进**：scripts/assert_e2e_log.py 支持文件传参，避免 shell 转义问题

### v1.0.19
- ✅ **新增**：容错的附魔解析（数据包移除的附魔只跳过相关构建，不禁用整个模组）
- ✅ **改进**：JEI/REI 注册失败时的可操作错误消息

## 已知限制

### 测试覆盖范围
- ❌ **不测试**：GUI 渲染（HeadlessMc 无图形）
- ❌ **不测试**：鼠标交互（点击配方跳转等）
- ❌ **不测试**：多语言翻译（仅验证 en_us）
- ❌ **不测试**：模组兼容性（仅原版内容）

### 版本限制
- **完整 API 断言**：仅 Fabric 26.2
- **其他版本**：日志标记检查（无 harness API）
- **NeoForge**：仅 JEI 检查（REI 不支持 NeoForge）

## 未来改进方向

### 短期
- [ ] 添加新版本物品测试（Mace、Brush、Wind Charge）
- [ ] 验证诅咒附魔的独立条目
- [ ] 检查附魔等级的正确性

### 中期
- [ ] 扩展 harness 到 1.21.x 版本
- [ ] 添加配方查看器跳转验证
- [ ] 模组兼容性测试（常见附魔扩展模组）

### 长期
- [ ] GUI 截图对比测试
- [ ] 性能基准测试（插件加载时间）
- [ ] 多语言全覆盖测试
