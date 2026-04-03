# Phase 6: Visual Lock Insights & Safety - Discussion Log

> **Audit trail only.** Do not use as input to planning, research, or execution agents.
> Decisions are captured in CONTEXT.md; this log preserves the alternatives considered.

**Date:** 2026-04-03
**Phase:** 06-visual-lock-insights-safety
**Areas discussed:** 锁信息面向对象, 权限边界表达, 目标摘要语义, Safety 深度

---

## 锁信息面向对象

| Option | Description | Selected |
|--------|-------------|----------|
| 仅主人可见 | 继续只有主人能看到完整锁信息，其他人不新增任何查看面 | |
| 已授权玩家只读查看 | 主人保留完整管理 GUI；已授权玩家可看只读摘要；未授权玩家不开放完整名单 | yes |
| 所有人都能看完整名单 | 任何点到锁的人都能看到 owner、名单和扩展详情 | |

**User's choice:** 按推荐默认  
**Notes:** 采用推荐默认，保持 owner-only manage 规则，同时给已授权玩家一个更现代、但不越权的只读查看面。

---

## 权限边界表达

| Option | Description | Selected |
|--------|-------------|----------|
| 隐式表达 | 继续靠按钮是否可点来暗示权限，不额外解释 access / manage 差异 | |
| 显式标签 | 在 GUI 和 `/bl info` 明确标注“可访问 / 可管理”，让 owner、authorized、admin 身份一眼可懂 | yes |
| 详细教程式文案 | 加更长的解释性文案，把权限边界逐条讲给玩家看 | |

**User's choice:** 按推荐默认  
**Notes:** 采用显式标签路线，优先提升理解成本而不是堆长说明。

---

## 目标摘要语义

| Option | Description | Selected |
|--------|-------------|----------|
| 保持原样 | 继续显示原始方块类型和坐标，玩家自己理解 shared-target | |
| 人话摘要 + canonical target | 用单箱 / 双箱 / 木桶 / 潜影盒等更直观的目标描述，并把双箱统一显示为同一共享锁目标 | yes |
| 按点击位置展示 | 点到哪一半就展示哪一半，视觉上更贴近当前交互位置 | |

**User's choice:** 按推荐默认  
**Notes:** 采用 canonical target 的人话摘要，避免把双箱重新讲回两个独立目标。

---

## Safety 深度

| Option | Description | Selected |
|--------|-------------|----------|
| 最小自动回归 | 只补几条核心单测，手测留到以后 | |
| 自动回归 + Paper/Folia 手测 | 把 GUI、命令、viewer 权限分流、shared-target 摘要和保护矩阵一起收成完整 closeout | yes |
| 顺手扩成审计能力 | 借 Phase 6 一起加审计日志和管理员总览 | |

**User's choice:** 按推荐默认  
**Notes:** 采用完整 closeout 路线，因为 Phase 6 是 v1.1 的收尾阶段，安全基线比继续加范围更重要。

---

## the agent's Discretion

- 只读视图最终是复用同一 GUI 但隐藏管理控件，还是拆成轻量摘要变体，由后续 research / planning 决定。
- `/bl info` 最终输出格式的精简方式由后续规划决定，但必须与 GUI 摘要模型对齐。

## Deferred Ideas

- 审计日志、变更历史、管理员总览面板
- 搜索 / 过滤 / 分页式授权管理
- Web 或跨服远程管理
