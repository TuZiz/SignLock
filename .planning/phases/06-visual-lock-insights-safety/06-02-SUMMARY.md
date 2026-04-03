# Summary 06-02: Viewer-Aware GUI Routing

## What Changed

- kept owner/manage GUI behavior intact while adding a read-only summary GUI for authorized non-owners
- routed managed-sign right-clicks by viewer scope: owners still manage, authorized players now view, unauthorized players still do not get a full summary surface
- hardened GUI click handling so read-only holders cannot trigger add/remove writes even if a client clicks those slots

## Key Files

- `src/main/java/ym/signLock/listener/LockListener.java`
- `src/main/java/ym/signLock/gui/LockManagementGui.java`
- `src/main/java/ym/signLock/gui/LockManagementGuiHolder.java`
- `src/main/java/ym/signLock/gui/LockManagementGuiService.java`
- `src/main/java/ym/signLock/gui/LockManagementGuiActionService.java`
- `src/test/java/ym/signLock/gui/LockManagementReadOnlyGuiTest.java`
- `src/test/java/ym/signLock/listener/LockListenerViewerSummaryEntryTest.java`

## Verification

- `.\gradlew.bat test --tests "ym.signLock.gui.*" --tests "ym.signLock.listener.LockListenerViewerSummaryEntryTest" --tests "ym.signLock.listener.LockGuiChatInputTest"`
- Result: `BUILD SUCCESSFUL`

## Notes

- owner sneak-right-click remains the native sign editor fallback
- container interaction rules were left untouched; Phase 6 only upgraded the managed-sign insight surface

---

*Plan: 06-02*
