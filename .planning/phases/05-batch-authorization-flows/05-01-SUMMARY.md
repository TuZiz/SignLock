# Summary 05-01: Establish Shared Batch Authorization Contracts

## What Changed

- added `LockBatchTargetParser` so legacy single-target input and comma/space separated multi-target input now share one parsing path
- introduced `LockBatchAuthorizationService` to orchestrate batch add/remove while reusing `LockService` single-player semantics
- locked the partial-success contract with dedicated service tests before touching GUI or command entrypoints

## Key Files

- `src/main/java/ym/signLock/service/LockBatchTargetParser.java`
- `src/main/java/ym/signLock/service/LockBatchAuthorizationService.java`
- `src/test/java/ym/signLock/service/LockBatchTargetParserTest.java`
- `src/test/java/ym/signLock/service/LockBatchAuthorizationServiceTest.java`

## Verification

- `.\gradlew.bat test --tests "ym.signLock.service.LockBatchAuthorizationServiceTest" --tests "ym.signLock.service.LockBatchTargetParserTest"`
- Result: `BUILD SUCCESSFUL`

## Notes

- This plan intentionally stayed at the shared batch semantics layer.
- GUI and command entrypoints were left untouched until the contract was green.

---

*Plan: 05-01*
