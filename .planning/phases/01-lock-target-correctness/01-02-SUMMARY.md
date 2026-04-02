---
phase: 01-lock-target-correctness
plan: 02
subsystem: testing
tags: [spigot, paper, folia, mockbukkit, listener, chest-lock, inventory]
requires:
  - phase: 01-lock-target-correctness
    provides: canonical target resolution for shared lock targets in LockService
provides:
  - listener regression coverage for shared-target interact and inventory-open protection
  - LockListener canonical-target reuse across interact, inventory open, and automation paths
  - owner sign-editor behavior preserved while keeping locked-use and locked-container prompts separate
affects: [phase-01-plan-03, protection-paths, listener-regressions]
tech-stack:
  added: [none]
  patterns: [service-owned canonical target resolution, MockBukkit listener regression coverage]
key-files:
  created:
    - src/test/java/ym/signLock/listener/LockListenerInteractTest.java
    - src/test/java/ym/signLock/listener/LockListenerInventoryOpenTest.java
    - src/main/java/ym/signLock/listener/LockListener.java
    - src/main/java/ym/signLock/config/SignLockConfig.java
    - src/main/java/ym/signLock/service/PlayerIdentityService.java
  modified: []
key-decisions:
  - "Listener entrypoints continue to delegate shared-target normalization to LockService instead of reimplementing chest-half logic locally."
  - "locked-use and locked-container remain separate messages per D-07 while still sharing the same canonical target resolution chain."
patterns-established:
  - "Listener protection checks should resolve a canonical target via LockService before findLock/canAccess decisions."
  - "Regression tests for shared containers should exercise both clicked halves and InventoryHolder-based access paths."
requirements-completed: [PROT-01, PROT-02]
duration: 20min
completed: 2026-04-02
---

# Phase 01 Plan 02: Lock Target Correctness Summary

**Listener protection now reuses canonical shared-target resolution for right-click, inventory-open, and automation paths, with MockBukkit regressions covering double-chest access from both halves and holder-driven inventory opens.**

## Performance

- **Duration:** 20 min
- **Started:** 2026-04-02T04:19:00Z
- **Completed:** 2026-04-02T04:39:25Z
- **Tasks:** 2
- **Files modified:** 5

## Accomplishments
- Added listener regressions for unauthorized double-chest interaction, holder-based container opening, and owner sign-editor access.
- Confirmed `LockListener` routes interact, inventory-open, and automation checks through `findLock`, `canAccess`, and `resolveInventoryBlock`.
- Preserved separate `locked-use` and `locked-container` prompts while closing the shared-target bypass risk.

## Task Commits

Each task was committed atomically:

1. **Task 1: 为右键与开箱路径补齐共享目标拦截回归测试** - `bb1c652` (test)
2. **Task 2: 收口 LockListener 的交互、开箱与自动化入口到同一锁语义** - `83c4739` (feat)

## Files Created/Modified
- `src/test/java/ym/signLock/listener/LockListenerInteractTest.java` - Covers PROT-01 for both double-chest halves and owner sign editing.
- `src/test/java/ym/signLock/listener/LockListenerInventoryOpenTest.java` - Covers PROT-02 for `Container`, `DoubleChest`, and automation holder paths.
- `src/main/java/ym/signLock/listener/LockListener.java` - Shares canonical target access checks across interaction, inventory open, and automation listeners.
- `src/main/java/ym/signLock/config/SignLockConfig.java` - Provides listener-visible message/config access needed by the newly committed listener implementation.
- `src/main/java/ym/signLock/service/PlayerIdentityService.java` - Supplies identity resolution used by the committed listener access checks.

## Decisions Made
- Reused `LockService` canonical-target semantics end-to-end instead of introducing listener-local double chest branching.
- Kept owner sign-editor access as the only interaction-path exception before normal access denial.

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 3 - Blocking] Included minimal listener dependencies in Task 2 commit**
- **Found during:** Task 2 (收口 LockListener 的交互、开箱与自动化入口到同一锁语义)
- **Issue:** `LockListener` in the dirty worktree depended on untracked `SignLockConfig` and `PlayerIdentityService`, so committing only the listener file would leave the task commit uncompilable on a clean checkout.
- **Fix:** Committed `SignLockConfig` and `PlayerIdentityService` together with `LockListener` as the smallest compilable implementation slice.
- **Files modified:** `src/main/java/ym/signLock/listener/LockListener.java`, `src/main/java/ym/signLock/config/SignLockConfig.java`, `src/main/java/ym/signLock/service/PlayerIdentityService.java`
- **Verification:** `./gradlew.bat test --tests "ym.signLock.listener.*"`
- **Committed in:** `83c4739`

---

**Total deviations:** 1 auto-fixed (1 blocking)
**Impact on plan:** The added files were compile-time prerequisites for the planned listener implementation. No behavioral scope creep beyond Plan 01-02.

## Issues Encountered
- The plan target was already partially implemented in the dirty worktree, so execution focused on verification and safe commit slicing rather than rewriting the listener/tests.

## User Setup Required

None - no external service configuration required.

## Next Phase Readiness
- Listener shared-target protection is covered and green, so Phase 01 Plan 03 can focus on placement and managed-sign structure preservation.
- Unrelated dirty worktree items remain outside this plan, including command wiring, plugin bootstrap, and resource-file updates.

## Self-Check: PASSED
- FOUND: `.planning/phases/01-lock-target-correctness/01-02-SUMMARY.md`
- FOUND: `bb1c652`
- FOUND: `83c4739`

---
*Phase: 01-lock-target-correctness*
*Completed: 2026-04-02*
