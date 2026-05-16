package com.jzy.aicodeplatform.model.dto.app;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户更新自己应用请求（仅支持修改名称）
 */
@Data
public class AppUpdateRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 应用名称
     */
    private String appName;

    private static final long serialVersionUID = 1L;
}

