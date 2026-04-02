# Summary 04-03: Add GUI Authorization Actions and Compatibility Coverage

## What Changed

- added single-player add/remove GUI actions and a pending chat-input bridge for guided player entry
- extracted `LockPlayerNameNormalizer` so GUI flows and `/bl add|remove|info` share the same player-name normalization path
- expanded regression coverage to include GUI actions, async chat handoff, and command compatibility after the shared helper extraction

## Key Files

- `src/main/java/ym/signLock/gui/LockManagementGuiActionService.java`
- `src/main/java/ym/signLock/gui/LockManagementPendingInputStore.java`
- `src/main/java/ym/signLock/listener/LockGuiChatInputListener.java`
- `src/main/java/ym/signLock/service/LockPlayerNameNormalizer.java`
- `src/main/java/ym/signLock/command/SignLockCommand.java`
- `src/test/java/ym/signLock/gui/LockManagementGuiActionTest.java`
- `src/test/java/ym/signLock/listener/LockGuiChatInputTest.java`
- `src/test/java/ym/signLock/command/SignLockCommandCompatibilityTest.java`

## Verification

- `.\gradlew.bat test --tests "ym.signLock.listener.LockListenerGuiEntryTest" --tests "ym.signLock.gui.LockManagementGuiViewTest" --tests "ym.signLock.gui.LockGuiListenerClickSafetyTest" --tests "ym.signLock.gui.LockManagementGuiActionTest" --tests "ym.signLock.listener.LockGuiChatInputTest" --tests "ym.signLock.command.SignLockCommandCompatibilityTest"`
- Result: `BUILD SUCCESSFUL`

## Notes

- GUI add/remove still delegates to `LockService`; no second authorization store was introduced.
- Batch authorization remains deferred to Phase 5.

---

*Plan: 04-03*
