package com.example.aicodemother.ai.model;

import com.example.aicodemother.ai.createcodingagent.tools.Tool;
import lombok.Data;

/**
 * @program: ai-code-mother
 * @description: 工具描述类
 * @author: lk_hhh
 * @create: 2025-10-14 11:11
 **/
@Data
public class ToolInfo {

    private String name;
    private String description;
    private Tool tool;

}