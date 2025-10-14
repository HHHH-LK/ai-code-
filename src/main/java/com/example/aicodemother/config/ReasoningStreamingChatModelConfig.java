package com.example.aicodemother.config;

import dev.langchain4j.community.model.dashscope.QwenStreamingChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import lombok.Data;

/**
 * @program: ai-code-mother
 * @description: 推理模型获取
 * @author: lk_hhh
 * @create: 2025-10-14 20:09
 **/

@Configuration
@Data
public class ReasoningStreamingChatModelConfig {


    @Bean
    public QwenStreamingChatModel reasoningStreamingChatModel() {

        return QwenStreamingChatModel.builder()
                .apiKey(System.getenv("DASHSCOPE_API_KEY"))
                .modelName("deepseek-r1")
                .maxTokens(32768)
                .build();
    }


}