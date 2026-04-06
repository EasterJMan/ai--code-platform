package com.jzy.aicodeplatform.mq;

import cn.hutool.core.util.StrUtil;
import com.jzy.aicodeplatform.constant.ScreenshotMqConstant;
import com.jzy.aicodeplatform.manager.ScreenshotTaskProducer;
import com.jzy.aicodeplatform.model.dto.mq.ScreenshotTaskMessage;
import com.jzy.aicodeplatform.model.entity.App;
import com.jzy.aicodeplatform.service.AppService;
import com.jzy.aicodeplatform.service.ScreenShotService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 截图任务消费者
 */
@Component
@Slf4j
@ConditionalOnProperty(prefix = "app.screenshot", name = "mq-enabled", havingValue = "true")
@RocketMQMessageListener(
        topic = "${app.screenshot.topic:screenshot-topic}",
        selectorExpression = "${app.screenshot.tag:generate}",
        consumerGroup = "${app.screenshot.consumer-group:screenshot-consumer-group}",
        consumeMode = ConsumeMode.CONCURRENTLY
)
public class ScreenshotTaskConsumer implements RocketMQListener<ScreenshotTaskMessage> {
    /**
     * RocketMQ 延迟等级：3=10s, 4=30s, 5=1m
     */
    private static final int[] RETRY_DELAY_LEVELS = {3, 4, 5};

    @Resource
    private ScreenShotService screenShotService;

    @Resource
    private AppService appService;

    @Resource
    private ScreenshotTaskProducer screenshotTaskProducer;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Value("${app.screenshot.max-retry:" + ScreenshotMqConstant.DEFAULT_MAX_RETRY + "}")
    private int maxRetry;

    @Override
    public void onMessage(ScreenshotTaskMessage message) {
        if (message == null || StrUtil.isBlank(message.getTaskId())
                || message.getAppId() == null || StrUtil.isBlank(message.getAppUrl())) {
            log.error("收到非法截图消息: {}", message);
            return;
        }
        if (!tryAcquireIdempotentLock(message.getTaskId())) {
            log.info("检测到重复截图消息，已跳过, taskId={}, appId={}", message.getTaskId(), message.getAppId());
            return;
        }
        Long appId = message.getAppId();
        String appUrl = message.getAppUrl();
        int retryCount = message.getRetryCount() == null ? 0 : message.getRetryCount();
        try {
            log.info("开始消费截图任务, taskId={}, appId={}, appUrl={}, retryCount={}",
                    message.getTaskId(), appId, appUrl, retryCount);
            String screenshotUrl = screenShotService.generateAndUploadScreenshot(appUrl);
            if (StrUtil.isBlank(screenshotUrl)) {
                throw new IllegalStateException("截图上传结果为空");
            }
            App updateApp = new App();
            updateApp.setId(appId);
            updateApp.setCover(screenshotUrl);
            boolean updated = appService.updateById(updateApp);
            if (!updated) {
                throw new IllegalStateException("更新应用封面失败, appId=" + appId);
            }
            log.info("截图任务处理成功, appId={}, cover={}", appId, screenshotUrl);
        } catch (Exception e) {
            scheduleRetry(message, e);
        }
    }

    private void scheduleRetry(ScreenshotTaskMessage message, Exception cause) {
        int currentRetry = message.getRetryCount() == null ? 0 : message.getRetryCount();
        if (currentRetry >= maxRetry) {
            log.error("截图任务达到最大重试次数, appId={}, appUrl={}, retryCount={}",
                    message.getAppId(), message.getAppUrl(), currentRetry, cause);
            return;
        }
        int nextRetry = currentRetry + 1;
        int delayLevel = RETRY_DELAY_LEVELS[Math.min(nextRetry - 1, RETRY_DELAY_LEVELS.length - 1)];
        ScreenshotTaskMessage retryMessage = new ScreenshotTaskMessage();
        retryMessage.setTaskId(message.getTaskId());
        retryMessage.setAppId(message.getAppId());
        retryMessage.setAppUrl(message.getAppUrl());
        retryMessage.setCreatedTime(message.getCreatedTime());
        retryMessage.setRetryCount(nextRetry);
        try {
            screenshotTaskProducer.sendDelayScreenshotTask(retryMessage, delayLevel);
            log.warn("截图任务处理失败，已进入延迟重试, appId={}, retryCount={}, delayLevel={}",
                    retryMessage.getAppId(), retryMessage.getRetryCount(), delayLevel, cause);
        } catch (Exception sendEx) {
            log.error("截图任务重试投递失败, appId={}, retryCount={}",
                    retryMessage.getAppId(), retryMessage.getRetryCount(), sendEx);
            throw new RuntimeException("截图任务重试投递失败", sendEx);
        }
    }

    private boolean tryAcquireIdempotentLock(String taskId) {
        String idempotentKey = ScreenshotMqConstant.IDEMPOTENT_KEY_PREFIX + taskId;
        Boolean success = stringRedisTemplate.opsForValue().setIfAbsent(
                idempotentKey,
                "1",
                ScreenshotMqConstant.IDEMPOTENT_EXPIRE_HOURS,
                TimeUnit.HOURS
        );
        return Boolean.TRUE.equals(success);
    }
}
