package com.jzy.aicodeplatform.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class WebScreenshotUtilsTest {

    @Test
    void saveWebPageScreenshot() {
        String screenshotPath = WebScreenshotUtils.saveWebPageScreenshot("https://www.baidu.com");
        Assertions.assertNotNull(screenshotPath);
    }
}