---
phase: 01
slug: lock-target-correctness
status: draft
nyquist_compliant: false
wave_0_complete: false
created: 2026-04-02
---

# Phase 01 - Validation Strategy

> Per-phase validation contract for feedback sampling during execution.

---

## Test Infrastructure

| Property | Value |
|----------|-------|
| **Framework** | JUnit Jupiter 6.0.3 + MockBukkit 4.108.0 + Mockito JUnit Jupiter 5.23.0 |
| **Config file** | none - Wave 0 installs |
| **Quick run command** | `./gradlew.bat test --tests "ym.signLock.*"` |
| **Full suite command** | `./gradlew.bat test build` |
| **Estimated runtime** | ~30 seconds |

---

## Sampling Rate

- **After every task commit:** Run `./gradlew.bat test --tests "ym.signLock.*"`
- **After every plan wave:** Run `./gradlew.bat test build`
- **Before `$gsd-verify-work`:** Full suite must be green
- **Max feedback latency:** 30 seconds

---

## Per-Task Verification Map

| Task ID | Plan | Wave | Requirement | Test Type | Automated Command | File Exists | Status |
|---------|------|------|-------------|-----------|-------------------|-------------|--------|
| 01-01-01 | 01 | 0 | LOCK-01, LOCK-03 | unit | `./gradlew.bat test --tests "ym.signLock.service.LockServiceCanonicalTargetTest"` | No - W0 | pending |
| 01-01-02 | 01 | 0 | LOCK-02 | integration | `./gradlew.bat test --tests "ym.signLock.service.LockServiceExtensionTest"` | No - W0 | pending |
| 01-02-01 | 02 | 1 | PROT-01 | integration | `./gradlew.bat test --tests "ym.signLock.listener.LockListenerInteractTest"` | No - W0 | pending |
| 01-02-02 | 02 | 1 | PROT-02 | integration | `./gradlew.bat test --tests "ym.signLock.listener.LockListenerInventoryOpenTest"` | No - W0 | pending |
| 01-03-01 | 03 | 2 | LOCK-04 | integration | `./gradlew.bat test --tests "ym.signLock.listener.LockListenerSignEditTest"` | No - W0 | pending |
| 01-03-02 | 03 | 2 | LOCK-01, LOCK-02 | integration | `./gradlew.bat test --tests "ym.signLock.listener.LockListenerPlacementTest"` | No - W0 | pending |

*Status: pending / green / red / flaky*

---

## Wave 0 Requirements

- [ ] `src/test/java/ym/signLock/service/LockServiceCanonicalTargetTest.java` - canonical target stubs for LOCK-01 / LOCK-03
- [ ] `src/test/java/ym/signLock/service/LockServiceExtensionTest.java` - extension sign coverage for LOCK-02
- [ ] `src/test/java/ym/signLock/listener/LockListenerPlacementTest.java` - placement flow coverage for LOCK-01
- [ ] `src/test/java/ym/signLock/listener/LockListenerInteractTest.java` - right-click denial coverage for PROT-01
- [ ] `src/test/java/ym/signLock/listener/LockListenerInventoryOpenTest.java` - inventory-open denial coverage for PROT-02
- [ ] `src/test/java/ym/signLock/listener/LockListenerSignEditTest.java` - managed-sign structure preservation coverage for LOCK-04
- [ ] `build.gradle` test dependencies + `useJUnitPlatform()` bootstrap

---

## Manual-Only Verifications

| Behavior | Requirement | Why Manual | Test Instructions |
|----------|-------------|------------|-------------------|
| 双箱两半在真实服务端交互下共享同一主锁语义 | LOCK-03 | 需要真实客户端交互验证主手/副手和容器 UI 体验 | 启动本地 Paper 测试服，给双箱任一半边贴主锁，再分别从两半右键和开箱验证均被同一授权控制 |
| 主人右键锁牌进入编辑后结构行不会被破坏 | LOCK-04 | `Player#openSign` 相关体验在 MockBukkit 覆盖有限 | 本地服中右键已有主锁和扩展牌，修改名单行并确认头部/主人行自动恢复 |

---

## Validation Sign-Off

- [ ] All tasks have automated verify or Wave 0 dependencies
- [ ] Sampling continuity: no 3 consecutive tasks without automated verify
- [ ] Wave 0 covers all missing references
- [ ] No watch-mode flags
- [ ] Feedback latency < 30s
- [ ] `nyquist_compliant: true` set in frontmatter

**Approval:** pending
