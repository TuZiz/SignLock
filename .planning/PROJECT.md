# SignLock

## What This Is

SignLock 是一个面向 Spigot / Paper / Folia 的轻量牌子锁插件。它使用牌子为箱子、木桶、门、熔炉、潜影盒等目标提供所有权与授权访问控制，重点是原版式交互、稳定保护和中文服可直接落地。

## Core Value

稳定、可预期地只允许锁主人与已授权玩家访问受保护目标。

## Current State

- v1.0 `Initial Release` 已于 2026-04-02 归档
- 共享容器锁语义、授权矩阵、自动化保护和中文默认资源都已达成交付标准
- 仓库已具备快速开始、手测矩阵、发布前检查和 phase 级验证报告

## Current Milestone: v1.1 Modern UX

**Goal:** 在不破坏 v1.0 锁语义的前提下，把牌子锁的管理体验做得更现代、更高效、更直观。

**Target features:**
- GUI 锁管理入口与锁信息面板
- 批量授权 / 批量移除能力
- 更可视化的锁状态与授权信息展示

## Requirements

### Validated

- [x] 共享容器与双箱锁目标在所有入口保持一致 - v1.0
- [x] 主人可通过命令和扩展牌管理授权名单 - v1.0
- [x] 未授权访问、破坏与自动化绕过都会被拦截 - v1.0
- [x] 中文默认配置、重载反馈和发布文档可直接交付 - v1.0

### Active

- [ ] 落地 GUI 锁管理
- [ ] 落地批量授权 / 批量移除
- [ ] 落地可视化锁信息展示
- [ ] 如需补保险，补做 milestone audit / cross-phase UAT

### Out of Scope

- 跨服同步或数据库化锁数据
- Web 面板或远程管理
- 基于经济、职业、组权限的复杂动态锁规则

## Context

- 技术栈：Java 21、Gradle Wrapper、Spigot API 1.21.11
- 兼容性：保留 `folia-supported: true`
- 测试基线：MockBukkit 服务层 / 监听器回归 + `./gradlew.bat test`
- 当前代码库已经是 brownfield 项目，应继续优先做稳定化与增量演进

## Key Decisions

| Decision | Rationale | Outcome |
|----------|-----------|---------|
| 以 brownfield 稳定化为主，而不是重写 | 现有插件已有可用骨架，重写成本高且风险大 | Good |
| 双箱两半统一视为同一锁目标 | 避免 shared-target 保护漂移 | Good |
| 授权矩阵明确区分访问、管理、破坏 | 让命令与监听路径行为一致 | Good |
| v1.0 的“现代化”只做文案、配置和交付体验 | 避免发布收尾阶段 scope creep | Good |

## Next Milestone Goals

- 先把 GUI、批量授权、锁信息可视化做成正式能力
- 保持 v1.0 的牌子与命令流仍可继续使用
- 为后续更完整的审计 / 管理面板打基础

## Release Status

- v1.0 archive: `.planning/milestones/v1.0-ROADMAP.md`
- v1.0 requirements: `.planning/milestones/v1.0-REQUIREMENTS.md`
- milestone summary: `.planning/MILESTONES.md`

---
*Last updated: 2026-04-02 after starting v1.1 milestone*
