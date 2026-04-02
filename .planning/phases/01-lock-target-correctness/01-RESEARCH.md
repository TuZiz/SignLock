# Phase 01: lock-target-correctness - Research

**Researched:** 2026-04-02
**Domain:** Bukkit / Spigot sign-lock target normalization and access enforcement
**Confidence:** HIGH

<user_constraints>
## User Constraints (from CONTEXT.md)

### Locked Decisions
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

### Claude's Discretion
- 双箱共享目标的具体归一化算法可以由后续研究/规划决定，只要最终满足“同一把锁语义”。
- 访问判定与锁牌扫描的内部复用方式、辅助方法拆分和最小测试形式由后续阶段自行决定。

### Deferred Ideas (OUT OF SCOPE)
None - discussion stayed within phase scope.
</user_constraints>

<phase_requirements>
## Phase Requirements

| ID | Description | Research Support |
|----|-------------|------------------|
| LOCK-01 | 玩家可以在支持的单箱、双箱、木桶和潜影盒上创建主锁牌，且主锁牌绑定到正确的受保护目标。 | 统一 `findPlacementTarget` / `resolveDirectLockTarget` / `resolveInventoryBlock` 到同一个 canonical target 规则。 |
| LOCK-02 | 玩家可以在已有主锁的目标上继续添加扩展授权牌，且只有锁主人或管理员绕过用户可以执行该操作。 | 扩展牌判定必须基于共享目标扫描，且复用 `canManage`。 |
| LOCK-03 | 双箱的两个半边必须被视为同一把锁的共享目标，任一半边上的锁牌与授权都对整个双箱生效。 | 必须引入稳定的双箱归一化算法和“单主锁语义”。 |
| LOCK-04 | 锁牌编辑时必须保留系统控制的结构行，避免主人名、锁头和扩展牌格式被误改坏。 | 保留 `onSignChange` 的结构行覆盖逻辑，并补回归测试。 |
| PROT-01 | 未授权玩家尝试右键使用已上锁方块时会被稳定拦截并收到正确提示。 | 右键入口只能依赖统一 `findLock`，不能使用点击半边的局部语义。 |
| PROT-02 | 未授权玩家尝试打开已上锁容器时会被稳定拦截，包括双箱和其他 `InventoryHolder` 路径。 | `InventoryOpenEvent` 与自动化入口必须先解析 holder 对应的共享目标，再做访问判定。 |
</phase_requirements>

## Summary

现有实现已经把大部分锁语义集中在 [`LockService`](D:\codex\SignLock\src\main\java\ym\signLock\service\LockService.java) 中，这是 Phase 01 最稳的修复点。问题不在“有没有扫描双箱”，而在“有没有把所有入口都先归一到同一个 canonical target 再扫描”。当前实现会把双箱的两个半边都视为 related blocks，但 `normalizeProtectedBlock` 只处理门上下半，不会为双箱选出稳定主目标；于是主锁查找、扩展牌创建、容器打开和命令授权虽然都调用 `findLock(...)`，却仍然可能围绕不同半边做局部判断。

官方 Spigot API 当前同时提供 `Chest.Type` 用于识别 `SINGLE/LEFT/RIGHT`，以及 `DoubleChest` 作为容器聚合 holder。研究结论是：Phase 01 不应把 `DoubleChest#getLocation()` 或“玩家点了哪一半”当作锁语义本身，而应在服务层显式定义一个共享目标解析器。推荐做法是先解析“锁语义目标集”（双箱两个半边、门上下半），再从该目标集中选出一个稳定 canonical block，并让 `findPlacementTarget`、`resolveDirectLockTarget`、`resolveInventoryBlock`、`findManagedSignLock`、`collectManagedSigns` 全部复用它。

当前仓库没有自动化测试，`./gradlew.bat test` 可执行但没有任何 `src/test` 覆盖；因此规划时必须把 Wave 0 的最小回归测试骨架写进本阶段，否则很容易出现“修了开箱，回退了贴牌/命令”的局部修复。

**Primary recommendation:** 以 `LockService` 为唯一锁目标语义源，新增稳定的 canonical target 解析，并用 MockBukkit 回归覆盖“单箱/双箱/扩展牌/右键/开箱/命令”六条入口。

## User Constraints

按 CONTEXT.md 执行，不扩权限系统、不改数据真源、不引入数据库或 GUI；只修复目标归一和入口一致性。

## Project Constraints (from CLAUDE.md)

- 通过 GSD 工作流推进，不要绕开规划产物直接改仓库。
- 小修优先 `gsd-quick`，问题调查优先 `gsd-debug`，阶段实现优先 `gsd-execute-phase`。
- 必须保持 Spigot / Paper / Folia 兼容声明与主线程安全。
- 不得破坏已有牌子锁数据或当前牌子格式。
- 中文可读默认值和稳定锁语义是 release-blocking 质量线。

## Standard Stack

### Core
| Library / Tool | Version | Purpose | Why Standard |
|---------|---------|---------|--------------|
| Java | 21 target | 插件源码与字节码目标 | 仓库已固定 `options.release = 21`，避免额外迁移风险。 |
| Gradle Wrapper | 8.8 | 构建、测试、运行 Paper 开发服 | 仓库现有 wrapper 已可运行。 |
| Spigot API | 1.21.11-R0.1-SNAPSHOT | Bukkit/Spigot 事件与方块 API | 当前代码已基于该 API 编译，Phase 01 不需要换平台。 |
| `xyz.jpenilla.run-paper` | 2.3.1 | 本地 Paper 运行任务 | 仓库已配置 `runServer`，适合作为手工回归基线。 |

### Supporting
| Library | Version | Purpose | When to Use |
|---------|---------|---------|-------------|
| MockBukkit | 4.108.0 | 插件行为回归测试 | 用于单箱/双箱/牌子/容器事件的自动化回归。 |
| JUnit Jupiter | 6.0.3 | 测试框架 | 用于 `LockService` 和监听器行为断言。 |
| Mockito JUnit Jupiter | 5.23.0 | 协作对象替身 | 用于 `PlayerIdentityService`、权限与消息路径隔离。 |

### Alternatives Considered
| Instead of | Could Use | Tradeoff |
|------------|-----------|----------|
| 继续使用 Spigot API + 现有 Java 插件结构 | Paper-only API | Paper 专属 API 会增加兼容成本，不符合当前阶段约束。 |
| MockBukkit | 纯手工服回归 | 手工清单已存在，但不足以稳定覆盖多入口回归。 |
| 服务层集中归一 | 在监听器里分别修补 | 短期快，长期一定分叉。 |

**Installation:**
```gradle
dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter:6.0.3")
    testImplementation("org.mockbukkit.mockbukkit:mockbukkit-v1.21:4.108.0")
    testImplementation("org.mockito:mockito-junit-jupiter:5.23.0")
}
```

**Version verification:**
- Java target and Spigot dependency are pinned in [`build.gradle`](D:\codex\SignLock\build.gradle#L17).
- Local wrapper reports Gradle `8.8`.
- Maven Central metadata on 2026-04-02 reports `mockbukkit-v1.21` latest `4.108.0`, `mockito-junit-jupiter` latest `5.23.0`, and `junit-jupiter` latest release channel includes milestone `6.1.0-M1`; recommendation here uses latest stable non-milestone `6.0.3`.

## Architecture Patterns

### Recommended Project Structure
```text
src/
├── main/java/ym/signLock/service/    # canonical target parsing, lock scanning, access checks
├── main/java/ym/signLock/listener/   # thin event adapters
├── main/java/ym/signLock/command/    # command entry points delegating to LockService
└── test/java/ym/signLock/            # LockService + listener regression tests
```

### Pattern 1: Canonical Target Resolver
**What:** 为“被保护目标”定义单一解析入口，输出稳定 canonical block 和 related blocks 集合。  
**When to use:** 所有需要从方块、牌子、容器 holder、命令目标反推锁的地方。  
**Example:**
```java
record ProtectedTarget(Block canonicalBlock, List<Block> relatedBlocks) {}

ProtectedTarget resolveProtectedTarget(Block blockOrHalf) {
    Block normalized = normalizeBaseBlock(blockOrHalf);
    if (normalized == null) return null;

    List<Block> related = collectRelatedBlocks(normalized);
    Block canonical = pickCanonicalBlock(related);
    return new ProtectedTarget(canonical, related);
}
```
// Source: current code structure in LockService + Spigot Chest/DoubleChest docs

### Pattern 2: Service-First Enforcement
**What:** 监听器和命令只负责取事件上下文，真正的锁判断只走 `LockService`。  
**When to use:** `SignChangeEvent`、`PlayerInteractEvent`、`InventoryOpenEvent`、`/bl add|remove|info`。  
**Example:**
```java
LockInfo lock = lockService.findLock(target);
if (lock != null && !lockService.canAccess(lock, player)) {
    event.setCancelled(true);
    player.sendMessage(config.lockedContainerMessage());
}
```
// Source: current listener pattern in LockListener

### Pattern 3: One Primary Lock Per Shared Target
**What:** 双箱 related blocks 上只允许一把有效主锁；扩展牌和授权读取要跨整个 shared target 聚合。  
**When to use:** 主锁创建、扩展牌放置、授权读写。  
**Example:**
```java
boolean hasPrimary = findPrimaryLock(sharedTarget.canonicalBlock()) != null;
if (hasPrimary) {
    // owner/admin may add extension, nobody creates a second primary
}
```

### Pattern 4: Structure-Preserving Sign Editing
**What:** 允许主人编辑名单行，但在 `SignChangeEvent` 中强制恢复 header / owner。  
**When to use:** 主锁牌或 `[更多用户]` 牌的直接编辑。  
**Example:**
```java
if (lock.type() == LockType.PRIMARY) {
    event.setLine(0, config.lockHeader());
    event.setLine(1, lock.owner());
} else {
    event.setLine(0, config.moreUsersHeader());
}
```
// Source: current `preserveManagedSignStructure(...)`

### Anti-Patterns to Avoid
- **Clicked-half semantics:** 不要把“玩家点了双箱哪一半”直接当作锁 identity。
- **Listener-local fixes:** 不要在 `onPlayerInteract`、`onInventoryOpen`、命令里各写一套 related-block 逻辑。
- **Hard sign count by chest size:** 用户已否定“单箱 1 / 双箱 2”硬限制。
- **Primary lock from arbitrary sign:** 不要从任意 `[更多用户]` 牌反推 owner 并覆盖主锁语义。

## Don't Hand-Roll

| Problem | Don't Build | Use Instead | Why |
|---------|-------------|-------------|-----|
| 双箱识别 | 自己用坐标猜左右半边 | `Chest` block data 的 `getType()` + related block collection | API 已区分 `SINGLE/LEFT/RIGHT`，自行猜测更脆弱。 |
| 容器入口识别 | 把 `InventoryHolder` 强转成若干具体类散落在监听器里 | `resolveInventoryBlock(...)` + 统一 target resolver | 事件入口多，散落处理必然不一致。 |
| 玩家身份匹配 | 只按字符串大小写比较 | 复用 `PlayerIdentityService` UUID/name 双路匹配 | 已有离线玩家与改名兼容逻辑。 |
| 回归验证 | 只靠手工开服点点点 | MockBukkit + JUnit | Phase 01 的核心风险就是多入口回归。 |

**Key insight:** 这个阶段真正复杂的不是“扫描到另一半箱子”，而是“让所有入口共享同一个锁 identity”。一旦 identity 没有单一来源，后续每个 bug 都会反复出现。

## Common Pitfalls

### Pitfall 1: 用 related-blocks 代替 canonical target
**What goes wrong:** 代码能“找到双箱另一半”，但 `LockInfo.targetBlock()` 仍然取决于输入半边。  
**Why it happens:** 当前 `findPrimaryLock(...)` 返回的是调用方传入的 `normalizedTarget`，而不是稳定 canonical block。  
**How to avoid:** 先解析 `ProtectedTarget`，再让 `LockInfo` 持有 canonical block。  
**Warning signs:** `/bl add`、开箱、贴扩展牌在双箱不同半边上结果不一致。

### Pitfall 2: 把 `DoubleChest#getLocation()` 当作锁持久化 key
**What goes wrong:** 聚合位置适合事件入口映射，不适合定义锁 identity。  
**Why it happens:** `DoubleChest` 是 holder 聚合对象，不是实际世界中的单个方块。  
**How to avoid:** holder 只用于“找到一个属于此共享容器的真实 block”，随后立即走 canonical resolver。  
**Warning signs:** 容器能拦截，但命令或牌子扫描找不到同一把锁。

### Pitfall 3: 牌子附着解析只靠兜底邻接扫描
**What goes wrong:** 某些边缘朝向下会把牌子错误关联到相邻可锁方块。  
**Why it happens:** `resolveAttachedBlock(...)` 在不是 `Directional` 时回退到六面邻接探测。  
**How to avoid:** 先信任 `Directional` / wall sign facing；只有确认需要支持非常规牌子数据时再保留兜底。  
**Warning signs:** 锁牌贴在双箱/门附近时偶发绑定错目标。

### Pitfall 4: 只测右键，不测命令和开箱
**What goes wrong:** 玩家点击没问题，但 `/bl add` 或 `InventoryOpenEvent` 仍然按旧语义工作。  
**Why it happens:** 当前所有入口都调用 `LockService`，但调用前的目标解析并不完全一致。  
**How to avoid:** 以 requirement 为单位做入口矩阵测试。  
**Warning signs:** 单点修复后，另一条入口回归。

## Code Examples

Verified patterns from official sources and current code:

### Chest Half Detection
```java
BlockData data = block.getBlockData();
if (data instanceof Chest chest && chest.getType() != Chest.Type.SINGLE) {
    // this block participates in a double chest
}
```
// Source: https://hub.spigotmc.org/javadocs/spigot/org/bukkit/block/data/type/Chest.html

### Inventory Holder Branching
```java
if (holder instanceof Container container) {
    return container.getBlock();
}
if (holder instanceof DoubleChest doubleChest) {
    return doubleChest.getLocation().getBlock();
}
```
// Source: current `resolveInventoryBlock(...)` plus official Container/DoubleChest docs

### Preserve Managed Sign Structure
```java
private void preserveManagedSignStructure(SignChangeEvent event, LockInfo lock) {
    if (lock.type() == LockType.PRIMARY) {
        event.setLine(0, config.lockHeader());
        event.setLine(1, lock.owner());
    } else {
        event.setLine(0, config.moreUsersHeader());
    }
}
```
// Source: current `LockListener`

## State of the Art

| Old Approach | Current Approach | When Changed | Impact |
|--------------|------------------|--------------|--------|
| 按点击方块或输入半边做局部判断 | 先解析共享目标，再做锁扫描 | 应在 Phase 01 引入 | 双箱所有入口才能一致。 |
| 纯手工服回归 | MockBukkit + targeted manual smoke | 近年 Bukkit 插件常态 | 更适合多入口行为回归。 |
| 监听器各自处理目标差异 | 服务层统一 canonical target | 当前项目已具雏形 | 便于后续 Phase 2/3 继续复用。 |

**Deprecated/outdated:**
- “单箱只能一个牌子 / 双箱只能两个牌子”的硬编码限制：不符合当前用户决策。
- 在每个入口独立写“双箱特殊处理”：短期可工作，长期一定回归。

## Open Questions

1. **双箱 canonical block 具体怎么选？**
   - What we know: 需要稳定、可重复，且不能依赖玩家点击的是哪一半。
   - What's unclear: 是按坐标最小块、按 `Chest.Type.LEFT/RIGHT`、还是按 facing + side 归一。
   - Recommendation: 规划阶段明确一个可测试的纯函数规则，并让所有入口复用。

2. **`resolveAttachedBlock(...)` 是否要缩减兜底邻接扫描？**
   - What we know: 当前代码先看 `Directional`，再六面兜底；这在双箱附近有误绑风险。
   - What's unclear: 是否存在项目必须保留的非 `Directional` 牌子场景。
   - Recommendation: 先保留现状，但为错误附着加回归测试；若测试暴露误绑，再在实现计划里收窄兜底规则。

## Environment Availability

| Dependency | Required By | Available | Version | Fallback |
|------------|------------|-----------|---------|----------|
| Java | Gradle build / tests | ✓ | 25.0.2 runtime, target 21 | Gradle toolchain can target 21 if needed |
| Gradle Wrapper | Build / test / local run | ✓ | 8.8 | — |
| Network access to Maven / Spigot repo | Dependency resolution | ✓ | — | 无，本地首次解析依赖需要网络 |
| Paper local run task | Manual smoke | ✓ | run-paper plugin 2.3.1 configured | 也可用外部测试服手工验证 |

**Missing dependencies with no fallback:**
- None found.

**Missing dependencies with fallback:**
- None found.

## Validation Architecture

### Test Framework
| Property | Value |
|----------|-------|
| Framework | None currently installed in repo; recommended JUnit Jupiter 6.0.3 + MockBukkit 4.108.0 + Mockito 5.23.0 |
| Config file | none — see Wave 0 |
| Quick run command | `./gradlew.bat test --tests "ym.signLock.*"` |
| Full suite command | `./gradlew.bat test build` |

### Phase Requirements → Test Map
| Req ID | Behavior | Test Type | Automated Command | File Exists? |
|--------|----------|-----------|-------------------|-------------|
| LOCK-01 | 主锁牌创建绑定到正确共享目标 | integration | `./gradlew.bat test --tests "ym.signLock.listener.LockListenerPlacementTest"` | ❌ Wave 0 |
| LOCK-02 | 扩展牌只能加到已有主锁且对共享目标生效 | integration | `./gradlew.bat test --tests "ym.signLock.service.LockServiceExtensionTest"` | ❌ Wave 0 |
| LOCK-03 | 双箱任一半边共享同一锁 identity | unit | `./gradlew.bat test --tests "ym.signLock.service.LockServiceCanonicalTargetTest"` | ❌ Wave 0 |
| LOCK-04 | 编辑锁牌保留 header / owner | integration | `./gradlew.bat test --tests "ym.signLock.listener.LockListenerSignEditTest"` | ❌ Wave 0 |
| PROT-01 | 未授权右键使用被拦截 | integration | `./gradlew.bat test --tests "ym.signLock.listener.LockListenerInteractTest"` | ❌ Wave 0 |
| PROT-02 | 未授权开箱与 holder 路径被拦截 | integration | `./gradlew.bat test --tests "ym.signLock.listener.LockListenerInventoryOpenTest"` | ❌ Wave 0 |

### Sampling Rate
- **Per task commit:** `./gradlew.bat test --tests "ym.signLock.*"`
- **Per wave merge:** `./gradlew.bat test build`
- **Phase gate:** Full suite green plus `TESTING.md` 中 Phase 1 相关手工步骤复核

### Wave 0 Gaps
- [ ] `src/test/java/ym/signLock/service/LockServiceCanonicalTargetTest.java` — covers LOCK-01 / LOCK-03
- [ ] `src/test/java/ym/signLock/service/LockServiceExtensionTest.java` — covers LOCK-02
- [ ] `src/test/java/ym/signLock/listener/LockListenerPlacementTest.java` — covers LOCK-01
- [ ] `src/test/java/ym/signLock/listener/LockListenerInteractTest.java` — covers PROT-01
- [ ] `src/test/java/ym/signLock/listener/LockListenerInventoryOpenTest.java` — covers PROT-02
- [ ] `src/test/java/ym/signLock/listener/LockListenerSignEditTest.java` — covers LOCK-04
- [ ] `build.gradle` test dependencies and `useJUnitPlatform()` — framework bootstrap

## Sources

### Primary (HIGH confidence)
- Local codebase: [`LockService.java`](D:\codex\SignLock\src\main\java\ym\signLock\service\LockService.java), [`LockListener.java`](D:\codex\SignLock\src\main\java\ym\signLock\listener\LockListener.java), [`SignLockCommand.java`](D:\codex\SignLock\src\main\java\ym\signLock\command\SignLockCommand.java)
- Local build config: [`build.gradle`](D:\codex\SignLock\build.gradle)
- Local planning context: [`01-CONTEXT.md`](D:\codex\SignLock\.planning\phases\01-lock-target-correctness\01-CONTEXT.md), [`REQUIREMENTS.md`](D:\codex\SignLock\.planning\REQUIREMENTS.md), [`ROADMAP.md`](D:\codex\SignLock\.planning\ROADMAP.md), [`STATE.md`](D:\codex\SignLock\.planning\STATE.md)
- Spigot Javadocs: `Chest`, `Chest.Type`, `DoubleChest`, `Container`, `InventoryOpenEvent`, `Player#openSign`
- Maven Central metadata: `org.junit.jupiter:junit-jupiter`, `org.mockbukkit.mockbukkit:mockbukkit-v1.21`, `org.mockito:mockito-junit-jupiter`

### Secondary (MEDIUM confidence)
- MC Plugin Neuron preflight / routing output: useful for reinforcing “service-first” and multi-entry consistency, but not used as the source of truth for Bukkit API claims.

### Tertiary (LOW confidence)
- None.

## Metadata

**Confidence breakdown:**
- Standard stack: HIGH - existing repo pins core runtime; supporting test stack versions were verified against Maven Central metadata.
- Architecture: HIGH - conclusions come directly from current code structure plus official Spigot API contracts.
- Pitfalls: HIGH - all listed pitfalls map to current implementation seams and Phase 01 requirements.

**Research date:** 2026-04-02
**Valid until:** 2026-05-02
