package com.example.aicodemother.core.handler;

import com.example.aicodemother.model.entity.User;
import com.example.aicodemother.service.ChatHistoryService;
import reactor.core.publisher.Flux;

public interface Handler {


    Flux<String> handle(Flux<String> originFlux,
                        ChatHistoryService chatHistoryService,
                        long appId, User loginUser);
}
