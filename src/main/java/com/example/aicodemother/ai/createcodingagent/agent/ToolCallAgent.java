package com.example.aicodemother.ai.createcodingagent.agent;

import com.example.aicodemother.ai.createcodingagent.tools.Tool;
import com.example.aicodemother.ai.createcodingagent.tools.ToolManagerment;
import com.example.aicodemother.model.entity.ToolInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.File;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @program: ai-code-mother
 * @description:
 * @author: lk_hhh
 * @create: 2025-10-14 08:17
 **/
@Slf4j
@EqualsAndHashCode(callSuper = true)
@Component
@Data
public class ToolCallAgent extends ReActAgent {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private ChatModel injectedChatModel;

    @Autowired
    private ObjectMapper objectMapper;

    private Map<String, ToolInfo> toolMap = new ConcurrentHashMap<>(32);
    private String pendingToolName;
    private Map<String, Object> pendingToolArgs = new HashMap<>();

    @Override
    protected Boolean think() {
        if (getNextPrompt() != null && !getNextPrompt().isEmpty()) {
            //构建出下一步提示词
            UserMessage userMessage = UserMessage.from(getNextPrompt());
            getMessagesList().add(userMessage);
        }
        //获取所有的Tool工具列表
        List<String> allToolInfos = getAllToolInfos();
        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append("你是代码生成与工具调度智能体。\n");
        if (getInitialUserMessage() != null && !getInitialUserMessage().isEmpty()) {
            promptBuilder.append("任务: ").append(getInitialUserMessage()).append("\n\n");
        }
        promptBuilder.append("可用工具（从中选择其一）：\n");
        for (String info : allToolInfos) {
            promptBuilder.append("- ").append(info);
        }
        promptBuilder.append("规则：\n")
                .append("- 仅输出严格 JSON，不要包含其它文本。\n")
                .append("- JSON 字段：action(\"ACT\"|\"FINISH\"), toolName(当 ACT 时必填), args(对象), thought(简要理由)。\n")
                .append("- 若任务已完成或无需继续，action=\"FINISH\"。\n")
                .append("示例：{\"action\":\"ACT\",\"toolName\":\"GenerateHtml\",\"args\":{\"title\":\"xxx\"},\"thought\":\"...\"}\n");

        String decisionPrompt = promptBuilder.toString();

        String llmOut;
        try {
            ChatModel chatModel = getChatModel() != null ? getChatModel() : injectedChatModel;
            llmOut = chatModel.chat(decisionPrompt);
        } catch (Exception e) {
            setNextPrompt("LLM 决策失败：" + e.getMessage() + "。请输出合法 JSON 决策。");
            return true;
        }

        ActionDecision decision;
        try {
            decision = objectMapper.readValue(llmOut, ActionDecision.class);
        } catch (Exception e) {
            setNextPrompt("你的输出不是有效 JSON。请仅输出 JSON，包含 action/toolName/args/thought。");
            return true;
        }

        if ("FINISH".equalsIgnoreCase(decision.action)) {
            setState(com.example.aicodemother.model.enums.AgentState.FINISHED);
            setNextPrompt(null);
            return false;
        }

        if (!"ACT".equalsIgnoreCase(decision.action)) {
            setNextPrompt("action 只能为 ACT 或 FINISH。请重新给出 JSON 决策。");
            return true;
        }

        if (decision.toolName == null || !toolMap.containsKey(decision.toolName)) {
            setNextPrompt("选择了不存在的工具：" + decision.toolName + "。请从以下工具中选择：" + String.join(", ", toolMap.keySet()));
            return true;
        }

        this.pendingToolName = decision.toolName;
        this.pendingToolArgs = decision.args != null ? decision.args : new HashMap<>();
        setNextPrompt("将调用工具：" + pendingToolName + "，参数：" + this.pendingToolArgs);
        return true;
    }

    @Override
    protected String act() {
        if (pendingToolName == null) {
            setNextPrompt("未选择工具。请输出 JSON 决策。");
            return "未选择任何工具";
        }

        ToolInfo info = toolMap.get(pendingToolName);
        if (info == null || info.getTool() == null) {
            setNextPrompt("工具不可用：" + pendingToolName + "。请重新选择。");
            return "工具不可用：" + pendingToolName;
        }

        String result;
        try {
            result = info.getTool().execute(pendingToolArgs);
        } catch (Exception e) {
            result = "tool use error: " + e.getMessage();
        }

        String observation = "Observation: tool=" + pendingToolName + ", result=" + truncate(result, 2000);
        getMessagesList().add(UserMessage.from(observation));
        setNextPrompt("基于上述 Observation，继续输出严格 JSON 的下一步决策。");
        return observation;
    }

    @PostConstruct
    public void init() {
        // 等待所有Tool bean初始化
        waitForToolsInitialization();
        // 初始化数据
        toolMap = ToolManagerment.getToolMap();
        if (getChatModel() == null && injectedChatModel != null) {
            setChatModel(injectedChatModel);
        }
    }

    private final List<String> getAllToolInfos() {
        return toolMap.values().stream().map(toolInfo -> "工具名称：" + toolInfo.getName() + "," +
                "工具描述：" + toolInfo.getDescription() + "。\n"
        ).toList();

    }

    private void waitForToolsInitialization() {
        // 这里可以添加一个机制来等待工具bean初始化完成
        while (applicationContext.getBeansOfType(Tool.class).size() < expectedToolCount()) {
            // 等待工具数量达到预期，或者通过其他条件来判断工具是否初始化完毕
            try {
                Thread.sleep(100); // 等待一段时间
            } catch (InterruptedException e) {
                log.error("等待工具初始化时发生异常", e);
            }
        }
    }

    private int expectedToolCount() {
        List<Class<?>> implementingClasses = new ArrayList<>();
        // 返回预期工具数量，根据实际情况进行调整
        String dirPath = "src/main/java/com/example/aicodemother/ai/createcodingagent/tools";
        File toolExPath = new File(dirPath);
        File[] allToolImplFiles = toolExPath.listFiles((dir, name) -> name.endsWith(".class"));
        if (allToolImplFiles == null) {
            return 0;
        }
        try {
            for (File toolImpl : allToolImplFiles) {
                String className = toolImpl.getName().replace(".class", "");
                Class<?> aClass = Class.forName(className);
                if (aClass.isAssignableFrom(Tool.class) && !aClass.equals(Tool.class) && Modifier.isAbstract(aClass.getModifiers())) {
                    implementingClasses.add(aClass);
                }
            }
        } catch (Exception e) {
            log.error("获取工具类失败", e);
        }
        return implementingClasses.size();
    }


    private static String truncate(String s, int max) {
        if (s == null) {
            return "";
        }
        return s.length() <= max ? s : s.substring(0, max);
    }

    @Data
    private static class ActionDecision {
        public String action;
        public String toolName;
        public Map<String, Object> args;
        public String thought;
    }

}