# SignLock 快速开始

## 1. 安装

1. 将构建出的插件 jar 放入服务器 `plugins/` 目录。
2. 启动服务器一次，生成默认配置。
3. 确认插件目录下已生成 `config.yml` 和 `players.yml`。

## 2. 默认命令

- `/signlock reload`
  作用：重载配置
- `/bl add <玩家名>`
  作用：给当前瞄准的锁牌添加授权玩家
- `/bl remove <玩家名>`
  作用：从当前瞄准的锁牌移除授权玩家
- `/bl info`
  作用：查看当前瞄准锁牌的所有者、授权列表与扩展牌数量

## 3. 默认使用方式

1. 手持普通牌子，对可上锁方块右键即可创建锁。
2. 所有者可以继续放置 `[更多用户]` 牌，或使用 `/bl add`、`/bl remove` 管理授权。
3. 未授权玩家不能打开、破坏或通过自动化绕过已保护目标。

## 4. 建议上线前确认

1. 根据 [TESTING.md](/D:/codex/SignLock/TESTING.md) 跑完一遍手测矩阵。
2. 如修改过默认文案或可上锁方块列表，执行 `/signlock reload` 验证配置重载。
3. 若服务器使用 Folia，至少做一次多人并发开箱与授权测试。

## 5. 常见配置项

- `signs.lock-header`
  作用：主锁牌标题
- `signs.more-users-header`
  作用：扩展授权牌标题
- `protection.admin-bypass`
  作用：是否允许管理员绕过
- `protection.max-more-user-signs`
  作用：每把锁最多允许的扩展牌数量
