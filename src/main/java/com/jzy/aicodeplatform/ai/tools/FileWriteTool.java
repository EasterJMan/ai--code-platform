package com.jzy.aicodeplatform.ai.tools;

import cn.hutool.core.util.StrUtil;
import com.jzy.aicodeplatform.constant.AppConstant;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolMemoryId;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * 文件写入工具
 */
@Slf4j
public class FileWriteTool {

    @Tool("写入文件到指定路径")
    public String writeFile(
            @P("文件的相对路径") String filePath,
            @P("要写入的完整文件正文（UTF-8）。工具调用 JSON 中须对引号、反斜杠、换行等按 JSON 规范正确转义") String content,
            @ToolMemoryId Long appId) {
        try {
            if (StrUtil.isBlank(filePath) || StrUtil.isBlank(content) || appId == null) {
                return "文件写入失败：参数不完整，filePath=" + filePath + ", appId=" + appId;
            }
            Path path = Paths.get(filePath);
            if (!path.isAbsolute()) {
                String projectDirName = "vue_project" + appId;
                Path projectRoot = Paths.get(AppConstant.CODE_OUTPUT_ROOT_DIR, projectDirName);
                path = projectRoot.resolve(filePath);
            }
            Path parent = path.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.write(path, content.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            log.info("文件写入成功 :{}", path.toAbsolutePath());
            return "文件写入成功" + filePath;
        } catch (IOException e) {
            String result = "文件写入失败：" + filePath + ",错误:" + e.getMessage();
            log.error(result, e);
            return result;
        }
    }
}
