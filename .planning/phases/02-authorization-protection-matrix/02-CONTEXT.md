# Phase 2: Authorization & Protection Matrix - Context

**Gathered:** 2026-04-02
**Status:** Ready for planning

<domain>
## Phase Boundary

本阶段只收口授权管理、破坏保护、自动化防绕过与玩家身份解析的一致性，确保主人、被授权玩家、管理员绕过三种身份在核心路径上表现稳定一致。它不新增 GUI、跨服同步、数据库或新的锁类型，只把现有牌子锁的权限矩阵补完整。

</domain>

<decisions>
## Implementation Decisions

### 破坏权限边界
- **D-01:** 被授权玩家拥有“使用权限”而不是“管理/破坏权限”；他们可以打开或使用受保护目标，但不能破坏受保护方块本体，也不能拆除任何受管理锁牌。
- **D-02:** 主人和管理员绕过仍然拥有完整管理权限；锁牌、扩展牌与被保护方块本体在破坏语义上都归入“管理操作”，而不是普通访问操作。

### 自动化与环境保护
- **D-03:** 非玩家路径默认走最保守策略：只要自动化搬运、爆炸、活塞或流体会影响已受保护目标，就直接阻止，不区分“合法搬运”与“非法搬运”细粒度模式。
- **D-04:** 保护范围覆盖锁目标本体及其受管理牌子，并且对单箱、双箱、其他 `InventoryHolder` 路径保持同一套共享目标语义，不允许通过另一半箱体或附着牌位置绕过。

### 命令与身份解析
- **D-05:** `/signlock add`、`/signlock remove`、`/signlock info` 都继续以玩家当前瞄准的受管理牌子为入口；无效目标、扩展牌已满、目标玩家不存在于锁内等场景必须返回明确结果，不能静默失败。
- **D-06:** 授权匹配保持“大小写不敏感 + 已知 UUID 优先”的策略；当缓存里已知玩家 UUID 时，以同一身份视角处理改名；当缓存暂时未知时，仍允许按名字文本执行授权操作，但后续一旦识别到 UUID，行为应收敛到同一身份。
- **D-07:** 单箱、双箱、主锁牌、扩展牌在命令授权入口上都应表现一致；主人指向同一锁目标下的任意受管理牌子时，看到的授权结果必须一致。

### the agent's Discretion
- 具体由 `LockService` 还是监听器/命令层暴露新的“可访问”“可管理”“可破坏”辅助方法，可由后续研究与规划决定，只要最终权限矩阵清晰且可测试。
- 玩家身份缓存的预热时机、保存时机、以及命令层如何组织错误分支，可由后续规划按现有架构收口。

</decisions>

<specifics>
## Specific Ideas

- 当前阶段优先修正“授权能用但不该能拆”“自动化不该绕过”的边界，而不是新增更复杂的权限系统。
- 用户在前序阶段已经明确：单箱和双箱都允许继续增加多个牌子，但只有主人能继续添加扩展授权牌；Phase 2 需要在这个前提下把授权与破坏语义收紧。

</specifics>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### Phase definition
- `.planning/ROADMAP.md` - Phase 2 的目标、成功标准与计划边界
- `.planning/REQUIREMENTS.md` - Phase 2 对应的 `PROT-03`、`PROT-04`、`OPS-02`、`OPS-03`
- `.planning/STATE.md` - 当前阶段状态与 Phase 1 已完成前置条件
- `.planning/phases/01-lock-target-correctness/01-CONTEXT.md` - Phase 1 已锁定的 shared target 与扩展牌语义
- `.planning/phases/01-lock-target-correctness/01-VERIFICATION.md` - Phase 1 已验证通过的路径，避免 Phase 2 回退

### Existing implementation
- `src/main/java/ym/signLock/service/LockService.java` - `canAccess`、`canManage`、授权写入、共享目标与自动化保护入口的核心实现
- `src/main/java/ym/signLock/listener/LockListener.java` - 玩家交互、开箱、破坏、爆炸、活塞、流体、自动化事件的统一拦截入口
- `src/main/java/ym/signLock/command/SignLockCommand.java` - `/signlock add|remove|info|reload` 的目标解析与用户反馈
- `src/main/java/ym/signLock/service/PlayerIdentityService.java` - `players.yml` 身份缓存、大小写归一化与改名解析
- `src/main/java/ym/signLock/config/SignLockConfig.java` - `admin-bypass`、文案消息与保护相关配置

</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets
- `LockService.canAccess(...)` 与 `LockService.canManage(...)` 已经体现出“访问”和“管理”两层语义，Phase 2 可以在此基础上补齐“可破坏”边界，而不是重新散落权限判断。
- `PlayerIdentityService` 已经保存 `名字 -> UUID` 和 `UUID -> 最近名字` 的双向缓存，适合继续承接大小写不敏感与改名兼容逻辑。
- `SignLockCommand` 已经把命令入口统一成“瞄准锁牌”模型，适合继续强化错误分支和结果一致性，而不是引入新的选取方式。

### Established Patterns
- 当前插件依旧以“牌子文本作为锁数据真源”为基础，Phase 2 不应引入额外数据库或破坏现有牌面格式。
- 监听器层负责拦截事件，服务层负责锁目标解析与权限判断；Phase 2 应继续维持这条职责边界。
- `admin-bypass` 是既有配置能力，Phase 2 需要兼容它，而不是另起一套管理员特判路径。

### Integration Points
- `LockListener.onBlockBreak(...)` 当前对锁目标本体和受管理牌子都调用 `canAccess(...)`，这是 Phase 2 需要收紧的关键入口。
- `LockListener.onInventoryMove(...)`、`onBlockExplode(...)`、`onEntityExplode(...)`、`onPistonExtend(...)`、`onPistonRetract(...)`、`onFluidFlow(...)` 共同构成自动化与环境保护矩阵，需要统一验证。
- `SignLockCommand.handleAdd(...)`、`handleRemove(...)` 与 `PlayerIdentityService.resolveStoredName(...)` / `findUuidByName(...)` 一起决定授权名单对离线玩家、大小写差异和改名场景的稳定性。

</code_context>

<deferred>
## Deferred Ideas

None - discussion stayed within phase scope.

</deferred>

---

*Phase: 02-authorization-protection-matrix*
*Context gathered: 2026-04-02*
