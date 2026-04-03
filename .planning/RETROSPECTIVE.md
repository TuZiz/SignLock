# Retrospective

## Milestone: v1.1 - Modern UX

**Shipped:** 2026-04-03  
**Phases:** 3  
**Plans:** 9

### What Was Built

- 主人可直接通过 GUI 管理锁与授权名单
- GUI 与命令都支持批量 add/remove
- GUI 和 `/bl info` 共享 viewer-aware 锁摘要语义
- 已授权非主人可以看到只读摘要，而不会拿到管理权限

### What Worked

- 先把 v1.0 底层语义稳定住，再在 v1.1 上做现代化，返工明显更少
- GUI、命令和服务层复用同一套 summary contract，避免了多入口分叉
- Phase 6 先补语义测试再接只读 GUI，收口比直接堆界面稳得多

### What Was Inefficient

- 里程碑 closeout 前没有先跑一次 milestone audit
- planning 文档和资源文件的历史编码问题，导致收尾阶段额外花时间清洗

### Patterns Established

- “服务层单一真源 + GUI/命令共享消费”适合继续沿用
- 现代化功能必须伴随 protection matrix 和 smoke checklist 一起交付

### Key Lessons

- 对 Minecraft 插件来说，现代化体验不能靠新界面硬堆，必须保证 shared-target 和权限边界先说清楚
- 资源文件编码问题越晚处理，closeout 成本越高

## Milestone: v1.0 - Initial Release

**Shipped:** 2026-04-02  
**Phases:** 3  
**Plans:** 9

### What Was Built

- shared-target 锁语义与双箱保护路径
- 授权、破坏、自动化和身份归一化的完整保护矩阵
- 中文默认资源、快速开始、手测矩阵和发布前检查

### What Worked

- 先做回归测试再修行为，能快速锁定边界
- 把“现代化”限定在交付体验，避免了 v1.0 scope creep

### What Was Inefficient

- closeout 前没有先跑 milestone audit
- 部分 planning 文档带着历史编码问题，收尾时需要额外清洗

### Patterns Established

- 先用 phase verification 报告收口，再做 milestone 归档
- 运行时默认文案与发布文档要同步推进

### Key Lessons

- 双箱 / shared-target 这类语义必须尽早测试驱动
- 发布收尾也需要明确 scope，否则很容易顺手继续加功能

## Cross-Milestone Trends

- v1.0：稳定化优先于扩功能
- v1.1：现代化体验必须绑定安全回归一起推进
