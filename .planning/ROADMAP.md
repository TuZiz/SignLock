# Roadmap: SignLock

## Milestones

- [x] **v1.0 Initial Release** - Phases 1-3 shipped on 2026-04-02. Archive: `.planning/milestones/v1.0-ROADMAP.md`
- [ ] **v1.1 Modern UX** - Phase 4 shipped on 2026-04-02; Phases 5-6 remain planned. Focus: GUI management, batch authorization, and clearer lock insight.

## Current Milestone

### Phase 4: GUI Lock Management
**Goal**: Give lock owners a stable, intuitive GUI management entry and summary panel without regressing v1.0 sign and command semantics.  
**Depends on**: v1.0 release baseline  
**Requirements**: [GUI-01, GUI-02, GUI-03]  
**Success Criteria**:
1. Owners can open a clear GUI entry without losing the existing `/bl` and sign-based workflow.
2. The GUI shows owner, authorized players, extension usage, and the current lock target summary.
3. Single-player add/remove actions stay synchronized with existing sign-backed lock data.
**Plans**: 3 plans

Plans:
- [x] 04-01-PLAN.md - foundation / test scaffolding / GUI summary model
- [x] 04-02-PLAN.md - entry integration / chest GUI rendering / click safety
- [x] 04-03-PLAN.md - single add/remove actions / chat input bridge / regression closeout

### Phase 5: Batch Authorization Flows
**Goal**: Upgrade authorization management from one-player-at-a-time flows to efficient batch operations.  
**Depends on**: Phase 4  
**Requirements**: [BATCH-01, BATCH-02, BATCH-03]  
**Success Criteria**:
1. Lock owners can add multiple players in one action.
2. Batch remove clearly distinguishes removed, missing, and protected targets.
3. GUI and command flows preserve the same ownership and authorization semantics.
**Plans**: 3 plans

### Phase 6: Visual Lock Insights & Safety
**Goal**: Make lock state easier to understand and complete the regression and safety coverage for the new UX paths.  
**Depends on**: Phase 5  
**Requirements**: [VIS-01, VIS-02, VIS-03, SAFE-01, SAFE-02, SAFE-03]  
**Success Criteria**:
1. Players can understand owner, authorization scope, and extension usage more easily.
2. Single chest, double chest, and managed-sign summaries remain aligned with shared-target semantics.
3. New interactions do not regress the v1.0 protection matrix or compatibility baseline.
**Plans**: 3 plans

## Progress

| Phase | Milestone | Plans Complete | Status | Completed |
|-------|-----------|----------------|--------|-----------|
| 1. Lock Target Correctness | v1.0 | 3/3 | Complete | 2026-04-02 |
| 2. Authorization & Protection Matrix | v1.0 | 3/3 | Complete | 2026-04-02 |
| 3. Localization & Release Readiness | v1.0 | 3/3 | Complete | 2026-04-02 |
| 4. GUI Lock Management | v1.1 | 3/3 | Complete | 2026-04-02 |
| 5. Batch Authorization Flows | v1.1 | 0/3 | Not started | - |
| 6. Visual Lock Insights & Safety | v1.1 | 0/3 | Not started | - |

---
*Roadmap updated: 2026-04-02 for v1.1 Modern UX*
