## 计划元数据

- 计划ID: `extra_loader_26_1_2`
- 正式路径: `plans/plan_extra_loader_26_1_2.md`
- 草稿路径: `plans/.draft_plan_extra_loader_26_1_2.md`
- 版本状态: `已批准`
- 审查结论: `条件通过 - 已修复`
- 审查日期: `2026-06-16`
- 触发原因: `初版`
- 规划代理: `猫娘规划师-缇娅`

# 计划：ExtraLoader MC 26.1.2 NeoForge 移植

## 概述

### 目标
将 ExtraLoader 从 **NeoForge 1.21.1 (21.1.218)** 移植到 **MC 26.1.2 NeoForge**，跟进上流 NeoForge 框架版本，保持与最新 Minecraft 版本的兼容性。

### 范围
- 构建系统版本升级（minecraft_version=26.1.2，Java 25，ModDevGradle 2.0.140）
- Mixin 0.8.7 → 0.17.3 适配（API 层面向前兼容，无需特殊处理；注意 `mixin.env.classReaderExpandFrames` 可能需要显式设置）
- MixinExtras 0.5.0-rc.3 → 0.5.4 适配（`@WrapOperation` 不受影响）
- NeoForge API 跨版本适配（21.x → 26.x）
- Java 25 语言特性适配（含 `javax.annotation` → `org.jetbrains.annotations` 迁移）
- 模组元数据文件更新（pack_format、minVersion 等）
- 依赖组件版本更新（JEI、EMI、Jade、ModernUI 等）
- 构建验证与冒烟测试
- **不包含**：功能改动、重构、新增功能、测试框架搭建
- **不包含**：Forge→Neo 迁移（源码已处于 NeoForge，仅做版本升级）

### 约束
- 源头项目：`E:\GitHub\ExtraLoader\ExtraLoader-1.21.1`（分支 `1.21.1`，已完成 NeoForge 移植）
- 目标分支：`26.1.2`（基于 `1.21.1` 创建）
- 目标 worktree：`E:\GitHub\ExtraLoader\ExtraLoader-26.1.2`
- 远程 fork：`myfork` → `xingluo01/ExtraLoader.git`
- 保留原有 mod id (`extraloader`)、包结构 (`dev.gateguardian.extraloader`) 与功能行为
- 移植完成后需能通过 `gradlew build` 编译
- **注意事项**：Mixin 0.8.7 → 0.17.3 是跨大版本跳升，但预调查确认 API 层面向前兼容，无破坏性变更。refmap 格式无变化。`@WrapOperation`（MixinExtras）不受影响。注意 `mixin.env.classReaderExpandFrames` 可能需要显式设置

### 与 1.21.1 移植的区别（重要上下文）

| 方面 | 1.21.1 移植 | 26.1.2 移植 |
|------|-------------|-------------|
| 迁移方向 | Forge → NeoForge | NeoForge 21.x → 26.x |
| 构建 DSL | legacyForge → neoForge | 保持 neoForge，更新版本 |
| Import 路径 | net.minecraftforge → net.neoforged | 已为 net.neoforged，仅调 API 签名 |
| Mixin | 0.8.7（不变） | 0.8.7 → 0.17.3 ✅（API 向前兼容，无破坏性变更） |
| Java | 17 → 21 | 21 → 25 ⚠️ |
| 参考项目 | 有 hoarding 项目可参考 | 预调查已覆盖 6 大关键项 ✅（详见"需先行调查的事项"） |
| 跨版本幅度 | 1.20.1 → 1.21.1（单版本） | 21.x → 26.x（多版本跨度） |

---

## 执行阶段

### 阶段 0 — 分支与 worktree 初始化

> 本阶段是物理前置条件，必须最先完成。

#### TASK-000: 创建远程分支与本地 worktree

- **scope**: Git 操作
- **DoD**: 远程 `myfork` 上存在 `26.1.2` 分支，本地 `ExtraLoader-26.1.2` worktree 可用
- **目标**: 建立隔离的工作空间
- **输入**: 当前 `1.21.1` 分支、远程 `myfork` 配置
- **输出**: 就绪的 worktree 工作空间
- **依赖**: 无
- **具体变更**:
  - `git push myfork 1.21.1:26.1.2`（远程创建 26.1.2 分支）
  - `git worktree add -b 26.1.2 ../ExtraLoader-26.1.2 myfork/26.1.2`（或等效操作）
- **验收要点**:
  - [ ] 远程 `myfork/26.1.2` 分支存在
  - [ ] 本地 `E:\GitHub\ExtraLoader\ExtraLoader-26.1.2` worktree 存在且指向 26.1.2
  - [ ] worktree 与 `1.21.1` 分支完全一致（初始状态）

---

### 阶段 1 — 构建系统与依赖升级

> 本阶段是编译就绪的前置条件。**与阶段 0 有前后依赖**（需在 worktree 内操作）。

#### TASK-100: Gradle 属性更新 (`gradle.properties`)

- **scope**: `gradle.properties`
- **DoD**: 所有版本号已更新为 26.1.2 目标值
- **目标**: 更新 Minecraft 版本、Java 版本、NeoForge 版本等属性
- **输入**: 当前 `gradle.properties`（1.21.1 值）
- **输出**: 更新后的 `gradle.properties`（含模组主版本号升级）
- **依赖**: TASK-000（worktree 就绪）
- **具体变更**:
  - `minecraft_version=1.21.1` → `26.1.2`
  - `minecraft_version_range` → 建议查阅 NeoForge 26.1.2 MDK 中的 gradle.properties 确认兼容范围
  - `neo_version=21.1.218` → `26.1.2.76`（预调查确认：对应 NeoForge 26.1.2）
  - `neo_version_range` → 需查阅 NeoForge 官方规范，建议 `[26,)`
  - `loader_version_range=[4,)` → 建议确认后保持或按官方 MDK 调整
  - Java 版本属性（如有）：`java_version=21` → `25`
  - 模组版本号按规范升级（如 `2.0.0 → 3.0.0` 或其他对应 26.1.2 的版本号），确保与 TASK-401（neoforge.mods.toml）中的版本号一致
- **验收要点**:
  - [ ] minecraft_version 为 26.1.2
  - [ ] neo_version 为 26.1.2.76
  - [ ] 版本范围合理
  - [ ] Java 版本为 25

#### TASK-101: 版本目录升级 (`gradle/libs.versions.toml`)

- **scope**: `gradle/libs.versions.toml`
- **DoD**: 所有版本指向 26.1.2 NeoForge 兼容版本
- **目标**: 更新 version catalog
- **输入**: 当前 `gradle/libs.versions.toml`
- **输出**: 更新后的版本目录
- **依赖**: TASK-000（worktree 就绪）
- **具体变更**:
  - `minecraft = "1.21.1"` → `"26.1.2"`
  - `neo = "21.1.218"` → `"26.1.2.76"`（预调查确认）
  - `mixin = "0.8.7"` → `"0.17.3"`（API 向前兼容，无破坏性变更 ✅）
  - `mixinExtras = "0.5.0-rc.3"` → `"0.5.4"`
  - `modDevGradle = "2.0.122"` → `"2.0.140"`（预调查确认）
  - `lombok = "9.1.0"` → 确认兼容性（可保持）
  - `jetbrains-annotations = "26.0.1"` → 可考虑升级（保持也可）
  - 所有依赖 artifact 版本更新至 26.1.2 对应版本：
    - `emi` → **不可用**（EMI 只到 MC 1.21.1，26.1.2 NeoForge 无对应版本 → 需注释掉相关依赖声明）
    - `jei` → **可用**（26.1.x NeoForge），具体最新版本号需查 Modrinth/CF
    - `jade` → **可用**（26.1.x NeoForge），具体最新版本号需查 Modrinth
    - `modernui` → 确认 26.1.2 兼容版本（需调查）
    - `jecharacters` → 确认 26.1.2 兼容版本（需调查）
- **验收要点**:
  - [ ] mixin 版本为 0.17.3（API 向前兼容，无需特殊处理）
  - [ ] mixinExtras 版本为 0.5.4
  - [ ] modDevGradle 版本为 2.0.140
  - [ ] neo 版本为 26.1.2.76
  - [ ] EMI 依赖已注释掉（因不兼容 26.1.2）
  - [ ] 所有可用的依赖 artifact 版本兼容 26.1.2

#### TASK-102: 构建脚本更新 (`build.gradle.kts`)

- **scope**: `build.gradle.kts`
- **DoD**: Java toolchain 版本为 25，其他配置适配 26.1.2
- **目标**: 更新 Java 版本和构建配置
- **输入**: 当前 `build.gradle.kts`、TASK-101 产出的版本目录
- **输出**: 更新后的 `build.gradle.kts`
- **依赖**: TASK-101（版本目录）
- **具体变更**:
  - `java { toolchain { languageVersion = JavaLanguageVersion.of(21) } }` → `JavaLanguageVersion.of(25)`
  - 确认 `neoForge { version = libs.versions.neo.get() }` 在新版本中 DSL 未变
  - 确认 runs 配置 DSL 在 26.1.2 中保持兼容
  - 仓库部分：确认所有 maven 仓库在 26.1.2 下仍有效
  - dependencies：确认 `jarJar(libs.mixinExtras.neoforge)` 等配置仍然有效
- **验收要点**:
  - [ ] Java toolchain 版本为 25
  - [ ] neoForge DSL 配置正确
  - [ ] 所有仓库可用

#### TASK-103: Gradle Wrapper 确认/升级

- **scope**: `gradle/wrapper/gradle-wrapper.properties`
- **DoD**: Gradle 版本满足 ModDevGradle 2.0.140 需求
- **目标**: 确认 Gradle 版本兼容性
- **输入**: 当前 `gradle-wrapper.properties`（Gradle 9.2.0）
- **输出**: 确认或升级后的属性文件
- **依赖**: TASK-101（了解插件版本需求）
- **具体变更**:
  - 当前 Gradle 9.2.0 可能已满足 2.0.140 需求，但需验证
  - 如不满足：升级 Gradle 版本
  - 如满足：记录确认
- **验收要点**:
  - [ ] Gradle 版本符合 ModDevGradle 2.0.140 要求

---

### 阶段 2 — Mixin 基础设施升级

> ✅ **预调查已完成**：Mixin 0.8.7 → 0.17.3 API 层面 **向前兼容**，无破坏性变更。refmap 格式无变化。`@WrapOperation`（MixinExtras）不受影响。本阶段由高风险降级为常规操作。

#### TASK-200: Mixin 0.17.3 API 变更确认（预调查已覆盖）

- **scope**: 确认任务（无代码改动）
- **DoD**: 确认预调查结论，记录注意事项
- **目标**: 验证 0.8.7 → 0.17.3 的兼容性结论
- **输入**: Mixin 0.17.3 release notes（可选验证）
- **输出**: 确认记录（可内联到本计划）
- **依赖**: 无
- **预调查结论**:
  - `@Mixin`、`@Inject`、`@At` 等注解 API **无破坏性变更**
  - `@WrapOperation`（MixinExtras）**不受影响**
  - refmap 格式**无变化**
  - **注意**：`mixin.env.classReaderExpandFrames` 可能需要显式设置（如在 JVM 参数或 `build.gradle.kts` 中配置）
  - `compatibilityLevel`：0.17.3 支持 `JAVA_21`，但 `JAVA_25` 需验证原生支持情况；如不持支可保持 `JAVA_21`
  - 与 MixinExtras 0.5.4 兼容性良好
  - 服务端 API（`IMixinConfig` 等）无破坏性变更
- **验收要点**:
  - [x] 已明确 0.8.7 → 0.17.3 无破坏性变更（预调查确认）
  - [ ] 已记录 `mixin.env.classReaderExpandFrames` 注意事项
  - [ ] 已确认 `compatibilityLevel` 方案（JAVA_21 或 JAVA_25）

#### TASK-201: Mixin 配置文件更新 (`extraloader.mixins.json`)

- **scope**: `src/main/resources/extraloader.mixins.json`
- **DoD**: Mixin 配置适配 0.17.3
- **目标**: 更新 mixin 兼容等级和配置格式
- **输入**: 当前 `extraloader.mixins.json`、TASK-200 确认结果
- **输出**: 更新后的 `extraloader.mixins.json`
- **依赖**: TASK-200（确认完成）
- **具体变更**:
  - `"compatibilityLevel": "JAVA_21"` → 可保持 `"JAVA_21"`（0.17.3 明确支持），如需使用 Java 25 特性则尝试 `"JAVA_25"`（需验证 Mixin 0.17.3 是否原生支持）
  - `"minVersion": "0.8"` → `"0.17"`（更新最低版本要求）
  - refmap 格式无变化，无需调整
  - `"required"` / `"priority"` 等字段行为不变
- **验收要点**:
  - [ ] compatibilityLevel 值合理（JAVA_21 保底，JAVA_25 可选）
  - [ ] minVersion 更新为 0.17
  - [ ] refmap 配置未破坏

#### TASK-202: Mixin 源码适配 (`OptionsMixin.java`)

- **scope**: `src/main/java/dev/gateguardian/extraloader/mixin/client/OptionsMixin.java`
- **DoD**: Mixin 编译通过，注入点匹配 26.1.2 目标类
- **目标**: 验证 Mixin 注解 API 在 0.17.3 中兼容，确认目标类签名变化
- **输入**: 当前 `OptionsMixin.java`、TASK-200 确认结果
- **输出**: 确认/调整后的 `OptionsMixin.java`（如需要）
- **依赖**: TASK-200（确认完成）、TASK-102（构建脚本就绪）
- **预调查确认**:
  - `@WrapOperation` 签名在 0.17.3 中**无变化**，不受影响
  - MixinExtras 0.5.4 与 Mixin 0.17.3 兼容
  - 无需特殊注解适配
- **具体变更**:
  - 主要关注目标方法 `updateResourcePacks` 和 `loadSelectedResourcePacks` 在 26.1.2 `Options` 类中的**签名变化**（Minecraft 版本升级导致，非 Mixin 版本导致）
  - `@At` 描述子可能因目标类变化需调整
  - 不需要因 Mixin 版本升级而调整 Mixin 注解 API
- **验收要点**:
  - [ ] Mixin 注入目标在 26.1.2 中存在且签名匹配
  - [ ] MixinExtras 的 `@WrapOperation` 签名兼容 0.5.4（预调查确认 ✅）
  - [ ] 编译通过

---

### 阶段 3 — NeoForge API 适配（源码层）

> ⚠️ **核心工作阶段**。NeoForge 21.x → 26.x 跳过多版本，事件系统、配置系统 API 可能变化。
> Pack 系统 API 已通过预调查确认大部分兼容（`Pack.readMetaAndCreate` 签名无变化）。
> 建议每个 TASK 完成后立即局部编译验证。

#### TASK-300: 入口类检查 (`Bootstrap.java`)

- **scope**: `src/main/java/dev/gateguardian/extraloader/Bootstrap.java`
- **DoD**: 编译通过，构造器签名适配 26.1.2
- **目标**: 验证 NeoForge 入口类规范在 26.1.2 中未变
- **输入**: 当前 `Bootstrap.java`
- **输出**: 确认/调整后的 `Bootstrap.java`
- **依赖**: TASK-102（构建脚本就绪）
- **具体变更**:
  - `@Mod` 注解包路径 `net.neoforged.fml.common.Mod` 是否变化
  - `IEventBus` 构造器模式在 26.1.2 中是否依然支持
  - `FMLEnvironment.dist` 是否存在
- **验收要点**:
  - [ ] Bootstrap.java 编译通过
  - [ ] 构造器签名与 26.1.2 规范一致

#### TASK-301: 配置类检查 (`ExtraConfig.java`)

- **scope**: `src/main/java/dev/gateguardian/extraloader/common/ExtraConfig.java`
- **DoD**: 编译通过，配置系统 API 适配 26.1.2
- **目标**: 验证 NeoForge 配置系统在 26.1.2 中 API 未变
- **输入**: 当前 `ExtraConfig.java`
- **输出**: 确认/调整后的 `ExtraConfig.java`
- **依赖**: TASK-102（构建脚本就绪）
- **具体变更**（需验证项）:
  - `net.neoforged.neoforge.common.ModConfigSpec` → 包路径和 API 是否变化
  - `ModConfigSpec.Builder.configure()` 签名是否变化
  - `ModConfigSpec.BooleanValue`、`ConfigValue` 等内部类是否保持
  - `ModConfig.Type` 枚举值是否变化
  - `ModConfig` 注册流程是否变化
- **验收要点**:
  - [ ] ModConfigSpec API 在 26.1.2 中保持兼容
  - [ ] 配置定义逻辑编译通过

#### TASK-302: 核心逻辑适配 (`ExtraLoader.java`)

- **scope**: `src/main/java/dev/gateguardian/extraloader/common/ExtraLoader.java`
- **DoD**: 编译通过，核心逻辑适配 26.1.2 API
- **目标**: 验证核心 API 调用在 26.1.2 中兼容
- **输入**: 当前 `ExtraLoader.java`
- **输出**: 确认/调整后的 `ExtraLoader.java`
- **依赖**: TASK-102（构建脚本就绪）、TASK-301（配置类）
- **具体变更**（需验证项）:
  - `ModLoadingContext.get().getActiveContainer().registerConfig(...)` → 注册配置的 API 是否变化
  - `IEventBus.addListener(this::addPackFinders)` → 事件监听注册方式是否变化
  - `AddPackFindersEvent` 的包路径 `net.neoforged.neoforge.event.AddPackFindersEvent` 是否变化
  - `FMLLoader.versionInfo().mcVersion()` 是否仍可用
  - `FMLPaths` 的包路径和 API 是否变化
- **验收要点**:
  - [ ] 配置注册 API 在 26.1.2 中兼容
  - [ ] 事件监听 API 适配
  - [ ] 文件路径 API 适配

#### TASK-303: Pack 系统适配 (`ExtraRepositorySource.java`)

- **scope**: `src/main/java/dev/gateguardian/extraloader/common/pack/ExtraRepositorySource.java`
- **DoD**: 编译通过，Pack 系统 API 适配 26.1.2
- **目标**: 适配 Minecraft Pack 系统在 26.1.2 中的变化
- **输入**: 当前 `ExtraRepositorySource.java`
- **输出**: 确认/调整后的 `ExtraRepositorySource.java`
- **依赖**: TASK-102（构建脚本就绪）
- **预调查结论**:
  - `Pack.readMetaAndCreate(...)` 签名**无变化**（与 1.21.1 相同 ✅）
  - `RepositorySource` 接口 `loadPacks(Consumer<Pack>)` 签名**未变**
  - `FilePackResources` / `PathPackResources` **已不存在**，但 1.21.1 移植时已改用 `ResourcesSupplier`，本次无需改动 ✅
  - `Pack.Metadata` 新增 `isHidden` 字段——**不影响**现有代码（加载逻辑不依赖该字段）
  - `PackLocationInfo`、`PackSelectionConfig` 构造函数签名应保持兼容
  - `Pack.Position.TOP` 枚举值未变化
  - `Component.literal(...)` vanilia API 保持
- **具体变更**（需验证项）:
  - 主要验证目标类签名是否因 Minecraft 版本升级而变化，而非 Pack API 本身（API 本身向前兼容）
  - `RepositorySource` 接口——`loadPacks(Consumer<Pack>)` 签名
  - `Pack.readMetaAndCreate(...)` ——确认签名匹配（预调查认为无变化，但仍需编译验证）
  - `Pack.ResourcesSupplier` ——接口签名
- **验收要点**:
  - [ ] Pack 系统 API 在 26.1.2 中兼容（预调查确认签名无变化 ✅）
  - [ ] 自定义 RepositorySource 加载逻辑正确
  - [ ] `Pack.readMetaAndCreate` 编译通过

#### TASK-304: Pack 加载模式适配 (`PackLoadMode.java`)

- **scope**: `src/main/java/dev/gateguardian/extraloader/common/pack/PackLoadMode.java`
- **DoD**: 编译通过，PackSource API 适配 26.1.2
- **目标**: 验证 PackSource API 在 26.1.2 中兼容
- **输入**: 当前 `PackLoadMode.java`
- **输出**: 确认/调整后的 `PackLoadMode.java`
- **依赖**: TASK-102（构建脚本就绪）
- **具体变更**（需验证项）:
  - `PackSource.create(...)` ——工厂方法签名是否变化
  - `PackSource` 的包路径 `net.minecraft.server.packs.repository.PackSource` 是否变化
  - `ChatFormatting` 是否变化
- **验收要点**:
  - [ ] PackSource 创建逻辑在 26.1.2 中兼容

#### TASK-305: 客户端入口适配 (`ExtraLoaderClient.java`)

- **scope**: `src/main/java/dev/gateguardian/extraloader/client/ExtraLoaderClient.java`
- **DoD**: 编译通过
- **目标**: 验证子类适配,同步父类变更
- **输入**: 当前 `ExtraLoaderClient.java`
- **输出**: 确认/调整后的 `ExtraLoaderClient.java`
- **依赖**: TASK-302（ExtraLoader.java 适配）
- **具体变更**: 同步 ExtraLoader 的 API 变更
- **验收要点**:
  - [ ] 与 ExtraLoader 构造器签名一致
  - [ ] `addPackFinders` 重写签名正确

#### TASK-306: 客户端事件适配 (`ExtraClientEvents.java`)

- **scope**: `src/main/java/dev/gateguardian/extraloader/client/ExtraClientEvents.java`
- **DoD**: 编译通过
- **目标**: 验证事件总线和事件类在 26.1.2 中兼容
- **输入**: 当前 `ExtraClientEvents.java`
- **输出**: 确认/调整后的 `ExtraClientEvents.java`
- **依赖**: TASK-102（构建脚本就绪）
- **具体变更**（需验证项）:
  - `@EventBusSubscriber` 注解在 26.1.2 中是否保持兼容
  - `RegisterClientCommandsEvent` 包路径 `net.neoforged.neoforge.client.event.RegisterClientCommandsEvent` 是否变化
  - `Dist` 枚举 `net.neoforged.api.distmarker.Dist` 是否变化
- **验收要点**:
  - [ ] 事件订阅机制在 26.1.2 中兼容

#### TASK-307: 客户端命令适配 (`ExtraClientCommands.java`)

- **scope**: `src/main/java/dev/gateguardian/extraloader/client/registry/ExtraClientCommands.java`
- **DoD**: 编译通过
- **目标**: 验证命令 API 和 Util 工具类在 26.1.2 中兼容
- **输入**: 当前 `ExtraClientCommands.java`
- **输出**: 确认/调整后的 `ExtraClientCommands.java`
- **依赖**: TASK-102（构建脚本就绪）
- **具体变更**（需验证项）:
  - `Util.getPlatform().openFile(...)` ——签名是否变化
  - `CommandSourceStack.sendSuccess(...)` / `sendFailure(...)` ——签名可能变化（1.21.1 中 sendSuccess 需要 Supplier）
  - `Commands.literal(...)`、`Commands.argument(...)` ——Brigadier API 通常稳定
- **验收要点**:
  - [ ] `Util.getPlatform().openFile(directory.toFile())` 在 26.1.2 中可用
  - [ ] 命令注册逻辑编译通过

#### TASK-308: 数据生成适配 (`DataGenerator.java`)

- **scope**: `src/main/java/dev/gateguardian/extraloader/generator/DataGenerator.java`
- **DoD**: 编译通过
- **目标**: 验证数据生成 API 在 26.1.2 中兼容
- **输入**: 当前 `DataGenerator.java`
- **输出**: 确认/调整后的 `DataGenerator.java`
- **依赖**: TASK-102（构建脚本就绪）
- **具体变更**（需验证项）:
  - `GatherDataEvent` 包路径 `net.neoforged.neoforge.data.event.GatherDataEvent` 是否变化
  - `GatherDataEvent.getGenerator()`、`.includeClient()` 等 API 是否变化
  - `PackOutput` ——是否需要新的构造参数
- **验收要点**:
  - [ ] GatherDataEvent API 在 26.1.2 中兼容
  - [ ] 数据提供者注册逻辑正确

#### TASK-309: 语言提供者适配 (`ExtraLanguageProvider.java`)

- **scope**: `src/main/java/dev/gateguardian/extraloader/generator/provider/ExtraLanguageProvider.java`
- **DoD**: 编译通过
- **目标**: 验证 LanguageProvider API 在 26.1.2 中兼容
- **输入**: 当前 `ExtraLanguageProvider.java`
- **输出**: 确认/调整后的 `ExtraLanguageProvider.java`
- **依赖**: TASK-102（构建脚本就绪）
- **具体变更**（需验证项）:
  - `LanguageProvider` 包路径 `net.neoforged.neoforge.common.data.LanguageProvider` 是否变化
  - `LanguageProvider` 构造器签名是否变化
  - `addTranslations()` 方法签名（`add(String key, String value)`）是否变化
- **验收要点**:
  - [ ] LanguageProvider API 在 26.1.2 中兼容

---

### 阶段 4 — 模组元数据与资源更新

> 本阶段与阶段 2、3 无强依赖，可在阶段 1 完成后并行推进。

#### TASK-400: pack 格式更新 (`pack.mcmeta`)

- **scope**: `src/main/templates/pack.mcmeta`
- **DoD**: 使用 26.1.2 对应的 pack_format 101.1（min/max 格式）
- **目标**: 更新资源包格式为 26.1.2 规范
- **输入**: 当前 `pack.mcmeta`（pack_format: 32）
- **输出**: 更新后的 `pack.mcmeta`
- **依赖**: 无（预调查已完成 ⚡）
- **预调查结果**: MC 26.1.2 使用统一格式 **101.1**，采用 min/max 表达
- **具体变更**:
  - 旧格式：`"pack_format": 32`
  - 新格式：
    ```json
    {
      "pack": {
        "min_format": 101,
        "max_format": 101
      }
    }
    ```
- **验收要点**:
  - [ ] pack_format 使用 101.1 统一格式（min_format=101, max_format=101）
  - [ ] pack.mcmeta JSON 格式正确

#### TASK-401: 模组元数据审查 (`neoforge.mods.toml`)

- **scope**: `src/main/templates/META-INF/neoforge.mods.toml`
- **DoD**: 模组元数据适配 26.1.2 规范
- **目标**: 审查并更新模组描述文件
- **输入**: 当前 `neoforge.mods.toml`
- **输出**: 确认/更新后的 `neoforge.mods.toml`
- **依赖**: TASK-100（版本属性）
- **具体变更**（需验证项）:
  - `neoforge.mods.toml` 格式在 26.1.2 中是否保持兼容（NeoForge 可能已演进新的元数据格式）
  - `modLoader="javafml"` 是否仍然有效
  - `loaderVersion` 范围是否需要调整
  - 依赖声明中 `modId="neoforge"` 是否仍然有效
  - `[[mixins]]` 块格式在 Mixin 0.17.3 下是否保持
- **验收要点**:
  - [ ] neoforge.mods.toml 格式符合 26.1.2 规范
  - [ ] 依赖的版本范围使用正确的属性

#### TASK-402: 包注解审查 (`package-info.java`)

- **scope**: `src/main/java/dev/gateguardian/extraloader/package-info.java`
- **DoD**: 编译通过，注解兼容 26.1.2
- **目标**: 审查空值注解在 26.1.2 中是否兼容
- **输入**: 当前 `package-info.java`
- **输出**: 确认/调整后的 `package-info.java`
- **依赖**: TASK-102（构建脚本就绪）
  - ⚠️ **与 TASK-500（javax.annotation 迁移）有功能重叠**：TASK-402 仅审查 `package-info.java` 中的注解引用并记录问题，实际的 `javax.annotation → org.jetbrains.annotations` 源码修改在 TASK-500 中完成
- **预调查结果**:
  - `javax.annotation` 非 JDK 25 内置模块，可能无法直接访问
  - NeoForge 26.1.2 已捆绑 `org.jetbrains:annotations:24.0.1`（`@Nullable`、`@NotNull` 等）
  - 如果当前 `package-info.java` 使用 `javax.annotation.ParametersAreNonnullByDefault`，需迁移到 JetBrains 等价注解或使用 `@org.jetbrains.annotations.ApiStatus.Internal` 等替代方案
- **具体变更**（需验证项）:
  - `net.minecraft.FieldsAreNonnullByDefault` 是否存在于 26.1.2
  - `net.minecraft.MethodsReturnNonnullByDefault` 是否存在于 26.1.2
  - `javax.annotation.ParametersAreNonnullByDefault` → **如使用，需迁移到 `org.jetbrains.annotations.NotNull` 组合方案或 `javax.annotation` 的第三方兼容库**
  - 若 `package-info.java` 未使用 `javax.annotation`，则无需处理
  - **注意**：本任务仅审查不动手修改，实际修改在 TASK-500 中完成
- **验收要点**:
  - [ ] 已审查 `package-info.java` 中所有注解引用，识别出 `javax.annotation` 使用位置（如有）
  - [ ] 已记录需迁移的注解清单，移交 TASK-500 执行修改
  - [ ] 无 `javax.annotation` 依赖残留（此条由 TASK-500 验证）

---

### 阶段 5 — Java 25 语言特性适配

> 本阶段为**可选适配**阶段。Java 25 是 LTS 版本（？待确认），但即使未使用新语言特性，修改 toolchain 后旧代码也应能编译。
> 建议：先确保旧代码在 Java 25 toolchain 下编译通过，再考虑新特性应用。

#### TASK-500: Java 25 编译兼容性验证（含 javax.annotation 迁移）

- **scope**: 全项目
- **DoD**: 仅更新 toolchain 版本后，`gradlew build` 无编译错误；`javax.annotation` 已迁移
- **目标**: 验证现有 Java 21 代码在 Java 25 中编译兼容，处理 javax.annotation 不可用问题
- **输入**: 所有 Java 源文件
- **输出**: 编译成功的 JAR
- **依赖**: TASK-102（Java 25 toolchain 已设置）
- **预调查结果**:
  - `javax.annotation` **非 JDK 25 内置模块**，在 Java 25 中默认不可访问
  - NeoForge 已捆绑 `org.jetbrains:annotations:24.0.1`，提供 `@Nullable`、`@NotNull` 等
  - 建议迁移方案：搜索整个项目中所有对 `javax.annotation` 的引用，统一替换为 `org.jetbrains.annotations` 等价注解：
    - `javax.annotation.Nullable` → `org.jetbrains.annotations.Nullable`
    - `javax.annotation.Nonnull` → `org.jetbrains.annotations.NotNull`
    - `javax.annotation.ParametersAreNonnullByDefault` → 使用 JetBrains `@NotNull` 包级注解，或使用 `@org.jetbrains.annotations.ApiStatus.Internal`
- **具体变更**:
  - **步骤 1**：全项目搜索 `javax.annotation` 引用——`grep -r "javax.annotation" src/`
  - **步骤 2**：对每个出现位置，确认替换为 `org.jetbrains.annotations.*` 等价注解
  - **步骤 3**：更新 `package-info.java`（如适用）
  - **步骤 4**：更新 `build.gradle.kts` 确保 `org.jetbrains:annotations` 在 compileOnly 范围内（NeoForge 已捆绑，但可显式添加以防解析问题）
  - Java 9~21 语言特性在 Java 25 中向后兼容，一般无需额外处理
  - 模块化系统（JPMS）需确认是否有依赖库需要 module-info 声明
- **验收要点**:
  - [ ] 现有代码在 Java 25 toolchain 下编译通过
  - [ ] 所有 `javax.annotation` 引用已迁移到 `org.jetbrains.annotations`
  - [ ] 无弃用 API 警告（或已评估可忽略）

#### TASK-501: Java 25 新特性评估（可选）

- **scope**: 全项目
- **DoD**:
  - [ ] 列出 Java 25 中 >= 3 个新语言特性
  - [ ] 对每个特性给出"适用/不适用/待定"判断及理由
  - [ ] 如有采用，对应文件已修改
- **目标**: 评估并选择性采用 Java 25 新特性
- **输入**: Java 25 release notes
- **输出**: 评估记录（可决定不做任何更改）
- **依赖**: TASK-500（编译兼容）
- **评估范围**:
  - 值对象（Value Objects）——如果引入
  - 模式匹配增强——switch 表达式等
  - 字符串模板——如果引入
  - 其他语言特性变化
- **验收要点**:
  - [ ] 已评估 Java 25 新特性是否适用于 ExtraLoader（>= 3 个特性）
  - [ ] 每个特性已记录判断及理由
  - [ ] 如有采用，代码已更新

---

### 阶段 6 — 依赖组件版本更新

> 本阶段与阶段 3、4 并行。外部依赖的版本更新可能需要协调 API 适配。

#### TASK-600: 编译时依赖更新

- **scope**: `gradle/libs.versions.toml` + `build.gradle.kts`
- **DoD**: JEI compileOnly 依赖版本适配 26.1.2；EMI 依赖已注释/移除
- **目标**: 更新配方查看器 API 依赖
- **输入**: 当前 libs.versions.toml
- **输出**: 更新后的版本目录
- **依赖**: 无（预调查已完成 ⚡）
- **预调查结果**:
  - **JEI**：✅ 可用（26.1.x NeoForge），具体最新版本号需查 Modrinth 或 CF
  - **EMI**：❌ **不可用**（只到 MC 1.21.1，无 26.1.2 NeoForge 版本）
- **具体变更**:
  - JEI artifact: 保留原有 artifact 模式，更新版本号为 26.1.x 对应版本
  - EMI artifact: **注释掉或移除** `emi-neoforge` 依赖声明及其版本引用
  - 检查代码中 `if` 条件编译或 EMI 相关 API 引用是否需要条件包裹
- **验收要点**:
  - [ ] JEI compileOnly 版本正确
  - [ ] EMI 依赖已注释/移除（因不兼容 26.1.2）
  - [ ] 无因 EMI 缺失导致的编译错误

#### TASK-601: 运行时依赖更新

- **scope**: `gradle/libs.versions.toml` + `build.gradle.kts`
- **DoD**: 运行时依赖版本适配 26.1.2
- **目标**: 更新 Jade、ModernUI、JECharaters 等运行时依赖
- **输入**: 当前版本目录
- **输出**: 更新后的版本目录
- **依赖**: 需进一步确认各组件在 26.1.2 NeoForge 的精确版本号
- **预调查结果**:
  - **Jade**：✅ **可用**（26.1.x NeoForge），具体版本需查 Modrinth
  - **ModernUI**：⚠️ 待进一步确认 26.1.2 兼容版本
  - **JECharacters**：⚠️ 待进一步确认 26.1.2 兼容版本
- **具体变更**:
  - `jade` → 更新版本 ref（查 Modrinth 获取 26.1.x NeoForge 最新版）
  - `modernui` → 更新版本 ref（需确认 26.1.2 兼容性）
  - `jecharacters` → 更新版本 ref（需确认 26.1.2 兼容性）
- **验收要点**:
  - [ ] Jade 版本兼容 26.1.2 NeoForge
  - [ ] ModernUI 版本兼容 26.1.2（如可用）
  - [ ] JECharacters 版本兼容 26.1.2（如可用）

---

### 阶段 7 — 构建验证与冒烟测试

> 最终验证阶段，依赖所有前置阶段完成。

#### TASK-700: 全量构建验证

- **scope**: 整个项目
- **DoD**: `gradlew build` 成功（退出码 0），产出可运行的 JAR
- **目标**: 全量编译验证
- **输入**: 所有阶段产出的文件
- **输出**: 成功构建的 JAR 文件
- **依赖**: 所有前置 TASK
- **具体变更**:
  - 运行 `gradlew build --no-daemon`
  - 依次排查：Gradle 配置错误 → 依赖解析错误 → 编译错误 → 资源处理错误
  - 确认 JAR 包含正确文件（`neoforge.mods.toml`、`pack.mcmeta`、mixins.json、编译后的 class 文件）
- **验收要点**:
  - [ ] `gradlew build` 成功（退出码 0）
  - [ ] 生成的 JAR 内包含预期文件
  - [ ] 可通过 `jar tf` 验证 JAR 内容

#### TASK-701: 冒烟运行测试

- **scope**: 客户端启动验证
- **DoD**: 模组能被 NeoForge 加载器识别并加载
- **目标**: 验证运行时兼容性
- **输入**: TASK-700 产出的 JAR
- **输出**: 验证运行结果
- **依赖**: TASK-700（构建成功）
- **具体变更**:
  - 将 JAR 放入 `.minecraft/mods/` 目录（使用 26.1.2 NeoForge 客户端）
  - 启动客户端，检查日志是否有错误或异常
  - 验证模组版本号显示正确
  - 验证加载的包是否可识别（开启游戏内资源包/数据包界面检查）
- **验收要点**:
  - [ ] NeoForge 26.1.2 客户端启动无异常
  - [ ] 模组列表显示 ExtraLoader 2.0.0（或更新版本号）
  - [ ] ExtraLoader 的包加载功能可用

---

## 任务依赖图

```
阶段 0（分支与 worktree）
  TASK-000 ───────────────────────────────────────────── 基础

阶段 1（构建系统）← 依赖 TASK-000
  TASK-100 (gradle.properties) ───┐
  TASK-101 (libs.versions.toml)   ├── 可并行
                                  │
  TASK-102 (build.gradle.kts) ←───┘ ← 依赖 TASK-101
  TASK-103 (Gradle Wrapper) ──────── 与 TASK-100/101 并行

阶段 2（Mixin 升级）← 依赖阶段 1（编译就绪）
  TASK-200 (调研) ← 前置调研（无代码依赖）
  TASK-201 (mixins.json) ← 依赖 TASK-200
  TASK-202 (OptionsMixin) ← 依赖 TASK-200, TASK-102

阶段 3（NeoForge API 适配）← 依赖阶段 1
  TASK-300 (Bootstrap)     ┐
  TASK-301 (ExtraConfig)   │  除 TASK-305 依赖
  TASK-302 (ExtraLoader)   ├── TASK-302 外均
  TASK-303 (Repository)    │   可并行
  TASK-304 (PackLoadMode)  │
  TASK-305 (Client)  ←─────┘ ← 依赖 TASK-302
  TASK-306 (Events)        ┐
  TASK-307 (Commands)      ├── 可并行
  TASK-308 (DataGen)       │
  TASK-309 (LangProvider)  ┘

阶段 4（元数据）← 与阶段 2、3 可并行
  TASK-400 (pack.mcmeta)  ┐
  TASK-401 (neoforge.mods) ├── 可并行
  TASK-402 (package-info)  ┘

阶段 5（Java 25）← 与阶段 2、3、4 可并行
  TASK-500 (编译兼容) ┐
  TASK-501 (新特性)   └── 可选

阶段 6（依赖更新）← 与阶段 2、3、4、5 可并行
  TASK-600 (编译时依赖) ┐
  TASK-601 (运行时依赖)  ├── 可并行

阶段 7（验证）
  TASK-700 (构建验证)  ← 依赖所有前置 TASK
  TASK-701 (冒烟测试)  ← 依赖 TASK-700
```

### 串行/并行策略

| 组 | 执行模式 | 说明 |
|----|---------|------|
| TASK-000 | 前置步骤 | 必须先完成 worktree 创建 |
| TASK-100, 101, 103 | **并行** | 配置文件独立修改 |
| TASK-102 | 等待 101 | 需要更新后的版本目录 |
| TASK-200 | 前置调研 | Mixin 0.17.3 API 变更需先明确 |
| TASK-201, 202 | 等待 200 | 需要调研结果指导 |
| 阶段 3（TASK-300 ~ 309） | **内部多并行** | 除 305 依赖 302 外均为并行关系 |
| 阶段 4（TASK-400 ~ 402） | **与阶段 2、3 并行** | 仅依赖阶段 1 |
| 阶段 5（TASK-500, 501） | **与阶段 2、3、4 并行** | 仅依赖 TASK-102 |
| 阶段 6（TASK-600, 601） | **与阶段 2、3、4、5 并行** | 可提前调研版本 |
| TASK-700 | 等待全部完成 | 最终构建验证 |
| TASK-701 | 等待 TASK-700 | 运行时验证 |

---

## 风险管理

### 已知风险

| # | 风险 | 影响 | 概率 | 降级/缓解方案 |
|---|------|------|------|-------------|
| R1 | **Mixin 0.8.7 → 0.17.3 API 不兼容** | Mixin 注入失败，编译/运行崩溃 | **低** 🔽 | **预调查已降低风险**：API 层面向前兼容，无破坏性变更。`@WrapOperation`（MixinExtras）不受影响。refmap 格式不变。注意 `mixin.env.classReaderExpandFrames` 可能需要显式设置 |
| R2 | **Java 21 → 25 兼容性问题（javax.annotation 不可用）** | `javax.annotation` 引用导致编译失败 | **中** | **预调查确认**：`javax.annotation` 非 JDK 25 内置模块。迁移方案：全项目替换为 `org.jetbrains.annotations`（NeoForge 已捆绑 `24.0.1`） |
| R3 | **NeoForge API 大范围变化（21.x → 26.x）** | 多处 API 调用需重写 | **高** | 逐文件编译验证；如 Pack 系统 API 大改，准备使用新 API 替代方案（如 `Pack.Builder`、`PackConfig` 等） |
| R4 | **Pack 系统在 26.1.2 中被重构** | 整个自定义 Pack 加载逻辑需重写 | **低** 🔽 | **预调查已降低风险**：`Pack.readMetaAndCreate` 签名无变化。`FilePackResources`/`PathPackResources` 虽已不存在，但 1.21.1 移植时已改用 `ResourcesSupplier`。`Pack.Metadata` 新增 `isHidden` 字段，不影响现有代码 |
| R5 | **FMLPaths / FMLLoader API 变化** | 路径获取和版本查询失效 | 中 | 查阅 NeoForge 26.x 中等效 API |
| R6 | **26.1.2 为快照/测试版，不稳定** | 构建或运行时出现预期外错误 | 中 | 锁定已知稳定版本；记录复现步骤等待上游修复 |
| R7 | **依赖组件在 26.1.2 上不可用** | JEI/EMI/Jade 等无法编译或运行时缺失 | **中** | **预调查确认**：JEI ✅ 可用、Jade ✅ 可用；EMI ❌ 不可用（需注释掉）。ModernUI/JECharacters 待确认。降级方案：不可用的注释掉 |
| R8 | **无参考项目** | 缺少 API 迁移的参考实现 | 中 | 查阅 NeoForge 官方示例仓库（NeoForge/NeoForge）或 NeoForge API diff |
| R9 | **MixinExtras 0.5.4 与 Mixin 0.17.3 不兼容** | `@WrapOperation` 等注解失效 | **低** 🔽 | **预调查确认兼容**：MixinExtras 0.5.4 与 Mixin 0.17.3 兼容，`@WrapOperation` 不受影响 |
| R10 | **`neoforge.mods.toml` 格式在 26.x 中变化** | 模组无法被加载器识别 | 低中 | 查阅 NeoForge 26.x 模组元数据规范，必要时迁移到新格式 |
| R11 | **pack_format 值在 26.1.2 中不明确** | 资源包无法加载 | **低** 🔽 | **预调查确认**：MC 26.1.2 使用统一格式 **101.1**（min_format=101, max_format=101），已填入 TASK-400 |
| R12 | **`Util.getPlatform().openFile()` 在 26.1.2 被移除或签名变化** | 打开目录功能失效 | 低 | 改用 `Desktop.getDesktop().open()` 或 `Runtime.getRuntime().exec()` 替代方案 |
| R13 | **Gradle Wrapper 与 ModDevGradle 2.0.140 兼容性** | 当前 Gradle 9.2.0 可能不满足 2.0.140 的最低需求，导致构建失败 | 低中 | 查阅 ModDevGradle 2.0.140 release notes 确认所需最低 Gradle 版本；如需升级则执行 `gradlew wrapper --gradle-version=<目标版本>` |
| R14 | **ModDevGradle 2.0.140 与 NeoForge 26.1.2 的 DSL 兼容性** | `neoForge {}` DSL 块或 runs 配置在 2.0.140 中签名变化，导致构建脚本编译失败 | 低中 | 查阅 ModDevGradle 2.0.140 changelog 确认 DSL 变更；按新 DSL 调整 `build.gradle.kts`；回退方案：锁定到确认兼容的上一版本 |

### 回退方案

1. **逐 TASK 回退**：每个 TASK 完成后使用 `git commit` 创建检查点；失败的 TASK 使用 `git revert` 撤销
2. **中间基线构建**：阶段 1 完成后即进行 `gradlew build` 验证构建系统；阶段 3 中期选择核心 3-4 个文件适配后早期验证
3. **Mixin 降级**：若 Mixin 0.17.3 导致大规模不可修复问题，评估是否可锁定到较低 Mixin 版本（需 NeoForge 26.1.2 支持）
4. **全量回退**：若移植不可行，删除 `26.1.2` 分支和 worktree，保持 `1.21.1` 分支状态不变
5. **功能裁剪**：如果 Pack 系统 API 大改导致 ExtraRepositorySource 无法适配，可考虑临时移除自定义 Pack 加载功能，保留基本入口和命令功能先发布

### 需先行调查的事项（执行前准备）

以下事项建议在正式执行阶段 1 之前先做调研，以降低执行阶段的风险：

| ~~调查项~~ | ~~目的~~ | ~~状态~~ |
|--------|------|------------|
| ~~NeoForge 26.1.2 对应的 `neo_version` 值~~ | ~~正确填写 gradle.properties~~ | ✅ **已完成** → `neo_version = 26.1.2.76` |
| ~~Mixin 0.8.7 → 0.17.3 破坏性变更清单~~ | ~~指导 TASK-200 ~ 202~~ | ✅ **已完成** → API 向前兼容，无破坏性变更 |
| ~~26.1.2 的 pack_format 值~~ | ~~指导 TASK-400~~ | ✅ **已完成** → 统一格式 101.1 (min=101, max=101) |
| ~~JEI/EMI/Jade 在 26.1.2 的可用版本~~ | ~~指导 TASK-600, 601~~ | ✅ **部分完成** → JEI ✅ / Jade ✅ / EMI ❌；精确版本号需查 Modrinth |
| ~~`Pack.readMetaAndCreate` 在 26.1.2 中的签名~~ | ~~评估 TASK-303 改动量~~ | ✅ **已完成** → 签名无变化；FilePackResources/PathPackResources 已不存在但已改用 ResourcesSupplier |
| ~~Java 25 的 `javax.annotation` 模块化状态~~ | ~~评估 TASK-402 和 TASK-500 风险~~ | ✅ **已完成** → 非 JDK 内置，需迁移到 `org.jetbrains.annotations` |
| ~~仍需确认的项~~ | | |
| JEI 精确版本号 | 填入 libs.versions.toml | ⏳ **待查**（Modrinth / CF） |
| Jade 精确版本号 | 填入 libs.versions.toml | ⏳ **待查**（Modrinth / CF） |
| ModernUI 26.1.2 兼容版本 | 指导 TASK-601 | ⏳ **待查** |
| JECharacters 26.1.2 兼容版本 | 指导 TASK-601 | ⏳ **待查** |
| Mixin 0.17.3 是否原生支持 `JAVA_25` | 指导 TASK-201 | ⏳ **待确认**（如不支持则保持 JAVA_21） |

---

## 总估算

| 指标 | 值 |
|------|-----|
| 阶段数 | 7（阶段 0~6）+ 验证（阶段 7） |
| 总任务数 | ~22 个 |
| 总复杂度 | **中高**（NeoForge 多版本跨度为主风险；Mixin 大版本跳升已通过预调查降级 ✅） |
| 关键路径 | TASK-000 → TASK-101 → TASK-102 → TASK-200 → TASK-202 → TASK-700 → TASK-701 |
| 预计构建验证轮次 | 3~6 轮（预调查覆盖 Mixin/Pack API/版本号等关键项，减少迭代轮次） |
| 并行执行窗口 | 阶段 4/5/6 与阶段 2/3 可完全并行 |
| 最大并行任务数 | 约 8 个（阶段 3 内部 7 个 + 阶段 4 的 3 个 + 阶段 5 的 1 个） |
| 前置调研（pre-flight） | 6 项关键调查已完成 5 项 ✅，1 项部分完成（精确版本号待查）；详见「需先行调查的事项」 |

---

## DoD（完成定义）

### 阶段 0 — 分支与 worktree
- [ ] 远程 `myfork/26.1.2` 分支存在
- [ ] 本地 `ExtraLoader-26.1.2` worktree 已就绪

### 阶段 1 — 构建系统
- [ ] `gradle.properties` 中所有版本指向 26.1.2
- [ ] `gradle/libs.versions.toml` 中所有依赖版本兼容 26.1.2
- [ ] `build.gradle.kts` 中 Java toolchain 为 25
- [ ] Gradle Wrapper 版本满足 ModDevGradle 2.0.140 需求

### 阶段 2 — Mixin 升级
- [x] Mixin 0.8.7 → 0.17.3 变更已调研完成（预调查确认 API 向前兼容）
- [ ] `extraloader.mixins.json` 适配 0.17.3 格式和 Java 25
- [ ] `OptionsMixin.java` 在 0.17.3 + 26.1.2 下编译通过

### 阶段 3 — NeoForge API 适配
- [ ] 所有 Java 源文件在 26.1.2 NeoForge 下编译通过
- [ ] Pack 系统 API 调用（`Pack.readMetaAndCreate`、`FilePackResources`、`PathPackResources` 等）适配正确

### 阶段 4 — 模组元数据
- [ ] `pack.mcmeta` 中 pack_format 为 101.1（min_format=101, max_format=101）
- [ ] `neoforge.mods.toml` 格式符合 26.1.2 规范
- [ ] `package-info.java` 中所有注解在 26.1.2 中有效

### 阶段 5 — Java 25
- [ ] 现有代码在 Java 25 toolchain 下编译通过（可选新特性适配）

### 阶段 6 — 依赖组件
- [ ] 所有外部依赖版本兼容 26.1.2

### 阶段 7 — 验证
- [ ] `gradlew build` 成功（退出码 0）
- [ ] 生成的 JAR 文件结构正确
- [ ] NeoForge 26.1.2 客户端可加载模组且无运行时异常
