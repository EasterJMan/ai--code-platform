package com.jzy.aicodeplatform.constant;

/**
 * 网页截图 MQ 常量
 */
public interface ScreenshotMqConstant {

    String SCREENSHOT_TOPIC = "screenshot-topic";

    String SCREENSHOT_TAG = "generate";

    String SCREENSHOT_CONSUMER_GROUP = "screenshot-consumer-group";

    String IDEMPOTENT_KEY_PREFIX = "screenshot:task:";

    long IDEMPOTENT_EXPIRE_HOURS = 24L;

    int DEFAULT_MAX_RETRY = 3;
}
