package com.example.aicodemother.controller;

import com.example.aicodemother.common.BaseResponse;
import com.example.aicodemother.common.ResultUtils;
import dev.langchain4j.model.chat.ChatModel;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/health")
@RequiredArgsConstructor
public class HealthController {

    private final ChatModel chatModel;

    @GetMapping("/")
    public BaseResponse<String> healthCheck() {
        return ResultUtils.success("OK");
    }

    @GetMapping("/ai")
    public BaseResponse<String> aiCheck() {

        String result = chatModel.chat("你好");

        return ResultUtils.success(result);

    }


}