# Phase 3: Localization & Release Readiness - Context

**Gathered:** 2026-04-02
**Status:** Ready for planning

<domain>
## Phase Boundary

本阶段只收口默认中文资源、插件元数据、运维提示与发布验证基线，让 SignLock 达到可直接交付的发布质量。
它不引入 GUI、批量授权、菜单交互、审计面板或新的锁类型；这些“更现代化的功能扩展”只作为后续 backlog 候选，不并入本阶段。
</domain>

<decisions>
## Implementation Decisions

### 中文资源与文案风格
- **D-01:** 默认中文文案采用“更现代、短句、少废话”的风格，优先保证服主和玩家一眼看懂，而不是保留冗长或偏旧式的提示语。
- **D-02:** `plugin.yml`、`config.yml`、默认消息与手测文档都以 UTF-8 中文可直接阅读为底线，不要求用户自行修码或二次整理。

### 配置与运维体验
- **D-03:** Phase 3 不只修乱码，还要顺手把默认 `config.yml` 整理成更易读的现代注释版；键名、行为与现有配置语义保持兼容。
- **D-04:** `/signlock reload`、启动日志与关键信息反馈采用明确结果提示：成功时说明已生效，失败时说明原因与下一步，而不是只给最小回执。

### 发布与现代化范围
- **D-05:** 发布产物按“快速开始 + 手测矩阵 + 发布前检查”交付，而不是只留一个最小 smoke checklist。
- **D-06:** “功能更现代化一点”的诉求在本阶段只落到文案、注释、命令反馈和交付体验，不扩展成新的玩家交互系统。

### the agent's Discretion
- 具体哪些消息保留现有 key、哪些需要细化成“成功 / 失败 / 下一步”三段式反馈，可在规划与实现时按现有配置结构收口。
- 手测文档与发布清单是否拆分成多个文件，还是整合在同一份面向服主的文档中，可在后续计划阶段根据仓库现状决定。
</decisions>

<specifics>
## Specific Ideas

- 现有仓库里的 `plugin.yml`、`config.yml` 与手测文档已经存在中文质量问题，Phase 3 应优先把这些默认交付面修到“开箱可用”。
- 这次“更现代化”优先体现为：更清楚的命令提示、更轻量的中文句式、更像当代插件的配置注释和发布说明，而不是新增复杂系统。
- 如果 Phase 3 顺利完成，后续再单独立一个 backlog / phase 讨论 GUI 管理、批量授权、交互式授权等新能力，会比混在发布收尾里更稳。
</specifics>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### Phase definition
- `.planning/ROADMAP.md` - Phase 3 的目标、成功标准与计划边界
- `.planning/REQUIREMENTS.md` - Phase 3 对应的 `OPS-01`、`REL-01`、`REL-02`、`REL-03`
- `.planning/STATE.md` - 当前项目状态与前两阶段完成情况
- `.planning/phases/01-lock-target-correctness/01-VERIFICATION.md` - 已通过的锁目标行为基线，避免资源层改动误伤核心行为
- `.planning/phases/02-authorization-protection-matrix/02-VERIFICATION.md` - 已通过的保护矩阵与命令语义基线

### Existing implementation
- `src/main/resources/plugin.yml` - 插件元数据与命令描述，当前存在中文可读性问题
- `src/main/resources/config.yml` - 默认配置、提示文案与注释结构，当前需要 UTF-8 中文整理
- `src/main/java/ym/signLock/SignLock.java` - 启动、重载与配置加载路径
- `src/main/java/ym/signLock/config/SignLockConfig.java` - 默认消息与配置项读取语义
- `TESTING.md` - 当前手测说明，可作为 Phase 3 发布文档的基础材料
</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets
- `SignLockConfig` 已经集中承接大部分消息与配置读取，适合在不改行为语义的前提下提升默认文案质量。
- `SignLock` 已包含启动与重载入口，适合补齐更明确的成功 / 失败反馈，而不是新开运维命令体系。
- 现有 `TESTING.md` 已有手测意图，适合扩展成面向发布的快速开始与验证矩阵。

### Established Patterns
- 当前项目优先稳定性和兼容性，Phase 3 应避免把“现代化”演变成架构重写或新增交互系统。
- 前两阶段已经把锁目标与权限语义稳定下来；本阶段的资源与文档修改必须建立在这些已验证行为之上。
- `plugin.yml` 中的 Folia 兼容声明与命令注册属于发布关键元数据，允许清理文案，不应误删运行时声明。

### Integration Points
- 资源层改动要覆盖启动信息、重载反馈、权限提示和命令说明，避免“配置里是新文案，运行时还是旧语气”的割裂。
- 发布文档应和当前实际命令、配置键、测试路径对齐，不能写成理想化说明。
</code_context>

<deferred>
## Deferred Ideas

- GUI 化锁管理
- 批量授权 / 批量移除
- 交互式菜单或聊天式授权流程
- 面向服主的审计 / 查看面板
- 更完整的国际化体系（多语言切换而非默认中文优化）
</deferred>

---

*Phase: 03-localization-release-readiness*
*Context gathered: 2026-04-02*
