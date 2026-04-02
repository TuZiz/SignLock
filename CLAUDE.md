<!-- GSD:project-start source:PROJECT.md -->
## Project

**SignLock**

SignLock 是一个面向 Spigot / Paper / Folia 服务器的牌子锁插件，用牌子为箱子、木桶、熔炉、潜影盒、门、活板门和栅栏门等方块提供所有权与授权访问控制。它服务于希望使用接近原版交互方式完成容器与门禁保护的服主和玩家，重点是轻量、直观、可配置、中文友好。

**Core Value:** 玩家贴上牌子后，受保护方块必须稳定、可预期地只允许主人和已授权玩家访问。

### Constraints

- **Tech stack**: 继续使用 Java Plugin + Gradle Wrapper + Spigot API 1.21.11 - 避免在初始化阶段引入框架迁移风险。
- **Compatibility**: 必须保持对 Spigot/Paper/Folia 的兼容声明与主线程安全 - 这是插件对外承诺的一部分。
- **Behavior**: 不破坏已有锁牌数据与当前牌子格式 - 用户已经可能在服务器里使用这些牌子。
- **Localization**: 默认体验必须对中文服务器友好 - 当前目标用户和文案方向已经明确。
<!-- GSD:project-end -->

<!-- GSD:stack-start source:STACK.md -->
## Technology Stack

Technology stack not yet documented. Will populate after codebase mapping or first phase.
<!-- GSD:stack-end -->

<!-- GSD:conventions-start source:CONVENTIONS.md -->
## Conventions

Conventions not yet established. Will populate as patterns emerge during development.
<!-- GSD:conventions-end -->

<!-- GSD:architecture-start source:ARCHITECTURE.md -->
## Architecture

Architecture not yet mapped. Follow existing patterns found in the codebase.
<!-- GSD:architecture-end -->

<!-- GSD:workflow-start source:GSD defaults -->
## GSD Workflow Enforcement

Before using Edit, Write, or other file-changing tools, start work through a GSD command so planning artifacts and execution context stay in sync.

Use these entry points:
- `/gsd:quick` for small fixes, doc updates, and ad-hoc tasks
- `/gsd:debug` for investigation and bug fixing
- `/gsd:execute-phase` for planned phase work

Do not make direct repo edits outside a GSD workflow unless the user explicitly asks to bypass it.
<!-- GSD:workflow-end -->



<!-- GSD:profile-start -->
## Developer Profile

> Profile not yet configured. Run `/gsd:profile-user` to generate your developer profile.
> This section is managed by `generate-claude-profile` -- do not edit manually.
<!-- GSD:profile-end -->
