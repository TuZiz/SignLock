# Phase 2: Authorization & Protection Matrix - Research

**Researched:** 2026-04-02
**Domain:** Bukkit/Paper/Folia sign-lock authorization, break protection, automation protection, and player identity resolution
**Confidence:** MEDIUM

<user_constraints>
## User Constraints (from CONTEXT.md)

### Locked Decisions
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

### Claude's Discretion
- 具体由 `LockService` 还是监听器/命令层暴露新的“可访问”“可管理”“可破坏”辅助方法，可由后续研究与规划决定，只要最终权限矩阵清晰且可测试。
- 玩家身份缓存的预热时机、保存时机、以及命令层如何组织错误分支，可由后续规划按现有架构收口。

### Deferred Ideas (OUT OF SCOPE)
None - discussion stayed within phase scope.
</user_constraints>

<phase_requirements>
## Phase Requirements

| ID | Description | Research Support |
|----|-------------|------------------|
| PROT-03 | 未授权玩家不能破坏受保护方块本体或别人的锁牌。 | 需要把 `canAccess` 与破坏语义解耦，新增统一 break/manage 判定，并补 `BlockBreakEvent` 回归。 |
| PROT-04 | 自动化搬运、爆炸、活塞和流体不能绕过现有锁保护破坏或操作锁目标。 | 需要统一保护矩阵入口，明确 shared target/managed sign 覆盖范围，并补 explosion/piston/fluid/automation 测试。 |
| OPS-02 | 锁主人可以通过 `/signlock add <玩家>` 和 `/signlock remove <玩家>` 管理授权名单，并在空间不足或目标无效时得到准确信息。 | 需要补命令分支一致性、扩展牌容量边界、无效瞄准目标、名字解析结果与消息断言。 |
| OPS-03 | 玩家身份缓存与名字解析在插件启停和授权操作后保持一致，不因离线玩家或大小写差异破坏授权判断。 | 需要把 `PlayerIdentityService` 行为显式测试化，并锁住 rename/cache-known/cache-unknown 场景。 |
</phase_requirements>

## Summary

当前代码已经具备 Phase 2 的两块关键基础：一是 `LockService` 已集中 shared target、`canAccess`、`canManage` 和授权名单写入；二是 `LockListener` 已把交互、开箱、搬运、爆炸、活塞、流体和破坏入口集中到同一个监听器。规划重点不是“再发明一套权限系统”，而是把现有两层语义明确扩成三层语义：`access`、`manage`、`break`，并让所有入口复用同一套判定。

现在最明显的行为缺口有两个。第一，`BlockBreakEvent` 仍复用 `canAccess`，所以被授权玩家当前也能破坏锁目标本体和受管理锁牌，这与 D-01/D-02 直接冲突。第二，命令与身份缓存链路尚未被测试锁住，`handleAdd(...)` 会先 `resolveStoredName(...)`，`handleRemove(...)` 却不会，导致 rename/cache-known/cache-unknown 的行为不完全对称。

自动化保护的总体方向应保持保守，但规划时要显式切分“已有覆盖”和“仍需补洞”的子问题。爆炸路径已经利用 `blockList()` 过滤受保护方块，活塞路径也已拦截 `getBlocks()` 返回的被移动方块；但 planner 需要决定是否把“活塞目的地碰撞导致锁牌被打掉”作为同一 plan 处理，因为当前实现只检查 moved blocks，不检查 movement destination 的副作用。

**Primary recommendation:** 以 `LockService` 为唯一权限矩阵语义源，先收口 `canBreak/canDestroyManagedArtifact` 与命令身份解析，再用单独的保护矩阵测试计划覆盖 break/explosion/piston/fluid/inventory automation。

## User Constraints

本 phase 已有明确锁定决策，不需要在 planning 阶段重新讨论“授权玩家能不能拆”“自动化是否允许细粒度白名单”“命令是否改成交互式选择”。Plan 必须围绕 D-01 到 D-07 直接落地。

## Project Constraints (from CLAUDE.md)

- 继续使用 Java plugin + Gradle Wrapper + Spigot API 1.21.11，避免引入框架迁移风险。
- 必须保持对 Spigot / Paper / Folia 的兼容声明与主线程安全。
- 不能破坏现有锁牌数据与当前牌子格式。
- 默认体验必须对中文服务器友好。
- 研究和规划应通过 GSD 工作流产物推进，不推荐绕开 `.planning/` 直接改仓库逻辑。

## Standard Stack

### Core
| Library | Version | Purpose | Why Standard |
|---------|---------|---------|--------------|
| Java | 21 target | 插件运行与构建目标 | 已由仓库 `build.gradle` 固定，避免运行时与 API 兼容漂移。 |
| Spigot API | 1.21.11-R0.1-SNAPSHOT | 主插件编译 API | 当前生产代码直接依赖，所有事件与方块类型语义以它为准。 |
| Gradle Wrapper | 8.8 | 构建与测试入口 | 当前仓库可直接执行，避免环境依赖系统级 Gradle。 |

### Supporting
| Library | Version | Purpose | When to Use |
|---------|---------|---------|-------------|
| Paper API | 1.21.11-R0.1-SNAPSHOT | 测试期补足 Paper/Bukkit API 类型 | MockBukkit/JUnit 回归测试需要。 |
| JUnit Jupiter | 6.0.3 | 单元与事件回归测试 | 新增权限矩阵与命令回归时使用。 |
| MockBukkit | 4.108.0 | Bukkit/Paper 事件与世界测试基座 | 继续做 listener/service 级验证，不要上真实服做第一轮回归。 |
| Mockito JUnit Jupiter | 5.23.0 | 玩家、命令、InventoryHolder、Event mock | 命令和边界事件很适合继续沿用。 |

### Alternatives Considered
| Instead of | Could Use | Tradeoff |
|------------|-----------|----------|
| 继续用 MockBukkit listener/service tests | 直接上手工服验证 | 手工验证适合最终 smoke，不适合锁住 Phase 2 的回归矩阵。 |
| 本地 `players.yml` 缓存 + UUID 比较 | `Bukkit.getOfflinePlayer(String)` 或外部名服查询 | 官方文档明确旧式按名字取离线玩家可能阻塞且会返回“即使不存在也给对象”，不适合作为命令真值来源。 |
| `LockService` 集中权限矩阵 | 监听器和命令各自写 if/else | 会再次把 shared target、admin bypass、rename 兼容散落到多个入口。 |

**Version verification:** 使用仓库已锁定版本并实测 `.\gradlew.bat test --tests "ym.signLock.service.*" --tests "ym.signLock.listener.*"` 通过；Gradle 8.8 当前在本机可运行，JVM 为 25.0.2，项目仍以 Java 21 `release` 编译。

## Architecture Patterns

### Recommended Project Structure
```text
src/
├── main/java/ym/signLock/service/    # shared target、权限矩阵、身份缓存
├── main/java/ym/signLock/listener/   # 事件拦截，只做入口编排
├── main/java/ym/signLock/command/    # 命令分支与玩家反馈
└── test/java/ym/signLock/            # service/listener/command/identity 回归
```

### Pattern 1: Central Authorization Matrix in `LockService`
**What:** 把 `access`、`manage`、`break` 三种语义统一收敛到服务层，监听器与命令只调用这些 helper。
**When to use:** 任何涉及玩家身份边界、admin bypass、owner-only、authorized-but-not-manage 的入口。
**Example:**
```java
// Source: repo pattern from LockService/LockListener
boolean canAccess(LockInfo lock, Player player);
boolean canManage(LockInfo lock, Player player);
boolean canBreak(LockInfo lock, Player player); // Phase 2 should add or equivalent
```

### Pattern 2: Resolve Shared Target Before Any Protection Decision
**What:** 所有保护入口先通过 canonical/shared target 归一化，再做权限判断。
**When to use:** 双箱、门上下半块、扩展牌、`InventoryHolder`、瞄准任意受管理牌子等场景。
**Example:**
```java
// Source: repo pattern from LockService
Block target = lockService.resolveInventoryBlock(holder);
LockInfo lock = lockService.findLock(target);
```

### Pattern 3: Enum-like Command Outcomes, Not Boolean Flags
**What:** 命令路径继续用结果枚举表达 `ADDED` / `ALREADY_AUTHORIZED` / `NO_SPACE` / `NOT_FOUND` / `OWNER_DENIED`。
**When to use:** `/signlock add`、`/signlock remove`、未来任何需要稳定中文提示的命令。
**Example:**
```java
// Source: repo pattern from LockService
AddPlayerResult result = lockService.addPlayerToLock(sign, lock, targetPlayer);
RemovePlayerResult result = lockService.removePlayerFromLock(sign, lock, targetPlayer);
```

### Pattern 4: Identity Matching Must Be UUID-Aware but Cache-First
**What:** 保持“名字忽略大小写 + 已缓存 UUID 时优先按 UUID 认同一玩家”的规则，不做阻塞式外部查询。
**When to use:** owner/authorized 对比、命令 add/remove、rename 兼容。
**Example:**
```java
// Source: repo pattern from LockService
if (storedName.equalsIgnoreCase(player.getName())) return true;
UUID storedUuid = playerIdentityService.findUuidByName(storedName);
return storedUuid != null && storedUuid.equals(player.getUniqueId());
```

### Anti-Patterns to Avoid
- **用 `canAccess` 代表一切权限：** 这正是当前 break 语义错误的根因，会让被授权玩家获得拆除能力。
- **在命令层直接拼名字字符串判断身份：** 会把 rename/case-insensitive 兼容再次写散。
- **把活塞/爆炸/流体每个事件都写成独立业务规则：** planner 应先定义“受保护对象集合”和“影响对象集合”，再映射到事件。
- **用阻塞式 Bukkit 名字解析补缓存：** 官方文档对 `Bukkit.getOfflinePlayer(String)` 的行为和阻塞风险都不适合作为权限链真值。

## Don't Hand-Roll

| Problem | Don't Build | Use Instead | Why |
|---------|-------------|-------------|-----|
| 双箱/门/扩展牌目标归一 | 各事件自己判断哪一半箱体或哪块牌子生效 | `LockService` canonical target + `findLock/findManagedSignLock` | Phase 1 已验证这条路线，重复实现最容易引入回退。 |
| 玩家身份持久识别 | 按展示名做永久 owner key | `PlayerIdentityService` 的 `names -> UUID` / `UUID -> lastKnownName` 缓存 | 官方 `OfflinePlayer` 文档明确名字不再适合作持久主键。 |
| 离线玩家名字解析 | `Bukkit.getOfflinePlayer(String)` 当作“存在性校验” | 仅使用本地缓存和在线加入预热 | 官方 Bukkit 文档说明它可能阻塞且即使玩家不存在也返回对象。 |
| 自动化保护 | 自定义 hopper/piston 线程或轮询 | Bukkit/Paper 现有事件：`InventoryMoveItemEvent`、`BlockExplodeEvent`、`EntityExplodeEvent`、`BlockPiston*Event`、`BlockFromToEvent` | 已有事件语义明确，可同步取消并保持 Bukkit/Folia 兼容。 |

**Key insight:** 这个 phase 的复杂度不在“写更多 if”，而在“让所有入口共用同一语义源”。只要权限矩阵与 shared target 继续分叉，任何修复都会在下一个入口复发。

## Common Pitfalls

### Pitfall 1: Authorized Players Can Still Break Locks
**What goes wrong:** 当前 `onBlockBreak(...)` 对锁目标本体和锁牌都调用 `canAccess(...)`，导致授权玩家具备不应拥有的拆除能力。
**Why it happens:** 代码目前只有 access/manage 两层，没有显式 break 语义。
**How to avoid:** Phase 2 先定义 `canBreak` 或同等 helper，再让 block/sign break 都走它。
**Warning signs:** 被授权玩家能拆箱体、拆主锁牌或扩展牌；admin/owner/authorized 三种边界在 break 上不一致。

### Pitfall 2: Piston Coverage Stops at `getBlocks()`
**What goes wrong:** 当前实现只检查 `BlockPistonExtendEvent` / `BlockPistonRetractEvent#getBlocks()` 返回的 moved blocks。
**Why it happens:** 官方 API 给的是“会被移动的方块列表”，不是“所有会受影响的位置”。
**How to avoid:** planner 需要明确是否在同一 plan 内补 destination collision/attached sign side effect 检查；至少要为此写失败测试先证明风险。
**Warning signs:** 活塞未直接推动受保护方块，却能通过推入占位、挤掉附着牌等方式破坏锁结构。

### Pitfall 3: Rename Handling Is Asymmetric Between Access and Commands
**What goes wrong:** 在线访问路径用 `player.getUniqueId()` 比对，命令 remove 路径却依赖输入名字是否已进缓存。
**Why it happens:** `handleAdd(...)` 先 `resolveStoredName(...)`，`handleRemove(...)` 没有同样的归一化步骤。
**How to avoid:** 规划里把 add/remove/info 的 identity normalization 作为同一子任务处理，并补 rename/cache-known/cache-unknown 测试。
**Warning signs:** 玩家改名后还能正常开箱，但 `/signlock remove <新名字>` 找不到旧授权记录。

### Pitfall 4: Tests Currently Prove Phase 1, Not Phase 2
**What goes wrong:** 现有测试主要覆盖 interact/open/placement/sign-edit/shared-target，没有 break/command/identity/protection matrix 的系统回归。
**Why it happens:** 仓库刚完成 Phase 1。
**How to avoid:** planner 必须把测试基座补齐放进 Wave 0 或首个实现 plan。
**Warning signs:** 修改 `LockListener.onBlockBreak`、命令或 identity service 后只有手工服验证，没有自动化断言。

## Code Examples

Verified patterns from current implementation and official APIs:

### Permission Matrix Split
```java
// Source: repo pattern
public boolean canAccess(LockInfo lock, Player player) { ... }
public boolean canManage(LockInfo lock, Player player) { ... }
// Phase 2 recommendation:
public boolean canBreak(LockInfo lock, Player player) {
    return canManage(lock, player);
}
```

### Explosion Filtering
```java
// Source: official Bukkit docs + current repo pattern
@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
public void onBlockExplode(BlockExplodeEvent event) {
    event.blockList().removeIf(lockService::isExplosionProtected);
}
```

### Hopper / Direct Automation Blocking
```java
// Source: Paper/Bukkit InventoryMoveItemEvent docs + current repo pattern
Block source = lockService.resolveInventoryBlock(event.getSource().getHolder());
Block destination = lockService.resolveInventoryBlock(event.getDestination().getHolder());
if ((source != null && lockService.isProtectedAutomationTarget(source))
        || (destination != null && lockService.isProtectedAutomationTarget(destination))) {
    event.setCancelled(true);
}
```

### UUID-Aware Identity Matching
```java
// Source: repo pattern from LockService
private boolean sameIdentity(String firstName, String secondName) {
    if (firstName.equalsIgnoreCase(secondName)) {
        return true;
    }
    UUID firstUuid = playerIdentityService.findUuidByName(firstName);
    UUID secondUuid = playerIdentityService.findUuidByName(secondName);
    return firstUuid != null && firstUuid.equals(secondUuid);
}
```

## State of the Art

| Old Approach | Current Approach | When Changed | Impact |
|--------------|------------------|--------------|--------|
| 名字文本即永久身份 | UUID-aware matching with cache-backed names | Bukkit 文档早已不建议把名字当持久主键；当前仓库已部分采用 | Phase 2 应继续沿用 UUID-first，而不是退回纯名字逻辑。 |
| 每个入口各自判断箱体半边/牌子位置 | `LockService` canonical target + shared target reuse | Phase 1 on 2026-04-02 | Phase 2 所有 break/automation/command 规划都应建立在这套 shared target 之上。 |
| 只做交互/开箱测试 | 需要补完整保护矩阵与命令/身份测试 | 当前 phase | 否则 break/rename/automation 修复没有回归护栏。 |

**Deprecated/outdated:**
- `Bukkit.getOfflinePlayer(String)` 作为命令存在性或 UUID 来源：官方文档标注 deprecated，且可能阻塞、即使玩家不存在也返回对象，不适合权限判断。
- 纯 name-based 持久 owner 设计：官方 `OfflinePlayer#getName()` 文档明确名字不再适合作持久存储主键。

## Open Questions

1. **`break` 语义是否单独公开为 helper，还是明确等价于 `canManage`？**
   - What we know: D-01/D-02 已锁定 authorized 不能拆，owner/admin 可以拆。
   - What's unclear: 是否需要专门 `canBreak(...)` 命名，还是由 `canManage(...)` 语义承载即可。
   - Recommendation: 规划层优先把它单独命名，这样测试和 listener 意图更清晰。

2. **活塞保护是否要在本 phase 一次补到“目的地副作用”级别？**
   - What we know: 当前代码只检查 moved blocks；官方 API 也只直接提供 moved blocks 列表。
   - What's unclear: 当前插件支持的锁目标中，是否存在未移动保护块但仍能通过活塞副作用破坏锁牌的稳定复现案例。
   - Recommendation: 先写失败测试验证；若能复现，单独切成一个 protection-matrix 子计划，不要和 break 语义混写。

3. **缓存未知时是否要继续允许 add/remove 纯名字文本？**
   - What we know: D-06 已允许 cache-unknown 时按文本执行，后续识别到 UUID 再收敛。
   - What's unclear: remove/info 场景如何表达“文本不存在于当前锁内，但未来可能与某 UUID 收敛”。
   - Recommendation: 维持当前 best-effort，不新增外部查询；只把消息与测试边界写清楚。

## Environment Availability

| Dependency | Required By | Available | Version | Fallback |
|------------|------------|-----------|---------|----------|
| Java runtime | Gradle build/test | ✓ | JVM 25.0.2 (Gradle reports) | Java 21 toolchain/release already configured in `build.gradle` |
| Gradle Wrapper | Build/test commands | ✓ | 8.8 | — |
| Git | GSD doc workflow | ✓ | 2.52.0.windows.1 | — |

**Missing dependencies with no fallback:**
- None for planning and local test execution.

**Missing dependencies with fallback:**
- None identified.

## Validation Architecture

### Test Framework
| Property | Value |
|----------|-------|
| Framework | JUnit Jupiter 6.0.3 + MockBukkit 4.108.0 + Mockito 5.23.0 |
| Config file | [build.gradle](D:/codex/SignLock/build.gradle) |
| Quick run command | `.\gradlew.bat test --tests "ym.signLock.service.*" --tests "ym.signLock.listener.*"` |
| Full suite command | `.\gradlew.bat test` |

### Phase Requirements → Test Map
| Req ID | Behavior | Test Type | Automated Command | File Exists? |
|--------|----------|-----------|-------------------|-------------|
| PROT-03 | unauthorized cannot break protected block body or managed sign; owner/admin can | MockBukkit listener test | `.\gradlew.bat test --tests "ym.signLock.listener.LockListenerBreakProtectionTest"` | ❌ Wave 0 |
| PROT-04 | automation/explosion/piston/fluid cannot bypass lock semantics across shared targets and managed signs | MockBukkit listener test | `.\gradlew.bat test --tests "ym.signLock.listener.LockListenerProtectionMatrixTest"` | ❌ Wave 0 |
| OPS-02 | `/signlock add/remove` returns correct outcomes for invalid target, no space, not found, rename/case inputs | command/service test | `.\gradlew.bat test --tests "ym.signLock.command.SignLockCommandAuthorizationTest"` | ❌ Wave 0 |
| OPS-03 | identity cache persists and matches case-insensitive / UUID-aware rename scenarios | service test | `.\gradlew.bat test --tests "ym.signLock.service.PlayerIdentityServiceTest"` | ❌ Wave 0 |

### Sampling Rate
- **Per task commit:** `.\gradlew.bat test --tests "ym.signLock.service.*" --tests "ym.signLock.listener.*"`
- **Per wave merge:** `.\gradlew.bat test`
- **Phase gate:** Full suite green before `/gsd:verify-work`

### Wave 0 Gaps
- [ ] `src/test/java/ym/signLock/listener/LockListenerBreakProtectionTest.java` — covers PROT-03
- [ ] `src/test/java/ym/signLock/listener/LockListenerProtectionMatrixTest.java` — covers PROT-04
- [ ] `src/test/java/ym/signLock/command/SignLockCommandAuthorizationTest.java` — covers OPS-02
- [ ] `src/test/java/ym/signLock/service/PlayerIdentityServiceTest.java` — covers OPS-03
- [ ] Command-side helper/mocks for `player.getTargetBlockExact(6)` targeting managed signs

## Sources

### Primary (HIGH confidence)
- Local repo: `src/main/java/ym/signLock/service/LockService.java` — current permission, target, and identity comparison logic.
- Local repo: `src/main/java/ym/signLock/listener/LockListener.java` — current protection matrix entry points.
- Local repo: `src/main/java/ym/signLock/command/SignLockCommand.java` — current add/remove/info command flow.
- Local repo: `src/main/java/ym/signLock/service/PlayerIdentityService.java` — current `players.yml` cache model.
- Local repo: `src/main/java/ym/signLock/SignLock.java` and `src/main/java/ym/signLock/listener/PlayerIdentityListener.java` — preload/save timing.
- Local repo: `build.gradle` — test/build stack and versions.
- Official Spigot docs: https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/event/block/BlockExplodeEvent.html — `blockList()` semantics for explosion filtering.
- Official Spigot docs: https://hub.spigotmc.org/javadocs/spigot/org/bukkit/event/entity/EntityExplodeEvent.html — entity explosion affected-block list semantics.
- Official Spigot docs: https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/event/block/BlockPistonExtendEvent.html — `getBlocks()` is the moved-block list.
- Official Spigot docs: https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/event/block/BlockFromToEvent.html — cancellable source/destination flow semantics.
- Official Spigot docs: https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/OfflinePlayer.html — names are not suitable persistent identity keys; use UUID.
- Official Bukkit docs: https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/Bukkit.html — `getOfflinePlayer(String)` deprecation and blocking/nonexistent-player caveat.
- Official Paper docs: https://jd.papermc.io/paper/1.21.3/org/bukkit/event/inventory/InventoryMoveItemEvent.html — cancellation/source/destination semantics for inventory automation.

### Secondary (MEDIUM confidence)
- `.planning/phases/02-authorization-protection-matrix/02-CONTEXT.md` — locked phase decisions and scope.
- `.planning/phases/01-lock-target-correctness/01-VERIFICATION.md` — verified baseline from Phase 1.

### Tertiary (LOW confidence)
- None.

## Metadata

**Confidence breakdown:**
- Standard stack: HIGH - repo-pinned versions and local test execution confirm the working toolchain.
- Architecture: HIGH - conclusions are grounded in current source structure and Phase 1 verified patterns.
- Pitfalls: MEDIUM - break/rename issues are directly visible in code, but piston side-effect risk still needs a reproducer test to prove exact scope.

**Research date:** 2026-04-02
**Valid until:** 2026-05-02
