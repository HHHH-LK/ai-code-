package com.example.aicodemother.ai.createcodingagent.agent;

import cn.hutool.core.util.StrUtil;
import com.example.aicodemother.exception.ErrorCode;
import com.example.aicodemother.exception.ThrowUtils;
import com.example.aicodemother.model.enums.AgentState;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @program: ai-code-mother
 * @description: 基础代理智能体
 * @author: lk_hhh
 * @create: 2025-10-14 08:16
 **/
@Component
@Slf4j
@Data
public abstract class BaseAgent {

    private ChatModel chatModel;
    private final int MAX_STEP = 20;
    private int currentStep = 0;
    private String name;
    private AgentState state = AgentState.IDLE;
    private List<ChatMessage> messagesList;
    private String systemPrompt;
    private String nextPrompt;
    private String initialUserMessage;

    public String run(String userMessage) {
        ThrowUtils.throwIf(state == AgentState.RUNNING, ErrorCode.SYSTEM_ERROR, "当前代理正在运行中");
        ThrowUtils.throwIf(StrUtil.isBlank(userMessage), ErrorCode.PARAMS_ERROR, "用户输入不能为空");
        //修改相关状态
        state = AgentState.RUNNING;
        // 初始化消息列表
        if (messagesList == null) {
            messagesList = new ArrayList<>();
        }
        // 记录初始用户意图
        initialUserMessage = userMessage;
        messagesList.add(UserMessage.userMessage(userMessage));
        //定义一个结果队列来存储结果
        List<String> resultList = new ArrayList<>();
        try {
            while (currentStep < MAX_STEP && !state.equals(AgentState.FINISHED)) {
                currentStep++;
                log.info("智能体执行{}/{}，", currentStep, MAX_STEP);
                String stepResult = step();
                String result = "Step " + currentStep + ": " + stepResult;
                resultList.add(result);
            }
            //控制状态修改
            if (currentStep >= MAX_STEP) {
                state = AgentState.FINISHED;
                log.info("智能达到最大执行次数,执行结束");
                resultList.add("Terminated: 达到最大执行次数( " + MAX_STEP + " )");
            }

            if (state.equals(AgentState.FINISHED)) {
                log.info("智能体执行结束");
                resultList.add("Terminated: 执行结束");
                //清理资源
                clean();
            }

        } catch (Exception e) {
            state = AgentState.ERROR;
            log.error("智能体执行异常", e);
            return "智能体执行异常";
        }
        return String.join("\n", resultList);
    }

    protected abstract String step();


    protected void clean() {
        currentStep = 0;
        state = AgentState.IDLE;
        if (messagesList != null) {
            messagesList.clear();
        }
        nextPrompt = null;
        initialUserMessage = null;
    }

}