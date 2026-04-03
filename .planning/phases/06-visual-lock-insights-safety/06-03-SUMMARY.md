# Summary 06-03: `/bl info` Parity and Safety Closeout

## What Changed

- upgraded `/bl info` to print the same summary language as the GUI: owner, viewer scope, target summary, authorized roster visibility, and extension count
- rewrote `config.yml` and `TESTING.md` into clean, release-facing UTF-8 resources, including the new Phase 6 scope and target summary keys
- added Phase 6 command compatibility coverage and validated that the wider GUI / listener / command / service suite remains green

## Key Files

- `src/main/java/ym/signLock/command/SignLockCommand.java`
- `src/main/java/ym/signLock/config/SignLockConfig.java`
- `src/main/resources/config.yml`
- `src/test/java/ym/signLock/command/SignLockCommandCompatibilityTest.java`
- `src/test/java/ym/signLock/command/SignLockCommandInfoCompatibilityTest.java`
- `TESTING.md`

## Verification

- `.\gradlew.bat test --tests "ym.signLock.command.*" --tests "ym.signLock.gui.*" --tests "ym.signLock.listener.LockListenerGuiEntryTest" --tests "ym.signLock.listener.LockListenerViewerSummaryEntryTest" --tests "ym.signLock.service.LockServiceSummarySemanticsTest"`
- `.\gradlew.bat test`
- Result: `BUILD SUCCESSFUL`

## Notes

- unauthorized `/bl info` viewers now get summary scope and target context without seeing the full authorized roster
- manual Paper / Folia smoke steps are documented explicitly in `TESTING.md`

---

*Plan: 06-03*
