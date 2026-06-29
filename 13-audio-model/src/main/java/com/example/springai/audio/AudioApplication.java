package com.example.springai.audio;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ============================================================================
 * 模块 13：语音能力（Audio Model）—— 启动类
 * ============================================================================
 *
 * 【这个模块是做什么的】
 *   演示大模型的两项语音能力，并把它们串成一个"自洽闭环"：
 *     1) 文字转语音（TTS, Text-To-Speech）：把一段文字"读"成一段语音(mp3)。
 *     2) 语音转文字（转录, Transcription / Whisper）：把语音"听写"回文字。
 *   闭环验证：先 TTS 生成音频文件，再把这个文件转录回文字，看看能不能对上。
 *
 * 【怎么做】
 *   1) 引入 spring-ai-starter-model-openai，它自动配置了两个 Bean：
 *        - OpenAiAudioSpeechModel        （做 TTS）
 *        - OpenAiAudioTranscriptionModel （做转录）
 *   2) TTS：speechModel.call(文字) 返回 byte[]，用 java.nio 写到 generated-output/speech.mp3。
 *   3) 转录：用 FileSystemResource 包装这个 mp3，transcriptionModel.call(resource) 拿到文字。
 *
 * 【达到的目的】
 *   理解 Spring AI 中"语音模型"的两类能力，掌握 TTS 与转录的最小可用调用方式，
 *   并通过"文字→语音→文字"的闭环直观感受效果。
 *
 * 【重要】语音能力属于 OpenAI 真正的能力，DeepSeek 不支持。
 *   所以本模块必须使用真实的 OpenAI Key（环境变量 OPENAI_API_KEY）。
 *   生成的音频写在 generated-output/ 目录（已被父项目 .gitignore 忽略）。
 * ============================================================================
 */
@SpringBootApplication
public class AudioApplication {
    public static void main(String[] args) {
        SpringApplication.run(AudioApplication.class, args);
    }
}
