package com.jzy.aicodeplatform.ai.tools;

import com.jzy.aicodeplatform.constant.AppConstant;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolMemoryId;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * 文件写入工具
 */
@Slf4j
public class FileWriteTool {

    @Tool
    public String writeFile(@P("文件的相对路径") String filePath, @P("要写入文件的内容") String content, @ToolMemoryId Long appId) {
        try {
            Path path = Paths.get(filePath);
            if (!path.isAbsolute()) {
                //相对路径转换为绝对路径，创建基于appId的项目目录
                String projectDirName = "vue_project" + appId;
                Path projectRoot = Paths.get(AppConstant.CODE_OUTPUT_ROOT_DIR, projectDirName);
                path = projectRoot.resolve(filePath);
            }
            //创建父目录，如果不存在
            Path parent = path.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.write(path, content.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            log.info("文件写入成功 :{}",path.toAbsolutePath());
            return "文件写入成功" + filePath;
        } catch (IOException e) {
            String result =  "文件写入失败："+ filePath + ",错误:"  + e.getMessage();
            log.error(result, e);
            return result;
        }
    }
}
