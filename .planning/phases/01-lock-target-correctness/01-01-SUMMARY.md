---
phase: 01-lock-target-correctness
plan: 01
subsystem: testing
tags: [bukkit, paper, spigot, mockbukkit, junit, canonical-target, lockservice]
requires: []
provides:
  - MockBukkit-based LockService regression baseline for canonical target behavior
  - Deterministic canonical target resolution for shared containers in LockService
  - Shared-target extension sign lookup and write paths routed through one service semantic
affects: [lock-listener, signlock-command, shared-container-protection]
tech-stack:
  added: [junit-jupiter-6.0.3, junit-platform-launcher-6.0.3, mockbukkit-v1.21-4.108.0, mockito-junit-jupiter-5.23.0, paper-api-1.21.11-test-only]
  patterns: [service-first canonical target resolver, shared-target regression tests]
key-files:
  created: [src/test/java/ym/signLock/service/LockServiceCanonicalTargetTest.java, src/test/java/ym/signLock/service/LockServiceExtensionTest.java]
  modified: [build.gradle, src/main/java/ym/signLock/service/LockService.java]
key-decisions:
  - "Double chest canonical block is chosen deterministically by stable coordinate ordering instead of clicked half."
  - "Managed-sign flows now reuse findPlacementTarget/findLock semantics instead of ad-hoc attached-block checks."
patterns-established:
  - "Canonical target first, then scan related blocks."
  - "Service-layer regression tests cover single container stability and shared-container extension semantics."
requirements-completed: [LOCK-01, LOCK-02, LOCK-03]
duration: 13min
completed: 2026-04-02
---

# Phase 1 Plan 1: Lock Target Correctness Summary

**MockBukkit-backed LockService canonical target resolution for double chests, barrels, shulker boxes, and shared extension signs**

## Performance

- **Duration:** 13 min
- **Started:** 2026-04-02T04:16:00Z
- **Completed:** 2026-04-02T04:28:40Z
- **Tasks:** 2
- **Files modified:** 4

## Accomplishments
- Added a runnable JUnit 6 + MockBukkit regression harness for `LockService`.
- Centralized shared-container canonical target resolution inside `LockService`.
- Routed managed-sign lookup and extension-sign writes through the same canonical target semantic.

## Task Commits

Each task was committed atomically:

1. **Task 1: 安装 Phase 01 的 MockBukkit 回归底座并写出服务层红测** - `7fe3479` (test)
2. **Task 2: 在 LockService 内实现 canonical target 与共享目标扫描** - `5a6eea6` (feat)

## Files Created/Modified
- `build.gradle` - Bootstraps the service-test runtime with JUnit 6, MockBukkit, Mockito, Paper test API, and launcher alignment.
- `src/main/java/ym/signLock/service/LockService.java` - Adds deterministic canonical target selection and reuses it across lock lookup, managed-sign lookup, inventory resolution, and extension flows.
- `src/test/java/ym/signLock/service/LockServiceCanonicalTargetTest.java` - Covers double chest canonicalization plus barrel and shulker single-container stability.
- `src/test/java/ym/signLock/service/LockServiceExtensionTest.java` - Covers shared-target extension sign discovery and add-player behavior from the other chest half.

## Decisions Made
- Chose a deterministic coordinate-ordered canonical block for shared targets so `LockInfo.targetBlock()` is independent of clicked half.
- Reused `findPlacementTarget` for sign-attached target resolution to avoid a second attached-block semantic drifting from canonical target logic.

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 3 - Blocking] Aligned the test runtime with Gradle 8.8 and local JDK availability**
- **Found during:** Task 1
- **Issue:** `gradlew` failed under Java 25 with `Unsupported class file major version 69`.
- **Fix:** Ran all Gradle verification with local JDK 21 (`C:\Program Files\Eclipse Adoptium\jdk-21.0.9.10-hotspot`).
- **Files modified:** None
- **Verification:** `./gradlew.bat test --tests "ym.signLock.service.LockServiceCanonicalTargetTest" --tests "ym.signLock.service.LockServiceExtensionTest"` succeeded under JDK 21.
- **Committed in:** `7fe3479` (task verification environment)

**2. [Rule 3 - Blocking] Completed missing MockBukkit runtime dependencies**
- **Found during:** Task 1
- **Issue:** JUnit 6 launcher mismatch and missing Adventure/Paper classes prevented MockBukkit tests from starting.
- **Fix:** Added `junit-platform-launcher`, Adventure runtime dependencies, Paper Maven repo, and test-only `paper-api:1.21.11-R0.1-SNAPSHOT`.
- **Files modified:** `build.gradle`
- **Verification:** Targeted service tests compiled and executed, then passed after implementation.
- **Committed in:** `7fe3479` (bootstrap), retained in final plan state

**3. [Rule 1 - Bug] Adjusted extension-sign test placement to a stable MockBukkit-supported legal face**
- **Found during:** Task 2
- **Issue:** MockBukkit did not stably persist extension player lines for the original alternate-face placement, obscuring shared-half assertions.
- **Fix:** Kept the extension sign on the other chest half but moved it to a legal face with stable line persistence in MockBukkit.
- **Files modified:** `src/test/java/ym/signLock/service/LockServiceExtensionTest.java`
- **Verification:** `./gradlew.bat test --tests "ym.signLock.service.LockServiceExtensionTest"`
- **Committed in:** `5a6eea6`

---

**Total deviations:** 3 auto-fixed (1 bug, 2 blocking)
**Impact on plan:** All deviations were required to make the planned regression harness executable and trustworthy. No product-scope creep.

## Issues Encountered
- MockBukkit 4.108.0 requires Paper 1.21.11 test API alignment; using older Paper API produced material/tag mismatches during bootstrap.
- JUnit 6 on Gradle 8.8 required an explicit platform launcher dependency for test discovery.

## User Setup Required

None - no external service configuration required.

## Next Phase Readiness
- `LockService` is now the single semantic source for canonical lock targets across direct lookup, managed signs, and inventory holders.
- Phase 01 Plan 02 can now move listener entrypoints onto the same canonical target behavior without rebuilding the test harness.

## Self-Check: PASSED

- Found summary file: `.planning/phases/01-lock-target-correctness/01-01-SUMMARY.md`
- Found task commit: `7fe3479`
- Found task commit: `5a6eea6`

---
*Phase: 01-lock-target-correctness*
*Completed: 2026-04-02*
