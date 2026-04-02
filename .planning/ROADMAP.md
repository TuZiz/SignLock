# Roadmap: SignLock

## Milestones

- [x] **v1.0 Initial Release** - Phases 1-3 shipped on 2026-04-02. Archive: `.planning/milestones/v1.0-ROADMAP.md`
- [ ] **v1.1 Modern UX** - Phases 4-6 planned. Focus: GUI 管理、批量授权、锁信息可视化。

## Current Milestone

### Phase 4: GUI Lock Management
**Goal**: 为锁主人提供稳定、直观的 GUI 管理入口与锁摘要面板。  
**Depends on**: v1.0 release baseline  
**Requirements**: [GUI-01, GUI-02, GUI-03]  
**Success Criteria**:
1. 锁主人可以通过清晰入口打开 GUI，而不影响现有 `/bl` 和牌子工作流。
2. GUI 能展示所有者、授权玩家和扩展牌数量。
3. GUI 中的单人授权 / 移除会正确同步到现有锁数据。
**Plans**: 3 plans

### Phase 5: Batch Authorization Flows
**Goal**: 把单次只处理 1 名玩家的授权流升级为高效、明确的批量操作。  
**Depends on**: Phase 4  
**Requirements**: [BATCH-01, BATCH-02, BATCH-03]  
**Success Criteria**:
1. 锁主人可以一次添加多个玩家。
2. 批量移除会正确区分成功、未找到和不可移除结果。
3. GUI 路径与命令路径保持一致的所有权和授权语义。
**Plans**: 3 plans

### Phase 6: Visual Lock Insights & Safety
**Goal**: 让锁状态展示更直观，并补齐新交互的回归与兼容性验证。  
**Depends on**: Phase 5  
**Requirements**: [VIS-01, VIS-02, VIS-03, SAFE-01, SAFE-02, SAFE-03]  
**Success Criteria**:
1. 玩家能更容易理解当前锁的所有者、授权范围和扩展牌使用情况。
2. 单箱、双箱和 managed sign 的信息展示都遵守 shared-target 语义。
3. 新交互不会回退 v1.0 的保护矩阵与兼容性。
**Plans**: 3 plans

## Progress

| Phase | Milestone | Plans Complete | Status | Completed |
|-------|-----------|----------------|--------|-----------|
| 1. Lock Target Correctness | v1.0 | 3/3 | Complete | 2026-04-02 |
| 2. Authorization & Protection Matrix | v1.0 | 3/3 | Complete | 2026-04-02 |
| 3. Localization & Release Readiness | v1.0 | 3/3 | Complete | 2026-04-02 |
| 4. GUI Lock Management | v1.1 | 0/3 | Not started | - |
| 5. Batch Authorization Flows | v1.1 | 0/3 | Not started | - |
| 6. Visual Lock Insights & Safety | v1.1 | 0/3 | Not started | - |

---
*Roadmap updated: 2026-04-02 for v1.1 Modern UX*
