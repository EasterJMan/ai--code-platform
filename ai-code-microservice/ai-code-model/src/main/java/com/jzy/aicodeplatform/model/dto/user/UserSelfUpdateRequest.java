package com.jzy.aicodeplatform.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * 当前登录用户修改个人资料（不包含账号、密码、角色等敏感字段）
 */
@Data
public class UserSelfUpdateRequest implements Serializable {

    /**
     * 用户昵称；传 null 表示不修改
     */
    private String userName;

    /**
     * 用户头像 URL；传 null 表示不修改；传空字符串表示清空
     */
    private String userAvatar;

    /**
     * 用户简介；传 null 表示不修改
     */
    private String userProfile;

    private static final long serialVersionUID = 1L;
}
