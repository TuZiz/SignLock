# Phase 4: GUI Lock Management - Research

**Researched:** 2026-04-02
**Domain:** Bukkit/Paper/Folia chest-inventory GUI for SignLock owner management
**Confidence:** MEDIUM

<user_constraints>
## User Constraints (from CONTEXT.md)

### Locked Decisions
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

### Deferred Ideas (OUT OF SCOPE)
- 批量添加授权 / 批量移除授权 - Phase 5
- 更强的锁信息可视化、访问/管理边界提示强化 - Phase 6
- 面向管理员的审计或总览面板 - 后续 backlog
</user_constraints>

<phase_requirements>
## Phase Requirements

| ID | Description | Research Support |
|----|-------------|------------------|
| GUI-01 | 锁主人可以通过稳定入口打开锁管理界面，而不必只依赖命令和手改牌子。 | 入口建议固定在 `LockListener.tryOpenOwnedSignEditor(...)`；普通右键开 GUI，潜行右键保留 `Player#openSign(...)` 回退。 |
| GUI-02 | 锁管理界面能显示所有者、授权玩家、扩展牌数量和当前锁目标摘要。 | 直接复用 `LockService.describeLock(Block)`，并补一个 target summary 组装器，基于 canonical target 输出方块类型与坐标摘要。 |
| GUI-03 | 锁主人可以在 GUI 中执行单人授权与移除，而不破坏原有牌子数据格式。 | GUI 点击动作必须复用 `LockService.canManage(...)`、`addPlayerToLock(...)`、`removePlayerFromLock(...)` 与 `PlayerIdentityService.resolveStoredName(...)`。 |
</phase_requirements>

## Project Constraints (from CLAUDE.md)

- 继续使用 Java Plugin + Gradle Wrapper + Spigot API `1.21.11-R0.1-SNAPSHOT` 基线，避免框架迁移风险。
- 必须保持 Spigot / Paper / Folia 兼容声明与主线程安全。
- 不能破坏已有锁牌数据和当前牌子格式。
- 默认体验必须对中文服务器友好。
- 通过 GSD 流程推进工作；本 research 只给 planner 提供方案，不绕开规划工件。

## Summary

Phase 4 最稳的实现路径是不引入任何新的 GUI 框架，而是在现有 `listener/service/command` 架构上补一层轻量 chest GUI：`LockListener` 负责入口切换，新增 GUI 控制器负责打开与刷新库存界面，`LockService` 继续作为唯一锁语义与写回入口。这样可以满足 `GUI-01` 到 `GUI-03`，同时不改牌子文本真源、不引入 Adventure/UI 兼容风险，也不把 Phase 4 膨胀成完整菜单系统。

交互上，普通右键受管理锁牌打开单页摘要 GUI，潜行右键仍调用原生 `openSign(...)` 进入牌子编辑。GUI 首页展示 owner、授权玩家、扩展牌数量和锁目标摘要；移除动作应优先设计成“点击玩家槽位即移除”，避免额外输入复杂度。唯一需要额外输入的是“新增授权玩家”，对这个点最适合的 Phase 4 方案是“GUI 按钮进入一次性待输入状态，再通过聊天输入玩家名并在同步调度中完成写回与 GUI 重开”；不要在本阶段引入 anvil GUI、NMS、ProtocolLib 或第二套持久化。

测试层面，仓库已经有 MockBukkit + JUnit + Mockito 基础。Phase 4 不需要重建验证框架，而是新增 GUI 入口、摘要渲染、点击取消、单人 add/remove 与潜行回退测试，并保留当前 `LockListener` / `LockService` 的回归覆盖。由于 Spigot `InventoryClickEvent` 官方文档明确要求 `openInventory/closeInventory` 等跳转放到 next tick，planner 需要把“点击事件只计算状态，界面跳转统一 next-tick 调度”作为硬规则。

**Primary recommendation:** 使用 repo 内轻量自定义 chest GUI + custom `InventoryHolder` + next-tick UI 转场；普通右键进 GUI，潜行右键保留原牌子编辑，所有写操作只复用 `LockService`。

## Standard Stack

### Core
| Library | Version | Purpose | Why Standard |
|---------|---------|---------|--------------|
| Spigot API | `1.21.11-R0.1-SNAPSHOT` | 主插件 API 基线 | 已是当前项目编译基线，直接匹配现有 listener/service 实现。 |
| Paper API | `1.21.11-R0.1-SNAPSHOT` | 测试时补足 Paper 侧 API 兼容面 | 当前测试依赖已存在，有助于提前暴露较新的 inventory/chat API 差异。 |
| Repo-local custom GUI | none | Phase 4 单页 GUI、会话标识、点击路由 | 不引入新框架，不扩大迁移与兼容面，最符合 brownfield 约束。 |

### Supporting
| Library | Version | Purpose | When to Use |
|---------|---------|---------|-------------|
| MockBukkit | `4.108.0` | 单元/集成测试 GUI 入口与事件逻辑 | 覆盖 `PlayerInteractEvent`、`InventoryClickEvent`、`InventoryDragEvent`、close/reopen 逻辑。 |
| JUnit Jupiter | `6.0.3` | 测试框架 | Phase 4 所有新增自动化测试继续沿用。 |
| Mockito | `5.23.0` | mock Player、Inventory、scheduler 边界 | 对 GUI 点击、副作用消息与回退行为做精确断言。 |
| run-paper Gradle plugin | `2.3.1` | 手测/烟雾验证时拉起 Paper 服 | 当 planner 需要追加真实服手测脚本时使用。 |

### Alternatives Considered
| Instead of | Could Use | Tradeoff |
|------------|-----------|----------|
| Repo-local custom GUI | SmartInvs / Triumph GUI / EasyGUI 等第三方 GUI 框架 | 会引入新依赖、额外生命周期与兼容面，不符合当前阶段“单页、摘要优先、低迁移风险”目标。 |
| String inventory title | Adventure Component title | 当前 repo 没有 Adventure shading 需求；Phase 4 为了一个标题能力引入组件栈，收益过低，且有真实服打包风险。 |
| Guided chat input for add-player | Anvil GUI / NMS sign input / ProtocolLib 输入桥 | 复杂度和兼容面明显超出 Phase 4；建议留到后续阶段或完全不做。 |

**Installation:**
```bash
./gradlew.bat test
```

**Version verification:** Phase 4 不推荐新增任何 GUI 依赖；上述版本均为当前仓库 `build.gradle` 已固定基线，2026-04-02 已通过 `gradlew.bat` 与现有测试验证可用。

## Architecture Patterns

### Recommended Project Structure
```text
src/main/java/ym/signLock/
├── gui/                    # GUI holder、view model、open/refresh orchestration
├── listener/               # 入口监听与 GUI click/drag/close/input 事件
├── service/                # 继续承载 LockService / PlayerIdentityService 语义
├── command/                # 保留 /bl add|remove|info 兼容路径
└── config/                 # GUI 文案、标题、按钮提示
```

### Pattern 1: Sign Interaction Gate
**What:** 在 `LockListener` 内把“普通右键锁牌”切到 GUI，把“潜行右键锁牌”保留给原生牌子编辑。  
**When to use:** 处理 owner 点击受管理锁牌时。  
**Example:**
```java
// Source: local codebase + Spigot Player#openSign docs
private boolean tryOpenLockManagement(PlayerInteractEvent event, Player player, Block clicked) {
    if (!(clicked.getState() instanceof Sign sign)) {
        return false;
    }

    LockInfo lock = lockService.findManagedSignLock(clicked);
    if (lock == null || !lockService.canManage(lock, player)) {
        return false;
    }

    event.setCancelled(true);
    if (player.isSneaking()) {
        player.openSign(sign);
    } else {
        lockGuiService.openFor(player, sign);
    }
    return true;
}
```

### Pattern 2: Canonical Target Session Key
**What:** GUI session 不直接信任“被点击的哪一块牌子”，而是存 canonical target 坐标和首选 sign 坐标。  
**When to use:** 双箱、扩展牌、多块 related block 都可能指向同一把锁时。  
**Example:**
```java
// Source: local LockService canonical target pattern
record LockSessionKey(UUID worldId, int x, int y, int z, Location signLocation) {}

LockInfo lock = lockService.findManagedSignLock(signBlock);
Block target = lock.targetBlock();
LockSessionKey key = new LockSessionKey(
        target.getWorld().getUID(), target.getX(), target.getY(), target.getZ(), signBlock.getLocation()
);
```

### Pattern 3: Command-Parity GUI Actions
**What:** GUI 点击动作先做 owner/manage 检查，再调用与命令相同的 service 入口。  
**When to use:** 移除授权、添加授权、刷新摘要。  
**Example:**
```java
// Source: local SignLockCommand + LockService
String normalized = playerIdentityService.resolveStoredName(rawInput);
String targetPlayer = (normalized == null || normalized.isBlank()) ? rawInput : normalized;

if (!lockService.canManage(lock, actor)) {
    actor.sendMessage(config.addOnlyOwnerMessage());
    return;
}

AddPlayerResult result = lockService.addPlayerToLock(preferredSign, lock, targetPlayer);
```

### Pattern 4: Next-Tick Inventory Transition
**What:** `InventoryClickEvent`/`InventoryDragEvent` 里只 cancel 和记录动作；任何 `openInventory`、`closeInventory`、GUI refresh、聊天输入切换都延后一 tick。  
**When to use:** 所有 GUI 点击和拖拽事件。  
**Example:**
```java
// Source: Spigot InventoryClickEvent docs
@EventHandler(ignoreCancelled = true)
public void onGuiClick(InventoryClickEvent event) {
    if (!(event.getInventory().getHolder() instanceof LockGuiHolder holder)) {
        return;
    }

    event.setCancelled(true);
    GuiAction action = holder.resolve(event.getRawSlot());
    if (action == null) {
        return;
    }

    Bukkit.getScheduler().runTask(plugin, () -> guiActionService.apply((Player) event.getWhoClicked(), holder, action));
}
```

### Anti-Patterns to Avoid
- **在 GUI 点击事件里直接 `openInventory` / `closeInventory`:** Spigot 官方文档明确标为不安全，必须 next tick。
- **把 GUI 作为第二套授权系统:** 任何 owner/manage 判定都不能脱离 `LockService`。
- **按被点 sign 直接写回数据:** 双箱和扩展牌必须通过 canonical target 重解锁。
- **为 Phase 4 引入多页、搜索、批量操作:** 已越过 locked scope。
- **为了标题或输入引入 Adventure/NMS/ProtocolLib:** 对当前需求属于过度设计。

## Don't Hand-Roll

| Problem | Don't Build | Use Instead | Why |
|---------|-------------|-------------|-----|
| 锁数据来源 | 独立 GUI 数据表 / YAML / 内存索引 | 继续使用牌子文本 + `LockService` | 现有语义与兼容性都围绕牌子文本，重复存储会引发漂移。 |
| 权限判定 | GUI 专属 owner/manage 规则 | `LockService.canManage(...)` | 命令与 GUI 结果必须完全一致。 |
| 单人 add/remove 写回 | 手写 sign line 编辑逻辑 | `addPlayerToLock(...)` / `removePlayerFromLock(...)` | 已覆盖扩展牌、容量不足、owner 不可移除等边界。 |
| GUI 框架 | 多依赖菜单 DSL / NMS 容器层 | 简单 chest inventory + custom holder | Phase 4 只有单页摘要与单步动作，不值得引入框架成本。 |
| 名称解析 | GUI 内自建名字映射 | `PlayerIdentityService.resolveStoredName(...)` | 当前命令流已经依赖它处理改名兼容。 |

**Key insight:** Phase 4 的难点不是“怎么画个 GUI”，而是“怎么让 GUI 不发明第二套锁语义”。现有 `LockService` 已解决大多数真正复杂的边界，计划里应围绕复用它来拆任务。

## Common Pitfalls

### Pitfall 1: Click Handler Reopens Inventory Inline
**What goes wrong:** 点击一个按钮后直接在同一个 `InventoryClickEvent` 里 `openInventory`、`closeInventory` 或切换输入模式。  
**Why it happens:** chest GUI 看起来像普通回调，但 Spigot 把该事件定义在 inventory 修改流程内部。  
**How to avoid:** 统一 `event.setCancelled(true)` 后通过调度器 next tick 执行 refresh/close/reopen。  
**Warning signs:** 偶发重复点击、物品闪烁、GUI 无法稳定重开、MockBukkit 与真实服行为不一致。

### Pitfall 2: GUI Session 绑定到“点击到的这块牌”
**What goes wrong:** 双箱另一半或扩展牌点进来时，GUI 展示/写回的锁目标不一致。  
**Why it happens:** 把 sign block 当作锁主键，而不是 canonical target。  
**How to avoid:** session key 始终绑定 `LockInfo.targetBlock()` 的 world/x/y/z。  
**Warning signs:** 同一把锁从不同牌进入 GUI，显示人数或扩展牌数量不一致。

### Pitfall 3: Remove Action Bypasses Existing Result Semantics
**What goes wrong:** GUI 直接删行，导致 owner 被移除、NOT_FOUND 不可区分、空扩展牌不清理。  
**Why it happens:** 为了 GUI 快速落地，绕过 `LockService.removePlayerFromLock(...)`。  
**How to avoid:** GUI 只消费 `RemovePlayerResult`，不直接写 sign line。  
**Warning signs:** GUI remove 的反馈和 `/bl remove` 不同，或扩展牌残留空壳。

### Pitfall 4: Chat Input Touches Bukkit State Asynchronously
**What goes wrong:** 如果 add-player 走聊天输入，在异步聊天事件里直接操作锁和 GUI，Folia/Paper 下容易越线程。  
**Why it happens:** `AsyncChatEvent` 在真实玩家发言时通常是异步的。  
**How to avoid:** 聊天事件只捕获文本与取消广播；真正的锁写回与 GUI 重开切回同步调度。  
**Warning signs:** 偶发并发异常、Folia 上 region/thread 相关报错。

### Pitfall 5: GUI 没有拦住 Shift/Number Key/Drag
**What goes wrong:** 玩家把自己的物品塞进摘要 GUI，或通过热键绕过普通点击逻辑。  
**Why it happens:** 只监听普通 click，不拦 `InventoryDragEvent`、shift-click、number key。  
**How to avoid:** 对自定义 holder 的 inventory 全量 cancel click/drag，并忽略玩家背包区域的非目标槽位。  
**Warning signs:** 手测中玩家物品能进入 GUI 或 GUI 图标被搬走。

## Code Examples

Verified patterns from local code and official sources:

### GUI Entry Routing
```java
// Source: src/main/java/ym/signLock/listener/LockListener.java
if (lockService.findManagedSignLock(clicked) != null && lockService.canManage(lock, player)) {
    event.setCancelled(true);
    if (player.isSneaking()) {
        player.openSign(sign);
    } else {
        lockGuiService.openFor(player, sign);
    }
}
```

### Summary Reuse
```java
// Source: src/main/java/ym/signLock/service/LockService.java
LockDetails details = lockService.describeLock(signBlock);
String owner = details.owner();
List<String> allowed = details.allowedPlayers();
int extensionCount = details.extensionCount();
```

### Safe GUI Click Dispatch
```java
// Source: https://hub.spigotmc.org/javadocs/spigot/org/bukkit/event/inventory/InventoryClickEvent.html
event.setCancelled(true);
plugin.getServer().getScheduler().runTask(plugin, () -> {
    guiActionService.handleClick(player, holder, event.getRawSlot());
});
```

## State of the Art

| Old Approach | Current Approach | When Changed | Impact |
|--------------|------------------|--------------|--------|
| 主人右键锁牌直接进原生 sign edit | 主人右键先看摘要 GUI，潜行右键才进 sign edit | Phase 4 scope, 2026-04-02 | 提升主交互可发现性，同时保留低频兼容回退。 |
| 通过命令和手改牌子管理授权 | chest GUI 展示摘要并触发同语义 add/remove | Phase 4 scope, 2026-04-02 | 更现代，但不改锁真源和命令兼容路径。 |
| 在 inventory click 中直接改界面 | next-tick 调度 inventory transition | Spigot 当前官方文档要求 | 降低 GUI 事件重入和物品同步问题。 |
| 用 `ChatEvent` 强制主线程聊天处理 | 如需聊天输入，优先 `AsyncChatEvent` 并检查 `isAsynchronous()` | Paper 当前 1.21.11 API | 避免把整条聊天链强行拖回主线程。 |

**Deprecated/outdated:**
- `ChatEvent`: Paper 当前 API 已标记 deprecated，并说明会强制聊天等待主线程；如果 planner 需要聊天输入，优先 `AsyncChatEvent`。
- 在 click 里直接 `openInventory` / `closeInventory`: Spigot 当前 javadocs 明确要求改为 next-tick 调度。

## Open Questions

1. **新增授权玩家的输入通道要选什么？**
   - What we know: Phase 4 需要单人 add/remove，但 repo 目前没有现成 GUI 输入桥或菜单框架。
   - What's unclear: planner 是否愿意在本阶段接受“GUI 按钮 + 一次性聊天输入”作为 add-player 子流程。
   - Recommendation: 以 guided chat input 作为 Phase 4 默认；如果用户体验要求更高，把 richer input bridge 明确延后到 Phase 5/6。

2. **是否补一个 `/bl gui` 之类的辅助入口？**
   - What we know: `04-CONTEXT.md` 允许作为可选辅助入口，但不能成为核心依赖。
   - What's unclear: 现阶段是否需要它来帮助测试或帮助玩家发现 GUI。
   - Recommendation: 计划里把它降为 stretch item；Phase 4 主入口仍应是右键锁牌。

## Environment Availability

| Dependency | Required By | Available | Version | Fallback |
|------------|------------|-----------|---------|----------|
| Java runtime | Gradle build/test | ✓ | `openjdk 25.0.2 LTS` | — |
| Gradle Wrapper | Build/test/manual validation | ✓ | `Gradle 8.8` | — |
| Standalone Gradle | Optional local convenience | ✗ | — | Use `gradlew.bat` |
| Git | Optional commit/docs workflow | ✓ | `2.52.0.windows.1` | — |

**Missing dependencies with no fallback:**
- None.

**Missing dependencies with fallback:**
- Standalone `gradle` missing; use repo wrapper `gradlew.bat`.

## Validation Architecture

### Test Framework
| Property | Value |
|----------|-------|
| Framework | JUnit Jupiter `6.0.3` + MockBukkit `4.108.0` + Mockito `5.23.0` |
| Config file | none — Gradle default JUnit Platform setup in `build.gradle` |
| Quick run command | `.\gradlew.bat test --tests ym.signLock.listener.LockListenerInteractTest` |
| Full suite command | `.\gradlew.bat test` |

### Phase Requirements → Test Map
| Req ID | Behavior | Test Type | Automated Command | File Exists? |
|--------|----------|-----------|-------------------|-------------|
| GUI-01 | owner 普通右键锁牌开 GUI；潜行右键走 `openSign` 回退；非 owner 不得开管理 GUI | unit/integration | `.\gradlew.bat test --tests ym.signLock.listener.LockListenerGuiEntryTest` | ❌ Wave 0 |
| GUI-02 | GUI 基于 canonical target 渲染 owner、授权列表、扩展牌数量和锁目标摘要 | unit/integration | `.\gradlew.bat test --tests ym.signLock.gui.LockManagementGuiViewTest` | ❌ Wave 0 |
| GUI-03 | GUI 单人 add/remove 复用 `LockService`，结果与命令一致，写回不改数据格式 | unit/integration | `.\gradlew.bat test --tests ym.signLock.gui.LockManagementGuiActionTest` | ❌ Wave 0 |

### Sampling Rate
- **Per task commit:** `.\gradlew.bat test --tests <targeted test class>`
- **Per wave merge:** `.\gradlew.bat test`
- **Phase gate:** Full suite green before `/gsd:verify-work`

### Wave 0 Gaps
- [ ] `src/test/java/ym/signLock/listener/LockListenerGuiEntryTest.java` — covers `GUI-01`
- [ ] `src/test/java/ym/signLock/gui/LockManagementGuiViewTest.java` — covers `GUI-02`
- [ ] `src/test/java/ym/signLock/gui/LockManagementGuiActionTest.java` — covers `GUI-03`
- [ ] `src/test/java/ym/signLock/gui/LockGuiListenerClickSafetyTest.java` — covers click/drag cancel + next-tick reopen safety
- [ ] If guided chat input is chosen: `src/test/java/ym/signLock/listener/LockGuiChatInputTest.java` — covers async capture + sync handoff

## Sources

### Primary (HIGH confidence)
- Local codebase: `src/main/java/ym/signLock/listener/LockListener.java`, `src/main/java/ym/signLock/service/LockService.java`, `src/main/java/ym/signLock/command/SignLockCommand.java`, `src/test/java/ym/signLock/**`, `build.gradle`
- Spigot `InventoryClickEvent` javadocs: https://hub.spigotmc.org/javadocs/spigot/org/bukkit/event/inventory/InventoryClickEvent.html
- Spigot `Player#openSign(...)` javadocs: https://hub.spigotmc.org/javadocs/spigot/org/bukkit/entity/Player.html
- Folia `Server` scheduler javadocs: https://jd.papermc.io/folia/1.21.11/org/bukkit/Server.html

### Secondary (MEDIUM confidence)
- Paper `AsyncChatEvent` javadocs: https://jd.papermc.io/paper/1.21.11/io/papermc/paper/event/player/AsyncChatEvent.html
- MC Plugin Neuron preflight and project-memory guidance for Bukkit GUI/session routing

### Tertiary (LOW confidence)
- None.

## Metadata

**Confidence breakdown:**
- Standard stack: HIGH - 主要依赖均来自当前仓库已固定版本，且无需新增 GUI 依赖。
- Architecture: MEDIUM - 入口改造与 service reuse 很明确，但 add-player 输入通道仍有一个待定实现分支。
- Pitfalls: HIGH - 关键风险由官方 javadocs 和现有 repo 边界共同支持。

**Research date:** 2026-04-02
**Valid until:** 2026-05-02
