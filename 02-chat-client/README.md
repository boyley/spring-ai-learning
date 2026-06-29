# 02 · ChatClient API

> 本模块目标：全面掌握 `ChatClient` 的日常用法。这是后续所有对话类模块的基础。

## 一、ChatClient 是什么

`ChatClient` 是 Spring AI 提供的**流式（链式）API 风格**的对话客户端，封装了与大模型交互的所有细节。一次完整调用通常是：

```
prompt() → [system(...)] → user(...) → [options(...)] → call()/stream() → content()
```

| 方法 | 作用 |
|---|---|
| `prompt()` | 开始构建一次请求 |
| `system(...)` | 设定 AI 的角色/风格/约束（系统指令） |
| `user(...)` | 用户的具体问题 |
| `options(...)` | 运行时参数（临时换模型、调温度等） |
| `call()` | **非流式**：阻塞等完整结果 |
| `stream()` | **流式**：一块块实时返回（`Flux`） |
| `content()` | 取出回答文本 |

## 二、本模块四个演示

1. **非流式 `call()`** —— 一次性拿完整回答。
2. **System 角色** —— 给 AI 设人设，约束回答风格。
3. **流式 `stream()`** —— 打字机效果，实时返回。
4. **运行时参数** —— 不改配置，临时换模型/调温度。

## 三、流式 vs 非流式

```mermaid
flowchart TD
    subgraph 非流式 call
        A1[提问] --> A2[模型生成全部] --> A3[一次性返回整段]
    end
    subgraph 流式 stream
        B1[提问] --> B2[模型边生成边返回]
        B2 --> B3["块1: 你"] --> B4["块2: 好"] --> B5["块3: ..."]
    end
```

- **非流式**：实现简单，适合后台任务、需要完整结果再处理的场景。
- **流式**：用户体验好（秒出首字），适合聊天界面。Web 项目里通常把 `Flux` 直接以 SSE 返回前端。

## 四、`defaultSystem` vs `system`

- `builder.defaultSystem(...)`：给 ChatClient 设**默认人设**，之后每次调用都带上。
- `prompt().system(...)`：仅对**本次**请求生效。

## 五、运行

```bash
cd 02-chat-client
mvn spring-boot:run
```

依赖 DeepSeek 的 Key（已在 `../config/spring-ai-common.yml` 配置）。

## 六、小结

- `ChatClient` 链式 API 是 Spring AI 的核心入口。
- 记住调用四件套，并理解流式/非流式区别。
- 下一站：[03-prompt](../03-prompt) 学习提示词模板与角色。
