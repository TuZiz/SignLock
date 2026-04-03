# SignLock

## What This Is

SignLock 是一个面向 Spigot / Paper / Folia 的牌子锁插件。它以“保留原版交互习惯”为前提，为箱子、木桶、门、熔炉、潜影盒等目标提供 owner / authorized player 的访问控制，同时逐步加入更现代的 GUI 管理和锁信息展示体验。

## Core Value

稳定、可预期地保护共享容器与交互目标，同时让服主和玩家都能看懂锁的状态。

## Current State

- v1.0 `Initial Release` 已于 2026-04-02 归档完成。
- v1.1 `Modern UX` 已于 2026-04-03 交付，完成了 GUI 管理、批量授权和 viewer-aware 锁摘要。
- 代码库当前已具备 shared-target 保护、批量授权、GUI 管理、只读摘要和 `/bl info` parity 的完整基线。
- 发布侧已经具备可读的 `config.yml`、`TESTING.md` 和 phase 级 verification 报告。

## Validated

- [x] shared-target 语义在单箱、双箱和 managed sign 上保持一致
- [x] 锁主人可通过 GUI、牌子和命令稳定管理授权
- [x] 批量 add/remove 在 GUI 与命令路径上语义一致
- [x] 锁摘要能够明确表达 owner、viewer scope、target 与扩展牌状态
- [x] v1.0 保护矩阵在 v1.1 现代化升级后没有回退

## Active

- [ ] 设计下一里程碑的目标范围
- [ ] 决定是否引入更深的管理能力，例如审计、搜索、分页或后台面板
- [ ] 视发布需要补做或豁免 Paper / Folia 的真人 smoke checklist

## Out of Scope

- 跨服同步或数据库化锁数据
- Web 面板或远程管理后台
- 基于经济、职业、组权限的复杂动态锁规则

## Context

- 技术栈：Java 21、Gradle Wrapper、Spigot API 1.21.11
- 兼容性：保持 `folia-supported: true`
- 测试基线：MockBukkit + listener / command / service / gui 回归测试
- 当前代码库属于 brownfield 项目，应继续优先走“稳定增量演进”而非重写

## Key Decisions

| Decision | Rationale | Outcome |
|----------|-----------|---------|
| 以 brownfield 稳定化为主，而不是重写 | 现有插件已有可用骨架，重写成本高且风险大 | Good |
| 双箱两半统一视为同一 shared target | 避免保护判定因点击半边不同而漂移 | Good |
| owner 普通右键进 GUI，潜行右键保留原生编辑 | 在现代化体验和老习惯之间保持兼容 | Good |
| GUI、批量命令和 `/bl info` 共用一套摘要语义 | 避免权限与 target 说明在不同入口分叉 | Good |

## Next Milestone Goals

- 明确下一里程碑是否继续沿着“更现代的管理体验”推进
- 如果继续现代化，优先考虑审计能力、搜索筛选和更清晰的管理工作流
- 如果转向稳定性，则优先补 milestone audit、真人 smoke 与 release 流程固化

## Release Status

- v1.0 archive: `.planning/milestones/v1.0-ROADMAP.md`
- v1.1 archive: `.planning/milestones/v1.1-ROADMAP.md`
- milestone summary: `.planning/MILESTONES.md`

---
*Last updated: 2026-04-03 after completing v1.1 milestone*
