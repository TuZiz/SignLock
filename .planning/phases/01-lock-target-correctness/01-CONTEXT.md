# Phase 1: Lock Target Correctness - Context

**Gathered:** 2026-04-02
**Status:** Ready for planning

<domain>
## Phase Boundary

本阶段只处理“主锁牌、扩展牌、单箱/双箱与容器访问在所有入口上指向同一个受保护目标”的正确性问题。它不新增权限系统、GUI、跨服能力或第三方联动，只收紧现有牌子锁语义并修复当前判定不一致。

</domain>

<decisions>
## Implementation Decisions

### 扩展牌归属
- **D-01:** `[更多用户]` 牌只要依附在同一把锁目标的任意合法半边上，就视为有效；对双箱来说，任意半边都可以承载扩展牌。
- **D-02:** 扩展牌继续允许多个，数量上限沿用配置 `max-more-user-signs`；阶段 1 不额外引入“单箱 1 / 双箱 2”之类的新硬编码限制。

### 双箱主锁语义
- **D-03:** 双箱整体只能有一把主锁语义；逻辑上只允许一个有效 `[锁]` 主锁存在，另一半不应再形成第二把独立主锁。
- **D-04:** 双箱任一半边上的主锁或扩展牌，都必须对整个双箱生效；访问、查锁、授权写入都以“共享目标”而不是“单个方块半边”为准。

### 锁牌编辑体验
- **D-05:** 主人仍然可以右键自己的锁牌进入编辑界面，但系统控制的结构行必须被保留，不能通过编辑破坏主锁头、扩展牌头或所有者行。
- **D-06:** Phase 1 默认保留“可直接编辑授权名单行”的行为，不强制把授权管理完全收口到 `/bl add` 和 `/bl remove`。

### 提示与交互一致性
- **D-07:** Phase 1 暂时保留 `locked-use` 与 `locked-container` 两类提示，不额外统一文案语义；优先保证拦截结果正确，再在后续发布阶段处理文案整合。

### the agent's Discretion
- 双箱共享目标的具体归一化算法可以由后续研究/规划决定，只要最终满足“同一把锁语义”。
- 访问判定与锁牌扫描的内部复用方式、辅助方法拆分和最小测试形式由后续阶段自行决定。

</decisions>

<specifics>
## Specific Ideas

- 用户已明确否定“单箱只能一个牌子、大箱只能两个牌子”的限制，要求恢复为可继续贴多个牌子，但只有主人能加扩展牌。
- 用户当前更关心行为正确性，不要求在 Phase 1 一并重做交互文案或管理入口。

</specifics>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### Phase definition
- `.planning/ROADMAP.md` - Phase 1 的目标、成功标准与计划边界
- `.planning/REQUIREMENTS.md` - Phase 1 对应的 LOCK-01 ~ LOCK-04、PROT-01、PROT-02 需求
- `.planning/PROJECT.md` - 项目级核心价值、兼容性约束与“不破坏现有牌子格式”的约束

### Existing implementation
- `src/main/java/ym/signLock/service/LockService.java` - 锁目标归一化、主锁扫描、扩展牌创建与授权写入的核心实现
- `src/main/java/ym/signLock/listener/LockListener.java` - 贴牌、右键、开箱、编辑锁牌等入口监听
- `src/main/java/ym/signLock/command/SignLockCommand.java` - `/bl add` `/bl remove` `/bl info` 对锁目标的解析与写入入口
- `src/main/java/ym/signLock/service/PlayerIdentityService.java` - 玩家身份归一化与名字解析，影响授权匹配

</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets
- `LockService`: 已经集中承载了锁目标解析、主锁搜索、扩展牌读取和授权写入，Phase 1 应继续以这里为主修复点。
- `LockListener`: 已经把贴牌、直接上锁、右键开箱、锁牌编辑等玩家入口集中到一个监听器，适合做入口一致性修复。
- `PlayerIdentityService`: 已有名称大小写和离线玩家 UUID 映射逻辑，可直接复用，不需要在本阶段重做身份系统。

### Established Patterns
- 当前插件采用“服务层判定 + 监听器拦截”的结构，后续实现应保持这种分层，而不是把更多锁判定散落回监听器。
- 锁数据以牌子文本为真源，不走数据库；Phase 1 应避免引入额外持久化结构。
- 配置决定锁头、文案和可上锁方块，业务代码通过 `SignLockConfig` 读取；本阶段不应绕开配置层硬写可见行为。

### Integration Points
- `findPlacementTarget`、`findLock`、`findManagedSignLock`、`collectManagedSigns` 是 Phase 1 的关键接缝点。
- `onSignChange`、`onPlayerInteract`、`onInventoryOpen` 与 `/bl add|remove|info` 都依赖同一套锁目标语义，必须联动验证。

</code_context>

<deferred>
## Deferred Ideas

None - discussion stayed within phase scope.

</deferred>

---

*Phase: 01-lock-target-correctness*
*Context gathered: 2026-04-02*
