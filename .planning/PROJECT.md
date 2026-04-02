# SignLock

## What This Is

SignLock 是一个面向 Spigot / Paper / Folia 服务器的牌子锁插件，用牌子为箱子、木桶、熔炉、潜影盒、门、活板门和栅栏门等方块提供所有权与授权访问控制。它服务于希望使用接近原版交互方式完成容器与门禁保护的服主和玩家，重点是轻量、直观、可配置、中文友好。

## Core Value

玩家贴上牌子后，受保护方块必须稳定、可预期地只允许主人和已授权玩家访问。

## Requirements

### Validated

- [x] 玩家可以通过锁牌为支持的容器与门类方块建立所有权保护，且主人名会写入主锁牌。 - existing
- [x] 主人可以通过 `/signlock add`、`/signlock remove`、`/signlock info`、`/signlock reload` 管理授权与查看锁信息。 - existing
- [x] 插件会拦截未授权玩家的交互、开箱、破坏，以及部分自动化、爆炸、活塞和流体对锁的影响。 - existing
- [x] 插件支持管理员绕过、可配置锁头/文案/可上锁方块列表，并兼容 Folia 标记。 - existing

### Active

- [ ] 修复并稳定单双箱、双联容器等共享目标的锁判定，让保护效果在所有交互入口一致生效。
- [ ] 修复配置与插件元数据中的中文乱码，确保默认配置、提示消息、描述和使用说明可直接用于中文服务器。
- [ ] 建立可复用的验证基线，覆盖容器访问、授权扩展、破坏保护、自动化保护与 Spigot/Folia 兼容冒烟流程。

### Out of Scope

- 数据库存储或跨服同步锁数据 - 当前插件定位为轻量本地牌子锁，先把单服稳定性做好。
- GUI 菜单或复杂管理面板 - 当前核心价值是原版式交互，命令与牌子已足够支撑 v1。
- 与其他领地/权限插件的深度联动 - 在基础锁语义稳定前，引入外部联动只会扩大验证面。

## Context

- 技术环境是 Java 21 + Spigot API 1.21.11，使用 Gradle Wrapper 构建，并声明 `folia-supported: true`。
- 当前仓库已经具备锁牌监听、授权命令、玩家身份缓存、容器解析与多种破坏保护逻辑，属于 brownfield 项目而非空白起步。
- 当前最明显的产品风险不是“缺功能”，而是锁判定边界和默认中文配置质量，尤其是双箱、扩展授权牌和默认资源可读性。
- 当前用户预期是中文优先、服主可直接落地使用，因此配置默认值、提示文案和行为一致性都属于发布阻塞项。

## Constraints

- **Tech stack**: 继续使用 Java Plugin + Gradle Wrapper + Spigot API 1.21.11 - 避免在初始化阶段引入框架迁移风险。
- **Compatibility**: 必须保持对 Spigot/Paper/Folia 的兼容声明与主线程安全 - 这是插件对外承诺的一部分。
- **Behavior**: 不破坏已有锁牌数据与当前牌子格式 - 用户已经可能在服务器里使用这些牌子。
- **Localization**: 默认体验必须对中文服务器友好 - 当前目标用户和文案方向已经明确。

## Key Decisions

| Decision | Rationale | Outcome |
|----------|-----------|---------|
| 以现有代码库为基础做 brownfield 初始化，而不是当作绿地项目重建 | 当前仓库已具备完整插件骨架和主要功能，初始化应围绕稳定化与发布收口 | Pending |
| 初始化阶段先跳过 codebase map 与项目级研究代理 | 当前任务是尽快建立 `.planning/` 上下文，后续如需可单独补做 map/research | Pending |
| 将当前里程碑聚焦到“锁语义正确性 + 中文默认体验 + 验证基线” | 这些问题直接影响插件是否可安全发布，比扩展新功能优先级更高 | Pending |

## Evolution

This document evolves at phase transitions and milestone boundaries.

**After each phase transition** (via `$gsd-transition`):
1. Requirements invalidated? Move to Out of Scope with reason
2. Requirements validated? Move to Validated with phase reference
3. New requirements emerged? Add to Active
4. Decisions to log? Add to Key Decisions
5. "What This Is" still accurate? Update if drifted

**After each milestone** (via `$gsd-complete-milestone`):
1. Full review of all sections
2. Core Value check - still the right priority?
3. Audit Out of Scope - reasons still valid?
4. Update Context with current state

---
*Last updated: 2026-04-02 after initialization*
