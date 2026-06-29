package com.example.springai.multimodal;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeTypeUtils;

import java.net.URI;

/**
 * ============================================================================
 * 多模态演示：发送"图片 + 文字"让模型看图说话
 * ============================================================================
 *
 * 【做什么】
 *   把一张图片和一句文字问题一起发给大模型，让模型理解图片并用中文回答。
 *
 * 【怎么做】
 *   ChatClient 的 user(...) 接受一个 lambda（PromptUserSpec），在里面：
 *     u.text("文字问题")                          -> 设置这条用户消息的文字部分
 *      .media(图片类型, 图片来源)                   -> 给这条消息附加一张图片
 *   一次 call() 即可拿到模型对"图文"的综合回答。
 *
 * 【目的】
 *   理解多模态：模型的输入不只是文字，还能是图片（甚至音频/视频），
 *   多种"模态"一起给模型，它能做更丰富的理解。
 *
 * 【流程图（ASCII）】
 *
 *   你的代码                         ChatClient                 OpenAI 视觉模型(gpt-4o-mini)
 *     │                                 │                                │
 *     │ user(text="图里有什么" + 图片)   │                                │
 *     │ ──────────────────────────────► │                                │
 *     │                                 │  HTTP 请求(文字 + 图片URL/数据)  │
 *     │                                 │ ─────────────────────────────► │
 *     │                                 │                                │ 看图 + 理解中...
 *     │                                 │  HTTP 响应(对图片的文字描述)     │
 *     │                                 │ ◄───────────────────────────── │
 *     │   .call().content() → String    │                                │
 *     │ ◄────────────────────────────── │                                │
 *     ▼                                 ▼                                ▼
 *   打印到控制台
 * ============================================================================
 */
@Component
public class MultimodalDemoRunner implements CommandLineRunner {

    private final ChatClient chatClient;

    /** 注入自动配置好的 ChatClient.Builder（已根据本模块 application.yml 指向 OpenAI 视觉模型）。 */
    public MultimodalDemoRunner(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("\n========== 模块05：多模态（图片 + 文字）演示 ==========\n");

        // 演示用的网络图片地址（一张经典的 PNG 透明度演示图，公开可访问）。
        // 用"图片 URL"方式可以避免把二进制图片打进项目，演示最简单。
        String imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/4/47/"
                + "PNG_transparency_demonstration_1.png/280px-PNG_transparency_demonstration_1.png";

        System.out.println("【发送】文字：这张图片里有什么？请用中文描述");
        System.out.println("【发送】图片：" + imageUrl);

        // 先把字符串地址转成 java.net.URL（toURL() 是受检异常，需在 lambda 外面提前转好，
        // 因为 user(...) 的 lambda 是 Consumer，不允许抛受检异常）。
        java.net.URL url = URI.create(imageUrl).toURL();

        // ★★★ 核心：在一条用户消息里同时放"文字 + 图片" ★★★
        String answer = chatClient.prompt()
                .user(u -> u
                        // text(...)：这条用户消息的文字部分（你的问题）
                        .text("这张图片里有什么？请用中文描述")
                        // media(...)：给这条消息附加一张图片。
                        //   第1个参数：图片的 MIME 类型（这里是 image/png）。
                        //   第2个参数：图片来源——这里用图片 URL。
                        //   这个重载是 media(MimeType, java.net.URL)。
                        .media(MimeTypeUtils.IMAGE_PNG, url))
                .call()        // 同步调用，等模型看完图并生成完整回答
                .content();    // 取出回答文本

        System.out.println("\n【AI 看图回答】" + answer);

        // ------------------------------------------------------------------
        // 【替代写法】读取 classpath 本地图片（不联网）。
        //   1) 把任意一张 png 放到 src/main/resources/images/sample.png；
        //   2) 把上面的 .media(...) 换成下面这行即可（用的是 media(MimeType, Resource) 重载）：
        //
        //   .media(MimeTypeUtils.IMAGE_PNG,
        //          new org.springframework.core.io.ClassPathResource("images/sample.png"))
        //
        //   说明：ClassPathResource 会从编译后的 classpath 根目录查找该文件。
        // ------------------------------------------------------------------

        System.out.println("\n========== 模块05 演示结束 ==========\n");
    }
}
