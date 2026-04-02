---
phase: 01-lock-target-correctness
plan: 03
subsystem: testing
tags: [bukkit, paper, mockbukkit, listener, signlock, shared-container]
requires:
  - phase: 01-lock-target-correctness
    provides: canonical target resolution and listener shared-target access semantics from plans 01-01 and 01-02
provides:
  - listener placement regressions for primary and more-users signs on shared targets
  - managed sign edit regressions for owner-only structural preservation
  - LockListener fix for extension-sign header preservation during sign edits
affects: [phase-02-authorization-protection, listener-regressions, sign-editing]
tech-stack:
  added: []
  patterns: [LockService canonical target reuse in listener regressions, managed-sign header preservation by existing sign type]
key-files:
  created: [src/test/java/ym/signLock/listener/LockListenerPlacementTest.java, src/test/java/ym/signLock/listener/LockListenerSignEditTest.java]
  modified: [src/main/java/ym/signLock/listener/LockListener.java]
key-decisions:
  - "Model SignChangeEvent tests with old block state plus mutable event lines so managed-sign editing matches Bukkit behavior."
  - "Preserve extension-sign headers from the edited sign block's existing header instead of inferring sign type from LockInfo."
patterns-established:
  - "Listener shared-target regression pattern: placement and edit tests write through mocked SignChangeEvent#setLine to the backing sign state only after listener changes."
  - "Managed-sign structure preservation pattern: primary and extension sign headers are restored based on the sign being edited while lock ownership still comes from LockService."
requirements-completed: [LOCK-01, LOCK-02, LOCK-04]
duration: 21min
completed: 2026-04-02
---

# Phase 01 Plan 03: Lock Target Correctness Summary

**Listener placement regressions and sign-edit guards now keep primary and extension signs bound to the same shared target without letting owner/header lines drift**

## Performance

- **Duration:** 21 min
- **Started:** 2026-04-02T12:26:00+08:00
- **Completed:** 2026-04-02T12:47:25+08:00
- **Tasks:** 2
- **Files modified:** 3

## Accomplishments

- Added `LockListenerPlacementTest` to lock in shared-target primary sign placement, owner-only `[more users]` attachment, and capacity checks across either double-chest half.
- Added `LockListenerSignEditTest` to lock in owner-only managed-sign editing while preserving system-controlled header and owner lines.
- Fixed `LockListener.preserveManagedSignStructure()` so extension signs keep their own header during edits instead of being rewritten as primary locks.

## Task Commits

1. **Task 1: 为主锁/扩展牌贴牌与锁牌编辑补齐回归测试** - `90b3e5b` (test)
2. **Task 2: 收口 onSignChange 的共享目标贴牌与结构保护逻辑** - `9575e41` (fix)

## Files Created/Modified

- `src/test/java/ym/signLock/listener/LockListenerPlacementTest.java` - shared-target placement regressions for primary and more-users signs.
- `src/test/java/ym/signLock/listener/LockListenerSignEditTest.java` - managed-sign edit regressions for primary and extension sign structure preservation.
- `src/main/java/ym/signLock/listener/LockListener.java` - preserves extension headers from the edited sign block while keeping owner-only edit enforcement.

## Decisions Made

- Treated `LockService` as the only canonical target source and kept listener tests aligned to that shared-target path instead of reintroducing half-specific rules.
- Modeled sign-edit tests with existing sign state plus mutable event lines so extension-sign preservation verifies the real structure guard rather than a mock artifact.

## Deviations from Plan

None - plan executed exactly as written.

## Issues Encountered

- PowerShell in this environment does not support `&&`, so git staging/commit steps were run as separate commands.
- A transient git index-lock warning appeared while staging; the lock cleared without manual cleanup and did not affect plan scope.

## User Setup Required

None - no external service configuration required.

## Next Phase Readiness

- Phase 01 listener and placement coverage is complete; Phase 02 can now focus on authorization, break protection, and automation matrix behavior on top of the same canonical target semantics.
- Existing unrelated dirty worktree files remain untouched and should be preserved for their owner.

## Self-Check

PASSED

- Found `.planning/phases/01-lock-target-correctness/01-03-SUMMARY.md`
- Found commit `90b3e5b`
- Found commit `9575e41`

---
*Phase: 01-lock-target-correctness*
*Completed: 2026-04-02*
