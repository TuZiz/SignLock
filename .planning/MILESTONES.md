# Milestones

## v1.1 Modern UX

**Shipped:** 2026-04-03  
**Scope:** 3 phases, 9 plans

### Delivered

- 给锁主人交付了稳定的 GUI 管理入口，并保留潜行右键的原生牌子编辑回退。
- 把授权管理升级成了 GUI 和命令共享的批量 add/remove 流程。
- 把锁摘要统一成一套共享语义，让 GUI 和 `/bl info` 都能说清 owner、scope、target 和扩展牌状态。
- 给已授权非主人新增只读摘要界面，同时保持 v1.0 的保护矩阵不回退。

### Notes

- milestone audit 没有在 closeout 前单独执行，本次归档基于各 phase verification 报告完成。
- `.planning/phases/` 仍保留在原位，作为原始执行历史；如需后续整理，可再走 `$gsd-cleanup`。

### Archives

- `.planning/milestones/v1.1-ROADMAP.md`
- `.planning/milestones/v1.1-REQUIREMENTS.md`
- `.planning/RETROSPECTIVE.md`

## v1.0 Initial Release

**Shipped:** 2026-04-02  
**Scope:** 3 phases, 9 plans, 12 tasks

### Delivered

- 统一了单箱、双箱与 managed sign 的 shared-target 锁语义。
- 补齐了访问、破坏、自动化和身份归一化的完整保护矩阵。
- 交付了可直接上线的中文默认配置、快速开始、手测矩阵与发布前检查。

### Notes

- milestone audit 在收尾时未单独执行，当前归档基于各 phase 的验证报告完成。
- `.planning/phases/` 保留在原位，作为原始执行历史。

### Archives

- `.planning/milestones/v1.0-ROADMAP.md`
- `.planning/milestones/v1.0-REQUIREMENTS.md`
- `.planning/RETROSPECTIVE.md`
