# Roadmap: SignLock

## Overview

这条路线把 SignLock 从“已有基本功能的牌子锁插件”推进到“可稳定发布的中文服务器插件”。路线重点不是扩展新玩法，而是先收紧锁语义、补齐保护边界、修复资源质量，并建立可以重复执行的验证基线。

## Phases

**Phase Numbering:**
- Integer phases (1, 2, 3): Planned milestone work
- Decimal phases (2.1, 2.2): Urgent insertions (marked with INSERTED)

Decimal phases appear between their surrounding integers in numeric order.

- [ ] **Phase 1: Lock Target Correctness** - 修复单双箱和共享容器的锁归属与访问判定。
- [ ] **Phase 2: Authorization & Protection Matrix** - 收紧授权管理与保护矩阵，保证命令、破坏和自动化路径一致。
- [ ] **Phase 3: Localization & Release Readiness** - 修复默认中文资源质量并建立构建/冒烟验证基线。

## Phase Details

### Phase 1: Lock Target Correctness
**Goal**: 让主锁牌、扩展牌、单双箱与容器访问在所有入口都指向一致的受保护目标。
**Depends on**: Nothing (first phase)
**Requirements**: [LOCK-01, LOCK-02, LOCK-03, LOCK-04, PROT-01, PROT-02]
**Success Criteria** (what must be TRUE):
  1. 玩家给单箱或双箱贴主锁牌后，访问判定对整个目标一致生效。
  2. 双箱任一半边被未授权玩家点击或打开时，都会被正确拦截。
  3. 主人可以继续通过扩展牌增加授权，不会因为箱体半边或牌子位置造成误判。
  4. 锁牌被主人编辑时，系统控制的头部和所有者信息不会损坏。
**Plans**: 3 plans

Plans:
- [x] 01-01-PLAN.md - 安装 MockBukkit 回归底座并在 `LockService` 内收口 canonical target 与共享扩展牌语义。
- [ ] 01-02-PLAN.md - 修复 `LockListener` 的右键、开箱与自动化入口，使其全部复用共享目标判定。
- [ ] 01-03-PLAN.md - 收口主锁/扩展牌贴牌与锁牌编辑结构保护，避免共享目标与系统控制行回归。

### Phase 2: Authorization & Protection Matrix
**Goal**: 让授权管理、破坏保护、自动化保护和身份解析覆盖整个保护矩阵。
**Depends on**: Phase 1
**Requirements**: [PROT-03, PROT-04, OPS-02, OPS-03]
**Success Criteria** (what must be TRUE):
  1. 未授权玩家无法破坏受保护方块本体或他人的锁牌。
  2. 自动化搬运、爆炸、活塞和流体不会绕过既有保护语义。
  3. `/signlock add` 与 `/signlock remove` 在扩展牌已满、目标无效或玩家名解析场景下给出正确结果。
  4. 主人、授权玩家、管理员绕过三种身份边界在核心路径上表现一致。
**Plans**: 3 plans

Plans:
- [ ] 02-01: 整理命令授权链与玩家身份缓存行为。
- [ ] 02-02: 对破坏、爆炸、活塞、流体、自动化等保护事件做一致性核对。
- [ ] 02-03: 补充回归验证步骤并修正边缘条件。

### Phase 3: Localization & Release Readiness
**Goal**: 让默认资源、插件元数据和发布验证达到可交付状态。
**Depends on**: Phase 2
**Requirements**: [OPS-01, REL-01, REL-02, REL-03]
**Success Criteria** (what must be TRUE):
  1. 默认 `config.yml` 和 `plugin.yml` 在 UTF-8 环境下可直接阅读和使用。
  2. 服主可以重载配置并看到正确的中文提示，不需要手工修默认文案。
  3. 仓库包含覆盖关键保护路径的手工测试清单或发布前检查说明。
  4. 当前代码在目标 Java / Spigot 环境下可以稳定构建，并保留 Folia 兼容声明。
**Plans**: 3 plans

Plans:
- [ ] 03-01: 修复资源文件中的中文乱码与描述文本。
- [ ] 03-02: 整理发布前验证文档与测试矩阵。
- [ ] 03-03: 执行构建与最终兼容性确认，准备首个可发布版本。

## Progress

**Execution Order:**
Phases execute in numeric order: 1 -> 2 -> 3

| Phase | Plans Complete | Status | Completed |
|-------|----------------|--------|-----------|
| 1. Lock Target Correctness | 0/3 | Not started | - |
| 2. Authorization & Protection Matrix | 0/3 | Not started | - |
| 3. Localization & Release Readiness | 0/3 | Not started | - |
