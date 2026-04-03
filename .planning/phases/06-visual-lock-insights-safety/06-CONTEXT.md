# Phase 6: Visual Lock Insights & Safety - Context

**Gathered:** 2026-04-03
**Status:** Ready for planning

<domain>
## Phase Boundary

本阶段聚焦两件事：把锁状态做得更容易理解，以及把 v1.1 新增 GUI / 批量授权 / 信息查看路径的安全与回归覆盖补完整。它不引入审计面板、搜索分页、远程管理或新的持久化结构，只在现有牌子锁语义之上增强信息表达与验证闭环。

</domain>

<decisions>
## Implementation Decisions

### 锁信息面向对象
- **D-01:** 主人继续保留当前完整管理 GUI，仍然是唯一的管理入口，不把 manage 权限下放给已授权玩家。
- **D-02:** 已授权玩家默认获得只读锁摘要视图，用来看 owner、授权范围、扩展牌使用情况和目标摘要；未授权玩家不开放完整名单与管理信息，继续停留在拒绝访问反馈层。
- **D-03:** `/bl info` 继续保留为兼容命令路径，并与 GUI 复用同一套摘要语义，作为最低摩擦的只读查看入口。

### 权限边界表达
- **D-04:** Phase 6 需要把“可访问”和“可管理”明确写出来，而不是让玩家靠按钮有无来猜权限边界。
- **D-05:** 主人和管理员绕过在摘要中显示为可管理；已授权玩家只显示为可访问；任何只读视图都不能出现 add/remove/select 之类暗示管理权限的控件。

### 目标摘要语义
- **D-06:** 目标摘要要从当前偏技术化的方块枚举 + 坐标，升级为更人话的目标类型描述，例如单箱、双箱、木桶、潜影盒、受管理牌子锁等。
- **D-07:** 双箱摘要必须继续按 shared-target 语义展示成“一整个双箱锁目标”，不能因为点到左半、右半或扩展牌而显示成不同目标；坐标可保留为次级定位信息。

### Safety 与回归闭环
- **D-08:** Phase 6 默认按“完整 closeout”执行，而不是只做最小补丁。自动化回归至少覆盖 viewer / manager 边界、`/bl info` 与 GUI 摘要对齐、单箱 / 双箱 / managed-sign 摘要一致性，以及 v1.0 保护矩阵不回退。
- **D-09:** 手测基线需要显式包含 Paper 与 Folia 运行时检查，重点验证 GUI 打开、刷新、聊天输入回跳、只读查看与主线程安全边界。

### the agent's Discretion
- 具体图标、槽位重排、文案长短、只读视图是复用现有 GUI 还是轻量变体，可由后续 research / plan 根据现有单页 GUI 骨架决定，只要不打破 owner-only manage 规则。
- `/bl info` 是继续维持聊天文本输出再补清晰提示，还是收口到与 GUI 同一摘要模型后输出更整洁的多行文案，可由后续规划决定，但必须保持命令兼容。

</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### Phase definition
- `.planning/ROADMAP.md` - Phase 6 的目标、成功标准和与 Phase 4/5 的边界
- `.planning/REQUIREMENTS.md` - `VIS-01`、`VIS-02`、`VIS-03`、`SAFE-01`、`SAFE-02`、`SAFE-03` 的验收方向
- `.planning/STATE.md` - 当前里程碑状态、Phase 6 焦点和上阶段遗留的真实客户端 smoke 风险

### Locked decisions from prior phases
- `.planning/phases/01-lock-target-correctness/01-CONTEXT.md` - canonical target、shared-target、主锁与扩展牌语义
- `.planning/phases/02-authorization-protection-matrix/02-CONTEXT.md` - owner / manage / access / break 的权限边界
- `.planning/phases/04-gui-lock-management/04-CONTEXT.md` - GUI 入口、单页摘要优先、潜行回退到原生牌子编辑
- `.planning/phases/04-gui-lock-management/04-VERIFICATION.md` - Phase 4 已建立的 GUI、listener、command 回归基线
- `.planning/phases/05-batch-authorization-flows/05-CONTEXT.md` - GUI 与命令共享批量语义、聚合反馈与兼容路径
- `.planning/phases/05-batch-authorization-flows/05-VERIFICATION.md` - Phase 5 已验证通过的 batch GUI / command closeout 基线

### Existing implementation
- `src/main/java/ym/signLock/gui/LockManagementGui.java` - 当前单页 GUI 的摘要槽位、玩家列表与按钮结构
- `src/main/java/ym/signLock/gui/LockManagementGuiService.java` - GUI holder 构造、摘要装配与打开入口
- `src/main/java/ym/signLock/gui/LockManagementGuiHolder.java` - 当前 session、summary view 和选中状态承载点
- `src/main/java/ym/signLock/gui/LockSummaryView.java` - 当前锁摘要视图模型
- `src/main/java/ym/signLock/gui/LockSummaryTarget.java` - 当前目标摘要模型，仍偏原始 block enum + 坐标
- `src/main/java/ym/signLock/gui/LockManagementGuiActionService.java` - GUI 点击、批量移除、聊天输入回跳与 manage 校验
- `src/main/java/ym/signLock/command/SignLockCommand.java` - `/bl info`、`/bl add`、`/bl remove` 的兼容路径与现有摘要输出
- `src/main/java/ym/signLock/service/LockService.java` - `describeLock(...)`、`canAccess(...)`、`canManage(...)` 与 shared-target 统一语义
- `src/main/java/ym/signLock/listener/LockListener.java` - 主人普通右键 GUI / 潜行右键原生编辑的入口路由
- `src/main/java/ym/signLock/config/SignLockConfig.java` - GUI / info / batch 反馈文案与默认消息承载点

### Verification anchors
- `src/test/java/ym/signLock/gui/LockManagementGuiViewTest.java` - 当前 GUI 摘要渲染与 canonical target 基线
- `src/test/java/ym/signLock/gui/LockManagementBatchGuiTest.java` - 现有单页 GUI 的批量选择与结果反馈测试骨架
- `src/test/java/ym/signLock/listener/LockListenerGuiEntryTest.java` - owner GUI 入口 / sneak fallback 现有行为锁定
- `src/test/java/ym/signLock/listener/LockListenerProtectionMatrixTest.java` - explosions / fluid / piston / shared-target 保护矩阵回归
- `src/test/java/ym/signLock/command/SignLockCommandAuthorizationTest.java` - `/bl info` 与 targeted managed sign 兼容入口基线
- `src/test/java/ym/signLock/command/SignLockCommandCompatibilityTest.java` - 旧命令语义兼容基线
- `src/test/java/ym/signLock/command/SignLockCommandBatchCompatibilityTest.java` - Phase 5 批量命令兼容基线

</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets
- `LockSummaryView` 与 `LockSummaryTarget` 已经提供了摘要模型，Phase 6 可以在不新建存储层的前提下扩展 viewer mode、权限标签和更人话的 target summary。
- `LockManagementGui` 已经有稳定的单页 inventory 骨架，适合继续承载“管理态”与“只读态”两个视图分支，而不必新起多页菜单。
- `SignLockCommand.handleInfo(...)` 与 `LockService.describeLock(...)` 已经提供兼容命令路径和基础摘要数据，是 GUI / command 对齐的天然锚点。

### Established Patterns
- shared-target、授权判定和保护矩阵仍然集中在 `LockService`，Phase 6 不能把 viewer / manager 语义散落到 GUI 或命令层单独维护。
- sign interaction 路由集中在 `LockListener`，GUI 点击与聊天输入桥接集中在 `LockManagementGuiActionService`；Phase 6 应延续这条职责边界。
- 现有测试已经按 `gui / listener / command / protection matrix` 分层，适合补成 Phase 6 的 closeout regression 网。

### Integration Points
- `LockListener.tryOpenOwnedSignEditor(...)` 是扩展 owner-only 管理视图与 authorized read-only 视图分流的首选切入点。
- `LockManagementGuiService.createHolder(...)` 和 `LockManagementGuiHolder` 是注入 viewer role、权限标签和 target summary 的自然入口。
- `SignLockCommand.handleInfo(...)` 是把聊天摘要与 GUI 摘要模型收口到同一语义的关键整合点。
- `LockListenerProtectionMatrixTest`、`LockManagementGuiViewTest`、`SignLockCommandAuthorizationTest` 已经给出了 closeout 所需的大部分测试骨架。

</code_context>

<specifics>
## Specific Ideas

- 这轮“更现代化”的重点不是继续加新功能，而是让玩家更容易看懂“这把锁是谁的、我能做什么、这个目标到底是哪一个”。
- 推荐把 owner / authorized / unauthorized 三种视角拆清：主人看完整管理视图，已授权玩家看只读摘要，未授权玩家只保留必要的拒绝反馈。
- 推荐让 GUI 和 `/bl info` 共用同一套摘要语言，这样玩家不会在两个入口看到冲突的信息。
- 双箱建议明确写成“共享锁目标”或同等人话表达，避免又回到“点击哪一半就像是不同箱子”的认知问题。

</specifics>

<deferred>
## Deferred Ideas

- 审计日志、变更历史、管理员总览面板 - 后续 backlog
- 搜索 / 过滤 / 分页式授权成员管理 - 后续 backlog
- Web 端或跨服远程管理 - 当前里程碑外

</deferred>

---

*Phase: 06-visual-lock-insights-safety*
*Context gathered: 2026-04-03*
