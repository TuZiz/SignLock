---
phase: 05
slug: batch-authorization-flows
status: ready
nyquist_compliant: true
wave_0_complete: false
created: 2026-04-02
---

# Phase 05 - Validation Strategy

> Per-phase validation contract for feedback sampling during execution.

---

## Test Infrastructure

| Property | Value |
|----------|-------|
| **Framework** | JUnit Jupiter 6.0.3 + MockBukkit 4.108.0 + Mockito 5.23.0 |
| **Config file** | `build.gradle` |
| **Quick run command** | `.\gradlew.bat test --tests "ym.signLock.service.LockBatchAuthorizationServiceTest" --tests "ym.signLock.gui.LockManagementBatchGuiTest" --tests "ym.signLock.command.SignLockCommandBatchCompatibilityTest"` |
| **Full suite command** | `.\gradlew.bat test` |
| **Estimated runtime** | ~60 seconds |

---

## Sampling Rate

- **After every task commit:** Run `.\gradlew.bat test --tests "ym.signLock.service.LockBatchAuthorizationServiceTest" --tests "ym.signLock.gui.LockManagementBatchGuiTest" --tests "ym.signLock.command.SignLockCommandBatchCompatibilityTest"`
- **After every plan wave:** Run `.\gradlew.bat test`
- **Before `$gsd-verify-work`:** Full suite must be green
- **Max feedback latency:** 60 seconds

---

## Per-Task Verification Map

| Task ID | Plan | Wave | Requirement | Test Type | Automated Command | File Exists | Status |
|---------|------|------|-------------|-----------|-------------------|-------------|--------|
| 05-01-01 | 01 | 1 | BATCH-01, BATCH-02, BATCH-03 | unit/integration | `.\gradlew.bat test --tests ym.signLock.service.LockBatchAuthorizationServiceTest` | No - W0 | pending |
| 05-01-02 | 01 | 1 | BATCH-01, BATCH-03 | unit | `.\gradlew.bat test --tests ym.signLock.service.LockBatchTargetParserTest` | No - W0 | pending |
| 05-02-01 | 02 | 2 | BATCH-01, BATCH-02 | unit/integration | `.\gradlew.bat test --tests ym.signLock.gui.LockManagementBatchGuiTest` | No - W0 | pending |
| 05-02-02 | 02 | 2 | BATCH-01, BATCH-03 | unit/integration | `.\gradlew.bat test --tests ym.signLock.listener.LockGuiBatchChatInputTest` | No - W0 | pending |
| 05-03-01 | 03 | 3 | BATCH-01, BATCH-02, BATCH-03 | unit/integration | `.\gradlew.bat test --tests ym.signLock.command.SignLockCommandBatchCompatibilityTest` | No - W0 | pending |
| 05-03-02 | 03 | 3 | BATCH-03 | unit/integration | `.\gradlew.bat test --tests ym.signLock.command.SignLockCommandCompatibilityTest` | Yes | pending |

*Status: pending / green / red / flaky*

---

## Wave 0 Requirements

- [ ] `src/test/java/ym/signLock/service/LockBatchAuthorizationServiceTest.java` - batch add/remove orchestrator reuses `LockService` semantics and supports partial success summaries
- [ ] `src/test/java/ym/signLock/service/LockBatchTargetParserTest.java` - parser accepts single-target legacy input plus comma/space separated multi-target input and de-duplicates safely
- [ ] `src/test/java/ym/signLock/gui/LockManagementBatchGuiTest.java` - GUI supports multi-name add input, multi-select remove, and aggregated feedback without leaving the single-page model
- [ ] `src/test/java/ym/signLock/listener/LockGuiBatchChatInputTest.java` - batch add chat capture stays async-safe and hands writeback back to sync scheduling
- [ ] `src/test/java/ym/signLock/command/SignLockCommandBatchCompatibilityTest.java` - `/bl add` and `/bl remove` accept multiple targets while preserving single-target compatibility
- [ ] `src/test/java/ym/signLock/command/SignLockCommandCompatibilityTest.java` - pre-Phase-5 legacy command coverage stays green after batch enhancements

---

## Manual-Only Verifications

| Behavior | Requirement | Why Manual | Test Instructions |
|----------|-------------|------------|-------------------|
| GUI multiple-selection readability and accidental-delete risk | BATCH-02 | MockBukkit cannot validate real client feel | Open the lock GUI on Paper, select several players, confirm the selected-state styling is obvious and remove only happens after confirmation |
| Batch add text input discoverability and Chinese copy quality | BATCH-01 | MockBukkit cannot validate instruction clarity | Trigger batch add from GUI, enter `Alice Bob,Charlie`, confirm the prompt and result copy are understandable |
| Aggregated batch feedback remains concise and useful | BATCH-01, BATCH-02, BATCH-03 | Hard to judge UX quality from pure assertions | Exercise mixed-result cases and confirm messages summarize success / skipped / denied counts clearly |

---

## Validation Sign-Off

- [x] All tasks have automated verify or Wave 0 dependencies
- [x] Sampling continuity: no 3 consecutive tasks without automated verify
- [x] Wave 0 covers all missing references
- [x] No watch-mode flags
- [x] Feedback latency < 60s
- [x] `nyquist_compliant: true` set in frontmatter

**Approval:** pending
