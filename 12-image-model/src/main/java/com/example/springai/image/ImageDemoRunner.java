package com.example.springai.image;

import org.springframework.ai.image.Image;
import org.springframework.ai.image.ImageModel;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.ai.openai.OpenAiImageOptions;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * ============================================================================
 * 文生图演示：一句话描述 → 一张图片 URL
 * ============================================================================
 *
 * 【流程图】
 *
 *   文字描述              参数 OpenAiImageOptions
 *   "一只橘猫…"   +   (模型/数量/尺寸/质量)
 *        │                     │
 *        └──────► ImagePrompt ◄┘
 *                     │
 *                     ▼  imageModel.call(prompt)   （HTTP 调用 OpenAI DALL·E）
 *               ImageResponse
 *                     │ getResult().getOutput()
 *                     ▼
 *                 Image 对象 ──► getUrl() ──► 图片链接（浏览器可打开查看）
 *
 * 【核心抽象速记】
 *   - ImageModel   ：图片模型的统一接口，方法 call(ImagePrompt) 返回 ImageResponse。
 *   - ImagePrompt  ：一次文生图请求 = 文字描述 + 可选的生成参数。
 *   - ImageResponse：模型返回结果，内部装着一个或多个 ImageGeneration。
 *   - Image        ：单张图片，提供 getUrl()（图片链接）/ getB64Json()（Base64 内容）。
 * ============================================================================
 */
@Component
public class ImageDemoRunner implements CommandLineRunner {

    /**
     * 注入 Spring AI 的图片模型抽象 ImageModel。
     * 引入 OpenAI starter 后，框架已自动创建好它的实现（OpenAiImageModel），直接用即可。
     */
    private final ImageModel imageModel;

    public ImageDemoRunner(ImageModel imageModel) {
        this.imageModel = imageModel;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("\n========== 模块12：文生图（DALL·E）演示开始 ==========");

        // 1) 我们想画的内容（提示词）。描述越具体，画面越贴近预期。
        String description = "一只戴眼镜在敲代码的橘猫，卡通风格";
        System.out.println("【描述】" + description);

        // 2) 构造本次生成的参数。OpenAiImageOptions 是 OpenAI 专属的图片参数对象。
        //    注意：方法名以 jar 中真实签名为准（1.1.7 用 .N(...).width(...).height(...) 链式风格）。
        OpenAiImageOptions options = OpenAiImageOptions.builder()
                .model("dall-e-3")   // 使用的文生图模型（dall-e-3 画质更好；dall-e-2 更便宜）
                .N(1)                // 生成几张图。dall-e-3 只支持 1 张
                .width(1024)         // 图片宽度（像素）。会被换算成 OpenAI 的 size 参数
                .height(1024)        // 图片高度（像素）。1024x1024 是 dall-e-3 的标准尺寸
                .quality("standard") // 画质：standard（标准）/ hd（更精细，更贵）
                .build();

        // 3) 把"文字描述 + 参数"打包成一次文生图请求 ImagePrompt。
        ImagePrompt prompt = new ImagePrompt(description, options);

        // 4) 真正发起调用：HTTP 请求 OpenAI，模型据描述作画后返回结果。
        ImageResponse response = imageModel.call(prompt);

        // 5) 取出结果。getResult() 拿到第一个生成结果（ImageGeneration），
        //    再 getOutput() 拿到 Image 对象（单张图片的描述）。
        Image image = response.getResult().getOutput();

        // 6) Image.getUrl() 是这张图片的临时下载链接（OpenAI 默认返回 URL 形式）。
        //    把它复制到浏览器地址栏即可查看生成的图片。
        String url = image.getUrl();

        System.out.println("\n【生成成功】图片 URL 如下，复制到浏览器即可查看：");
        System.out.println(url);
        System.out.println("\n（提示：该 URL 是 OpenAI 提供的临时链接，过段时间会失效，请及时打开/下载）");
        System.out.println("========== 模块12 演示结束 ==========\n");
    }
}
