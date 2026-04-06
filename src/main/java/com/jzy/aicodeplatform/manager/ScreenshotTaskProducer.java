package com.jzy.aicodeplatform.manager;

import com.jzy.aicodeplatform.constant.ScreenshotMqConstant;
import com.jzy.aicodeplatform.model.dto.mq.ScreenshotTaskMessage;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 截图任务生产者
 */
@Component
@Slf4j
@ConditionalOnProperty(prefix = "app.screenshot", name = "mq-enabled", havingValue = "true")
public class ScreenshotTaskProducer {

    @Resource
    private RocketMQTemplate rocketMQTemplate;

    @Value("${app.screenshot.topic:" + ScreenshotMqConstant.SCREENSHOT_TOPIC + "}")
    private String screenshotTopic;

    @Value("${app.screenshot.tag:" + ScreenshotMqConstant.SCREENSHOT_TAG + "}")
    private String screenshotTag;

    public void sendScreenshotTask(Long appId, String appUrl) {
        ScreenshotTaskMessage message = new ScreenshotTaskMessage();
        message.setTaskId(UUID.randomUUID().toString());
        message.setAppId(appId);
        message.setAppUrl(appUrl);
        message.setCreatedTime(LocalDateTime.now());
        message.setRetryCount(0);
        String destination = screenshotTopic + ":" + screenshotTag;
        rocketMQTemplate.convertAndSend(destination, message);
        log.info("截图任务已投递, taskId={}, appId={}, appUrl={}", message.getTaskId(), appId, appUrl);
    }

    public void sendDelayScreenshotTask(ScreenshotTaskMessage message, int delayLevel) {
        String destination = screenshotTopic + ":" + screenshotTag;
        // 通过 RocketMQ 延迟级别实现退避重试
        rocketMQTemplate.syncSend(destination, MessageBuilder.withPayload(message).build(), 3000, delayLevel);
        log.info("截图任务已延迟重投, appId={}, retryCount={}, delayLevel={}",
                message.getAppId(), message.getRetryCount(), delayLevel);
    }
}
