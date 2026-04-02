# Summary 05-03: Extend `/bl` Batch Compatibility and Close the Phase

## What Changed

- upgraded `/bl add` and `/bl remove` to accept multiple players while preserving single-target legacy behavior
- kept single-target success/failure messaging compatible and introduced aggregated feedback only for true batch operations
- added dedicated batch command regression coverage and validated that GUI and command paths now share the same batch semantics

## Key Files

- `src/main/java/ym/signLock/command/SignLockCommand.java`
- `src/main/java/ym/signLock/config/SignLockConfig.java`
- `src/test/java/ym/signLock/command/SignLockCommandBatchCompatibilityTest.java`
- `src/test/java/ym/signLock/command/SignLockCommandCompatibilityTest.java`

## Verification

- `.\gradlew.bat test --tests "ym.signLock.command.SignLockCommandBatchCompatibilityTest" --tests "ym.signLock.command.SignLockCommandCompatibilityTest"`
- `.\gradlew.bat test --tests "ym.signLock.listener.*" --tests "ym.signLock.command.*" --tests "ym.signLock.service.*" --tests "ym.signLock.gui.*"`
- `.\gradlew.bat test`
- Result: `BUILD SUCCESSFUL`

## Notes

- The batch command path reuses the same parser and orchestration semantics as the GUI path.
- Legacy `/bl info` behavior remained unchanged.

---

*Plan: 05-03*
