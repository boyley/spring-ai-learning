package com.example.springai.prompt;

import java.util.List;
import java.util.Map;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

/**
 * ============================================================================
 * Prompt 提示词四种构建方式演示
 * ============================================================================
 *
 * 【核心概念】
 *   一条发给大模型的"提示词(Prompt)"通常由若干条带"角色"的消息(Message)组成：
 *     - System(系统)消息：设定 AI 的人设/风格/约束（如"你是严谨的技术导师"）。
 *     - User(用户)消息：用户的具体问题。
 *   "模板(PromptTemplate)"则是带占位符 {变量} 的文本，运行时把变量填进去生成最终提示词。
 *
 * 【流程图：模板变量是怎么变成最终提示词的】
 *
 *   模板字符串                    填充变量(param/Map)              最终提示词              大模型
 *   "讲讲 {topic} 的要点"   ──►   topic="Spring Boot"   ──►   "讲讲 Spring Boot..."  ──►  AI 回答
 *        │                              │                            │
 *     含占位符                     键值对替换                   占位符已被替换
 *
 * 【为什么要用模板】
 *   - 把"固定话术"和"可变参数"分离，提示词可复用、可参数化。
 *   - 配合资源文件(.st)，还能把提示词从 Java 代码里抽出来，方便非程序员维护。
 * ============================================================================
 */
@Component
public class PromptDemoRunner implements CommandLineRunner {

    private final ChatClient chatClient;

    /**
     * 用 @Value 注入 classpath 下的模板资源文件。
     * "classpath:/prompts/joke.st" 表示从编译后的资源根目录下的 prompts/joke.st 读取。
     * Resource 是 Spring 对"资源(文件/URL/类路径资源)"的统一抽象。
     */
    @Value("classpath:/prompts/joke.st")
    private Resource jokeResource;

    /** 通过构造器注入自动配置好的 ChatClient.Builder，再 build() 出可复用的 ChatClient。 */
    public PromptDemoRunner(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    @Override
    public void run(String... args) {
        demo1_inlineTemplateVariables();
        demo2_promptTemplateObject();
        demo3_messageListWithRoles();
        demo4_loadTemplateFromResource();
        System.out.println("\n========== 模块03 演示全部结束 ==========\n");
    }

    /**
     * 演示1：在 ChatClient 链式 API 里直接使用模板变量占位符。
     *
     * user(u -> ...) / system(s -> ...) 提供了一个"配置器"写法：
     *   - text("...{变量}...")：设置带占位符的文本。
     *   - param("变量名", 值)：为占位符填入实际值。
     * Spring AI 会在发送前自动把 {topic}、{role} 替换成填入的值。
     */
    private void demo1_inlineTemplateVariables() {
        System.out.println("\n===== 演示1：ChatClient 内联模板变量（param 填充占位符）=====");
        String answer = chatClient.prompt()
                // system：用 {role} 占位符设定 AI 人设，再用 param 填入具体角色
                .system(s -> s.text("你是一位{role}").param("role", "严谨的技术导师"))
                // user：用 {topic} 占位符表示主题，运行时填入 "Spring Boot"
                .user(u -> u.text("请讲讲关于 {topic} 的 3 个要点").param("topic", "Spring Boot"))
                .call()
                .content();
        System.out.println("AI：" + answer);
    }

    /**
     * 演示2：使用底层的 PromptTemplate 对象。
     *
     * 适合在调用 ChatClient 之前就把提示词"组装好"的场景。
     *   new PromptTemplate("...{变量}...")  -> 创建一个带占位符的模板
     *   pt.create(Map.of(...))              -> 用 Map 一次性填充所有变量，得到一个 Prompt 对象
     *   chatClient.prompt(prompt)           -> 直接把组装好的 Prompt 交给 ChatClient 发送
     */
    private void demo2_promptTemplateObject() {
        System.out.println("\n===== 演示2：底层 PromptTemplate + create(Map) =====");
        // 1) 定义模板：含两个占位符 {animal} 和 {adjective}
        PromptTemplate pt = new PromptTemplate("给我讲一个关于 {animal} 的{adjective}笑话");
        // 2) 填充占位符，生成最终的 Prompt（一个完整的、可直接发送的请求对象）
        Prompt p = pt.create(Map.of("animal", "猫", "adjective", "冷"));
        // 3) 把组装好的 Prompt 交给 ChatClient 调用
        String answer = chatClient.prompt(p).call().content();
        System.out.println("AI：" + answer);
    }

    /**
     * 演示3：用消息列表 + 角色手动构建 Prompt。
     *
     * 一个 Prompt 本质上是"一串带角色的消息"。这里我们显式地：
     *   - new SystemMessage("...")：系统消息，设定 AI 身份/风格。
     *   - new UserMessage("...")：用户消息，提出具体问题。
     *   - new Prompt(List.of(系统消息, 用户消息))：把多条消息组合成一个完整请求。
     * 这种写法最贴近"提示词的本质结构"，也便于动态拼装多条消息（如多轮对话）。
     */
    private void demo3_messageListWithRoles() {
        System.out.println("\n===== 演示3：消息列表 + System/User 角色 =====");
        SystemMessage system = new SystemMessage("你是一位精通中文的诗人，回答都要押韵。");
        UserMessage user = new UserMessage("用两句话描述春天的早晨。");
        Prompt prompt = new Prompt(List.of(system, user));
        String answer = chatClient.prompt(prompt).call().content();
        System.out.println("AI：" + answer);
    }

    /**
     * 演示4：从 classpath 资源文件加载模板。
     *
     * 把提示词写在 src/main/resources/prompts/joke.st 里（内容含 {topic}、{style} 占位符），
     * 用 @Value 注入成 Resource，再 new PromptTemplate(resource) 构建模板。
     * 好处：提示词与代码解耦，改提示词不用改 Java、不用重新理解代码逻辑。
     */
    private void demo4_loadTemplateFromResource() {
        System.out.println("\n===== 演示4：从资源文件 prompts/joke.st 加载模板 =====");
        // 用资源文件的内容构建模板对象（构造器接收 Resource）
        PromptTemplate pt = new PromptTemplate(jokeResource);
        // 填充模板里的 {topic} 和 {style} 占位符，生成最终 Prompt
        Prompt p = pt.create(Map.of("topic", "程序员", "style", "冷幽默"));
        String answer = chatClient.prompt(p).call().content();
        System.out.println("AI：" + answer);
    }
}
