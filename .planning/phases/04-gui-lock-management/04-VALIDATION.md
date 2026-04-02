---
phase: 04
slug: gui-lock-management
status: ready
nyquist_compliant: true
wave_0_complete: false
created: 2026-04-02
---

# Phase 04 - Validation Strategy

> Per-phase validation contract for feedback sampling during execution.

---

## Test Infrastructure

| Property | Value |
|----------|-------|
| **Framework** | JUnit Jupiter 6.0.3 + MockBukkit 4.108.0 + Mockito 5.23.0 |
| **Config file** | `build.gradle` |
| **Quick run command** | `.\gradlew.bat test --tests "ym.signLock.listener.LockListenerGuiEntryTest" --tests "ym.signLock.gui.*" --tests "ym.signLock.command.SignLockCommandCompatibilityTest"` |
| **Full suite command** | `.\gradlew.bat test` |
| **Estimated runtime** | ~45 seconds |

---

## Sampling Rate

- **After every task commit:** Run `.\gradlew.bat test --tests "ym.signLock.listener.LockListenerGuiEntryTest" --tests "ym.signLock.gui.*" --tests "ym.signLock.command.SignLockCommandCompatibilityTest"`
- **After every plan wave:** Run `.\gradlew.bat test`
- **Before `$gsd-verify-work`:** Full suite must be green
- **Max feedback latency:** 60 seconds

---

## Per-Task Verification Map

| Task ID | Plan | Wave | Requirement | Test Type | Automated Command | File Exists | Status |
|---------|------|------|-------------|-----------|-------------------|-------------|--------|
| 04-01-01 | 01 | 1 | GUI-01 | unit/integration | `.\gradlew.bat test --tests ym.signLock.listener.LockListenerGuiEntryTest` | No - W0 | pending |
| 04-01-02 | 01 | 1 | GUI-02 | unit/integration | `.\gradlew.bat test --tests ym.signLock.gui.LockManagementGuiViewTest` | No - W0 | pending |
| 04-02-01 | 02 | 2 | GUI-01 | unit/integration | `.\gradlew.bat test --tests ym.signLock.gui.LockGuiListenerClickSafetyTest` | No - W0 | pending |
| 04-02-02 | 02 | 2 | GUI-02 | unit/integration | `.\gradlew.bat test --tests ym.signLock.gui.LockManagementGuiViewTest` | No - W0 | pending |
| 04-03-01 | 03 | 3 | GUI-03 | unit/integration | `.\gradlew.bat test --tests ym.signLock.gui.LockManagementGuiActionTest` | No - W0 | pending |
| 04-03-02 | 03 | 3 | GUI-03 | unit/integration | `.\gradlew.bat test --tests ym.signLock.listener.LockGuiChatInputTest` | No - W0 | pending |
| 04-03-03 | 03 | 3 | GUI-01, GUI-03 | unit/integration | `.\gradlew.bat test --tests ym.signLock.command.SignLockCommandCompatibilityTest` | No - W0 | pending |

*Status: pending / green / red / flaky*

---

## Wave 0 Requirements

- [ ] `src/test/java/ym/signLock/listener/LockListenerGuiEntryTest.java` - owner normal right-click opens GUI, sneak-right-click preserves sign edit fallback, non-owner cannot open management GUI
- [ ] `src/test/java/ym/signLock/gui/LockManagementGuiViewTest.java` - summary view renders owner, allowed players, extension count, and canonical target summary
- [ ] `src/test/java/ym/signLock/gui/LockManagementGuiActionTest.java` - single-player add/remove actions reuse `LockService` result semantics and keep sign-backed data format stable
- [ ] `src/test/java/ym/signLock/gui/LockGuiListenerClickSafetyTest.java` - click/drag/shift/number-key interactions are cancelled and GUI transitions are deferred to next tick
- [ ] `src/test/java/ym/signLock/listener/LockGuiChatInputTest.java` - guided chat input captures text asynchronously and hands writeback / reopen back to sync scheduling
- [ ] `src/test/java/ym/signLock/command/SignLockCommandCompatibilityTest.java` - `/bl add`, `/bl remove`, `/bl info` stay compatible if GUI work extracts shared normalization helpers

---

## Manual-Only Verifications

| Behavior | Requirement | Why Manual | Test Instructions |
|----------|-------------|------------|-------------------|
| GUI title, icon layout, and Chinese copy readability in a real Paper client | GUI-01, GUI-02 | MockBukkit cannot validate actual client presentation | Start a Paper test server, create a lock, open GUI by normal right-click, confirm single-page summary is readable and action hints match behavior |
| Sneak-right-click fallback opens native sign editor without GUI flicker | GUI-01 | Native sign editor UX is client-coupled | On Paper and Folia, sneak-right-click a managed sign as owner and confirm native sign editor opens directly |
| Guided add-player chat flow returns to GUI cleanly after success and failure | GUI-03 | Real chat timing and reopen feel are hard to assert fully in mocks | Trigger add-player from GUI, enter a valid player then an invalid player, confirm messages and GUI reopen are both correct |

---

## Validation Sign-Off

- [x] All tasks have automated verify or Wave 0 dependencies
- [x] Sampling continuity: no 3 consecutive tasks without automated verify
- [x] Wave 0 covers all missing references
- [x] No watch-mode flags
- [x] Feedback latency < 60s
- [x] `nyquist_compliant: true` set in frontmatter

**Approval:** pending
