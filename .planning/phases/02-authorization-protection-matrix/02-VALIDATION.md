---
phase: 02
slug: authorization-protection-matrix
status: draft
nyquist_compliant: true
wave_0_complete: false
created: 2026-04-02
---

# Phase 02 - Validation Strategy

> Per-phase validation contract for feedback sampling during execution.

---

## Test Infrastructure

| Property | Value |
|----------|-------|
| **Framework** | JUnit Jupiter 6.0.3 + MockBukkit 4.108.0 + Mockito JUnit Jupiter 5.23.0 |
| **Config file** | `build.gradle` |
| **Quick run command** | `.\gradlew.bat test --tests "ym.signLock.listener.*" --tests "ym.signLock.command.SignLockCommandAuthorizationTest" --tests "ym.signLock.service.*"` |
| **Full suite command** | `.\gradlew.bat test` |
| **Estimated runtime** | ~30-45 seconds |

---

## Sampling Rate

- **After every task commit:** Run the narrowest relevant suite for the touched task.
- **After every plan completion:** Run `.\gradlew.bat test --tests "ym.signLock.listener.*" --tests "ym.signLock.command.SignLockCommandAuthorizationTest" --tests "ym.signLock.service.*"`.
- **Before phase verification:** Run `.\gradlew.bat test`.
- **Max feedback latency:** Keep each verification loop under 1 minute.

---

## Per-Task Verification Map

| Task ID | Plan | Wave | Requirement | Test Type | Automated Command | File Exists | Status |
|---------|------|------|-------------|-----------|-------------------|-------------|--------|
| 02-01-01 | 01 | 1 | PROT-03, PROT-04 | integration | `.\gradlew.bat test --tests "ym.signLock.listener.LockListenerBreakProtectionTest" --tests "ym.signLock.listener.LockListenerProtectionMatrixTest"` | No - W0 | pending |
| 02-01-02 | 01 | 1 | OPS-02, OPS-03 | unit/integration | `.\gradlew.bat test --tests "ym.signLock.command.SignLockCommandAuthorizationTest" --tests "ym.signLock.service.PlayerIdentityServiceTest"` | No - W0 | pending |
| 02-02-01 | 02 | 2 | PROT-03 | integration | `.\gradlew.bat test --tests "ym.signLock.listener.LockListenerBreakProtectionTest"` | No - depends on 02-01 | pending |
| 02-02-02 | 02 | 2 | PROT-04 | integration | `.\gradlew.bat test --tests "ym.signLock.listener.LockListenerProtectionMatrixTest"` | No - depends on 02-01 | pending |
| 02-03-01 | 03 | 3 | OPS-02, OPS-03 | unit/integration | `.\gradlew.bat test --tests "ym.signLock.command.SignLockCommandAuthorizationTest" --tests "ym.signLock.service.PlayerIdentityServiceTest"` | No - depends on 02-01 | pending |
| 02-03-02 | 03 | 3 | PROT-03, PROT-04, OPS-02, OPS-03 | regression | `.\gradlew.bat test --tests "ym.signLock.listener.*" --tests "ym.signLock.command.SignLockCommandAuthorizationTest" --tests "ym.signLock.service.*"` | Yes - after 02-01 | pending |

*Status: pending / green / red / flaky*

---

## Wave 0 Requirements

- [ ] `src/test/java/ym/signLock/listener/LockListenerBreakProtectionTest.java` - PROT-03 break matrix for unauthorized / authorized / owner / admin
- [ ] `src/test/java/ym/signLock/listener/LockListenerProtectionMatrixTest.java` - PROT-04 matrix for single-container, shared-target, and managed-sign coverage
- [ ] `src/test/java/ym/signLock/command/SignLockCommandAuthorizationTest.java` - OPS-02 command outcome coverage for invalid target, no-space, rename/case inputs, and consistent managed-sign targeting
- [ ] `src/test/java/ym/signLock/service/PlayerIdentityServiceTest.java` - OPS-03 cache-known, cache-unknown, preload, remember, and rename convergence coverage

---

## Manual-Only Verifications

| Behavior | Requirement | Why Manual | Test Instructions |
|----------|-------------|------------|-------------------|
| 活塞目的地副作用是否能在真实服务端稳定复现并影响锁牌附着面 | PROT-04 | MockBukkit 能锁住主事件路径，但目的地碰撞的真实世界交互更适合最终烟测确认 | 在本地 Paper 测试服构造活塞推动相邻方块、附着锁牌/扩展牌的场景，确认不会通过目的地副作用拆掉锁结构 |
| 改名后老授权数据在真实上下线流程中的一致性 | OPS-03 | 自动化可覆盖缓存模型，但真实玩家上下线与缓存持久化最好再做一次冒烟 | 使用测试账号完成“添加授权 -> 更新缓存/改名 -> remove/info 查询”流程，确认结果一致 |

---

## Validation Sign-Off

- [ ] All tasks have automated verify or explicit manual-only rationale
- [ ] Wave 0 covers all missing regression suites before risky behavior changes
- [ ] No parallel wave writes overlap on the same production file set
- [ ] Single-container, shared-target, and managed-sign samples are all explicit in PROT-04 coverage
- [ ] Feedback latency remains under 1 minute per loop
- [ ] `nyquist_compliant: true` remains valid after plan revisions

**Approval:** pending
