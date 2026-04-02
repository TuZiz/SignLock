# Phase 5: Batch Authorization Flows - Context

**Gathered:** 2026-04-02
**Status:** Ready for planning

<domain>
## Phase Boundary

本阶段在 Phase 4 的单人 GUI 管理基础上，把授权管理升级为批量添加与批量移除，同时继续复用 v1.0 的锁语义、shared-target 解析和牌子文本作为真源。它不提前引入可视化审计面板、复杂搜索筛选、分页式成员管理或新的持久化结构；这些内容继续留给 Phase 6 或后续 backlog。

</domain>

<decisions>
## Implementation Decisions

### 批量入口
- **D-01:** 现有 GUI 继续作为主入口，不新增必须依赖的新命令入口或外部面板。
- **D-02:** 现有 `/bl add` 与 `/bl remove` 继续保留，并扩展为可接受多个玩家输入；单玩家旧写法必须继续可用。

### 批量添加语义
- **D-03:** GUI 的“添加授权”入口升级为批量输入模式，允许主人一次输入多个玩家名，而不是新增第二个“批量添加”按钮。
- **D-04:** 推荐采用轻量文本输入协议，接受以空格或逗号分隔的多个玩家名，保持低学习成本并兼容命令路径。
- **D-05:** 批量添加允许部分成功；结果需要区分“成功添加”“已存在”“名单已满/无法继续扩展”三类，不因为单个玩家失败就整批回滚。

### 批量移除语义
- **D-06:** GUI 内的批量移除优先沿用当前单页模型，采用“选择多个玩家后确认移除”的思路，而不是立刻跳到多页或复杂筛选界面。
- **D-07:** 批量移除结果必须至少区分“成功移除”“原本不存在/已不在锁中”“所有者不可移除”，并允许部分成功。

### GUI 与命令一致性
- **D-08:** GUI 和命令都必须复用同一套玩家名归一化与 `LockService` 写入语义，不能因为批量模式再分叉出第二套规则。
- **D-09:** 所有批量路径都必须继续遵守 owner/manage 权限边界，不能让已授权玩家获得批量管理能力。

### 反馈与现代化体验
- **D-10:** 批量结果优先使用聚合反馈，而不是为每个玩家刷一行消息；但消息内容必须足够明确，让主人知道哪些玩家成功、哪些失败、哪些因规则被拒绝。
- **D-11:** Phase 5 继续避免 scope creep：不在这阶段吸收搜索、排序、访问/管理边界可视化和审计历史。

### the agent's Discretion
- 具体的 GUI 选择态表现、确认按钮样式、批量输入提示文案和结果消息排版，可由后续 research / plan 根据现有代码结构决定，只要符合“单页延续、低学习成本、主线程安全”的目标。
- 命令侧是采用“多参数”还是“单字符串后自行拆分”的解析实现，可由后续规划基于 Bukkit 命令处理现状决定，但必须保留单玩家兼容写法。

</decisions>

<specifics>
## Specific Ideas

- Phase 5 的“现代化”重点不是把授权改成一套新系统，而是把 Phase 4 已可用的单人管理流升级为更高效的批量工作流。
- GUI 最好仍然让主人感觉是在同一个管理面板里完成扩展，而不是被迫跳去命令、分页菜单或聊天式向导之间来回切换。
- 命令兼容优先级很高：很多服主已经形成 `/bl add`、`/bl remove` 肌肉记忆，Phase 5 应该是“增强原语义”，而不是要求记新命令。
- 批量结果天然可能出现部分成功，Phase 5 应显式接受这种现实，并把反馈做清楚，而不是追求全有或全无。

</specifics>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### Phase definition
- `.planning/ROADMAP.md` - Phase 5 的目标、成功标准与和 Phase 4/6 的边界
- `.planning/REQUIREMENTS.md` - `BATCH-01`、`BATCH-02`、`BATCH-03` 的约束与验收方向
- `.planning/STATE.md` - 当前里程碑状态与 Phase 4 已完成基线
- `.planning/phases/04-gui-lock-management/04-CONTEXT.md` - Phase 4 GUI 入口、单页模型与兼容约束
- `.planning/phases/04-gui-lock-management/04-VERIFICATION.md` - Phase 4 已验证完成的 GUI、命令兼容与主线程安全基线
- `.planning/phases/02-authorization-protection-matrix/02-CONTEXT.md` - owner/manage/access 权限矩阵基线

### Existing implementation
- `src/main/java/ym/signLock/gui/LockManagementGui.java` - 当前单页 GUI 槽位与按钮结构
- `src/main/java/ym/signLock/gui/LockManagementGuiActionService.java` - 当前 GUI 单人 add/remove 与聊天桥接入口
- `src/main/java/ym/signLock/gui/LockManagementPendingInputStore.java` - 当前待输入会话存储，可作为批量输入扩展基础
- `src/main/java/ym/signLock/command/SignLockCommand.java` - `/bl add|remove|info` 兼容路径和现有参数形态
- `src/main/java/ym/signLock/service/LockPlayerNameNormalizer.java` - GUI 与命令共享的玩家名归一化路径
- `src/main/java/ym/signLock/service/LockService.java` - 授权写回、扩展牌生成、移除语义和 shared-target 一致性来源
- `src/main/java/ym/signLock/config/SignLockConfig.java` - 批量提示文案、结果消息与 GUI 引导文案的配置承载点

</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets
- `LockManagementGui` 已经有稳定单页框架，可在不改分页模型的前提下增加批量选择或批量确认入口。
- `LockManagementGuiActionService` 已经承接 GUI 点击与聊天输入流程，是批量输入和批量结果聚合的自然扩展点。
- `LockPlayerNameNormalizer` 已经把 GUI 与命令的名字归一化收口，为 Phase 5 保持一致语义提供了现成基础。
- `LockService.addPlayerToLock(...)` 与 `removePlayerFromLock(...)` 已经定义了单个玩家的真实写回语义，批量模式应围绕它们做编排而不是重写底层规则。

### Established Patterns
- 当前项目仍然把牌子文本作为唯一真源；Phase 5 不能为批量能力引入旁路缓存或第二套授权存储。
- GUI 动作与聊天写回都已经遵守“异步捕获、同步落地”的主线程安全边界；批量路径必须继续遵守。
- 兼容命令测试在 Phase 4 已经成为 release-blocking 约束；Phase 5 若改命令输入解析，必须补强这条回归。

### Integration Points
- `LockManagementPendingInputStore` 可从单个 add 输入扩展到“批量 add / 批量 remove / 选择确认”等待处理状态。
- `SignLockCommand.onCommand(...)` 当前只接受单参数 add/remove，Phase 5 的命令增强会从这里切入。
- `LockManagementGuiHolder` 当前持有玩家槽位映射，后续可扩展为可选中列表或批量确认态，而不必推翻单页 GUI。

</code_context>

<deferred>
## Deferred Ideas

- 更强的授权搜索、过滤、排序和分页 - Phase 6 或 backlog
- 访问/管理权限边界的更强可视化提醒 - Phase 6
- 审计日志、变更历史与管理员总览 - 后续 backlog

</deferred>

---

*Phase: 05-batch-authorization-flows*
*Context gathered: 2026-04-02*
