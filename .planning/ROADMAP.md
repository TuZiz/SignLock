# Roadmap: SignLock

## Overview

This roadmap moves SignLock from an already-functional sign lock plugin to a release-ready plugin for Chinese Minecraft servers. The priority is not feature expansion, but locking down shared-target semantics, authorization boundaries, resource quality, and repeatable validation.

## Phases

**Phase Numbering:**
- Integer phases (1, 2, 3): Planned milestone work
- Decimal phases (2.1, 2.2): Urgent insertions (marked with INSERTED)

Decimal phases appear between their surrounding integers in numeric order.

- [x] **Phase 1: Lock Target Correctness** - Fix canonical lock ownership and shared-target behavior for single containers, double chests, and managed signs.
- [x] **Phase 2: Authorization & Protection Matrix** - Close authorization, break protection, automation protection, and identity-resolution gaps so all core entrypoints behave consistently.
- [ ] **Phase 3: Localization & Release Readiness** - Repair default Chinese resources and establish the final release and smoke-validation baseline.

## Phase Details

### Phase 1: Lock Target Correctness
**Goal**: Make main signs, extension signs, single containers, double chests, and container access all resolve to the same protected target across every entrypoint.
**Depends on**: Nothing (first phase)
**Requirements**: [LOCK-01, LOCK-02, LOCK-03, LOCK-04, PROT-01, PROT-02]
**Success Criteria** (what must be TRUE):
  1. Creating a lock on a single or double chest protects the same underlying target regardless of which half was clicked.
  2. Unauthorized players are blocked consistently from either side of a shared target.
  3. Owners can continue extending authorization via managed signs without half-block ambiguity.
  4. Editing lock signs preserves system-controlled structure and owner data.
**Plans**: 3 plans

Plans:
- [x] 01-01-PLAN.md - Install the MockBukkit regression base and centralize canonical target semantics in `LockService`.
- [x] 01-02-PLAN.md - Make `LockListener` reuse shared-target decisions for interact, open, and automation entrypoints.
- [x] 01-03-PLAN.md - Close lock sign placement and sign edit protections around main and extension signs.

### Phase 2: Authorization & Protection Matrix
**Goal**: Cover authorization management, break protection, automation protection, and identity resolution across the full protection matrix.
**Depends on**: Phase 1
**Requirements**: [PROT-03, PROT-04, OPS-02, OPS-03]
**Success Criteria** (what must be TRUE):
  1. Unauthorized players cannot break protected block bodies or other players' managed signs.
  2. Automation, explosions, pistons, and fluids cannot bypass protected-target semantics.
  3. `/signlock add` and `/signlock remove` return correct outcomes for exhausted extension space, invalid targets, and identity-resolution cases.
  4. Owner, authorized player, and admin-bypass boundaries are consistent across command and listener paths.
**Plans**: 3 plans

Plans:
- [x] 02-01 - Add regression-first suites for listener protection, command authorization, and identity-cache behavior.
- [x] 02-02 - Repair listener-side break and protection-matrix semantics through `LockService`.
- [x] 02-03 - Close command identity normalization gaps and rerun full Phase 2 plus Phase 1 shared-target regressions.

### Phase 3: Localization & Release Readiness
**Goal**: Make default resources, plugin metadata, and release verification ready for delivery.
**Depends on**: Phase 2
**Requirements**: [OPS-01, REL-01, REL-02, REL-03]
**Success Criteria** (what must be TRUE):
  1. Default `config.yml` and `plugin.yml` are directly readable and usable in UTF-8 Chinese environments.
  2. Operators can reload configuration and receive correct Chinese messages without patching defaults.
  3. The repository includes a minimal but executable smoke and release checklist.
  4. The project builds successfully for the target Java / Spigot environment while preserving Folia compatibility declarations.
**Plans**: 3 plans

Plans:
- [ ] 03-01 - Repair garbled Chinese text in resources and metadata.
- [ ] 03-02 - Produce release-checklist and validation-matrix documentation.
- [ ] 03-03 - Run final build and compatibility verification before release.

## Progress

**Execution Order:**
Phases execute in numeric order: 1 -> 2 -> 3

| Phase | Plans Complete | Status | Completed |
|-------|----------------|--------|-----------|
| 1. Lock Target Correctness | 3/3 | Complete | 2026-04-02 |
| 2. Authorization & Protection Matrix | 3/3 | Complete | 2026-04-02 |
| 3. Localization & Release Readiness | 0/3 | Not started | - |
