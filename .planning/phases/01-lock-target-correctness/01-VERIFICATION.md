---
phase: 01-lock-target-correctness
verified: 2026-04-02T12:52:01+08:00
status: passed
score: 4/4 must-haves verified
---

# Phase 01: Lock Target Correctness Verification Report

**Phase Goal:** 让主锁牌、扩展牌、单双箱与容器访问在所有入口都指向一致的受保护目标。  
**Verified:** 2026-04-02T12:52:01+08:00  
**Status:** passed  
**Re-verification:** No — initial verification

## Goal Achievement

### Observable Truths

| # | Truth | Status | Evidence |
| --- | --- | --- | --- |
| 1 | 玩家给单箱或双箱贴主锁牌后，访问判定对整个目标一致生效。 | ✓ VERIFIED | `LockService` 将 `findPlacementTarget` / `resolveDirectLockTarget` / `findLock` 全部收口到 `canonicalTarget` 与 `resolveProtectedTarget` ([src/main/java/ym/signLock/service/LockService.java:57](D:/codex/SignLock/src/main/java/ym/signLock/service/LockService.java), [src/main/java/ym/signLock/service/LockService.java:73](D:/codex/SignLock/src/main/java/ym/signLock/service/LockService.java), [src/main/java/ym/signLock/service/LockService.java:365](D:/codex/SignLock/src/main/java/ym/signLock/service/LockService.java)); `LockServiceCanonicalTargetTest` 覆盖双箱、木桶、潜影盒 ([src/test/java/ym/signLock/service/LockServiceCanonicalTargetTest.java:42](D:/codex/SignLock/src/test/java/ym/signLock/service/LockServiceCanonicalTargetTest.java), [src/test/java/ym/signLock/service/LockServiceCanonicalTargetTest.java:67](D:/codex/SignLock/src/test/java/ym/signLock/service/LockServiceCanonicalTargetTest.java), [src/test/java/ym/signLock/service/LockServiceCanonicalTargetTest.java:85](D:/codex/SignLock/src/test/java/ym/signLock/service/LockServiceCanonicalTargetTest.java)); `LockListenerPlacementTest` 覆盖主锁贴牌 ([src/test/java/ym/signLock/listener/LockListenerPlacementTest.java:60](D:/codex/SignLock/src/test/java/ym/signLock/listener/LockListenerPlacementTest.java)). |
| 2 | 双箱任一半边被未授权玩家点击或打开时，都会被正确拦截。 | ✓ VERIFIED | `LockListener.onPlayerInteract` 与 `onInventoryOpen` 都复用 `lockService.findLock` / `resolveInventoryBlock` / `canAccess` ([src/main/java/ym/signLock/listener/LockListener.java:78](D:/codex/SignLock/src/main/java/ym/signLock/listener/LockListener.java), [src/main/java/ym/signLock/listener/LockListener.java:107](D:/codex/SignLock/src/main/java/ym/signLock/listener/LockListener.java), [src/main/java/ym/signLock/listener/LockListener.java:117](D:/codex/SignLock/src/main/java/ym/signLock/listener/LockListener.java)); `LockListenerInteractTest` 与 `LockListenerInventoryOpenTest` 分别覆盖右键与开箱路径 ([src/test/java/ym/signLock/listener/LockListenerInteractTest.java:58](D:/codex/SignLock/src/test/java/ym/signLock/listener/LockListenerInteractTest.java), [src/test/java/ym/signLock/listener/LockListenerInventoryOpenTest.java:79](D:/codex/SignLock/src/test/java/ym/signLock/listener/LockListenerInventoryOpenTest.java)). |
| 3 | 主人可以继续通过扩展牌增加授权，不会因为箱体半边或牌子位置造成误判。 | ✓ VERIFIED | `LockService.addPlayerToLock`、`canCreateMoreUsersSign`、`collectManagedSigns` 全部基于 `lock.targetBlock()` 与 canonical target ([src/main/java/ym/signLock/service/LockService.java:164](D:/codex/SignLock/src/main/java/ym/signLock/service/LockService.java), [src/main/java/ym/signLock/service/LockService.java:254](D:/codex/SignLock/src/main/java/ym/signLock/service/LockService.java), [src/main/java/ym/signLock/service/LockService.java:455](D:/codex/SignLock/src/main/java/ym/signLock/service/LockService.java)); `LockServiceExtensionTest` 与 `LockListenerPlacementTest` 覆盖扩展牌授权与容量逻辑 ([src/test/java/ym/signLock/service/LockServiceExtensionTest.java:62](D:/codex/SignLock/src/test/java/ym/signLock/service/LockServiceExtensionTest.java), [src/test/java/ym/signLock/listener/LockListenerPlacementTest.java:86](D:/codex/SignLock/src/test/java/ym/signLock/listener/LockListenerPlacementTest.java), [src/test/java/ym/signLock/listener/LockListenerPlacementTest.java:117](D:/codex/SignLock/src/test/java/ym/signLock/listener/LockListenerPlacementTest.java)). |
| 4 | 锁牌被主人编辑时，系统控制的头部和所有者信息不会损坏。 | ✓ VERIFIED | `LockListener.onSignChange` 先识别 managed sign，再走 `preserveManagedSignStructure` ([src/main/java/ym/signLock/listener/LockListener.java:49](D:/codex/SignLock/src/main/java/ym/signLock/listener/LockListener.java), [src/main/java/ym/signLock/listener/LockListener.java:327](D:/codex/SignLock/src/main/java/ym/signLock/listener/LockListener.java)); `LockListenerSignEditTest` 覆盖主人编辑主锁牌/扩展牌与非主人拒绝 ([src/test/java/ym/signLock/listener/LockListenerSignEditTest.java:57](D:/codex/SignLock/src/test/java/ym/signLock/listener/LockListenerSignEditTest.java), [src/test/java/ym/signLock/listener/LockListenerSignEditTest.java:76](D:/codex/SignLock/src/test/java/ym/signLock/listener/LockListenerSignEditTest.java), [src/test/java/ym/signLock/listener/LockListenerSignEditTest.java:95](D:/codex/SignLock/src/test/java/ym/signLock/listener/LockListenerSignEditTest.java)). |

**Score:** 4/4 truths verified

### Required Artifacts

| Artifact | Expected | Status | Details |
| --- | --- | --- | --- |
| `build.gradle` | JUnit/MockBukkit/Mockito test bootstrap | ✓ VERIFIED | Declares `junit-jupiter`, `mockbukkit-v1.21`, `mockito-junit-jupiter`, and `useJUnitPlatform()` ([build.gradle:21](D:/codex/SignLock/build.gradle), [build.gradle:51](D:/codex/SignLock/build.gradle)). |
| `src/main/java/ym/signLock/service/LockService.java` | canonical target resolution and shared-target lock scanning | ✓ VERIFIED | Centralizes canonical target, related block collection, lock lookup, extension sign scan, inventory target resolution, and `LockInfo.targetBlock` canonicalization. |
| `src/test/java/ym/signLock/service/LockServiceCanonicalTargetTest.java` | LOCK-01 / LOCK-03 regression coverage | ✓ VERIFIED | Covers double chest, barrel, shulker canonical target behavior. |
| `src/test/java/ym/signLock/service/LockServiceExtensionTest.java` | LOCK-02 regression coverage | ✓ VERIFIED | Covers shared-target extension sign write and capacity behavior. |
| `src/main/java/ym/signLock/listener/LockListener.java` | unified enforcement for interact/open/move/place/edit | ✓ VERIFIED | All relevant listener paths delegate back to `LockService` instead of local chest-half logic. |
| `src/test/java/ym/signLock/listener/LockListenerInteractTest.java` | PROT-01 regression coverage | ✓ VERIFIED | Covers unauthorized right-click on both double chest halves and owner sign editing exception. |
| `src/test/java/ym/signLock/listener/LockListenerInventoryOpenTest.java` | PROT-02 regression coverage | ✓ VERIFIED | Covers `Container`, `DoubleChest`, and automation move path. |
| `src/test/java/ym/signLock/listener/LockListenerPlacementTest.java` | LOCK-01 / LOCK-02 listener placement coverage | ✓ VERIFIED | Covers primary sign placement, extension sign placement, and max extension capacity on shared targets. |
| `src/test/java/ym/signLock/listener/LockListenerSignEditTest.java` | LOCK-04 structure preservation coverage | ✓ VERIFIED | Covers primary lock, more-users sign, and non-owner rejection. |

### Key Link Verification

| From | To | Via | Status | Details |
| --- | --- | --- | --- | --- |
| `LockService.java` | `LockServiceCanonicalTargetTest.java` | canonical target regression coverage | ✓ WIRED | Test directly exercises `findPlacementTarget`, `resolveDirectLockTarget`, and `findLock` on shared and single containers. |
| `LockService.java` | `LockServiceExtensionTest.java` | extension sign shared-target coverage | ✓ WIRED | Test directly exercises `addPlayerToLock` and `canCreateMoreUsersSign` across double chest halves. |
| `LockListener.java` | `LockService.java` | `findLock` / `resolveInventoryBlock` / `canAccess` | ✓ WIRED | Interact/open/move paths call `lockService` methods at [src/main/java/ym/signLock/listener/LockListener.java:97](D:/codex/SignLock/src/main/java/ym/signLock/listener/LockListener.java), [src/main/java/ym/signLock/listener/LockListener.java:117](D:/codex/SignLock/src/main/java/ym/signLock/listener/LockListener.java), [src/main/java/ym/signLock/listener/LockListener.java:133](D:/codex/SignLock/src/main/java/ym/signLock/listener/LockListener.java). |
| `LockListenerInventoryOpenTest.java` | `LockListener.java` | InventoryHolder shared-target regression | ✓ WIRED | Test verifies unauthorized inventory open emits `lockedContainerMessage()` on barrel and double chest paths. |
| `LockListener.java` | `LockService.java` | `findPlacementTarget` / `findManagedSignLock` / `canCreateMoreUsersSign` | ✓ WIRED | Placement/edit paths delegate to shared-target service methods at [src/main/java/ym/signLock/listener/LockListener.java:50](D:/codex/SignLock/src/main/java/ym/signLock/listener/LockListener.java), [src/main/java/ym/signLock/listener/LockListener.java:197](D:/codex/SignLock/src/main/java/ym/signLock/listener/LockListener.java), [src/main/java/ym/signLock/listener/LockListener.java:256](D:/codex/SignLock/src/main/java/ym/signLock/listener/LockListener.java). |
| `LockListenerSignEditTest.java` | `LockListener.java` | owner-only structure preservation regression | ✓ WIRED | Test verifies `preserveManagedSignStructure` behavior and `signEditDeniedMessage` handling. |
| `SignLockCommand.java` | `LockService.java` | command-side managed sign targeting | ✓ WIRED | `/signlock` target acquisition uses `findManagedSignLock`, keeping command path on the same sign-to-target resolution chain ([src/main/java/ym/signLock/command/SignLockCommand.java:181](D:/codex/SignLock/src/main/java/ym/signLock/command/SignLockCommand.java)). |

### Data-Flow Trace (Level 4)

| Artifact | Data Variable | Source | Produces Real Data | Status |
| --- | --- | --- | --- | --- |
| `LockService.java` | `normalizedTarget` / `LockInfo.targetBlock()` | World block state + adjacent sign scan in `resolveProtectedTarget` / `findPrimaryLock` | Yes | ✓ FLOWING |
| `LockListener.java` | `lock` | Event target block or inventory holder resolved through `LockService`, then permission-checked via `canAccess` | Yes | ✓ FLOWING |
| `SignLockCommand.java` | `lock` | Player gaze target -> `findManagedSignLock(targetBlock)` | Yes | ✓ FLOWING |

### Behavioral Spot-Checks

| Behavior | Command | Result | Status |
| --- | --- | --- | --- |
| Phase 01 service + listener regression suite | `./gradlew.bat test --tests "ym.signLock.service.*" --tests "ym.signLock.listener.*"` | `BUILD SUCCESSFUL` | ✓ PASS |

### Requirements Coverage

| Requirement | Source Plan | Description | Status | Evidence |
| --- | --- | --- | --- | --- |
| `LOCK-01` | `01-01`, `01-03` | 玩家可以在支持的单箱、双箱、木桶和潜影盒上创建主锁牌，且主锁牌绑定到正确的受保护目标。 | ✓ SATISFIED | Service canonical target logic plus placement tests on chest/barrel/shulker. |
| `LOCK-02` | `01-01`, `01-03` | 玩家可以在已有主锁的目标上继续添加扩展授权牌，且只有锁主人或管理员绕过用户可以执行该操作。 | ✓ SATISFIED | `addPlayerToLock`, `canCreateMoreUsersSign`, placement owner-only tests. |
| `LOCK-03` | `01-01` | 双箱的两个半边必须被视为同一把锁的共享目标，任一半边上的锁牌与授权都对整个双箱生效。 | ✓ SATISFIED | Double chest canonical target tests and listener open/interact regressions. |
| `LOCK-04` | `01-03` | 锁牌编辑时必须保留系统控制的结构行，避免主人名、锁头和扩展牌格式被误改坏。 | ✓ SATISFIED | `onSignChange` managed-sign path and sign edit regression tests. |
| `PROT-01` | `01-02` | 未授权玩家尝试右键使用已上锁方块时会被稳定拦截并收到正确提示。 | ✓ SATISFIED | `onPlayerInteract` + `LockListenerInteractTest`. |
| `PROT-02` | `01-02` | 未授权玩家尝试打开已上锁容器时会被稳定拦截，包括双箱和其他 `InventoryHolder` 路径。 | ✓ SATISFIED | `onInventoryOpen`, `resolveInventoryBlock`, `LockListenerInventoryOpenTest`. |

No orphaned Phase 01 requirements were found in `REQUIREMENTS.md`; the plan frontmatter covers all six phase requirement IDs.

### Anti-Patterns Found

No blocker or warning-level anti-patterns found in Phase 01 artifacts. Guard-clause `return null` occurrences are used for invalid target / non-lock / non-holder early exits and are covered by surrounding real logic and regression tests rather than placeholder stubs.

### Human Verification Required

None for phase-goal achievement. The phase goal is behavior-centric and already covered by automated service/listener regressions.

### Gaps Summary

No automated gaps found. Phase 01 delivers a single canonical target semantic through `LockService`, wires all listener and command entry points back to that semantic, and protects the phase requirements with runnable regression tests.

---

_Verified: 2026-04-02T12:52:01+08:00_  
_Verifier: Claude (gsd-verifier)_
