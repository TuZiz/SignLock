# SignLock Agent Guide

This repository is initialized for the GSD workflow.

## Planning Artifacts

- Primary project context: `.planning/PROJECT.md`
- Requirements: `.planning/REQUIREMENTS.md`
- Roadmap: `.planning/ROADMAP.md`
- State: `.planning/STATE.md`
- Generated workflow guide: `CLAUDE.md`

## Working Rules

- Prefer entering work through a GSD command so planning context stays in sync.
- Use `gsd-quick` for small fixes, `gsd-debug` for bug investigations, and `gsd-execute-phase` for planned phase work.
- Keep behavior compatible with Spigot / Paper / Folia and do not break existing sign lock data.
- Treat Chinese-readable defaults and stable lock semantics as release-blocking quality bars.
