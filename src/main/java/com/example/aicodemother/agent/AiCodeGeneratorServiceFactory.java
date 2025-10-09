package com.example.aicodemother.agent;

import dev.langchain4j.community.model.dashscope.QwenStreamingChatModel;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.service.AiServices;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;

/**
 * @program: ai-code-mother
 * @description: Ai服务创建工厂，支持流式 Qwen 模型
 * @author: lk_hhh
 * @create: 2025-10-06 13:34
 **/

@Configuration
@RequiredArgsConstructor
public class AiCodeGeneratorServiceFactory {

    private final ChatModel chatModel;  // 原来的 ChatModel，保持不变

    @Bean
    public StreamingChatModel streamingChatModel() {
        return QwenStreamingChatModel.builder().apiKey(System.getenv("DASHSCOPE_API_KEY"))
                .modelName("deepseek-v3")
                .build();
    }

    @Bean
    public AiCodeGeneratorService aiCodeGeneratorService() {
        return AiServices.builder(AiCodeGeneratorService.class)
                .chatModel(chatModel)
                .streamingChatModel(streamingChatModel())  // 注入 StreamingChatModel Bean
                .build();
    }
}
