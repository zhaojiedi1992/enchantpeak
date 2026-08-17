# Mod Loader 支持分析：NeoForge、Forge、LiteLoader

**分析日期**：2026-08-14  
**当前项目状态**：Fabric-only，支持 MC 1.18.2–26.2（13 个版本家族）

---

## 1. NeoForge 分析

### 背景
- **NeoForge** 是 MinecraftForge 的官方继任者（fork）
- **起始版本**：MC 1.20.1（与老 Forge 并存），MC 1.20.2+ 成为唯一 Forge 分支
- **核心团队**：cpw 等原 Forge 核心开发者主导
- **现状**：MC 1.21+ 的事实标准 Forge-like loader

### 生态现状（2026）
✅ **用户基数**：
- CurseForge/Modrinth 统计：约占模组用户 40-45%（Fabric 占 50-55%）
- 大型整合包（科技、魔法、冒险类）主要在 NeoForge 生态

✅ **JEI 支持**：
- 有完整的 NeoForge 版本（`mezz.jei:jei-xxx-neoforge`）
- API 与 Fabric 版本 95% 兼容

❌ **REI 支持**：
- REI 是 Fabric 原生项目，NeoForge 支持需要通过 **Architectury API** 跨平台层
- 官方 REI NeoForge 版本存在但功能不如 Fabric 版完整
- EnchantPeak 的核心卖点（独立搜索条目）在 NeoForge 端可能降级

### 迁移成本评估

#### 方案 A：Architectury API 跨平台重构
**工作量**：⭐⭐⭐⭐⭐（极高，2-3 周全职）
- 重写所有注册代码（Fabric Registry → Architectury Registry）
- 重写事件系统（Fabric callbacks → Architectury events）
- 重写 JEI/REI 插件为跨平台版本
- 新增 `common`、`fabric`、`neoforge` 三个 source set
- 构建脚本完全重构（Loom → Architectury Loom）
- 构建目标翻倍：13 个版本 → 26 个 jar（Fabric + NeoForge 各 13 个）

**维护成本**：
- CI 时间翻倍（每次发布约 20-30 分钟）
- 每个 bug 需要在两个平台验证
- Architectury API 本身有版本依赖和兼容性问题

**收益**：
- 覆盖 Fabric + NeoForge 两大生态（用户覆盖率 95%+）
- 一套代码维护（平台差异由 Architectury 抽象）

#### 方案 B：双代码库（独立 Fabric 和 NeoForge 项目）
**工作量**：⭐⭐⭐⭐⭐⭐（最高，且长期维护成本巨大）
- 创建独立的 `enchantpeak-neoforge` 仓库
- 手动同步功能和 bug 修复
- 两套 CI/CD 流程

**不推荐**：维护噩梦

---

## 2. Forge 分析

### 现状
- **MC 1.20.1 及之前**：MinecraftForge 是主流
- **MC 1.20.2+**：官方停止更新，社区全面转向 NeoForge
- **MC 1.21+**：基本无人维护 Forge 1.21，全部用 NeoForge

### 结论
❌ **不建议单独支持老 Forge**：
- MC 1.18.2–1.20.1 的 Forge 用户大多已迁移到 NeoForge（或直接用 Fabric）
- 如果要支持 Forge 生态，直接支持 NeoForge 即可（向下兼容 1.20.1 Forge 用户）

---

## 3. LiteLoader 分析

### 现状
- **最后官方版本**：MC 1.12.2（2017 年）
- **MC 1.13+ 状态**：完全停止开发（扁平化重构后未跟进）
- **生态**：已死亡，用户全部迁移到 Fabric 或 Forge

### 结论
❌ **完全不考虑**：生态已死，无用户基数

---

## 4. 综合建议

### 当前 EnchantPeak 的特殊性
1. **纯信息展示模组**（不修改游戏机制，只提供 JEI/REI 集成）
2. **REI 独立搜索条目是核心卖点**（Fabric 生态独有优势）
3. **已支持 13 个版本家族**（维护复杂度已较高）

### 推荐方案：保持 Fabric-only（短期 1-2 年）

✅ **理由**：
1. **REI 支持完整**：EnchantPeak 的核心价值（独立搜索条目）在 Fabric 端体验最佳
2. **维护成本最低**：当前 13 个版本已有一定复杂度，加入 NeoForge 会翻倍
3. **MC 26.x 未来主流**：Fabric 对最新版本支持最好，NeoForge 26.x 尚不成熟
4. **用户覆盖率足够**：Fabric 占 50-55%，且增长趋势明显

❌ **放弃**：
- NeoForge 用户（约 40-45%）无法使用

### 可选方案：长期考虑 Architectury 重构

**触发条件**（满足任一即可考虑）：
1. 有大量 NeoForge 用户明确反馈需求（Issue/Discord 请求 > 50 次）
2. 有足够时间投入（至少 3-4 周全职开发 + 测试）
3. 可以接受 REI 功能在 NeoForge 端降级或缺失（只保留 JEI 支持）

**优先级**：低（除非用户强需求）

---

## 5. 技术对比表

| 特性 | Fabric（当前） | + NeoForge（Architectury） | + LiteLoader |
|------|---------------|---------------------------|--------------|
| **用户覆盖率** | 50-55% | 95%+ | 0% |
| **开发工作量** | - | ⭐⭐⭐⭐⭐（2-3周） | ❌ 不可行 |
| **维护成本** | 低 | 高（翻倍） | - |
| **JEI 支持** | ✅ 完整 | ✅ 完整 | ❌ 无 |
| **REI 支持** | ✅ 完整 | ⚠️ 降级 | ❌ 无 |
| **REI 独立搜索** | ✅ 核心卖点 | ⚠️ 可能缺失 | ❌ 无 |
| **MC 26.x 支持** | ✅ 最佳 | ⚠️ 不成熟 | ❌ 无 |
| **构建目标数** | 13 | 26 | - |
| **CI 时间** | 8-10 分钟 | 20-30 分钟 | - |

---

## 6. 决策树

```
EnchantPeak 是否支持 NeoForge？
│
├─ 短期（2026-2027）
│  └─ ❌ 不支持
│     理由：REI 核心功能 + 维护成本 + MC 26.x Fabric 优先
│
├─ 长期（2028+）
│  └─ ⚠️ 根据用户反馈决定
│     - 如果 NeoForge 用户强烈需求（>50 次请求）
│     - 且愿意接受 REI 功能降级
│     - 且有充足开发时间（3-4 周）
│     → 考虑 Architectury 重构
│
└─ LiteLoader / 老 Forge
   └─ ❌ 完全不考虑（生态已死或被 NeoForge 替代）
```

---

## 7. 最终建议

### ✅ 立即行动
**保持 Fabric-only，优化当前体验**：
1. 完善 README 说明 Fabric 独占原因（REI 独立搜索是核心卖点）
2. 在 Modrinth/CurseForge 页面明确标注"Fabric 专用"
3. 提供清晰的安装指南（Fabric Loader + Fabric API + JEI/REI）

### ⏳ 观察等待
**收集 NeoForge 用户反馈**：
1. 在 GitHub Issue 模板中添加"是否需要 NeoForge 版本"选项
2. 在 Discord/Reddit 观察 NeoForge 用户需求热度
3. 如果 6 个月内收集到 >50 次明确请求，重新评估

### ❌ 不做
1. 不支持 LiteLoader（生态已死）
2. 不支持老 Forge 1.20.1-（被 NeoForge 替代）
3. 短期内不投入 Architectury 重构（ROI 低）

---

**结论**：当前阶段**保持 Fabric-only** 是最优解，长期根据用户反馈灵活调整。

---

## 附：2026-08-17 Forge 落地时的 maven 实测数据

实施 Forge 支持时对官方 maven 的实测（决定 forge/forge7 目标矩阵的依据）：

- **LexForge 仍全线存活**：从 1.18.2（40.3.12）到 26.2（65.1.1）都有官方构建。
- **JEI 的 Forge 构建止于 1.21.1**（`jei-*-forge-api` 在 maven 上 1.21.4+ 为 404）。
  因此 Forge 轨道只做 1.18.2 ~ 1.21.1：本 mod 的展示层只有 JEI/REI，再往上的
  Forge 版本没有可用的展示端（EMI 是未来选项）。
- **REI 的 Forge 构建止于 1.20.4**（`RoughlyEnoughItems-forge` 最高 14.1.x）。
- **工具链在 MC 1.20.5 分界**：MDK 在 1.20.4 及以前用 ForgeGradle 6（Gradle 8.8，
  产物需 reobfJar 回映射到 SRG 名），1.20.6 起用 ForgeGradle 7（Gradle 9.x，
  官方名运行时，无 reobf）。对应本仓库的 `forge/` 与 `forge7/` 两个构建。
