package com.example.aicodemother.ai.createcodingagent.agent;


import com.example.aicodemother.model.enums.AgentState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @program: ai-code-mother
 * @description: ReAct智能体
 * @author: lk_hhh
 * @create: 2025-10-14 08:17
 **/


@Slf4j
@Component
public abstract class ReActAgent extends BaseAgent {
    @Override
    protected String step() {
        try {
            //思考是否需要执行
            Boolean thinkIsAct = think();
            return Boolean.TRUE.equals(thinkIsAct) ? act() : callToFinish();
        } catch (Exception e) {
            log.error("智能体执行异常", e);
            return "步骤执行失败：" + e.getMessage();
        }
    }

    private final String callToFinish() {
        return "智能体执行成功";
    }

    protected abstract Boolean think();


    protected abstract String act();
}