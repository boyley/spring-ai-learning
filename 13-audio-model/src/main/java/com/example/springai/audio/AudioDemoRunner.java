package com.example.springai.audio;

import org.springframework.ai.openai.OpenAiAudioSpeechModel;
import org.springframework.ai.openai.OpenAiAudioTranscriptionModel;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * ============================================================================
 * 语音能力演示：文字 → 语音(mp3) → 文字（自洽闭环）
 * ============================================================================
 *
 * 【流程图】
 *
 *   一段文字
 *      │  speechModel.call(text)            （TTS：调用 OpenAI tts-1）
 *      ▼
 *   byte[] 音频字节
 *      │  Files.write(...)                  （用 java.nio 写文件，目录自动创建）
 *      ▼
 *   generated-output/speech.mp3            （磁盘上的音频文件）
 *      │  new FileSystemResource(path)      （把文件包装成 Spring 的 Resource）
 *      ▼
 *   transcriptionModel.call(resource)       （转录：调用 OpenAI whisper-1）
 *      │
 *      ▼
 *   转录文字 ──► 与最初的文字比对，验证闭环是否成功
 *
 * 【核心抽象速记】
 *   - OpenAiAudioSpeechModel        ：文字转语音。byte[] call(String text)。
 *   - OpenAiAudioTranscriptionModel ：语音转文字。String call(Resource audio)。
 * ============================================================================
 */
@Component
public class AudioDemoRunner implements CommandLineRunner {

    /** TTS 模型（文字转语音）：OpenAI starter 已自动配置好这个 Bean。 */
    private final OpenAiAudioSpeechModel speechModel;
    /** 转录模型（语音转文字）：OpenAI starter 已自动配置好这个 Bean。 */
    private final OpenAiAudioTranscriptionModel transcriptionModel;

    public AudioDemoRunner(OpenAiAudioSpeechModel speechModel,
                           OpenAiAudioTranscriptionModel transcriptionModel) {
        this.speechModel = speechModel;
        this.transcriptionModel = transcriptionModel;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("\n========== 模块13：语音能力（TTS + 转录）演示开始 ==========");

        // 我们要朗读的文字。稍后会把生成的语音再转录回来，看是否能还原成这句话。
        String text = "你好，欢迎学习 Spring AI 的语音能力。";

        // ---------- 第一步：文字转语音（TTS） ----------
        System.out.println("\n----- 第一步：文字转语音(TTS) -----");
        System.out.println("【原始文字】" + text);

        // speechModel.call(String) 直接返回音频的字节数组（byte[]，mp3 格式）。
        byte[] audioBytes = speechModel.call(text);
        System.out.println("已生成音频，字节数：" + audioBytes.length);

        // 用 java.nio 把字节写到文件。generated-output 目录若不存在则先创建。
        Path outputDir = Paths.get("generated-output");
        Files.createDirectories(outputDir);                 // 目录不存在则创建（已存在不报错）
        Path mp3Path = outputDir.resolve("speech.mp3");      // 目标文件路径
        Files.write(mp3Path, audioBytes);                    // 把字节写入文件
        System.out.println("音频已保存到：" + mp3Path.toAbsolutePath());

        // ---------- 第二步：语音转文字（转录 / Whisper） ----------
        System.out.println("\n----- 第二步：语音转文字(转录) -----");

        // 把刚生成的 mp3 文件包装成 Spring 的资源对象，交给转录模型。
        FileSystemResource audioResource = new FileSystemResource("generated-output/speech.mp3");

        // transcriptionModel.call(Resource) 直接返回转录得到的纯文本。
        String transcribed = transcriptionModel.call(audioResource);

        System.out.println("【转录结果】" + transcribed);

        // ---------- 闭环验证 ----------
        System.out.println("\n----- 闭环验证 -----");
        System.out.println("原始文字：" + text);
        System.out.println("转录文字：" + transcribed);
        System.out.println("（两者意思一致即说明 文字→语音→文字 闭环成功；标点/用词可能略有差异属正常现象）");

        System.out.println("\n========== 模块13 演示结束 ==========\n");
    }
}
