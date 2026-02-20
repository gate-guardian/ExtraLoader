# Extra Loader

Minecraft Forge 加载额外的资源包和数据包

## 设计理念

Extra Loader 的目标是让 Mod 平台玩家更简单地管理游戏资源。通过将资源包集中存放在 `~/.extraloader/<mc_version>/` 目录，玩家可以为不同版本的游戏维护独立的资源配置。我们提供三种加载模式（required/optional/default），让你对资源包的启用与禁用拥有灵活的控制权，既能确保关键资源必加载，也能避免不必要的包干扰游戏体验。

### 存储目录

将资源包或数据包放入对应目录：

```
~/.extraloader/<mc_version>/
├── required/    # 必须加载
├── optional/    # 默认禁用
└── default/     # 默认启用
```

### 示例

**资源包**：

- `~/.extraloader/1.20.1/required/my-pack.zip`
- `~/.extraloader/1.20.1/optional/bonus-textures/`

**数据包**：

- `~/.extraloader/1.20.1/default/nice-fonts.jar`

## 加载模式

| 模式       | 行为                 | 玩家可禁用 |
| ---------- | -------------------- | ---------- |
| `required` | 强制加载，缺失时跳过 | ❌ 否      |
| `optional` | 默认禁用，需手动启用 | ✅ 是      |
| `default`  | 默认启用，可手动禁用 | ✅ 是      |

## 包识别规则

- 包含 `assets/` → 资源包
- 包含 `data/` → 数据包
- 两者都有 → 同时加载

支持 `.zip`、`.jar` 文件或直接目录。

## FAQ

**Q: 包没生效？**

- 检查是否包含 `assets/` 或 `data/` 目录
- 确认有 `pack.mcmeta` 文件
- 路径是否正确（`~/.extraloader/<mc版本>/`）

**Q: 如何禁用包？**

- `default`：游戏内手动禁用
- `optional`：默认禁用，需手动启用
- `required`：无法禁用，删除或移出该目录

**Q: 服务端要装吗？**

- 资源包仅客户端生效
- 数据包需要服务端安装

## 许可证

[MIT License](LICENSE)
