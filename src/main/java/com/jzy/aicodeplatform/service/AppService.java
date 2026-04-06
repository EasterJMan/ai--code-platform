package com.jzy.aicodeplatform.service;

import com.jzy.aicodeplatform.model.dto.app.AppAddRequest;
import com.jzy.aicodeplatform.model.dto.app.AppQueryRequest;
import com.jzy.aicodeplatform.model.entity.App;
import com.jzy.aicodeplatform.model.entity.User;
import com.jzy.aicodeplatform.model.vo.AppVO;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 *  服务层。
 *
 * @author ai
 */
public interface AppService extends IService<App> {

    Long createApp(AppAddRequest appAddRequest,User loginUser);

    /**
     * 通过对话生成应用代码
     * @param appId 应用id
     * @param userMessage  提示词
     * @param loginUser 登录用户
     * @return
     */
    Flux<String> chatToGenCode(Long appId, String userMessage, User loginUser);

    /**
     * 应用部署
     * @param appId
     * @param loginUser
     * @return 可访问的部署地址
     */
    String deployApp(Long appId,User loginUser);

    /**
     * 获取应用封装类
     *
     * @param app 应用实体
     * @return 视图对象
     */
    AppVO getAppVO(App app);

    /**
     * 获取应用封装类对象列表
     *
     * @param appList 应用实体列表
     * @return 视图对象列表
     */
    List<AppVO> getAppVOList(List<App> appList);

    /**
     * 获取应用查询包装器
     *
     * @param appQueryRequest 查询请求
     * @return 查询包装器
     */
    QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest);


    /**
     * 异步生成应用截图封面
     * @param appId
     * @param appUrl
     */
    void generateAppScreenshotAsync(Long appId, String appUrl);
}


