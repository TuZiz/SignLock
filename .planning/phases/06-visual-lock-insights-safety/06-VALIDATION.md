---
phase: 06
slug: visual-lock-insights-safety
status: ready
nyquist_compliant: true
wave_0_complete: false
created: 2026-04-03
---

# Phase 06 - Validation Strategy

> Per-phase validation contract for feedback sampling during execution.

---

## Test Infrastructure

| Property | Value |
|----------|-------|
| **Framework** | JUnit Jupiter 6.0.3 + MockBukkit 4.108.0 + Mockito 5.23.0 |
| **Config file** | `build.gradle` |
| **Quick run command** | `.\gradlew.bat test --tests "ym.signLock.service.LockServiceSummarySemanticsTest" --tests "ym.signLock.gui.LockManagementReadOnlyGuiTest" --tests "ym.signLock.command.SignLockCommandInfoCompatibilityTest"` |
| **Full suite command** | `.\gradlew.bat test` |
| **Estimated runtime** | ~75 seconds |

---

## Sampling Rate

- **After every task commit:** Run `.\gradlew.bat test --tests "ym.signLock.service.LockServiceSummarySemanticsTest" --tests "ym.signLock.gui.LockManagementReadOnlyGuiTest" --tests "ym.signLock.command.SignLockCommandInfoCompatibilityTest"`
- **After every plan wave:** Run `.\gradlew.bat test`
- **Before `$gsd-verify-work`:** Full suite must be green
- **Max feedback latency:** 75 seconds

---

## Per-Task Verification Map

| Task ID | Plan | Wave | Requirement | Test Type | Automated Command | File Exists | Status |
|---------|------|------|-------------|-----------|-------------------|-------------|--------|
| 06-01-01 | 01 | 1 | VIS-03, SAFE-03 | unit/integration | `.\gradlew.bat test --tests ym.signLock.service.LockServiceSummarySemanticsTest` | No - W0 | pending |
| 06-01-02 | 01 | 1 | VIS-01, VIS-02, VIS-03 | unit/integration | `.\gradlew.bat test --tests ym.signLock.gui.LockManagementGuiViewTest --tests ym.signLock.command.SignLockCommandAuthorizationTest` | Yes | pending |
| 06-02-01 | 02 | 2 | VIS-01, VIS-02, SAFE-01 | unit/integration | `.\gradlew.bat test --tests ym.signLock.gui.LockManagementReadOnlyGuiTest --tests ym.signLock.listener.LockListenerViewerSummaryEntryTest` | No - W0 | pending |
| 06-02-02 | 02 | 2 | VIS-02, SAFE-02 | unit/integration | `.\gradlew.bat test --tests ym.signLock.gui.LockGuiListenerClickSafetyTest --tests ym.signLock.listener.LockGuiChatInputTest` | Yes | pending |
| 06-03-01 | 03 | 3 | VIS-01, VIS-02, VIS-03, SAFE-01 | unit/integration | `.\gradlew.bat test --tests ym.signLock.command.SignLockCommandInfoCompatibilityTest --tests ym.signLock.command.SignLockCommandCompatibilityTest` | No - W0 | pending |
| 06-03-02 | 03 | 3 | SAFE-01, SAFE-02, SAFE-03 | integration/manual | `.\gradlew.bat test --tests ym.signLock.listener.LockListenerProtectionMatrixTest --tests ym.signLock.listener.LockListenerGuiEntryTest` | Yes | pending |

*Status: pending / green / red / flaky*

---

## Wave 0 Requirements

- [ ] `src/test/java/ym/signLock/service/LockServiceSummarySemanticsTest.java` - locks human-readable single chest / double chest / managed-sign summary semantics to canonical shared-target behavior
- [ ] `src/test/java/ym/signLock/gui/LockManagementReadOnlyGuiTest.java` - proves authorized viewers receive read-only summary UI with no management controls
- [ ] `src/test/java/ym/signLock/listener/LockListenerViewerSummaryEntryTest.java` - locks owner/manage vs authorized/view-only vs unauthorized/no-view entry routing
- [ ] `src/test/java/ym/signLock/command/SignLockCommandInfoCompatibilityTest.java` - proves `/bl info` shares the same summary language and target semantics as the GUI
- [ ] Existing `src/test/java/ym/signLock/listener/LockListenerProtectionMatrixTest.java` stays green after viewer/read-only UX changes
- [ ] Existing `src/test/java/ym/signLock/command/SignLockCommandCompatibilityTest.java` stays green after info-summary upgrades

---

## Manual-Only Verifications

| Behavior | Requirement | Why Manual | Test Instructions |
|----------|-------------|------------|-------------------|
| Authorized player read-only summary clarity | VIS-01, VIS-02 | MockBukkit cannot judge whether the distinction between “可访问” and “可管理” is obvious to real players | On Paper, have an authorized non-owner right-click a managed sign, confirm a summary opens, confirm there are no add/remove controls, and confirm the wording clearly states view-only access |
| Double chest target wording is human-readable and stable | VIS-03 | MockBukkit can assert data, but not whether the final wording is understandable in-client | On Paper, open summaries from both halves of the same double chest and from an extension sign, confirm all three render the same shared target wording |
| Folia/Paper main-thread safety for GUI open / refresh / chat return | SAFE-02 | Automated unit tests cannot fully simulate real scheduler behavior on live server variants | On Paper and Folia, open owner GUI, trigger refresh, trigger add prompt then cancel, and confirm no async warnings or GUI desync occurs |
| Phase 6 smoke closeout for new UX paths | SAFE-03 | Release-readiness still requires human signoff of end-to-end feel | Follow a written smoke pass covering owner manage GUI, authorized read-only GUI, `/bl info`, and unchanged unauthorized denial behavior |

---

## Validation Sign-Off

- [x] All tasks have automated verify or Wave 0 dependencies
- [x] Sampling continuity: no 3 consecutive tasks without automated verify
- [x] Wave 0 covers all missing references
- [x] No watch-mode flags
- [x] Feedback latency < 75s
- [x] `nyquist_compliant: true` set in frontmatter

**Approval:** pending
