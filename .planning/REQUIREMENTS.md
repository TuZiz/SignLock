# Requirements: SignLock

**Defined:** 2026-04-02
**Core Value:** 玩家贴上牌子后，受保护方块必须稳定、可预期地只允许主人和已授权玩家访问。

## v1 Requirements

### Lock Semantics

- [x] **LOCK-01**: 玩家可以在支持的单箱、双箱、木桶和潜影盒上创建主锁牌，且主锁牌绑定到正确的受保护目标。
- [x] **LOCK-02**: 玩家可以在已有主锁的目标上继续添加扩展授权牌，且只有锁主人或管理员绕过用户可以执行该操作。
- [x] **LOCK-03**: 双箱的两个半边必须被视为同一把锁的共享目标，任一半边上的锁牌与授权都对整个双箱生效。
- [x] **LOCK-04**: 锁牌编辑时必须保留系统控制的结构行，避免主人名、锁头和扩展牌格式被误改坏。

### Protection

- [x] **PROT-01**: 未授权玩家尝试右键使用已上锁方块时会被稳定拦截并收到正确提示。
- [x] **PROT-02**: 未授权玩家尝试打开已上锁容器时会被稳定拦截，包括双箱和其他 `InventoryHolder` 路径。
- [x] **PROT-03**: 未授权玩家不能破坏受保护方块本体或别人的锁牌。
- [x] **PROT-04**: 自动化搬运、爆炸、活塞和流体不能绕过现有锁保护破坏或操作锁目标。

### Operations

- [ ] **OPS-01**: 服主可以通过 `/signlock reload` 重载配置，通过 `/signlock info` 查看锁的主人、授权列表和扩展牌数量。
- [x] **OPS-02**: 锁主人可以通过 `/signlock add <玩家>` 和 `/signlock remove <玩家>` 管理授权名单，并在空间不足或目标无效时得到准确信息。
- [x] **OPS-03**: 玩家身份缓存与名字解析在插件启停和授权操作后保持一致，不因离线玩家或大小写差异破坏授权判断。

### Localization & Release

- [ ] **REL-01**: 默认 `config.yml`、`plugin.yml` 和运行时提示文本对中文服务器保持可读，不出现乱码默认值。
- [ ] **REL-02**: 项目提供最小可执行的手工验证清单，覆盖主锁创建、扩展授权、容器访问、破坏保护、自动化保护和配置重载。
- [ ] **REL-03**: 插件在当前 Spigot/Paper/Folia 目标环境下可以成功构建，并保留兼容声明与基础运行能力。

## v2 Requirements

### Integrations

- **INT-01**: 与领地、权限或经济插件做深度联动。
- **INT-02**: 提供数据库或跨服同步的锁数据持久化方案。

### UX Expansion

- **UX-01**: 提供 GUI 化锁管理界面。
- **UX-02**: 提供更复杂的批量授权、模板锁或审计日志能力。

## Out of Scope

| Feature | Reason |
|---------|--------|
| Web 面板或远程管理 | 超出轻量服务器插件的核心目标 |
| 锁权限按组/职业/经济等级动态计算 | 会显著增加规则复杂度，当前优先级低 |
| 兼容所有第三方自动化插件的专用桥接 | 先确保原生 Bukkit 事件路径正确 |

## Traceability

| Requirement | Phase | Status |
|-------------|-------|--------|
| LOCK-01 | Phase 1 | Complete |
| LOCK-02 | Phase 1 | Complete |
| LOCK-03 | Phase 1 | Complete |
| LOCK-04 | Phase 1 | Complete |
| PROT-01 | Phase 1 | Complete |
| PROT-02 | Phase 1 | Complete |
| PROT-03 | Phase 2 | Complete |
| PROT-04 | Phase 2 | Complete |
| OPS-01 | Phase 3 | Complete |
| OPS-02 | Phase 2 | Complete |
| OPS-03 | Phase 2 | Complete |
| REL-01 | Phase 3 | Complete |
| REL-02 | Phase 3 | Complete |
| REL-03 | Phase 3 | Complete |

**Coverage:**
- v1 requirements: 14 total
- Mapped to phases: 14
- Unmapped: 0

---
*Requirements defined: 2026-04-02*
*Last updated: 2026-04-02 after phase 3 completion*
