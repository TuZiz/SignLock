---
status: passed
score: 6/6 must-haves verified
gaps:
  - Real-client Paper/Folia smoke checks remain recommended before final milestone archive, but the checklist is now documented in `TESTING.md`.
phase: 06-visual-lock-insights-safety
updated: 2026-04-03
---

# Phase 6 Verification

## Outcome

Phase 6 passed. SignLock now presents lock state through a shared summary contract across GUI and `/bl info`, adds an authorized read-only summary surface, and keeps the v1.0 protection matrix intact.

## Must-Have Checks

### 1. Lock insights now express owner, viewer scope, target summary, and extension usage through one shared contract

Passed.

- `LockService.describeLock` now returns canonical target summary data
- `LockSummaryView` carries viewer scope semantics instead of letting GUI and commands guess them separately
- single chest and double chest summaries are both anchored to canonical lock targets

### 2. Authorized non-owners get a stable read-only summary path without gaining management actions

Passed.

- owner normal right-click still opens the management GUI
- owner sneak-right-click still falls back to native sign editing
- authorized non-owners now open a read-only GUI with management controls replaced by placeholders

### 3. Unauthorized viewers do not receive full roster visibility through the new UX paths

Passed.

- unauthorized managed-sign clicks do not open the full summary GUI
- denied `/bl info` output shows scope and target context while hiding the authorized roster

### 4. `/bl info` now matches GUI summary language without breaking targeted sign compatibility

Passed.

- primary signs and `[更多用户]` extension signs still share the same targeted entry point
- command output now includes owner, viewer scope, target summary, roster visibility, and extension count

### 5. The wider GUI and interaction baseline stayed safe after the viewer-aware upgrade

Passed.

- click safety and chat input tests remain green
- read-only GUI clicks are rejected before any write path can execute
- managed-sign routing changes did not loosen block or container protection

### 6. Repository-wide regression coverage remains green after the Phase 6 resource rewrite

Passed.

- the new config and testing resources compile and package cleanly
- targeted Phase 6 suites passed
- full repository tests passed

## Commands Run

```text
.\gradlew.bat test --tests "ym.signLock.service.LockServiceSummarySemanticsTest" --tests "ym.signLock.gui.LockManagementGuiViewTest" --tests "ym.signLock.gui.LockManagementReadOnlyGuiTest" --tests "ym.signLock.listener.LockListenerViewerSummaryEntryTest" --tests "ym.signLock.gui.LockManagementGuiActionTest" --tests "ym.signLock.command.SignLockCommandAuthorizationTest" --tests "ym.signLock.command.SignLockCommandCompatibilityTest" --tests "ym.signLock.command.SignLockCommandInfoCompatibilityTest"
.\gradlew.bat test --tests "ym.signLock.gui.*" --tests "ym.signLock.listener.LockListenerGuiEntryTest" --tests "ym.signLock.listener.LockListenerViewerSummaryEntryTest" --tests "ym.signLock.listener.LockGuiChatInputTest" --tests "ym.signLock.command.*" --tests "ym.signLock.service.LockServiceSummarySemanticsTest"
.\gradlew.bat test
```

## Result

All Phase 6 automated success criteria are satisfied. The milestone is ready for closeout once the documented Paper/Folia smoke checks are completed or explicitly waived.
