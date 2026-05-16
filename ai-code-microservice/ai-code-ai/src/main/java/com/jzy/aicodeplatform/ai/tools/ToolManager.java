package com.jzy.aicodeplatform.ai.tools;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class ToolManager {

    /**
     * 工具名称到工具实例的映射
     */
    private final ConcurrentHashMap<String, BaseTool> toolMap = new ConcurrentHashMap<>();

    /**
     * 自动注入所有工具
     */
    @Resource
    private BaseTool[] tools;

    @PostConstruct
    public void initTools() {
        for (BaseTool tool : tools) {
            toolMap.put(tool.getToolName(), tool);
            log.info("已注册工具：{}-{}", tool.getDisplayName(), tool.getDisplayName());
        }
        log.info("工具管理器初始化完成：共注册 {} 个工具", toolMap.size());
    }

    /**
     * 根据工具名称获取工具实例
     */
    public BaseTool getTool(String toolName) {
        return toolMap.get(toolName);
    }

    /**
     * 获取所有工具
     */
    public BaseTool[] getAllTools() {
        return tools;
    }
}
