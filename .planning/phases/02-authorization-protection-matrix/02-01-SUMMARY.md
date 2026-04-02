---
phase: 02-authorization-protection-matrix
plan: 01
subsystem: testing
tags: [junit, mockbukkit, mockito, regression, authorization, protection-matrix]
requires:
  - phase: 01-lock-target-correctness
    provides: shared target resolution and managed-sign semantics reused by Phase 2 regressions
provides:
  - Phase 2 listener regression suites for break protection and protection matrix coverage
  - Phase 2 command and identity-cache regression suites for authorization outcomes and rename handling
  - Executable red tests for authorized-break and remove-normalization gaps before behavior fixes
affects: [02-02, 02-03, protection, commands, identity-cache]
tech-stack:
  added: []
  patterns: [MockBukkit listener regressions, mocked command entrypoint tests, temp-dir players.yml persistence tests]
key-files:
  created:
    - src/test/java/ym/signLock/listener/LockListenerBreakProtectionTest.java
    - src/test/java/ym/signLock/listener/LockListenerProtectionMatrixTest.java
    - src/test/java/ym/signLock/command/SignLockCommandAuthorizationTest.java
    - src/test/java/ym/signLock/service/PlayerIdentityServiceTest.java
  modified: []
key-decisions:
  - "Phase 02-01 ships regression-first contracts and intentionally preserves red tests for current authorization gaps."
  - "Piston destination-collision risk stays documented as a boundary proof test instead of speculative production changes in this plan."
patterns-established:
  - "Listener regression suites should distinguish intended red tests from fixture-noise by reusing proven sign-placement helpers."
  - "Command authorization regressions should mock SignLock and LockService directly while PlayerIdentityService persistence uses a real temp players.yml."
requirements-completed: [PROT-03, PROT-04, OPS-02, OPS-03]
duration: 10min
completed: 2026-04-02
---

# Phase 2 Plan 1: Authorization Regression Entrypoints Summary

**Four Phase 2 regression suites now lock break protection, protection matrix, command outcomes, and identity-cache rename behavior before production fixes land.**

## Performance

- **Duration:** 10 min
- **Started:** 2026-04-02T06:00:00Z
- **Completed:** 2026-04-02T06:10:32Z
- **Tasks:** 2
- **Files modified:** 4

## Accomplishments
- Added listener regressions for owner, authorized, unauthorized, and admin break semantics plus automation/explosion/fluid/piston coverage.
- Added command regressions for invalid target, no-space, remove-not-found, rename normalization, and consistent managed-sign info targeting.
- Added real `players.yml` cache tests for preload/remember persistence, case-insensitive lookup, and stored-name convergence after rename.

## Task Commits

Each task was committed atomically:

1. **Task 1: 为破坏保护与环境保护矩阵写红测合同** - `7b1e2cc` (test)
2. **Task 2: 为命令授权与身份缓存写红测合同** - `4817e4a` (test)

## Files Created/Modified
- `src/test/java/ym/signLock/listener/LockListenerBreakProtectionTest.java` - PROT-03 break matrix for protected block body and managed sign ownership boundaries.
- `src/test/java/ym/signLock/listener/LockListenerProtectionMatrixTest.java` - PROT-04 matrix for inventory move, explosion, fluid, piston, and shared-target coverage.
- `src/test/java/ym/signLock/command/SignLockCommandAuthorizationTest.java` - OPS-02 command outcome coverage for invalid targets, extension exhaustion, info routing, and rename inputs.
- `src/test/java/ym/signLock/service/PlayerIdentityServiceTest.java` - OPS-03 persistence and name-resolution coverage with real temp storage.

## Decisions Made
- Use failing regression tests as the Phase 2 delivery artifact for currently incorrect authorization behavior instead of mixing fixes into this plan.
- Keep piston destination collision as a documented boundary test that proves current coverage limits without changing runtime semantics yet.

## Deviations from Plan

None - plan executed exactly as written.

## Issues Encountered

- The plan-level verification command remains red by design. Current expected failures are:
  - `LockListenerBreakProtectionTest.authorizedPlayerCanAccessButCannotBreakProtectedContainerBody`
  - `LockListenerBreakProtectionTest.authorizedPlayerCannotBreakAnotherOwnersManagedSigns`
  - `SignLockCommandAuthorizationTest.removeShouldNormalizeKnownRenameBeforeRemoving`

## User Setup Required

None - no external service configuration required.

## Next Phase Readiness

- Phase 02-02 can now fix `onBlockBreak` semantics against stable red tests instead of manual reproduction.
- Phase 02-03 can normalize `/signlock remove` identity handling and rerun the same command suite as proof.

## Self-Check: PASSED

- Summary file exists.
- All four regression test files exist.
- Task commits `7b1e2cc` and `4817e4a` are present in git history.
