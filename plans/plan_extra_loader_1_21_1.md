# 计划：ExtraLoader NeoForge 1.21.1 移植

## 计划元数据

| 字段 | 值 |
|------|-----|
| 计划ID | `extra_loader_1_21_1` |
| plan_path | `plans/plan_extra_loader_1_21_1.md` |
| 版本状态 | `已批准` |
| 批准日期 | `2026-06-15` |
| 审查结论 | `审查通过。计划结构完整，阶段划分合理，23 个 TASK 覆盖构建系统、元数据、源码和验证全链路；依赖关系清晰，风险项已识别并标注降级方案。建议按计划执行。` |
| 建议下一步 | `审查通过` |
| 触发原因 | `初版（草稿 → 正式转换）` |
| 负责代理 | `猫娘编写官-米娅` |
| 规划代理 | `猫娘规划师-缇娅` |
| 总任务数 | `23 个` |
| 总阶段数 | `4 个（阶段 0~3）` |

---

## 概述

### 目标

将 ExtraLoader 从 **1.20.1 Forge** 移植到 **1.21.1 NeoForge**，使其成为真正的 NeoForge 1.21.1 模组项目。

### 范围

- 构建系统从 `legacyForge` DSL 迁移到 `neoForge` DSL
- 模组元数据从 `mods.toml` 迁移到 `neoforge.mods.toml`
- Java 源码包路径从 `net.minecraftforge.*` 迁移到 `net.neoforged.*`
- 更新所有依赖版本以适配 1.21.1
- 清理 Forge 遗留文件（空 accesstransformer.cfg 等）
- **不包含**：功能改动、重构、新增功能、测试框架搭建

### 约束

- 目标工作树：`E:\GitHub\ExtraLoader\ExtraLoader-1.21.1`（分支 `1.21.1`）
- 参考项目：`E:\GitHub\hoarding-1.21.1-neoforge`
- 保留原有 mod id (`extraloader`)、包结构 (`dev.gateguardian.extraloader`) 与功能行为
- 移植完成后需能通过 `gradlew build` 编译

---

## 执行阶段

### 阶段 0 — 构建系统与依赖基础

> 本阶段是所有后续步骤的前置条件。必须全部完成后才能进入阶段 1 和 2。

#### TASK-000: 版本目录更新 (`gradle/libs.versions.toml`)

- **scope**: `gradle/libs.versions.toml`
- **DoD**: 文件内容更新完毕，所有版本指向 1.21.1 NeoForge 兼容版本
- **目标**: 更新 version catalog 为 NeoForge 1.21.1
- **输入**: 当前 `gradle/libs.versions.toml`（1.20.1 Forge）
- **输出**: 更新后的版本目录
- **依赖**: 无
- **具体变更**:
  - `minecraft = "1.20.1"` → `"1.21.1"`
  - `forge = "47.4.3"` → 新增 `neo`, 移除 forge 版本
  - `parchment` → 更新为 1.21.1 对应版本（或移除，取决于 neoForge DSL 是否需要）
  - 插件 `modDevGradle` → 保持 `net.neoforged.moddev`（从 `net.neoforged.moddev.legacyforge` 改为 `net.neoforged.moddev`）
  - 所有依赖 artifact 更新至 1.21.1 版本：
    - `jei` → `jei-1.21.1-neoforge`
    - `emi` → `emi-neoforge`
    - `rei` → 确认 1.21.1 可用性
    - `jade`, `modernui`, `jecharacters` → 更新至 1.21.1 版本
  - `mixin` → 确认 0.8.7 或更新版本兼容性
  - `mixinExtras` → 更新至 1.21.1 兼容版本
- **验收要点**:
  - [ ] 所有 version ref 已更新为 1.21.1 兼容值
  - [ ] 插件 ID 已改为 `net.neoforged.moddev`（非 legacyforge）
  - [ ] Forge 专有依赖已替换为 NeoForge 变体

---

#### TASK-001: 设置脚本更新 (`settings.gradle.kts`)

- **scope**: `settings.gradle.kts`
- **DoD**: 文件内容更新，仅保留 NeoForge maven
- **目标**: 移除 MinecraftForge maven 仓库
- **输入**: 当前 `settings.gradle.kts`
- **输出**: 更新后的设置脚本
- **依赖**: 无（可与 TASK-000 并行）
- **具体变更**:
  - 移除 `maven { name = "MinecraftForge" ... }` 块
  - 保留 `maven { name = "NeoForge" ... }` 块
- **验收要点**:
  - [ ] settings.gradle.kts 不再引用 minecraftforge.net maven

---

#### TASK-002: Gradle 属性更新 (`gradle.properties`)

- **scope**: `gradle.properties`
- **DoD**: 属性更新完毕，Java 版本和版本范围正确
- **目标**: 更新版本号、范围和 Java 版本
- **输入**: 当前 `gradle.properties`
- **输出**: 更新后的 gradle.properties
- **依赖**: 无（可与 TASK-000、TASK-001 并行）
- **具体变更**:
  - `minecraft_version=1.20.1` → `1.21.1`
  - `minecraft_version_range=[1.20.1,1.21)` → `[1.21.1,1.22)`
  - 移除 `forge_version=47.4.13`（或改为 `neo_version`）
  - 移除 `forge_version_range=[47,)`（或改为 `neo_version_range`）
  - `loader_version_range=[47,)` → `[4,)`（NeoForge loader 版本范围）
  - 新增 `neo_version=` 和 `neo_version_range=`（参考 hoarding 项目）
  - Java toolchain → 21（在 build.gradle.kts 中体现，此处可添加注释）
- **验收要点**:
  - [ ] minecraft_version 为 1.21.1
  - [ ] 版本范围适配 1.21.1 NeoForge
  - [ ] Java 版本指向 21

---

#### TASK-003: 构建脚本重写 (`build.gradle.kts`)

- **scope**: `build.gradle.kts`
- **DoD**: 构建脚本适配 NeoForge 1.21.1，`legacyForge {}` 替换为 `neoForge {}`
- **目标**: 重写构建 DSL 和配置
- **输入**: 当前 `build.gradle.kts`、TASK-000 产出的版本目录
- **输出**: 更新后的 build.gradle.kts
- **依赖**: TASK-000（版本目录）、TASK-002（属性文件）
- **具体变更**:
  - `legacyForge { ... }` → `neoForge { ... }`
  - 移除 `obfuscation { ... }` 块（neoForge DSL 不适用）
  - Java 版本 `17` → `21`（`JavaLanguageVersion.of(21)`）
  - 更新 runs 配置以适应 NeoForge DSL（具体参考 hoarding 项目）
  - 移除 `parchment { ... }` 块（若 neoForge DSL 不支持）
  - 更新 `mixin { ... }` 配置（确认 NeoForge 兼容性）
  - 更新 dependencies：
    - `mixinExtras.forge` → `mixinExtras.neoforge`（如存在）
    - `modCompileOnly` → `compileOnly` 或 `modImplementation`（取决于 neoForge DSL）
    - `modLocalRuntime` → `localRuntime`
    - 所有依赖 artifact 更新为 NeoForge 变体
  - 移除 `apply(from = "repositories.gradle.kts")`，将仓库配置内联到此文件；标记 `repositories.gradle.kts` 为待删除
- **验收要点**:
  - [ ] legacyForge 块已完全移除
  - [ ] neoForge 块已正确配置
  - [ ] obfuscation 块已移除
  - [ ] Java toolchain 版本为 21
  - [ ] 所有依赖引用与版本目录一致
  - [ ] runs 配置适配 NeoForge（client/server/data）

---

#### TASK-004: 删除仓库配置文件 (`repositories.gradle.kts`)

- **scope**: `repositories.gradle.kts`
- **DoD**: 仓库配置已内联至 build.gradle.kts，此文件已从项目中删除
- **目标**: TASK-003 已将仓库配置内联，确认后删除此独立文件
- **输入**: 当前 `repositories.gradle.kts`、TASK-003 产出的 build.gradle.kts
- **输出**: 文件已删除
- **依赖**: TASK-003（确认仓库已内联至 build.gradle.kts）
- **具体变更**:
  - 验证 build.gradle.kts 中已包含所有必要仓库（NeoForge、JEI/EMI/REI 等）
  - 删除 `repositories.gradle.kts`
- **验收要点**:
  - [ ] build.gradle.kts 中的仓库配置完整无误
  - [ ] repositories.gradle.kts 已从项目中移除

---

### 阶段 1 — 模组元数据与资源适配

> 本阶段依赖阶段 0 完成，与阶段 2 可并行执行。元数据文件决定模组能否被 NeoForge 加载器识别。

#### TASK-010: 模组元数据转换 (`mods.toml` → `neoforge.mods.toml`)

- **scope**: `src/main/templates/META-INF/mods.toml` → `src/main/templates/META-INF/neoforge.mods.toml`
- **DoD**: 新文件 `neoforge.mods.toml` 创建，旧文件保留待清理；依赖声明适配 NeoForge
- **目标**: 将模组加载描述文件转换为 NeoForge 格式
- **输入**: 当前 `mods.toml`
- **输出**: `neoforge.mods.toml`
- **依赖**: TASK-002（属性文件中的版本范围）
- **具体变更**:
  - `modLoader="javafml"` → 保持（NeoForge 也使用 javafml）
  - `loaderVersion="${loader_version_range}"` → `${loader_version_range}` 使用新值
  - `[[dependencies.${mod_id}]]` 中：
    - `modId="forge"` → `modId="neoforge"`
    - `versionRange="${forge_version_range}"` → 使用 `${neo_version_range}` 或 `${loader_version_range}`
  - 可选：添加 `[[accessTransformers]]` 块（如果需要 AT）
- **验收要点**:
  - [ ] neoforge.mods.toml 文件已创建
  - [ ] 依赖声明指向 neoforge 而非 forge
  - [ ] 版本范围使用更新后的属性

---

#### TASK-011: pack 格式更新 (`pack.mcmeta`)

- **scope**: `src/main/templates/pack.mcmeta`
- **DoD**: `pack_format` 值为 32
- **目标**: 更新资源包格式至 1.21.1 版本
- **输入**: 当前 `pack.mcmeta`
- **输出**: 更新后的 `pack.mcmeta`
- **依赖**: 无
- **具体变更**:
  - `"pack_format": 15` → `"pack_format": 32`
- **验收要点**:
  - [ ] pack_format 值为 32

---

#### TASK-012: Mixin 配置更新 (`extraloader.mixins.json`)

- **scope**: `src/main/resources/extraloader.mixins.json`
- **DoD**: `compatibilityLevel` 为 `JAVA_21`
- **目标**: 更新 mixin 兼容等级至 Java 21
- **输入**: 当前 `extraloader.mixins.json`
- **输出**: 更新后的 mixins.json
- **依赖**: 无
- **具体变更**:
  - `"compatibilityLevel": "JAVA_17"` → `"JAVA_21"`
- **验收要点**:
  - [ ] compatibilityLevel 值为 JAVA_21

---

#### TASK-013: 清理空 AT 文件

- **scope**: `src/main/resources/META-INF/accesstransformer.cfg`
- **DoD**: 空文件已移除
- **目标**: 移除空的 accesstransformer.cfg
- **输入**: 当前空文件
- **输出**: 文件已删除
- **依赖**: 无
- **具体变更**:
  - 删除 `src/main/resources/META-INF/accesstransformer.cfg`
- **验收要点**:
  - [ ] accesstransformer.cfg 不再存在于 resources 中

---

### 阶段 2 — Java 源码迁移

> 本阶段依赖阶段 0 完成，与阶段 1 可并行执行（构建系统就绪后编译验证才有意义）。
> 所有 Java 文件的迁移核心模式：`net.minecraftforge.*` → `net.neoforged.*`

#### TASK-020: 入口类迁移 (`Bootstrap.java`)

- **scope**: `src/main/java/dev/gateguardian/extraloader/Bootstrap.java`
- **DoD**: 文件编译通过，使用 NeoForge 构造器签名
- **目标**: 适配 NeoForge 模组入口
- **输入**: 当前 Bootstrap.java
- **输出**: 更新后的 Bootstrap.java
- **依赖**: TASK-003（构建脚本）
- **具体变更**:
  - `import net.minecraftforge.fml.DistExecutor` → `net.neoforged.fml.DistExecutor`（验证路径）
  - `import net.minecraftforge.fml.common.Mod` → `net.neoforged.fml.common.Mod`
  - `import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext` → 验证 NeoForge 中等价类或改用 `IEventBus` 参数
  - 构造器参数 `FMLJavaModLoadingContext context` → 适配 NeoForge 模式（可能改为 `IEventBus modEventBus`）
- **验收要点**:
  - [ ] 所有 import 从 net.minecraftforge 改为 net.neoforged
  - [ ] 构造器签名适配 NeoForge 1.21.1 规范
  - [ ] `@Mod` 注解正确（包路径更新，value 属性值与 mod id `extraloader` 一致）

---

#### TASK-021: 配置类迁移 (`ExtraConfig.java`)

- **scope**: `src/main/java/dev/gateguardian/extraloader/common/ExtraConfig.java`
- **DoD**: 文件编译通过，`ForgeConfigSpec` → `ModConfigSpec`
- **目标**: 适配 NeoForge 配置系统
- **输入**: 当前 ExtraConfig.java
- **输出**: 更新后的 ExtraConfig.java
- **依赖**: TASK-003（构建脚本）
- **具体变更**:
  - `net.minecraftforge.common.ForgeConfigSpec` → `net.neoforged.neoforge.common.ModConfigSpec`
  - `net.minecraftforge.fml.config.ModConfig` → `net.neoforged.fml.config.ModConfig`
  - `net.minecraftforge.fml.loading.FMLPaths` → `net.neoforged.fml.loading.FMLPaths`
  - 所有 `ForgeConfigSpec` 引用 → `ModConfigSpec`
  - `ForgeConfigSpec.Builder` → `ModConfigSpec.Builder`
  - `ForgeConfigSpec.BooleanValue` → `ModConfigSpec.BooleanValue`
  - `ForgeConfigSpec.ConfigValue` → `ModConfigSpec.ConfigValue`
- **验收要点**:
  - [ ] 所有 ForgeConfigSpec 引用已替换为 ModConfigSpec
  - [ ] 包路径全部更新为 net.neoforged
  - [ ] 对比配置值 API 签名一致性（`ForgeConfigSpec.ConfigValue` → `ModConfigSpec.ConfigValue` 方法签名对齐）

---

#### TASK-022: 核心逻辑类迁移 (`ExtraLoader.java`)

- **scope**: `src/main/java/dev/gateguardian/extraloader/common/ExtraLoader.java`
- **DoD**: 文件编译通过，API 调用适配 NeoForge
- **目标**: 迁移 ExtraLoader 核心逻辑
- **输入**: 当前 ExtraLoader.java
- **输出**: 更新后的 ExtraLoader.java
- **依赖**: TASK-003（构建脚本）
- **具体变更**:
  - `net.minecraftforge.event.AddPackFindersEvent` → `net.neoforged.neoforge.event.AddPackFindersEvent`
  - `net.minecraftforge.fml.config.ModConfig` → `net.neoforged.fml.config.ModConfig`
  - `net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext` → 适配 NeoForge（改接收 `IEventBus` 或使用 `ModLoadingContext`）
  - `net.minecraftforge.fml.loading.FMLLoader` → `net.neoforged.fml.loading.FMLLoader`
  - `net.minecraftforge.fml.loading.FMLPaths` → `net.neoforged.fml.loading.FMLPaths`
  - `context.registerConfig(...)` → 确认 NeoForge 中的配置注册 API
  - `context.getModEventBus()` → 适配新构造器参数
- **验收要点**:
  - [ ] 所有 net.minecraftforge 引用已替换
  - [ ] 配置注册和事件监听逻辑适配 NeoForge

---

#### TASK-023: 仓库源迁移 (`ExtraRepositorySource.java`)

- **scope**: `src/main/java/dev/gateguardian/extraloader/common/pack/ExtraRepositorySource.java`
- **DoD**: 文件编译通过，Pack 系统 API 适配 1.21.1
- **目标**: 迁移自定义 Pack RepositorySource
- **输入**: 当前 ExtraRepositorySource.java
- **输出**: 更新后的 ExtraRepositorySource.java
- **依赖**: TASK-003（构建脚本）
- **具体变更**:
  - `net.minecraft.server.packs.FilePackResources` → 检查 1.21.1 中是否存在（可能改为 `PathPacks` 或 `PackResources` 的新实现）
  - `net.minecraft.server.packs.PathPackResources` → 检查 1.21.1 中是否存在
  - `net.minecraft.server.packs.repository.RepositorySource` → 确认接口未变
  - `Pack.readMetaAndCreate(...)` → 验证签名变化（1.21.1 可能增加了参数或改变了重载）
  - `new FilePackResources(name, path.toFile(), false)` → 可能需改为 `new FilePackResources(name, path, false)`（Path 而非 File）
  - `new PathPackResources(name, path, false)` → 验证构造函数签名
- **验收要点**:
  - [ ] Pack 系统 API 调用适配 1.21.1 签名
  - [ ] 文件包和目录包的资源创建逻辑正确

---

#### TASK-024: 加载模式迁移 (`PackLoadMode.java`)

- **scope**: `src/main/java/dev/gateguardian/extraloader/common/pack/PackLoadMode.java`
- **DoD**: 文件编译通过，PackSource API 适配
- **目标**: 迁移 PackSource 创建逻辑
- **输入**: 当前 PackLoadMode.java
- **输出**: 更新后的 PackLoadMode.java
- **依赖**: TASK-003（构建脚本）
- **具体变更**:
  - `PackSource.create(...)` → 验证 1.21.1 中 `PackSource` 的 API（可能使用新的工厂方法或 builder）
  - 检查 `PackSource` 的包路径 `net.minecraft.server.packs.repository.PackSource` 是否变化
- **验收要点**:
  - [ ] PackSource 创建逻辑适配 1.21.1 API
  - [ ] 枚举定义正确

---

#### TASK-025: 客户端入口迁移 (`ExtraLoaderClient.java`)

- **scope**: `src/main/java/dev/gateguardian/extraloader/client/ExtraLoaderClient.java`
- **DoD**: 文件编译通过
- **目标**: 迁移客户端入口类
- **输入**: 当前 ExtraLoaderClient.java
- **输出**: 更新后的 ExtraLoaderClient.java
- **依赖**: TASK-022（父类 ExtraLoader）
- **具体变更**:
  - `net.minecraftforge.event.AddPackFindersEvent` → `net.neoforged.neoforge.event.AddPackFindersEvent`
  - `net.minecraftforge.fml.config.ModConfig` → `net.neoforged.fml.config.ModConfig`
  - `net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext` → 适配 NeoForge
  - 构造器参数同步 ExtraLoader 的变更
- **验收要点**:
  - [ ] import 全部更新
  - [ ] 构造器签名与 ExtraLoader 一致

---

#### TASK-026: 客户端事件迁移 (`ExtraClientEvents.java`)

- **scope**: `src/main/java/dev/gateguardian/extraloader/client/ExtraClientEvents.java`
- **DoD**: 文件编译通过，事件订阅 API 适配
- **目标**: 迁移客户端事件注册
- **输入**: 当前 ExtraClientEvents.java
- **输出**: 更新后的 ExtraClientEvents.java
- **依赖**: TASK-003（构建脚本）
- **具体变更**:
  - `net.minecraftforge.api.distmarker.Dist` → `net.neoforged.api.distmarker.Dist`
  - `net.minecraftforge.client.event.RegisterClientCommandsEvent` → `net.neoforged.neoforge.client.event.RegisterClientCommandsEvent`
  - `net.minecraftforge.eventbus.api.SubscribeEvent` → `net.neoforged.bus.api.SubscribeEvent`
  - `net.minecraftforge.fml.common.Mod` → `net.neoforged.fml.common.Mod`
- **验收要点**:
  - [ ] 所有事件相关包路径已迁移

---

#### TASK-027: 客户端命令迁移 (`ExtraClientCommands.java`)

- **scope**: `src/main/java/dev/gateguardian/extraloader/client/registry/ExtraClientCommands.java`
- **DoD**: 文件编译通过（通常命令 API 在 1.21.1 变化较小）
- **目标**: 审查并适配命令注册 API
- **输入**: 当前 ExtraClientCommands.java
- **输出**: 审查/更新后的 ExtraClientCommands.java
- **依赖**: TASK-003（构建脚本）
- **具体变更**:
  - 该文件当前不直接引用 Forge 类（仅使用 vanilla Brigadier API），预计无需变更
  - 确认 `Util.getPlatform().openFile(...)` 在 1.21.1 中签名未变
  - 确认 `CommandSourceStack.sendSuccess(...)` / `sendFailure(...)` 签名未变
- **验收要点**:
  - [ ] 编译通过，命令注册逻辑正确
  - [ ] `Util.getPlatform().openFile(...)` 签名在 1.21.1 中与调用方一致

---

#### TASK-028: Mixin 迁移 (`OptionsMixin.java`)

- **scope**: `src/main/java/dev/gateguardian/extraloader/mixin/client/OptionsMixin.java`
- **DoD**: 文件编译通过，mixin 目标方法在 1.21.1 中存在
- **目标**: 验证 mixin 目标在 1.21.1 Options 类中签名匹配
- **输入**: 当前 OptionsMixin.java
- **输出**: 验证/更新后的 OptionsMixin.java
- **依赖**: TASK-003（构建脚本）
- **具体变更**:
  - 当前 mixin 引用的方法和类多为 vanilla Minecraft 类，通常不变
  - 验证 `updateResourcePacks(PackRepository)` 和 `loadSelectedResourcePacks(PackRepository)` 在 1.21.1 中签名一致
  - 验证 `@WrapOperation` 中的 `@At` 目标描述子正确
- **验收要点**:
  - [ ] Mixin 目标方法在 1.21.1 中存在且签名匹配
  - [ ] 编译通过

---

#### TASK-029: 数据生成迁移 (`DataGenerator.java`)

- **scope**: `src/main/java/dev/gateguardian/extraloader/generator/DataGenerator.java`
- **DoD**: 文件编译通过，GatherDataEvent 包路径更新
- **目标**: 迁移数据生成入口
- **输入**: 当前 DataGenerator.java
- **输出**: 更新后的 DataGenerator.java
- **依赖**: TASK-003（构建脚本）
- **具体变更**:
  - `net.minecraftforge.data.event.GatherDataEvent` → `net.neoforged.neoforge.data.event.GatherDataEvent`
  - `net.minecraftforge.eventbus.api.SubscribeEvent` → `net.neoforged.bus.api.SubscribeEvent`
  - `net.minecraftforge.fml.common.Mod` → `net.neoforged.fml.common.Mod`
  - 验证 `GatherDataEvent` 在 1.21.1 中的 API（getGenerator、getPackOutput 等）
  - ⚠️ **联动注意**：`ExtraLanguageProvider`（TASK-030）的接口签名变化可能波及 `DataGenerator` 中调用 `addProvider(...)` 的方式。建议同步审查两个文件的构造函数签名。
- **验收要点**:
  - [ ] 所有包路径已更新
  - [ ] GatherDataEvent API 调用适配 1.21.1
  - [ ] `@Mod` 注解 value 属性值与 mod id `extraloader` 一致

---

#### TASK-030: 语言提供者迁移 (`ExtraLanguageProvider.java`)

- **scope**: `src/main/java/dev/gateguardian/extraloader/generator/provider/ExtraLanguageProvider.java`
- **DoD**: 文件编译通过，LanguageProvider 包路径更新
- **目标**: 迁移语言提供者类
- **输入**: 当前 ExtraLanguageProvider.java
- **输出**: 更新后的 ExtraLanguageProvider.java
- **依赖**: TASK-003（构建脚本）
- **具体变更**:
  - `net.minecraftforge.common.data.LanguageProvider` → `net.neoforged.neoforge.common.data.LanguageProvider`
- **验收要点**:
  - [ ] LanguageProvider 包路径更新

---

### 阶段 3 — 清理与构建验证

> 本阶段在所有源码迁移完成后进行。

#### TASK-040: Gradle Wrapper 版本确认

- **scope**: `gradle/wrapper/gradle-wrapper.properties`
- **DoD**: Gradle 版本适配 NeoForge 1.21.1 构建需求
- **目标**: 确认 Gradle 版本 >= 9.2（当前配置），若不足则升级
- **输入**: 当前 `gradle-wrapper.properties`
- **输出**: 确认/更新后的属性文件
- **依赖**: 无
- **具体变更**:
  - 当前 gradle-9.2.0-bin.zip（通常满足 NeoForge 需求）
  - 验证 `net.neoforged.moddev` 插件对 Gradle 版本的要求
- **验收要点**:
  - [ ] Gradle 版本符合插件要求

---

#### TASK-041: 资源目录审计

- **scope**: `src/main/resources/`、`src/main/templates/`
- **DoD**: 资源文件无误，无 Forge 遗留残留
- **目标**: 清理不再需要的 Forge 资源文件
- **输入**: 当前 resources 和 templates 目录
- **输出**: 清理后的目录结构
- **依赖**: TASK-010、TASK-013
- **具体变更**:
  - 删除旧的 `mods.toml`（`src/main/templates/META-INF/mods.toml`）
  - 确认 templates 目录结构适配 NeoForge
  - 确认 `pack.mcmeta` 位于正确位置
- **验收要点**:
  - [ ] 旧的 `mods.toml` 已被删除（`src/main/templates/META-INF/mods.toml`）
  - [ ] 无残留 Forge 专有资源文件
  - [ ] 目录结构符合 NeoForge 规范

---

#### TASK-042: 构建验证

- **scope**: 整个项目
- **DoD**: `gradlew build` 成功，产出可运行的 JAR
- **目标**: 全量编译验证
- **输入**: 所有阶段产出的文件
- **输出**: 成功构建的 JAR 文件
- **依赖**: 所有前置 TASK
- **具体变更**:
  - 运行 `gradlew build --no-daemon`
  - 修复编译错误
  - 修复资源处理错误
  - 确认 JAR 包含正确文件
- **验收要点**:
  - [ ] `gradlew build` 成功（退出码 0）
  - [ ] 生成的 JAR 可用 `java -jar` 检查结构

---

## 任务依赖图

```
阶段 0（构建系统基础）
  TASK-000 (libs.versions.toml) ───┐
  TASK-001 (settings.gradle.kts)   ├── 并行（无相互依赖）
  TASK-002 (gradle.properties) ────┘
        │
        └── TASK-003 (build.gradle.kts) ← 依赖 TASK-000, TASK-002
              │
              └── TASK-004 (删除 repositories) ← 依赖 TASK-003（确认内联后删除）
                    │
        ┌───────────┴───────────┐
        ▼                       ▼
阶段 1（模组元数据）         阶段 2（Java 源码）
（与阶段 2 可并行）       （与阶段 1 可并行）
  TASK-010 (neoforge.mods.toml) ← 依赖 TASK-002
  TASK-011 (pack.mcmeta) ─── 无依赖
  TASK-012 (mixins.json) ─── 无依赖
  TASK-013 (清理 AT) ─────── 无依赖
                              TASK-020 (Bootstrap.java)
                              TASK-021 (ExtraConfig.java)
                              TASK-022 (ExtraLoader.java) ← TASK-025 依赖此任务
                              TASK-023 (ExtraRepositorySource.java)
                              TASK-024 (PackLoadMode.java)
                              TASK-025 (ExtraLoaderClient.java) ← 依赖 TASK-022
                              TASK-026 (ExtraClientEvents.java)
                              TASK-027 (ExtraClientCommands.java)
                              TASK-028 (OptionsMixin.java)
                              TASK-029 (DataGenerator.java)
                              TASK-030 (ExtraLanguageProvider.java)
  │  （阶段 1 内部全部并行）  │  （阶段 2 内部除 TASK-022→025 外均为并行关系）
  │                           │
  └───────────┬───────────────┘
              ▼
阶段 3（清理与验证）
  TASK-040 (Gradle Wrapper)
  TASK-041 (资源审计)
  TASK-042 (构建验证) ← 依赖所有前置任务
```

---

## 串行/并行策略

| 组 | 执行模式 | 说明 |
|---|---------|------|
| TASK-000, 001, 002 | **并行** | 三个配置文件独立修改 |
| TASK-003 | 等待 000+002 | 需要版本目录和属性 |
| TASK-004 | 等待 TASK-003 | 确认仓库内联后删除文件 |
| TASK-010 ~ 013（阶段 1） | **与阶段 2 并行** | 仅依赖阶段 0，与阶段 2 无相互依赖 |
| TASK-020 ~ 030（除 025）（阶段 2） | **与阶段 1 并行** | 仅依赖阶段 0，与阶段 1 无相互依赖 |
| TASK-025 | 等待 TASK-022 | 子类依赖父类接口 |
| TASK-040, 041 | **并行** | 两者独立 |
| TASK-042 | 等待全部完成 | 最终构建验证 |

---

## 风险管理

### 已知风险

| # | 风险 | 影响 | 概率 | 降级/缓解方案 |
|---|------|------|------|-------------|
| R1 | `ForgeConfigSpec` → `ModConfigSpec` API 不兼容导致配置行为变化 | 配置文件读写异常 | 中 | 对比 ModConfigSpec 文档；保留向后兼容的配置加载逻辑 |
| R2 | `Pack.readMetaAndCreate` 在 1.21.1 中签名变化 | 额外仓库源无法注册 | 高 | 查阅 1.21.1 NeoForge Pack 系统源码；必要时使用 Pack.Builder 替代 |
| R3 | `FilePackResources` / `PathPackResources` 在 1.21.1 中被移除或重构 | 包资源加载失败 | 高 | 使用 `PackFileResources` 或 `PathResources` 等新 API；参考 NeoForge 源码 |
| R4 | `FMLJavaModLoadingContext` 被移除（NeoForge 不再使用） | 入口类和配置注册需重写 | 高 | 改为 `IEventBus` 构造器参数；使用 `ModLoadingContext` 注册配置 |
| R5 | Mixin 目标方法在 1.21.1 Options 类中签名变化 | Mixin 注入失败，启动崩溃 | 中 | 检查 1.21.1 Options 源码调整 @At 描述子；或使用 @Overwrite 兜底 |
| R6 | `net.neoforged.moddev` 插件版本与 NeoForge 版本不兼容 | 构建系统配置困难 | 中 | 使用与 NeoForge 版本匹配的 ModDevGradle 版本（参考官方文档） |
| R7 | 依赖组件（JEI/EMI/REI）在 1.21.1 NeoForge 上不可用 | 编译或运行时缺失依赖 | 低-中 | 降级方案：注释依赖，作为可选/编译期仅依赖 |
| R8 | `DistExecutor` API 移除或变更 | 客户端/服务端分发逻辑需重写 | 中 | 检查 NeoForge 中的等效 API；或使用 `@OnlyIn` / `DistExecutor.safeRunForDist` |

### 回退方案

1. **阶段回退**：每个 TASK 完成后立即进行局部编译验证；若某 TASK 失败则只回退该文件，不影响同阶段其他任务
2. **回退到基线**：若阶段 2 中出现大规模 API 不兼容，使用 `git stash` 或 `git checkout` 回退到 `1.21.1` 分支的初始状态
3. **降级目标**：如 NeoForge 1.21.1 构建不可行，保留当前 `legacyForge` 配置，仅更新源码包路径（降级为 Forge-on-NeoForge 兼容模式） — ⚠️ **需验证前提**：确认 `net.neoforged.moddev.legacyforge` DSL 在目标 NeoForge 版本仍可用。若已废弃则此方案不适用。

---

## DoD（完成定义）

### 阶段 0 — 构建系统与依赖基础
- [ ] `gradle/libs.versions.toml` 所有版本指向 1.21.1 NeoForge 兼容版本
- [ ] `settings.gradle.kts` 不再引用 MinecraftForge maven
- [ ] `gradle.properties` 版本号、范围和 Java 版本正确
- [ ] `build.gradle.kts` 使用 `neoForge {}` DSL，Java 21，所有依赖正确
- [ ] `repositories.gradle.kts` 已删除，仓库已内联至 build.gradle.kts

### 阶段 1 — 模组元数据与资源适配
- [ ] `neoforge.mods.toml` 已创建，依赖声明指向 neoforge
- [ ] `pack.mcmeta` 中 `pack_format` 为 32
- [ ] `extraloader.mixins.json` 中 `compatibilityLevel` 为 `JAVA_21`
- [ ] 空的 `accesstransformer.cfg` 已移除

### 阶段 2 — Java 源码迁移
- [ ] 所有 Java 文件中 `net.minecraftforge.*` 导入已替换为 `net.neoforged.*`
- [ ] 入口类 `Bootstrap.java` 构造器适配 NeoForge 规范
- [ ] 配置类 `ExtraConfig.java` 使用 `ModConfigSpec`
- [ ] 核心类 `ExtraLoader.java` API 适配 NeoForge
- [ ] Pack 系统类（ExtraRepositorySource、PackLoadMode）适配 1.21.1 API
- [ ] 客户端类（ExtraLoaderClient、ExtraClientEvents、ExtraClientCommands）迁移完成
- [ ] Mixin（OptionsMixin）目标方法在 1.21.1 中签名匹配
- [ ] 数据生成类（DataGenerator、ExtraLanguageProvider）迁移完成

### 阶段 3 — 清理与构建验证
- [ ] Gradle Wrapper 版本 >= 9.2
- [ ] 旧的 `mods.toml` 已删除，无 Forge 遗留资源残留
- [ ] `gradlew build` 成功（退出码 0），产出可运行的 JAR

---

## 总估算

| 指标 | 值 |
|------|-----|
| 阶段数 | 4（阶段 0~3） |
| 总任务数 | 23 个 |
| 总复杂度 | **高**（涉及构建系统、元数据、源码三重迁移） |
| 关键路径 | TASK-000 → TASK-003 → TASK-022 → TASK-025 → TASK-042 |
| 预计构建验证轮次 | 3~5 轮 |
| 并行执行窗口 | 阶段 1 与阶段 2 可完全并行，阶段 0 内部有 3 任务并行 |
