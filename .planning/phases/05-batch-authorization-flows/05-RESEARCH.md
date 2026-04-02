# Phase 5: Batch Authorization Flows - Research

**Researched:** 2026-04-02
**Domain:** Bukkit/Paper/Folia batch lock authorization on top of the Phase 4 single-page management GUI
**Confidence:** MEDIUM

<user_constraints>
## User Constraints (from CONTEXT.md)

### Locked Decisions
### 批量入口
- **D-01:** 现有 GUI 继续作为主入口，不新增必须依赖的新命令入口或外部面板。
- **D-02:** 现有 `/bl add` 与 `/bl remove` 继续保留，并扩展为可接受多个玩家输入；单玩家旧写法必须继续可用。

### 批量添加语义
- **D-03:** GUI 的“添加授权”入口升级为批量输入模式，允许主人一次输入多个玩家名，而不是新增第二个“批量添加”按钮。
- **D-04:** 推荐采用轻量文本输入协议，接受以空格或逗号分隔的多个玩家名，保持低学习成本并兼容命令路径。
- **D-05:** 批量添加允许部分成功；结果需要区分“成功添加”“已存在”“名单已满/无法继续扩展”三类，不因为单个玩家失败就整批回滚。

### 批量移除语义
- **D-06:** GUI 内的批量移除优先沿用当前单页模型，采用“选择多个玩家后确认移除”的思路，而不是立刻跳到多页或复杂筛选界面。
- **D-07:** 批量移除结果必须至少区分“成功移除”“原本不存在/已不在锁中”“所有者不可移除”，并允许部分成功。

### GUI 与命令一致性
- **D-08:** GUI 和命令都必须复用同一套玩家名归一化与 `LockService` 写入语义，不能因为批量模式再分叉出第二套规则。
- **D-09:** 所有批量路径都必须继续遵守 owner/manage 权限边界，不能让已授权玩家获得批量管理能力。

### 反馈与现代化体验
- **D-10:** 批量结果优先使用聚合反馈，而不是为每个玩家刷一行消息；但消息内容必须足够明确，让主人知道哪些玩家成功、哪些失败、哪些因规则被拒绝。
- **D-11:** Phase 5 继续避免 scope creep：不在这阶段吸收搜索、排序、访问/管理边界可视化和审计历史。

### Deferred Ideas (OUT OF SCOPE)
- 更强的授权搜索、过滤、排序和分页 - Phase 6 或 backlog
- 访问/管理权限边界的更强可视化提醒 - Phase 6
- 审计日志、变更历史与管理员总览 - 后续 backlog
</user_constraints>

<phase_requirements>
## Phase Requirements

| ID | Description | Research Support |
|----|-------------|------------------|
| BATCH-01 | 锁主人可以一次添加多个玩家到同一把锁。 | 通过共享批量编排服务与批量输入解析，把 GUI 与 `/bl add` 都收口到“同一批、逐个写回、允许部分成功”的模型。 |
| BATCH-02 | 锁主人可以一次移除多个玩家，且结果能区分成功、已不存在和不可移除。 | GUI 采用多选后确认移除；命令与 GUI 均复用同一批量移除结果摘要模型。 |
| BATCH-03 | 批量授权能力在 GUI 与命令路径上保持一致语义，不能绕过所有权检查。 | 新增共享 parser / summary / orchestration service，GUI 和命令只做入口适配，不重复实现规则。 |
</phase_requirements>

## Project Constraints

- 不破坏现有牌子文本真源和扩展牌写回路径。
- 继续兼容 Spigot / Paper / Folia，尤其是 GUI 点击与聊天输入的主线程边界。
- Phase 4 已经把 owner 普通右键锁牌变成 GUI 主入口；Phase 5 必须在这个基础上平滑升级，而不是重新改入口。
- 现有 `/bl add`、`/bl remove`、`/bl info` 已有回归保护；批量增强必须保留旧写法。

## Summary

Phase 5 最稳的路径是引入一层共享的“批量授权编排服务”，把多玩家输入先解析成标准化目标列表，再逐个复用 `LockService.addPlayerToLock(...)` 和 `removePlayerFromLock(...)` 的单玩家语义，最后输出一份聚合结果摘要。这样可以让 GUI 和命令只各自负责入口与结果展示，不会在批量模式里复制底层规则，也不会破坏 sign-backed 数据模型。

GUI 侧，最合适的升级方式不是推翻 Phase 4 的单页 chest GUI，而是保留现有结构并增加两类能力：第一，`添加授权` 按钮进入“批量输入”待处理状态，允许一次输入多个名字；第二，玩家槽位支持“切换选中”，再通过单独的确认按钮提交批量移除。这样能满足 “单页延续、低学习成本、现代化更强” 三个目标，同时避免立刻引入分页、搜索或更重的菜单系统。

命令侧，最稳的增强是继续沿用 `/bl add ...` 与 `/bl remove ...`，但把参数解析从“只接受一个玩家名”升级为“接受多个 token，再按空格/逗号拆分”。单玩家旧写法必须保持可用。聚合反馈不适合为每个玩家刷一行，因此 Phase 5 应新增批量结果消息模板，例如“成功 3 个，已存在 1 个，名单已满后跳过 2 个”，必要时再附上短名单片段。

线程与兼容性方面，Phase 4 已经有 `LockManagementPendingInputStore` 和聊天输入桥接。Phase 5 不应在异步聊天线程里直接跑批量写回，而是继续把文本捕获和取消广播留在异步线程，把名字归一化、锁写回、GUI reopen/refresh 全部切回同步调度。Neuron 预检也明确提示，先确认容器交互策略，再落具体桥接实现；因此 Phase 5 应明确锁定“SHIFT_ONLY / selection-confirm”一类轻量策略，而不是一上来把 GUI 做成 full bridge 式复杂交互。

**Primary recommendation:** 新增共享 `LockBatchAuthorizationService`（或等价命名）+ 统一多目标解析器 + 聚合结果摘要模型；Wave 2 只接 GUI 批量交互，Wave 3 只接命令批量兼容与 closeout。

## Standard Stack

### Core
| Library | Version | Purpose | Why Standard |
|---------|---------|---------|--------------|
| Spigot API | `1.21.11-R0.1-SNAPSHOT` | 插件命令、GUI、聊天和事件 API 基线 | 已是当前项目编译基线，继续沿用最稳。 |
| Paper API | `1.21.11-R0.1-SNAPSHOT` | Paper / Folia 兼容面验证 | 现有项目已经在这个兼容面上工作。 |
| Repo-local batch orchestration | none | 批量授权 parser / summary / orchestration | 不引入新依赖，不改变当前 brownfield 架构。 |

### Supporting
| Library | Version | Purpose | When to Use |
|---------|---------|---------|-------------|
| MockBukkit | `4.108.0` | 命令、GUI、聊天输入与锁写回回归 | Phase 5 的自动化主力。 |
| JUnit Jupiter | `6.0.3` | 测试框架 | 全部新增测试继续沿用。 |
| Mockito | `5.23.0` | mock Player、消息与 reopen 副作用 | 对聚合反馈和 GUI 动作做精确断言。 |

### Alternatives Considered
| Instead of | Could Use | Tradeoff |
|------------|-----------|----------|
| 共享批量编排服务 | GUI 和命令各自 for-loop 写回 | 规则会分叉，Phase 5 最容易把批量语义做歪。 |
| 单页多选后确认移除 | 直接点击即移除多个 / 分页式成员管理 | 前者误操作风险高，后者超出当前阶段边界。 |
| 轻量文本批量输入 | anvil / sign / NMS / ProtocolLib 输入桥 | 复杂度过高，不适合只为批量名字输入扩 scope。 |

## Architecture Patterns

### Pattern 1: Shared Batch Orchestration Service
**What:** 解析后的多个玩家目标统一交给一个批量服务，由它顺序调用单玩家 `LockService` 方法并累计结果。  
**When to use:** GUI 批量 add/remove、命令 `/bl add ...` 和 `/bl remove ...`。  
**Why:** 保证 GUI 与命令共享完全一致的写回和结果语义。

### Pattern 2: Batch Target Parser with Compatibility Mode
**What:** 支持把 `Alice Bob,Charlie` 这类输入解析成有序去重列表，同时保留单玩家输入兼容。  
**When to use:** 命令参数拼接后的原始字符串、GUI 聊天输入文本。  
**Why:** 让 GUI 与命令走同一输入协议，减少认知成本。

### Pattern 3: Aggregated Result Summary
**What:** 把逐个玩家的写回结果收敛成 `added / alreadyAuthorized / noSpace / removed / notFound / ownerDenied` 这样的分组摘要。  
**When to use:** 向玩家发送反馈、测试断言批量结果。  
**Why:** 符合 D-10，避免刷屏且更适合部分成功模型。

### Pattern 4: Selection-Confirm GUI Flow
**What:** 玩家槽位点击只切换“待移除选择状态”，真正写回只在确认按钮发生。  
**When to use:** GUI 批量移除。  
**Why:** 比“点击即删”更符合批量管理心智，也能降低误操作。

## Key Findings

### Finding 1: 当前架构已经具备批量模式的天然切入点
- `LockManagementGuiActionService` 已经承接 GUI add/remove 与聊天桥接。
- `LockManagementPendingInputStore` 可以扩展为区分 add/remove/select-confirm 的待处理状态。
- `LockPlayerNameNormalizer` 已经解决 GUI / 命令共享名称归一化的一半问题。

### Finding 2: Phase 5 最大风险不是底层写回，而是入口层语义漂移
- 底层 `LockService` 已经稳定。
- 真正危险的是 GUI 和命令各自定义一套批量结果与失败边界。
- 因此 planner 应把共享 batch service 视为阻断级前置。

### Finding 3: 单页 GUI 仍可承载 Phase 5 的最小现代化升级
- Phase 4 已经是单页摘要优先。
- Phase 5 只需补“选中状态”和“批量确认按钮”，不必立刻进入分页或搜索。
- 如果授权玩家数量超出当前可显示槽位，命令路径仍可作为完整回退；这不影响 Phase 5 的核心成功标准。

### Finding 4: 验证层必须显式保护旧命令兼容
- Phase 4 的 planning blocker 就是因为命令兼容缺少显式回归。
- Phase 5 若改 `/bl add` / `/bl remove` 输入解析，必须新增专门的批量命令回归套件。

## Neuron Evidence

- `preflight_task_validation` 结论：可以直接进入代码骨架阶段，当前任务命中 `gui-dsl`、`async-pagination`、`input-bridge` 等专题。
- `check_antipatterns` 提醒：先确认容器交互策略，不要在交互复杂后继续把大量槽位和业务硬编码耦在一起。
- `verify_api_usage` 提醒：异步聊天只捕获文本，所有 Bukkit 状态写回与 GUI 跳转仍需切回同步调度；Folia 兼容不能被批量路径破坏。

## Planning Implications

1. Wave 1 先建共享批量 parser / result summary / orchestration service 与红绿测试，不碰 GUI 或命令入口。
2. Wave 2 只接 GUI 批量 add/remove 流程与选择态，不碰命令解析。
3. Wave 3 再接 `/bl add` / `/bl remove` 批量兼容与 full regression，避免和 GUI 接线在同一波互相踩文件。

## Recommended Execution Order

1. 新增批量目标解析器、批量结果摘要模型、批量编排服务和测试。
2. 把 GUI 的 pending input / selection-confirm / 聚合反馈接到共享服务。
3. 把命令多目标输入和聚合反馈接到共享服务，并补全回归和 validation。

---

*Phase: 05-batch-authorization-flows*
*Research completed: 2026-04-02*
