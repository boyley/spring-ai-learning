package com.example.springai.evaluation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ============================================================================
 * 模块 15：模型评估（Model Evaluation）—— 启动类
 * ============================================================================
 *
 * 【这个模块是做什么的】
 *   演示“用 LLM 当裁判（LLM-as-a-judge）”来自动评估一段 AI 回答的质量，
 *   主要看两个维度：
 *     1) 相关性（Relevancy）：回答有没有切题、是否在回答用户真正问的问题。
 *     2) 事实性（Fact Checking）：回答是否被给定的上下文/资料所支持（有没有“胡编”）。
 *
 * 【为什么需要它（“LLM 当裁判”的原理与用途）】
 *   AI 回答是自由文本，没有标准答案、很难用传统断言（equals）来判对错。
 *   于是我们再请“另一个大模型”当裁判：把【问题 + 上下文 + 待评回答】交给它，
 *   让它按规则判定“通过/不通过”并打分。常见用途：
 *     - 回归测试：模型/提示词改动后，批量跑评估，防止质量悄悄下降。
 *     - 质量监控：线上抽样回答做相关性/事实性打分，及时发现问题。
 *
 * 【怎么做】
 *   - 先用 ChatClient 对一个问题拿到 AI 回答；
 *   - 再用 RelevancyEvaluator（相关性裁判）/ FactCheckingEvaluator（事实性裁判）评估这段回答；
 *   - 打印是否通过(isPass)与分数(getScore)。
 *   裁判模型这里直接复用 DeepSeek（与被评模型同一个，纯演示）。
 *
 * 【达到的目的】
 *   掌握 Spring AI 的 Evaluator 用法，理解如何把“主观质量”转成“可自动化的判定”。
 * ============================================================================
 */
@SpringBootApplication
public class EvaluationApplication {
    public static void main(String[] args) {
        SpringApplication.run(EvaluationApplication.class, args);
    }
}
