# Summary 06-01: Shared Lock Summary Contract

## What Changed

- extended `LockService.describeLock` so it now returns canonical target summary data alongside owner, authorized players, and extension counts
- introduced viewer scope semantics (`可管理` / `可访问` / `未授权`) without creating a second permission system
- upgraded `LockSummaryView` and `LockSummaryTarget` so GUI and `/bl info` can share the same summary contract and target wording

## Key Files

- `src/main/java/ym/signLock/service/LockService.java`
- `src/main/java/ym/signLock/gui/LockSummaryView.java`
- `src/main/java/ym/signLock/gui/LockSummaryTarget.java`
- `src/test/java/ym/signLock/service/LockServiceSummarySemanticsTest.java`

## Verification

- `.\gradlew.bat test --tests "ym.signLock.service.LockServiceSummarySemanticsTest" --tests "ym.signLock.gui.LockManagementGuiViewTest" --tests "ym.signLock.command.SignLockCommandAuthorizationTest"`
- Result: `BUILD SUCCESSFUL`

## Notes

- double chest summaries now stay anchored to the same canonical shared target even when the entry sign moves between halves
- summary wording is now centralized instead of being redefined separately in GUI and command paths

---

*Plan: 06-01*
