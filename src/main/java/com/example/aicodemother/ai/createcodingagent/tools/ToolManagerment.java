package com.example.aicodemother.ai.createcodingagent.tools;

import com.example.aicodemother.exception.ErrorCode;
import com.example.aicodemother.exception.ThrowUtils;
import com.example.aicodemother.ai.model.ToolInfo;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @program: ai-code-mother
 * @description: 工具管理
 * @author: lk_hhh
 * @create: 2025-10-14 10:24
 **/
public class ToolManagerment {

    //工具管理器
    private static Map<String, ToolInfo> toolMap = new ConcurrentHashMap<>(16);

    /**
     * 向工具映射中添加新工具的方法
     *
     * @param toolName 要添加的工具名称
     * @param tool     要添加的工具对象
     * @return 如果添加成功返回true，如果工具名称已存在则返回false
     */
    public static Boolean addTool(String toolName, ToolInfo tool) {
        // 检查工具映射中是否已存在该工具名称
        if (toolMap.containsKey(toolName)) {
            // 如果工具名称已存在，返回false表示添加失败
            return false;
        }
        // 如果工具名称不存在，将工具名称和工具对象添加到映射中
        toolMap.put(toolName, tool);
        // 返回true表示添加成功
        return true;
    }

    /**
     * 根据工具名称获取工具对象
     *
     * @param toolName 工具名称
     * @return 对应的工具对象
     */
    public static ToolInfo getTool(String toolName) {
        // 检查工具是否存在，如果不存在则抛出异常
        ThrowUtils.throwIf(!toolMap.containsKey(toolName), ErrorCode.SYSTEM_ERROR, "工具不存在");
        // 返回工具名称对应的工具对象
        return toolMap.get(toolName);
    }

    /**
     * 获取工具映射表的方法
     *
     * @return 返回一个工具名称到工具对象的映射表
     * 如果当前映射表为空或不存在，则返回一个新的空并发映射表
     */
    public static Map<String, ToolInfo> getToolMap() {
        // 检查工具映射表是否为null或空
        if (toolMap == null || toolMap.isEmpty()) {
            // 如果为空或不存在，返回一个新的并发哈希映射实例
            return new ConcurrentHashMap<>();
        }
        // 如果工具映射表存在且不为空，直接返回该映射表
        return toolMap;
    }

}