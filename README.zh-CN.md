<p align="center">
  <img src="assets/branding/fatai-banner.svg" alt="FatAI — 以 Context 为中心的 AI 工作台" width="100%" />
</p>

<p align="center">
  <img src="assets/branding/fatai-icon.svg" alt="FatAI 图标" width="112" />
</p>

<h1 align="center">FatAI</h1>

<p align="center">一个基于 Kotlin Multiplatform 构建、以 Context 为中心的模块化 AI 工作台。</p>

<p align="center">
  <a href="#支持平台">Kotlin Multiplatform</a> ·
  <a href="#模块化架构">Context Engine</a> ·
  <a href="#第一阶段mvp状态">MVP</a> ·
  <a href="README.md">English</a>
</p>

FatAI 将多模型聊天、工作区上下文、提示词组装、记忆与文件附件整合到可扩展架构中，为后续 RAG、MCP 工具和 Agent 打下基础。

## 模块化架构

> 设计原则：Chat 只是 Context 的一个消费者。任何新能力通过 Feature 契约和有序 Context Provider 接入，而不是直接耦合到某个页面。

项目正在从单一 `shared` 模块迁移到真实的 KMP Feature Modules。`commonMain` 只暴露跨平台契约与可复用领域逻辑；数据库驱动、网络引擎、文件访问和 UI 平台实现分别放在 `androidMain`、`jvmMain`、`iosMain` 等平台 Source Set 中。

```
core                 # 跨模块契约、上下文基础模型
database             # SQLDelight Schema、迁移、平台数据库驱动
feature-chat         # 会话与消息用例
feature-prompt       # Context Engine 与 Prompt Provider
feature-memory       # 最近消息、摘要、长期记忆
feature-model        # Provider 抽象与模型配置
feature-files        # 文件元数据与附件管线
feature-workspace    # 工作区与上下文绑定
feature-settings     # 用户设置与主题
feature-knowledge    # V2：RAG 契约与索引
feature-tools        # V2：工具与 MCP 契约
feature-agent        # V2：规划与执行契约
composeApp           # Compose Multiplatform 应用壳
```

目前的 `shared` 仅作为迁移兼容层保留。新功能不应继续添加到其中，而应进入对应的 Feature Module。

`core` 和 `database` 已完成迁移：消息/Provider 基础类型位于 `core`，SQLDelight Schema、迁移以及 Android/JVM/iOS 数据库 Driver 位于 `database`。

`feature-model` 负责 Provider 契约、OpenAI-compatible 流式适配器、API Key 持久化和 Ktor 平台网络引擎。

`feature-workspace` 负责工作区创建、选择、工作区指令以及默认个人工作区。

`feature-memory` 负责分层记忆召回和基于模型的会话摘要策略。

`feature-files` 负责跨平台附件元数据；平台文件选择器保留在 Compose 应用层。

`feature-prompt` 负责 Context Engine、有序 Prompt Provider 与可复用提示词模板。

`feature-chat` 负责会话与消息持久化；`feature-settings` 负责主题偏好的持久化。

桌面版首次启动时会将旧 `.ai-assistant/app.db` 复制到新的 `.fatai/app.db`，保留原有聊天数据。

应用主路由使用 Decompose。Feature 页面通过根组件传入的导航回调跳转，而不直接控制导航器；后续新增 Workspace、Knowledge、Agent 页面时可作为独立 Child 接入。

## 第一阶段（MVP）状态

- [x] 响应式 Open WebUI 风格聊天工作台：桌面端常驻侧栏、移动端抽屉、居中聊天列、模型状态与悬浮输入框
- [x] 流式聊天、停止生成、重新生成、续写
- [x] Provider / API Key 切换和模型选择
- [x] Context Engine：系统提示词、模板、工作区、记忆、文件清单、最近历史
- [x] 工作区绑定会话
- [x] 会话文件附件：PDF、Office、Markdown、文本、图片
- [x] 主题持久化：跟随系统 / 浅色 / 深色
- [x] 跟随系统语言的中英文界面文案
- [x] 提供商下拉选择与支持键盘收起的 API 密钥弹窗
- [x] 流式请求期间带动画的“正在思考”状态
- [x] 摘要记忆服务（默认每 500 条消息触发）
- [x] 助手 Markdown 渲染（GFM、表格、代码块、链接）
- [x] 移动优先的聊天与 Markdown 字号
- [x] 随已发送消息保留的图片预览与文件卡片
- [ ] 工具结果、思考消息渲染器，以及编辑/重试
- [ ] 文件内容提取、OCR、Vision 与 RAG 索引
- [ ] Knowledge、Tool/MCP、Agent 执行（V2）

## 多语言

Compose 界面的可见文案不再内嵌在 Kotlin 代码中：英文位于 `composeApp/src/commonMain/composeResources/values/strings.xml`，中文位于 `values-zh/strings.xml`。Compose 会根据 Android、桌面端与 iOS 的系统语言自动选择资源包。

平台专属文案仍放在各自原生资源目录。Android 应用名称位于 `composeApp/src/androidMain/res/values/strings.xml`，中文资源位于 `res/values-zh/strings.xml`；iOS 原生字符串表位于 `iosApp/iosApp/{en,zh-Hans}.lproj/`。

## 多模态消息架构

消息角色与消息内容类型相互独立。`ChatItemType` 标识发送方（`Question` 或 `Answer`），`MessageContentType` 标识正文载体（`Text`、`Markdown`、`Image`、`File`、`ToolResult`、`Thinking`）。主要内容类型持久化在 `ChatItem.contentType` 中（数据库迁移 4），Compose 聊天层按该类型分发到对应渲染器。

用户输入保存为 `Text`，流式模型输出保存为 `Markdown`，并使用 `com.mikepenz:multiplatform-markdown-renderer-m3` 0.37.0 渲染。该库具备 Android、JVM Desktop 与 iOS Target。附件独立持久化在 `FileAsset`：待发送附件的 `messageId` 为空，发送时会绑定至对应用户消息。聊天历史按该 ID 归组，图片通过 KMP 的 FileKit/Coil 集成展示预览，其他文件以紧凑卡片展示。重新打开会话仍会显示在正确消息下，无需改变流式响应、历史记录或模型 Provider 流程。

## 代码质量

- Detekt 已应用到全部 Gradle 模块，统一配置位于 `config/detekt/detekt.yml`。
- Kover 已应用到 `composeApp`，用于 JVM 覆盖率报告。当前架构迁移阶段不把 UT 作为交付阻塞项；补齐 JVM 测试后即可生成覆盖率报告。

## 品牌资源

图标 SVG 源文件位于 `assets/branding/fatai-icon.svg`。Android 各密度启动图标与 iOS 1024 AppIcon 均由该源文件生成，确保产品端视觉一致。

## KMP 依赖原则

- `commonMain` 仅使用 Kotlin Multiplatform 可用的库和接口。
- Ktor、SQLDelight、Compose、FileKit 等依赖必须选择有 Android、JVM、iOS 实现的版本。
- 网络 engine、数据库 driver、系统文件路径和剪贴板等能力只能在平台 Source Set 中实现，并通过 `expect/actual` 或接口向上提供。
- 引入新三方库前，必须确认其 KMP Targets 覆盖本项目已支持的平台。
