---
phase: 02-authorization-protection-matrix
plan: 03
subsystem: auth
tags: [bukkit, paper, folia, commands, identity-cache, regression]
requires:
  - phase: 02-authorization-protection-matrix
    provides: Phase 02-01 regression suites and Phase 02-02 listener-side authorization matrix fixes
provides:
  - Symmetric identity normalization for `/signlock add` and `/signlock remove`
  - Cache-first command resolution that preserves unknown text inputs while converging to last-known names
  - Green Phase 2 command, identity, listener, and Phase 1 shared-target regression verification
affects: [phase-03, commands, identity-cache, authorization]
tech-stack:
  added: []
  patterns: [shared command-side identity normalization helper, cache-first UUID-aware authorization resolution]
key-files:
  created: []
  modified:
    - src/main/java/ym/signLock/command/SignLockCommand.java
key-decisions:
  - "Command-side target player normalization stays in SignLockCommand so listener and LockService scopes remain unchanged."
  - "Unknown or unresolved identity lookups fall back to the original text input instead of producing null command targets."
patterns-established:
  - "Add and remove command paths must share the same remember -> resolveStoredName -> save normalization chain."
  - "Phase 2 closeout requires both current-phase regressions and Phase 1 shared-target suites to prove no cross-entry regressions."
requirements-completed: [OPS-02, OPS-03, PROT-03]
duration: 9min
completed: 2026-04-02
---

# Phase 2 Plan 3: Authorization Command Identity Closure Summary

**`/signlock add` and `/signlock remove` now share the same cache-backed identity normalization path, and the full Phase 2 authorization matrix remains green alongside Phase 1 shared-target regressions.**

## Performance

- **Duration:** 9 min
- **Started:** 2026-04-02T14:18:00+08:00
- **Completed:** 2026-04-02T14:26:51+08:00
- **Tasks:** 2
- **Files modified:** 1

## Accomplishments
- Normalized `/signlock remove` through the same identity-resolution chain already used by `/signlock add`, closing the rename/cache-known asymmetry.
- Added a defensive fallback so unresolved cache lookups preserve the original command input instead of passing null into authorization mutations.
- Verified the full Phase 2 listener/command/service suites and reran Phase 1 shared-target listener/service suites without reopening `LockListener` scope.

## Task Commits

Each task was committed atomically:

1. **Task 1: 统一 add/remove/info 的身份归一化与结果分支** - `5d31ac6` (feat)
2. **Task 2: 跑 Phase 2 全量回归并只修命令/身份链收尾缺口** - `409e078` (test)

## Files Created/Modified
- `src/main/java/ym/signLock/command/SignLockCommand.java` - Extracted shared command-side identity normalization and made remove follow the same cache-first resolution path as add.

## Decisions Made
- Kept the fix in `SignLockCommand` instead of spreading identity normalization rules into `LockService`, because this plan was explicitly limited to command/identity scope.
- Treated unresolved stored-name lookups as best-effort normalization and fell back to the original input so cache-unknown flows stay usable.

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 1 - Bug] Prevented null normalized command targets**
- **Found during:** Task 1 (统一 add/remove/info 的身份归一化与结果分支)
- **Issue:** After unifying command normalization, a missing or unstubbed `resolveStoredName(...)` result could propagate `null` into remove flows.
- **Fix:** Added a fallback in the shared normalization helper to preserve the original command input when the cache cannot resolve a stored name.
- **Files modified:** src/main/java/ym/signLock/command/SignLockCommand.java
- **Verification:** `.\gradlew.bat test --tests "ym.signLock.command.SignLockCommandAuthorizationTest" --tests "ym.signLock.service.PlayerIdentityServiceTest"`
- **Committed in:** `5d31ac6`

---

**Total deviations:** 1 auto-fixed (1 bug)
**Impact on plan:** The auto-fix was required for correctness after consolidating add/remove identity handling. No scope creep.

## Issues Encountered

- MC Plugin Neuron routed the preflight request to unrelated knowledge sources for this repository, so execution fell back to the local SignLock plan, source, and test context.
- PowerShell on this machine could not execute `rg.exe`; file discovery fell back to `Get-ChildItem` without affecting implementation or verification.

## User Setup Required

None - no external service configuration required.

## Next Phase Readiness

- Phase 2 code scope is closed: listener protection, command authorization, and identity-cache regressions are green together.
- Remaining work is Phase 3 localization and release-readiness; no unresolved command/identity gaps remain from Phase 2.

## Self-Check: PASSED

- Summary file exists.
- Task commits `5d31ac6` and `409e078` are present in git history.
