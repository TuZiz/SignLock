# SignLock 发布前检查

## 构建

1. 执行 `./gradlew.bat test`
2. 确认构建成功，且没有新增阻塞性错误

## 资源与元数据

1. 确认 `src/main/resources/config.yml` 默认中文可直接阅读
2. 确认 `src/main/resources/plugin.yml` 保留 `folia-supported: true`
3. 确认命令说明仍为 `/signlock reload`、`/bl add`、`/bl remove`、`/bl info`

## 手测

1. 按 [TESTING.md](/D:/codex/SignLock/TESTING.md) 跑完核心流程
2. 至少验证一次双箱、扩展牌、未授权访问、破坏保护、自动化保护
3. 至少验证一次 `/signlock reload` 和 `/bl info`

## 发布确认

1. 确认默认文案符合当前服务器使用语言
2. 确认如启用 Folia，已做并发场景验证
3. 确认没有把新功能半成品混进本次发布
