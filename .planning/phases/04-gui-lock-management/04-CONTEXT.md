# Phase 4: GUI Lock Management - Context

**Gathered:** 2026-04-02
**Status:** Ready for planning

<domain>
## Phase Boundary

本阶段只为锁主人提供稳定、直观的 GUI 管理入口与锁摘要面板，并允许在 GUI 内完成单人授权与移除。它不提前引入批量授权、重型可视化面板、审计系统或新的锁数据格式；这些内容分别留给后续阶段处理。

</domain>

<decisions>
## Implementation Decisions

### GUI 入口
- **D-01:** 主人的默认 GUI 入口是右键受管理的锁牌；不要求 Phase 4 额外引入新的必经命令入口。
- **D-02:** 现有 `/bl add`、`/bl remove`、`/bl info` 继续保留，作为兼容路径和回退路径，Phase 4 不能让原有命令流失效。

### 界面层级
- **D-03:** Phase 4 采用单页、摘要优先的箱子 GUI，而不是多页或分层菜单。
- **D-04:** GUI 首屏至少清晰展示 owner、已授权玩家列表、扩展牌数量和当前锁目标摘要，让主人打开后能立刻知道这把锁的状态。

### GUI 内操作范围
- **D-05:** GUI 在本阶段只支持单人添加授权与单人移除授权，不提前吸收批量添加、批量移除或更复杂的筛选操作。
- **D-06:** GUI 内的所有操作必须复用现有 owner/manage 语义和现有锁牌数据写入路径，结果要与 `/bl add`、`/bl remove` 保持一致，不能形成第二套规则。

### 与现有牌子编辑的关系
- **D-07:** GUI 取代主人“普通右键锁牌”时直接进入牌子编辑的默认行为，成为新的主交互。
- **D-08:** 仍然保留一个手动编辑牌子的低频回退方式，推荐用“潜行右键锁牌”进入原生牌子编辑，避免完全丢失兼容操作。

### the agent's Discretion
- GUI 的具体布局、槽位编排、按钮材质、翻页与输入引导细节可由后续 research / plan 决定，只要符合“摘要优先、低学习成本、主线程安全”的目标。
- 是否在 Phase 4 同时补一个轻量命令别名作为 GUI 辅助入口，可由后续规划根据代码结构决定，但不能成为核心依赖。

</decisions>

<specifics>
## Specific Ideas

- 这轮“现代化”优先提升的是主人管理锁时的第一触达体验，而不是推翻 v1.0 已稳定的牌子锁语义。
- Phase 4 的 GUI 应该像对现有 `/bl info + /bl add + /bl remove` 的收口，而不是另起一套与牌子数据脱节的新系统。
- 主人右键锁牌直接进入 GUI，会比继续先开牌子编辑再手改内容更符合“现代化”目标；但保留潜行回退，能避免把老操作方式彻底切断。

</specifics>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### Phase definition
- `.planning/ROADMAP.md` - Phase 4 的目标、成功标准与和 Phase 5/6 的边界
- `.planning/REQUIREMENTS.md` - `GUI-01`、`GUI-02`、`GUI-03` 的约束与验收方向
- `.planning/STATE.md` - 当前里程碑状态与 v1.0 已完成基线
- `.planning/phases/01-lock-target-correctness/01-CONTEXT.md` - shared-target、双箱与扩展牌语义基线
- `.planning/phases/02-authorization-protection-matrix/02-CONTEXT.md` - owner/manage/access 权限矩阵基线
- `.planning/phases/03-localization-release-readiness/03-CONTEXT.md` - “现代化”在 v1.0 的收口边界，避免 Phase 4 再次 scope creep

### Existing implementation
- `src/main/java/ym/signLock/listener/LockListener.java` - 当前右键锁牌进入牌子编辑、交互拦截与管理边界入口
- `src/main/java/ym/signLock/service/LockService.java` - 锁目标解析、锁摘要、授权写入、权限判定与 shared-target 统一语义
- `src/main/java/ym/signLock/command/SignLockCommand.java` - `/bl add|remove|info` 的兼容路径和当前用户反馈模型
- `src/main/java/ym/signLock/config/SignLockConfig.java` - GUI 引导文案、错误提示与兼容消息的配置承载点

</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets
- `LockService.describeLock(Block)` 已能提供 owner、allowedPlayers、extensionCount，适合作为 GUI 摘要数据源。
- `LockService.canManage(...)`、`addPlayerToLock(...)`、`removePlayerFromLock(...)` 已承载现有管理语义，GUI 应优先复用这些入口而不是重写授权规则。
- `SignLockCommand` 已有完整的兼容命令路径，可作为 GUI 行为核对基线和回退路径。

### Established Patterns
- 当前项目把锁语义集中在 `LockService`，把事件编排放在 `LockListener`；Phase 4 应继续保持这个职责边界。
- 现有锁数据仍然以牌子文本为真源，Phase 4 不能为了 GUI 方便引入破坏兼容的新存储层。
- Bukkit/Paper/Folia 兼容与主线程安全仍然是 release-blocking 约束，GUI 交互不能跨线程直接触碰 Bukkit 侧对象。

### Integration Points
- `LockListener.tryOpenOwnedSignEditor(...)` 是切入 GUI 的最自然替换点，可在这里把“普通右键 -> GUI / 潜行右键 -> 原牌子编辑”收口。
- `LockService.findManagedSignLock(...)` 与 `describeLock(...)` 可以把点击到的锁牌稳定映射到 canonical lock target，避免 GUI 在双箱上再次出现目标漂移。
- `LockService.addPlayerToLock(...)` / `removePlayerFromLock(...)` 是 GUI 与命令保持一致写回结果的关键接口。

</code_context>

<deferred>
## Deferred Ideas

- 批量添加授权 / 批量移除授权 - Phase 5
- 更强的锁信息可视化、访问/管理边界提示强化 - Phase 6
- 面向管理员的审计或总览面板 - 后续 backlog

</deferred>

---

*Phase: 04-gui-lock-management*
*Context gathered: 2026-04-02*
