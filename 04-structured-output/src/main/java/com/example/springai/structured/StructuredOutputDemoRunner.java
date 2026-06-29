package com.example.springai.structured;

import java.util.List;
import java.util.Map;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;

/**
 * ============================================================================
 * 结构化输出演示：把 AI 文本回答自动转成 Java 对象 / List / Map
 * ============================================================================
 *
 * 【底层原理（重要！）】
 *   .entity(目标类型) 的背后是一个"结构化输出转换器"BeanOutputConverter，工作分两步：
 *
 *     第1步（发请求前）：根据目标 Java 类型，自动生成一段 JSON 格式说明
 *                        （形如"请只返回符合这个 JSON Schema 的内容..."），
 *                        并把这段说明追加到我们的用户提示词后面一起发给模型。
 *     第2步（收响应后）：模型按要求返回一段 JSON 文本，转换器用 Jackson
 *                        把这段 JSON 反序列化成我们要的 Java 对象/List/Map。
 *
 * 【流程图】
 *
 *   你的提问 ──┐
 *             ├─►  追加"JSON 格式说明"  ──►  发给大模型  ──►  模型返回 JSON 文本
 *   目标类型 ──┘        (getFormat())                              │
 *                                                                  ▼
 *                                          Jackson 反序列化 ──► Java 对象 / List / Map
 *
 * 【注意】
 *   record 必须是"顶层类型"或"静态嵌套类型"，Jackson 才能正确反序列化。
 *   这里 ActorFilms 定义为本类的 static 嵌套 record（见文件底部）。
 * ============================================================================
 */
@Component
public class StructuredOutputDemoRunner implements CommandLineRunner {

    private final ChatClient chatClient;

    public StructuredOutputDemoRunner(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    @Override
    public void run(String... args) {
        demo1_toSingleObject();
        demo2_toList();
        demo3_toMap();
        demo4_revealUnderlyingFormat();
        System.out.println("\n========== 模块04 演示全部结束 ==========\n");
    }

    /**
     * 演示1：把回答转成"单个对象"。
     *
     * .entity(ActorFilms.class) —— 告诉 Spring AI：请把模型返回的 JSON
     * 反序列化成一个 ActorFilms 对象。返回值直接就是强类型的 ActorFilms，
     * 不再是一段需要我们手动解析的字符串。
     */
    private void demo1_toSingleObject() {
        System.out.println("\n===== 演示1：转单个对象 entity(ActorFilms.class) =====");
        ActorFilms actorFilms = chatClient.prompt()
                .user("列出周星驰的5部代表作")
                .call()
                .entity(ActorFilms.class);   // 直接拿到 Java 对象
        System.out.println("演员：" + actorFilms.actor());
        System.out.println("代表作：" + actorFilms.movies());
    }

    /**
     * 演示2：把回答转成 List<ActorFilms>。
     *
     * 泛型 List 在 Java 里会被"类型擦除"，所以不能写 List.class。
     * 必须用 ParameterizedTypeReference 这个"泛型类型令牌"把完整的 List<ActorFilms>
     * 类型信息传给转换器（注意末尾的 {} —— 这是创建匿名子类来保留泛型信息）。
     */
    private void demo2_toList() {
        System.out.println("\n===== 演示2：转 List<ActorFilms> =====");
        List<ActorFilms> list = chatClient.prompt()
                .user("生成3位著名导演及其代表作")
                .call()
                .entity(new ParameterizedTypeReference<List<ActorFilms>>() {});
        // 遍历强类型列表
        for (ActorFilms af : list) {
            System.out.println("导演：" + af.actor() + " -> " + af.movies());
        }
    }

    /**
     * 演示3：把回答转成 Map<String, Object>。
     *
     * 当结构不固定、又不想专门定义一个类时，可以转成 Map。
     * 同样用 ParameterizedTypeReference 保留泛型信息。
     */
    private void demo3_toMap() {
        System.out.println("\n===== 演示3：转 Map<String, Object> =====");
        Map<String, Object> map = chatClient.prompt()
                .user("用 JSON 给出一部电影《大话西游》的 标题(title)、上映年份(year)、主演(actors) 三个字段")
                .call()
                .entity(new ParameterizedTypeReference<Map<String, Object>>() {});
        // 遍历键值对
        map.forEach((k, v) -> System.out.println("  " + k + " = " + v));
    }

    /**
     * 演示4：揭示底层——手动创建 BeanOutputConverter，打印它生成的"JSON 格式说明"。
     *
     * 这正是 .entity(ActorFilms.class) 在内部偷偷追加到提示词后面的那段文字。
     * 看一眼它长什么样，就能彻底理解结构化输出"为什么能成功"。
     */
    private void demo4_revealUnderlyingFormat() {
        System.out.println("\n===== 演示4：底层原理——BeanOutputConverter.getFormat() =====");
        // 针对 ActorFilms 类型创建一个转换器
        BeanOutputConverter<ActorFilms> converter = new BeanOutputConverter<>(ActorFilms.class);
        // getFormat() 返回的就是"请按此 JSON 格式回答"的说明，会被自动拼到提示词里
        System.out.println("Spring AI 自动追加到提示词的格式说明如下：\n");
        System.out.println(converter.getFormat());
    }

    /**
     * 目标数据类型：一个演员(actor) 和他的代表作列表(movies)。
     *
     * record 是 Java 16+ 的"不可变数据载体"，会自动生成构造器、getter(同名方法)、
     * equals/hashCode/toString。定义为 static 嵌套类型，保证 Jackson 能正常反序列化。
     */
    public record ActorFilms(String actor, List<String> movies) {
    }
}
