package com.jzy.aicodeplatform.service;

import com.jzy.aicodeplatform.model.dto.app.AppQueryRequest;
import com.jzy.aicodeplatform.model.entity.App;
import com.jzy.aicodeplatform.model.vo.AppVO;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;

import java.util.List;

public interface AppService extends IService<App> {

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

}



