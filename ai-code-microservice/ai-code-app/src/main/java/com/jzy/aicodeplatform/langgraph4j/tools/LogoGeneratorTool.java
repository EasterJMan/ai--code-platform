package com.jzy.aicodeplatform.langgraph4j.tools;

import cn.hutool.core.util.StrUtil;
import com.alibaba.dashscope.aigc.imagegeneration.*;
import com.jzy.aicodeplatform.langgraph4j.enums.ImageCategoryEnum;
import com.jzy.aicodeplatform.langgraph4j.model.ImageResource;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;


@Slf4j
@Component
public class LogoGeneratorTool {

    @Value("${dashscope.api-key:}")
    private String dashScopeApiKey;

    @Value("${dashscope.image-model:wan2.6-t2i}")
    private String imageModel;

    @Tool("根据描述生成 Logo 设计图片，用于网站品牌标识")
    public List<ImageResource> generateLogos(@P("Logo 设计描述，如名称、行业、风格等，尽量详细") String description) {
        List<ImageResource> logoList = new ArrayList<>();
        try {
            // 构建 Logo 设计提示词
            String logoPrompt = String.format("生成 Logo，Logo 中禁止包含任何文字！Logo 介绍：%s", description);
            ImageGenerationMessage message = ImageGenerationMessage.builder()
                    .role("user")
                    .content(Collections.singletonList(
                            Collections.singletonMap("text", logoPrompt)
                    )).build();
            ImageGenerationParam param = ImageGenerationParam.builder()
                    .apiKey(dashScopeApiKey)
                    .model(imageModel)
                    .n(1)
                    .size("1024*1024")
                    .negativePrompt("")
                    .promptExtend(true)
                    .watermark(false)
                    .messages(Collections.singletonList(message))
                    .build();

            ImageGeneration imageGeneration = new ImageGeneration();
            ImageGenerationResult result = imageGeneration.call(param);
            if (result != null && result.getOutput() != null && result.getOutput().getChoices() != null) {
                List<Map<String, Object>> results = result.getOutput().getChoices().getFirst().getMessage().getContent();
                for (Map<String, Object> imageResult : results) {
                    String imageUrl = null;
                    // 安全地提取 "image" 字段
                    Object imageUrlObj = imageResult.get("image");
                    if (imageUrlObj instanceof String) {
                        imageUrl = (String) imageUrlObj;
                        log.info("图片的URL:{} ", imageUrl);
                        // 在这里调用你下载图片的方法，比如 downloadImage(imageUrl, "img_" + i + ".png");
                    } else {
                        // 处理字段缺失或类型错误的情况
                        log.error("未能解析张图片的信息。");
                        continue;
                    }
                    if (StrUtil.isNotBlank(imageUrl)) {
                        logoList.add(ImageResource.builder()
                                .category(ImageCategoryEnum.LOGO)
                                .description(description)
                                .url(imageUrl)
                                .build());
                    }
                }
            }
        } catch (Exception e) {
            log.error("生成 Logo 失败: {}", e.getMessage(), e);
        }
        return logoList;
    }
}
