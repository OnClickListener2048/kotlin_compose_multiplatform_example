# AI Assistant - 多模型 AI 助手

一个基于 Kotlin Multiplatform（KMP）的 AI Assistant。支持流式聊天、多 Provider、工作区、上下文组装、记忆和文件附件。

English documentation: [README.md](README.md)

## 模块化架构

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

应用主路由使用 Decompose。Feature 页面通过根组件传入的导航回调跳转，而不直接控制导航器；后续新增 Workspace、Knowledge、Agent 页面时可作为独立 Child 接入。

## 第一阶段（MVP）状态

- [x] 流式聊天、停止生成、重新生成、续写
- [x] Provider / API Key 切换和模型选择
- [x] Context Engine：系统提示词、模板、工作区、记忆、文件清单、最近历史
- [x] 工作区绑定会话
- [x] 会话文件附件：PDF、Office、Markdown、文本、图片
- [x] 主题持久化：跟随系统 / 浅色 / 深色
- [x] 摘要记忆服务（默认每 500 条消息触发）
- [ ] Markdown 渲染、富消息类型、编辑与重试
- [ ] 文件内容提取、OCR、Vision 与 RAG 索引
- [ ] Knowledge、Tool/MCP、Agent 执行（V2）

## 代码质量

- Detekt 已应用到全部 Gradle 模块，统一配置位于 `config/detekt/detekt.yml`。
- Kover 已应用到 `composeApp`，用于 JVM 覆盖率报告。当前架构迁移阶段不把 UT 作为交付阻塞项；补齐 JVM 测试后即可生成覆盖率报告。

## KMP 依赖原则

- `commonMain` 仅使用 Kotlin Multiplatform 可用的库和接口。
- Ktor、SQLDelight、Compose、FileKit 等依赖必须选择有 Android、JVM、iOS 实现的版本。
- 网络 engine、数据库 driver、系统文件路径和剪贴板等能力只能在平台 Source Set 中实现，并通过 `expect/actual` 或接口向上提供。
- 引入新三方库前，必须确认其 KMP Targets 覆盖本项目已支持的平台。
