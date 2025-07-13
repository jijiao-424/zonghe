zonghe项目作业
这是一个基于Android平台开发的简易记账本项目，其核心亮点是集成了一个由大型语言模型（LLM）驱动的AI财务助手。用户不仅可以记录日常收支，还能通过自然语言对话的方式，对自己的账单进行智能分析。
这个项目旨在演示如何在Android原生应用中，安全、高效地集成云端AI服务，并记录了从开发到调试的全过程。

**主要功能**

- **基础记账**：记录每日的收入与支出。
- **数据可视化**：提供月度和年度的收支统计图表。
- **AI财务助手**：
    - 一个独立的AI问答界面。
    - 能够基于当日的账单数据，回答用户的自然语言提问（例如：“今天哪一项花得最多？”）。
    - 动态构建上下文（Prompt），引导AI进行精准分析。
- **安全实践**：采用官方推荐的最佳实践，通过 `local.properties` 和 `.gitignore` 文件安全管理API Key，杜绝敏感信息泄露。
- **现代化UI**：使用Material Design组件，如 `FloatingActionButton`，提供更佳的用户交互体验。

**技术栈 & 核心库**

- **开发语言**: Java
- **IDE**: Android Studio Dolphin
- **数据库**: SQLite
- **网络请求**: OkHttp
- **AI 服务**: 阿里云通义千问 (DashScope - `qwen-plus`)
- **UI 组件**: AndroidX, Material Design Components
