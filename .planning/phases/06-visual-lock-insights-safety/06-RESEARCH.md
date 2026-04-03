# Phase 6: Visual Lock Insights & Safety - Research

**Researched:** 2026-04-03
**Domain:** Bukkit/Paper/Folia lock summary UX, viewer-safe read-only GUI, and regression closeout on top of the existing SignLock management flow
**Confidence:** MEDIUM

<user_constraints>
## User Constraints (from CONTEXT.md)

### Locked Decisions
### 锁信息面向对象
- **D-01:** 主人继续保留当前完整管理 GUI，仍然是唯一的管理入口，不把 manage 权限下放给已授权玩家。
- **D-02:** 已授权玩家默认获得只读锁摘要视图，用来看 owner、授权范围、扩展牌使用情况和目标摘要；未授权玩家不开放完整名单与管理信息，继续停留在拒绝访问反馈层。
- **D-03:** `/bl info` 继续保留为兼容命令路径，并与 GUI 复用同一套摘要语义，作为最低摩擦的只读查看入口。
### 权限边界表达
- **D-04:** Phase 6 需要把“可访问”和“可管理”明确写出来，而不是让玩家靠按钮有无来猜权限边界。
- **D-05:** 主人和管理员绕过在摘要中显示为可管理；已授权玩家只显示为可访问；任何只读视图都不能出现 add/remove/select 之类暗示管理权限的控件。
### 目标摘要语义
- **D-06:** 目标摘要要从当前偏技术化的方块枚举 + 坐标，升级为更人话的目标类型描述，例如单箱、双箱、木桶、潜影盒、受管理牌子锁等。
- **D-07:** 双箱摘要必须继续按 shared-target 语义展示成“一个双箱锁目标”，不能因为点到左半、右半或扩展牌而显示成不同目标；坐标可保留为次级信息。
### Safety 与回归闭环
- **D-08:** Phase 6 默认按“完整 closeout”执行，而不是只做最小补丁。自动化回归至少覆盖 viewer / manager 边界、`/bl info` 与 GUI 摘要对齐、单箱 / 双箱 / managed-sign 摘要一致性，以及 v1.0 保护矩阵不回退。
- **D-09:** 手测基线需要显式包含 Paper 与 Folia 运行时检查，重点验证 GUI 打开、刷新、聊天输入回跳、只读查看与主线程安全边界。

### Deferred Ideas (OUT OF SCOPE)
- 审计日志、变更历史、管理员总览面板
- 搜索 / 过滤 / 分页式授权成员管理
- Web 端或跨服远程管理
</user_constraints>

<phase_requirements>
## Phase Requirements

| ID | Description | Research Support |
|----|-------------|------------------|
| VIS-01 | 玩家可以更直观地查看锁的所有者、授权列表和扩展牌使用情况。 | 复用现有 `LockService.describeLock(...)` 数据源，但需要新增人话 target summary 和 viewer-aware role label，把 GUI 与 `/bl info` 收口到同一摘要模型。 |
| VIS-02 | GUI 或信息展示需要明确区分“可访问”和“可管理”边界，避免误解权限。 | 当前 `canAccess(...)` 与 `canManage(...)` 已分离，Phase 6 重点是新增只读 viewer path 和显式 scope 文案，而不是改权限规则。 |
| VIS-03 | 锁信息展示要兼容单箱、双箱与受管理牌子的 shared-target 语义。 | 现有 canonical target 已经稳定，需把 `LockSummaryTarget` 从原始 `blockType + coords` 提升为 human-readable canonical summary。 |
| SAFE-01 | v1.0 的牌子创建、扩展牌放置、命令授权和保护矩阵行为不能回退。 | 需要复用现有 listener / command / protection matrix 回归套件，并把新 viewer 入口覆盖进同一条 closeout 验证链。 |
| SAFE-02 | 新交互不能破坏 Spigot / Paper / Folia 兼容声明和主线程安全边界。 | GUI 打开、刷新、聊天输入回跳仍需沿用现有 `nextTick` handoff；不引入异步 Bukkit 写回；Paper/Folia 手测要显式列入计划。 |
| SAFE-03 | 仓库要补充对应的测试和手测更新，覆盖新 GUI / 锁信息主路径。 | 现有测试分层已经足够，Phase 6 需要补 viewer/read-only 套件、info parity 套件和 smoke checklist 更新。 |
</phase_requirements>

## Project Constraints

- 不能破坏现有牌子文本真源、扩展牌写回逻辑和 canonical shared-target 语义。
- 继续兼容 Spigot / Paper / Folia，尤其是 GUI 打开、Inventory click 和 chat input handoff 的主线程边界。
- owner 普通右键 managed sign 打开 GUI、潜行右键保留原生牌子编辑，这条交互基线不能回退。
- `/bl add`、`/bl remove`、`/bl info` 都已有兼容回归；Phase 6 只能增强摘要表达，不应要求服主改学习路径。

## Summary

Phase 6 最稳的路线不是再做一套新面板，而是在现有单页 GUI 和 `/bl info` 之上建立一层共享的“锁摘要表达契约”。当前代码已经具备三个关键前提：`LockService.describeLock(...)` 提供 owner / allowedPlayers / extensionCount；`LockService.canAccess(...)` 与 `canManage(...)` 已明确分开；`LockManagementGuiService.createHolder(...)` 和 `SignLockCommand.handleInfo(...)` 都是天然的摘要装配入口。也就是说，Phase 6 不需要新增持久层，只需要把“摘要长什么样、面向谁显示、权限标签怎么说”做成共享模型。

Viewer 模式方面，最安全的方案是“同一把锁，两种 GUI 视图”：主人/管理员仍走完整管理视图，已授权玩家走只读摘要视图。这样能复用当前 `LockManagementGui` 和 holder/session 骨架，同时避免把 add/remove 逻辑暴露给无管理权限的玩家。相比之下，为已授权玩家新建独立命令或完全独立 inventory，会增加语义漂移风险，也更容易在后续维护中与 owner 视图脱节。

Target summary 方面，当前 `LockSummaryTarget` 只携带 `blockType/world/x/y/z`，已经足够“定位”，但不够“说明”。Phase 6 应把它升级为 canonical shared-target 的人话摘要，例如“单箱锁”“双箱锁”“木桶锁”“潜影盒锁”，并保留坐标作为次级信息。这样做能直接服务 VIS-03，也能减少玩家在双箱场景下的误判。

Safety closeout 方面，现有测试骨架已经很好：`LockManagementGuiViewTest` 锁住 GUI 摘要数据，`LockListenerGuiEntryTest` 锁住 owner GUI 入口，`SignLockCommandAuthorizationTest` 锁住 `/bl info` 的 targeted sign compatibility，`LockListenerProtectionMatrixTest` 锁住 v1.0 保护矩阵。Phase 6 不需要新建一套验证哲学，重点是补 viewer/read-only 路径、summary parity 路径和 Paper/Folia 手测合同。

**Primary recommendation:** 先在 Wave 1 建立共享摘要模型和 role-aware scope 表达，再在 Wave 2 接入 owner/manage vs authorized/read-only GUI 分流，最后在 Wave 3 把 `/bl info` 与 smoke/closeout 完整收口。这样可以把 VIS 系需求与 SAFE 系需求拆开推进，减少一边改 GUI 一边补 closeout 时互相踩文件的风险。

## Standard Stack

### Core
| Library | Version | Purpose | Why Standard |
|---------|---------|---------|--------------|
| Spigot API | `1.21.11-R0.1-SNAPSHOT` | 插件命令、GUI 与事件 API 基线 | 当前项目已在这条基线工作，Phase 6 不引入新依赖更稳。 |
| Paper API | `1.21.11-R0.1-SNAPSHOT` | Paper / Folia 兼容面与真实运行时验证 | Phase 6 的 viewer GUI 与 refresh 行为需要继续沿用这条兼容面。 |
| Repo-local summary / view model | none | viewer role、human-readable target、GUI / command parity | 最符合 brownfield sign-backed 结构，不会引入新持久层。 |

### Supporting
| Library | Version | Purpose | When to Use |
|---------|---------|---------|-------------|
| MockBukkit | `4.108.0` | GUI / listener / command summary regression | 继续作为 Phase 6 自动化回归主力。 |
| JUnit Jupiter | `6.0.3` | 测试框架 | 所有新增 Phase 6 回归继续沿用。 |
| Mockito | `5.23.0` | mock 玩家权限、命令消息、GUI reopen 侧效应 | 验证 viewer / manager 差异、命令摘要输出和回调边界。 |

### Alternatives Considered
| Instead of | Could Use | Tradeoff |
|------------|-----------|----------|
| 复用现有 GUI 做 viewer/read-only 变体 | 新建一套只读 inventory 或额外命令入口 | 语义更容易漂移，也会增加 future maintenance 成本。 |
| 共享摘要模型供 GUI 与 `/bl info` 共用 | GUI 和命令各自拼文案 | 最容易在 Phase 6 结束后出现展示不一致，违背 D-03。 |
| human-readable canonical target summary | 保持原始 block enum + 坐标 | 技术上省事，但很难真正满足 VIS-01/VIS-03 的“更直观”。 |

## Architecture Patterns

### Pattern 1: Shared Summary Model
**What:** 把 owner、allowedPlayers、extensionCount、viewerScope、targetLabel 聚合成一套共享摘要模型，由 GUI 和 `/bl info` 共用。  
**When to use:** 所有锁状态展示入口。  
**Why:** 保证 GUI 与命令展示的语义完全一致。

### Pattern 2: Role-Aware Read-Only Variant
**What:** 保留当前管理 GUI 结构，但基于 viewer scope 决定是否渲染 add/remove/select 控件。  
**When to use:** 主人/管理员与已授权玩家都从 managed sign 查看锁摘要时。  
**Why:** 最低成本复用现有 GUI 骨架，同时不放大 manage 权限。

### Pattern 3: Canonical Shared-Target Presentation
**What:** 基于 canonical target block 推导“单箱 / 双箱 / 木桶 / 潜影盒 / 受管理牌子锁”等人话标签，并保留坐标辅助定位。  
**When to use:** GUI target slot、`/bl info`、必要的测试断言。  
**Why:** 直接服务 VIS-03，并减少双箱半边点击导致的认知错位。

### Pattern 4: Closeout Regression Net
**What:** 用 `gui + listener + command + protection matrix` 四层测试收口，再补一份 Paper/Folia 手测清单。  
**When to use:** Phase 6 执行与 closeout。  
**Why:** SAFE-01/02/03 本质上是“不要因新 UX 路径回退稳定性”，这类目标最适合多层回归网。

## Key Findings

### Finding 1: 现有代码已经具备 viewer / manager 分流的基础条件
- `LockListener` 已经在 `tryOpenOwnedSignEditor(...)` 中区分 owner 点击 managed sign 的 GUI / native sign editor 分流。
- `LockService.canAccess(...)` 与 `canManage(...)` 已经明确区分访问与管理，不需要 Phase 6 再修改底层权限矩阵。
- `LockManagementGuiService.createHolder(...)` 是注入 viewer role 与 target summary 的自然切入点。

### Finding 2: `/bl info` 与 GUI 现在共享数据源，但还没共享表达层
- `SignLockCommand.handleInfo(...)` 已复用 `LockService.describeLock(...)`，说明命令和 GUI 在数据源层已经靠拢。
- 但当前命令只输出 owner / players / extensionCount 三行，GUI 也只显示方块类型和坐标，没有共享 target label 或 permission label。
- 因此最合适的增强点是“共享摘要模型”，而不是分别修改两套文案。

### Finding 3: 已授权玩家的只读摘要最好走 managed sign 入口，而不是容器本体入口
- 容器本体右键当前仍有正常 access 行为，贸然改成打开摘要会破坏已有使用习惯。
- managed sign 当前就是 owner GUI 入口，最适合作为 authorized read-only summary 的对称入口。
- 未授权玩家保持现有拒绝访问反馈即可，避免把敏感名单暴露给旁观者。

### Finding 4: SAFE closeout 的关键不是再发明测试，而是把新 viewer path 纳入现有回归网络
- `LockListenerProtectionMatrixTest` 已经锁住 explosions / fluids / pistons / shared-target。
- `LockManagementGuiViewTest` 已经锁住 canonical target summary data 的一部分。
- `SignLockCommandAuthorizationTest` 已经锁住 `/bl info` 针对 primary / extension managed sign 的 targeted entry point。
- Phase 6 主要补 viewer path、role labels、human-readable target summary 和 smoke checklist，不需要推翻现有测试分层。

## Neuron Evidence

- 项目记忆里已记录 Phase 4/5 成功路线：单页 chest GUI、shared summary data、chat input bridge、command compatibility 都是稳定路径，Phase 6 适合继续沿用，不适合新建平行系统。
- 当前工作上下文和项目决策也明确锁定：不破坏 sign-backed data、不打断 `/bl` 兼容路径、保持 shared-target chest lock semantics。

## Planning Implications

1. Wave 1 先把“摘要模型 + viewer scope + target label”收口成共享契约，并用测试锁定 `/bl info` 与 GUI 后续都必须对齐的表达层。
2. Wave 2 再接 owner/manage GUI 与 authorized/read-only GUI 的分流，保持 managed sign 作为入口，避免碰容器本体访问行为。
3. Wave 3 最后再把 `/bl info` 文案、manual smoke checklist 和 full regression closeout 补完，确保 SAFE-01/02/03 有完整收口。

## Recommended Execution Order

1. 新增或扩展 summary/viewer 相关测试与模型，让 GUI / command 共享同一套 target label 与 permission scope 语义。
2. 在 `LockListener`、`LockManagementGuiService`、`LockManagementGui` 上接 viewer/read-only GUI 变体，不触碰底层写回语义。
3. 更新 `/bl info`、补齐 Phase 6 相关测试与手测合同，最后用 full suite 做 closeout。

---

*Phase: 06-visual-lock-insights-safety*
*Research completed: 2026-04-03*
