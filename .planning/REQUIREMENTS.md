# Requirements: SignLock v1.1 Modern UX

**Defined:** 2026-04-02
**Core Value:** 在保留 v1.0 稳定锁语义的前提下，让锁管理更直观、更高效、更现代。

## v1.1 Requirements

### GUI Management

- [ ] **GUI-01**: 锁主人可以通过稳定入口打开锁管理界面，而不必只依赖命令和手改牌子。
- [ ] **GUI-02**: 锁管理界面能显示所有者、授权玩家、扩展牌数量和当前锁目标摘要。
- [ ] **GUI-03**: 锁主人可以在 GUI 中执行单人授权与移除，而不破坏原有牌子数据格式。

### Batch Authorization

- [ ] **BATCH-01**: 锁主人可以一次添加多个玩家到同一把锁。
- [ ] **BATCH-02**: 锁主人可以一次移除多个玩家，且结果能区分成功、已不存在和不可移除。
- [ ] **BATCH-03**: 批量授权能力在 GUI 与命令路径上保持一致语义，不能绕过所有权检查。

### Visual Lock Info

- [ ] **VIS-01**: 玩家可以更直观地查看锁的所有者、授权列表和扩展牌使用情况。
- [ ] **VIS-02**: GUI 或信息展示需要明确区分“可访问”和“可管理”边界，避免误解权限。
- [ ] **VIS-03**: 锁信息展示要兼容单箱、双箱与受管理牌子的 shared-target 语义。

### Compatibility & Safety

- [ ] **SAFE-01**: v1.0 的牌子创建、扩展牌放置、命令授权和保护矩阵行为不能回退。
- [ ] **SAFE-02**: 新交互不能破坏 Spigot / Paper / Folia 兼容声明和主线程安全边界。
- [ ] **SAFE-03**: 仓库要补充对应的测试和手测更新，覆盖新 GUI / 批量授权主路径。

## Future Requirements

- 审计日志与管理面板
- 更完整的搜索 / 过滤式授权管理
- 多语言切换而非默认中文优化

## Out of Scope

| Feature | Reason |
|---------|--------|
| Web 面板或远程管理 | 超出本地插件管理体验升级的范围 |
| 跨服同步或数据库化锁数据 | 当前里程碑聚焦本地 UX 升级 |
| 经济、组权限、职业联动 | 会显著放大规则复杂度 |

## Traceability

| Requirement | Phase | Status |
|-------------|-------|--------|
| GUI-01 | Phase 4 | Pending |
| GUI-02 | Phase 4 | Pending |
| GUI-03 | Phase 4 | Pending |
| BATCH-01 | Phase 5 | Pending |
| BATCH-02 | Phase 5 | Pending |
| BATCH-03 | Phase 5 | Pending |
| VIS-01 | Phase 6 | Pending |
| VIS-02 | Phase 6 | Pending |
| VIS-03 | Phase 6 | Pending |
| SAFE-01 | Phase 6 | Pending |
| SAFE-02 | Phase 6 | Pending |
| SAFE-03 | Phase 6 | Pending |

---
*Requirements defined: 2026-04-02 for v1.1 Modern UX*
