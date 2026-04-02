# Phase 3 Research: Localization & Release Readiness

**Date:** 2026-04-02
**Status:** Complete

## What Was Reviewed

- `.planning/REQUIREMENTS.md`
- `.planning/ROADMAP.md`
- `.planning/STATE.md`
- `.planning/phases/03-localization-release-readiness/03-CONTEXT.md`
- `src/main/resources/plugin.yml`
- `src/main/resources/config.yml`
- `src/main/java/ym/signLock/SignLock.java`
- `src/main/java/ym/signLock/config/SignLockConfig.java`
- `TESTING.md`

## Key Findings

### 1. Default release-facing resources are still a real blocker

`plugin.yml` and `config.yml` both contain garbled Chinese text. This is not just cosmetic:
- command descriptions and permission descriptions are unreadable to operators
- default runtime messages are not acceptable as a ship-ready Chinese experience
- the current defaults do not satisfy `REL-01`

### 2. Phase 3 should modernize delivery quality, not product scope

The user's "more modern" request fits cleanly into:
- shorter and clearer Chinese copy
- more readable config comments and operator guidance
- more explicit reload / info / startup feedback
- stronger release docs

It does **not** require:
- GUI lock management
- batch authorization flows
- new interaction systems
- new data storage or integration work

### 3. Existing implementation already gives us the right seams

The release-facing work can stay localized around:
- `plugin.yml` metadata
- `config.yml` defaults and comments
- `SignLock` startup / reload paths
- `SignLockConfig` message loading
- `TESTING.md` and related release docs

That means Phase 3 can remain low-risk and avoid touching the verified lock/protection semantics from phases 1 and 2.

### 4. Release docs need to be stronger than a bare smoke list

The repo already has a manual checklist, but it is not yet a polished operator-facing deliverable.
To satisfy the agreed Phase 3 direction, documentation should include:
- a quick start path
- a smoke / manual verification matrix
- a release-before-ship checklist

### 5. Final verification must explicitly preserve compatibility declarations

`plugin.yml` currently declares `folia-supported: true`.
Phase 3 must verify:
- the project still builds
- metadata remains intact
- the release docs and defaults match the actual shipped behavior

## Planning Implications

### Recommended plan split

1. **03-01:** Repair garbled Chinese resources, plugin metadata, and operator-facing messages.
2. **03-02:** Produce ship-ready operator docs: quick start, smoke matrix, and release checklist.
3. **03-03:** Run final build and compatibility verification; refresh state and verification artifacts.

### Recommended test / verification emphasis

- build success on current target environment
- resource files are UTF-8 Chinese and directly readable
- reload/info/operator feedback aligns with actual commands
- Folia compatibility declaration remains present
- release docs mention the real tested flows, not aspirational ones

## Risks To Avoid

- scope creep into new UX systems
- changing config keys or lock semantics in the name of "modernization"
- rewriting docs in a way that diverges from the real plugin behavior
- cleaning up `plugin.yml` while accidentally breaking command or permission metadata

---

*Phase: 03-localization-release-readiness*
