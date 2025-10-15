package com.example.aicodemother.core.handler;

import com.example.aicodemother.model.entity.User;
import com.example.aicodemother.model.enums.CodeGenTypeEnum;
import com.example.aicodemother.service.ChatHistoryService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

/**
 * @program: ai-code-mother
 * @description: 流失处理器执行器
 * @author: lk_hhh
 * @create: 2025-10-15 20:02
 **/
@Slf4j
@Component
public class StreamHandlerExecutor {

    private final SimpleTextStreamHandler simpleTextStreamHandler = new SimpleTextStreamHandler();
    @Resource
    private ComplexJsonStreamHandler complexJsonStreamHandler;

    public Flux<String> doExecutor(Flux<String> tempFlux, CodeGenTypeEnum codeGenTypeEnum, ChatHistoryService chatHistoryService, Long appId, User userLogin) {

        switch (codeGenTypeEnum) {
            case HTML, MULTI_FILE -> {
                return simpleTextStreamHandler.handle(tempFlux, chatHistoryService, appId, userLogin);
            }
            case VUE_PROJECT -> {
                return complexJsonStreamHandler.handle(tempFlux, chatHistoryService, appId, userLogin);
            }
            default -> {
                log.error("不支持的消息类型: {}", codeGenTypeEnum);
                return Flux.empty();
            }
        }


    }


}