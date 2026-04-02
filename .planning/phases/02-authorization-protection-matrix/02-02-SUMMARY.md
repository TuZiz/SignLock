---
phase: 02-authorization-protection-matrix
plan: 02
subsystem: auth
tags: [bukkit, paper, folia, protection, listener, lockservice, regression]
requires:
  - phase: 01-lock-target-correctness
    provides: shared target resolution and managed-sign semantics reused by Phase 2 protection decisions
  - phase: 02-authorization-protection-matrix
    provides: regression-first listener suites from 02-01 that exposed break and matrix gaps
provides:
  - Explicit manage-level break authorization via LockService canBreak semantics
  - Centralized protected-structure helpers reused by block break, piston, fluid, explosion, and inventory automation listeners
  - Green Phase 2 listener regressions for protected body, managed-sign, shared-target, and environment protection paths
affects: [02-03, protection, listeners, automation, authorization]
tech-stack:
  added: []
  patterns: [LockService-first authorization matrix, listener-as-orchestrator, shared protected-structure helpers]
key-files:
  created: []
  modified:
    - src/main/java/ym/signLock/service/LockService.java
    - src/main/java/ym/signLock/listener/LockListener.java
key-decisions:
  - "Break semantics are now explicitly named as canBreak(...) even though they remain equivalent to manage permissions."
  - "Environment listeners keep delegating all protected-target resolution to LockService instead of duplicating block iteration logic."
patterns-established:
  - "Use LockService as the single source of truth for access, manage, break, and protected-structure decisions."
  - "Listener protection entrypoints should only orchestrate events and messages while LockService owns target resolution."
requirements-completed: [PROT-03, PROT-04]
duration: 6min
completed: 2026-04-02
---

# Phase 2 Plan 2: Authorization Protection Matrix Summary

**LockService now defines explicit break authorization and shared protected-structure helpers, and LockListener reuses them so Phase 2 break and protection-matrix regressions stay green across body, managed-sign, and shared-target paths.**

## Performance

- **Duration:** 6 min
- **Started:** 2026-04-02T06:13:18Z
- **Completed:** 2026-04-02T06:19:10Z
- **Tasks:** 2
- **Files modified:** 2

## Accomplishments
- Added explicit `canBreak(...)` authorization semantics so authorized users retain access without inheriting destroy rights.
- Unified protected-structure checks in `LockService` and removed listener-local protection iteration for piston and fluid paths.
- Verified `LockListenerBreakProtectionTest`, `LockListenerProtectionMatrixTest`, and the full `ym.signLock.listener.*` suite all pass.

## Task Commits

Each task was committed atomically:

1. **Task 1: 在 LockService 收口破坏权限语义并修复 BlockBreak** - `50e7718` (fix)
2. **Task 2: 收紧自动化与环境保护矩阵并统一 listener 语义入口** - `96062cd` (refactor)

## Files Created/Modified
- `src/main/java/ym/signLock/service/LockService.java` - Added `canBreak(...)`, centralized protected-structure helpers, and made automation checks share the same protected-target semantics.
- `src/main/java/ym/signLock/listener/LockListener.java` - Switched break checks away from `canAccess(...)` and delegated piston/fluid protection iteration back to `LockService`.

## Decisions Made
- Named break authorization explicitly instead of continuing to overload `canManage(...)` at listener call sites, so intent is visible in production code and tests.
- Kept piston destination collision at its existing tested boundary because `02-01` already proved the current API path does not reproducibly cover that side effect.

## Deviations from Plan

None - plan executed exactly as written.

## Issues Encountered

- PowerShell rejected `&&` during the first commit attempt; the retry used native statement separators and did not affect repository contents.
- The broader phase validation command still includes `OPS-02/OPS-03` red tests owned by `02-03`, so final automated verification for this plan stayed scoped to listener suites.

## User Setup Required

None - no external service configuration required.

## Next Phase Readiness

- `02-03` can now focus on command authorization and identity normalization without reopening listener-side break or matrix behavior.
- Protection entrypoints now trace cleanly back to `LockService`, which reduces risk for the remaining Phase 2 edge-condition fixes.

## Self-Check: PASSED

- Summary file exists.
- Task commits `50e7718` and `96062cd` are present in git history.
