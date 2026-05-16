package com.jzy.aicodeplatform.model.dto.mq;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 截图任务消息体
 */
@Data
public class ScreenshotTaskMessage implements Serializable {

    /**
     * 幂等任务ID（全链路复用）
     */
    private String taskId;

    private Long appId;

    private String appUrl;

    private LocalDateTime createdTime;

    /**
     * 已重试次数（首次投递为 0）
     */
    private Integer retryCount;
}
