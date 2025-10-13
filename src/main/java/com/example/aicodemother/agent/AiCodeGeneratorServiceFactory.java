package com.example.aicodemother.agent;

import com.example.aicodemother.service.ChatHistoryService;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import dev.langchain4j.community.model.dashscope.QwenStreamingChatModel;
import dev.langchain4j.community.store.memory.chat.redis.RedisChatMemoryStore;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.service.AiServices;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * @program: ai-code-mother
 * @description: Ai服务创建工厂，支持流式 Qwen 模型
 * @author: lk_hhh
 * @create: 2025-10-06 13:34
 **/

@Configuration
@Slf4j
@RequiredArgsConstructor
public class AiCodeGeneratorServiceFactory {

    private final RedisChatMemoryStore redisChatMemoryStore;
    private final ChatHistoryService chatHistoryService;
    private final ChatModel chatModel;
    /**
     * AI 服务实例缓存
     * 缓存策略：
     * - 最大缓存 1000 个实例
     * - 写入后 30 分钟过期
     * - 访问后 10 分钟过期
     */
    private final Cache<Long, AiCodeGeneratorService> serviceCache = Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(Duration.ofMinutes(30))
            .expireAfterAccess(Duration.ofMinutes(10))
            .removalListener((key, value, cause) -> {
                log.debug("AI 服务实例被移除，appId: {}, 原因: {}", key, cause);
            })
            .build();


    @Bean
    public StreamingChatModel streamingChatModel() {
        return QwenStreamingChatModel.builder().apiKey(System.getenv("DASHSCOPE_API_KEY"))
                .modelName("deepseek-v3")
                .build();
    }


    /**
     * 根据 appId 获取服务（带缓存）
     */
    public AiCodeGeneratorService getAiCodeGeneratorService(long appId) {
        return serviceCache.get(appId, this::createAiCodeGeneratorService);
    }

    /**
     * 创建新的 AI 服务实例
     */
    private AiCodeGeneratorService createAiCodeGeneratorService(long appId) {
        log.info("为 appId: {} 创建新的 AI 服务实例", appId);
        // 根据 appId 构建独立的对话记忆
        MessageWindowChatMemory chatMemory = MessageWindowChatMemory
                .builder()
                .id(appId)
                .chatMemoryStore(redisChatMemoryStore)
                .maxMessages(20)
                .build();
        //从数据库中加载数据到内存中
        chatHistoryService.LoadChatHistoryToMemory(appId, chatMemory, 20);
        return AiServices.builder(AiCodeGeneratorService.class)
                .chatModel(chatModel)
                .streamingChatModel(streamingChatModel())
                .chatMemory(chatMemory)
                .build();
    }


    @Bean
    public AiCodeGeneratorService aiCodeGeneratorService() {
        return createAiCodeGeneratorService(0L);
    }
}
