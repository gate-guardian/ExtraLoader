# Extra Loader

一个 Forge 模组，通过约定优于配置的方式加载额外的资源包和数据包。

## 功能特点

- **自动检测包类型**：根据包内是否包含 `assets/` 或 `data/` 目录自动识别资源包或数据包
- **三种加载模式**：
  - `required/` - 必须启用的包（红色标识，无法禁用）
  - `optional/` - 可选包（青色标识，默认禁用）
  - `default/` - 默认启用的包（绿色标识，可手动禁用）
- **支持多种包格式**：支持路径包（目录）和文件包（.zip/.jar）
- **系统级配置**：支持全局配置选项，管理包的启用/禁用

## 兼容性

- **Minecraft 版本**：支持所有 Minecraft 1.20+ 版本
- **Forge 版本**：需要 Forge 1.20+ / NeoForge 1.20+
- **Java 版本**：Java 17+

## 安装步骤

### 客户端安装

1. 下载 **ExtraLoader** 模组文件（JAR 格式）
2. 将模组文件放入 Minecraft 的 `mods/` 文件夹
3. 启动游戏

### 服务端安装

1. 下载 **ExtraLoader** 模组文件（JAR 格式）
2. 将模组文件放入服务器上的 `mods/` 文件夹
3. 启动服务器

> **注意**：ExtraLoader 仅在 Forge 1.20+ 环境中运行，不支持其他模组加载器。

## 目录结构

### 全局包目录

模组会在游戏目录下创建 `extraloader/` 文件夹：

```
<游戏目录>/
└── extraloader/
    ├── required/      # 必须启用的包
    ├── optional/      # 可选包
    └── default/       # 默认启用的包
```

### 系统全局包目录

模组还会在用户目录下创建系统级包目录：

```
<用户目录>/
└── .extraloader/
    └── <Minecraft版本>/    # 自动根据 Minecraft 版本创建子目录
        ├── required/
        ├── optional/
        └── default/
```

### 加载模式说明

| 模式文件夹  | 包的状态       | 颜色标识 | 是否可禁用 |
| ----------- | -------------- | -------- | ---------- |
| `required/` | 必须启用       | 红色     | 否         |
| `optional/` | 可选，默认禁用 | 青色     | 是         |
| `default/`  | 默认启用       | 绿色     | 是         |

### 包类型识别

ExtraLoader 会自动检测包的类型：

- **资源包**：包含 `assets/` 目录的包
- **数据包**：包含 `data/` 目录的包

> **重要**：不需要在文件夹名称中指定 `resourcepacks/` 或 `datapacks/`，模组会根据包内容自动识别。

## 常用配置项

ExtraLoader 的配置文件位于 Minecraft 配置目录，包含以下配置项：

### Common 配置

| 配置项                    | 类型    | 默认值 | 说明                   |
| ------------------------- | ------- | ------ | ---------------------- |
| `enableSystemGlobalPacks` | Boolean | `true` | 是否启用系统全局包目录 |

### Client 配置

| 配置项                  | 类型 | 默认值 | 说明                 |
| ----------------------- | ---- | ------ | -------------------- |
| `disabledResourcePacks` | List | `[]`   | 禁用的资源包 ID 列表 |

### 配置文件示例

`config/extraloader-common.toml`:

```toml
enableSystemGlobalPacks = true
```

`config/extraloader-client.toml`:

```toml
disabledResourcePacks = []
```

## 使用示例

### 示例 1：启用主题包

1. 在 `extraloader/default/` 目录下放置主题包（.zip 或目录）
2. 包内容应包含 `assets/minecraft/textures/gui/title.png`
3. 启动游戏后，主题包会自动启用（绿色）

### 示例 2：使用强制资源包

1. 在 `extraloader/required/` 目录下放置强制包
2. 包内容应包含 `assets/minecraft/textures/` 目录
3. 启动游戏后，强制包会自动启用且无法禁用（红色）

### 示例 3：禁用某个资源包

1. 打开 `config/extraloader-client.toml`
2. 在 `disabledResourcePacks` 列表中添加包的 ID：

```toml
disabledResourcePacks = [
    "extraloader/default/example-theme.zip"
]
```

## 常见问题

### 1. 模组加载后看不到我的包

**可能原因**：

- 包放在了错误的文件夹（如 `resourcepacks/` 而不是 `extraloader/default/`）
- 包缺少 `pack.mcmeta` 文件
- 包内没有 `assets/` 或 `data/` 目录

**解决方法**：

- 确保包放在 `extraloader/` 文件夹下的正确模式目录中
- 检查包内容，确保包含必要的目录和文件
- 查看 Minecraft 启动日志中的错误信息

### 2. 资源包图标显示为"未知来源"

**原因**：包被标记为"未知来源"

**解决方法**：

- 检查包是否正确放置在 `extraloader/` 目录中
- 确保 `pack.mcmeta` 文件格式正确
- 查看控制台日志，确认包是否被正确加载

### 3. 系统全局包不生效

**原因**：`enableSystemGlobalPacks` 配置项被禁用

**解决方法**：

1. 打开 `config/extraloader-common.toml`
2. 将 `enableSystemGlobalPacks` 设置为 `true`
3. 重启游戏

## 开发说明

### 目录约定

ExtraLoader 使用约定优于配置的设计理念：

- **目录命名**：使用 `required`、`optional`、`default` 表示包的加载模式
- **包识别**：根据包内容自动识别资源包/数据包类型
- **配置简化**：通过简单的目录结构实现复杂的加载逻辑

### 日志级别

- `DEBUG`：包加载详细信息
- `INFO`：包加载成功提示
- `WARN`：无效的包被跳过
- `ERROR`：包加载失败

## 许可证

MIT License

## 贡献

欢迎提交 Issue 和 Pull Request！
