package com.ixhong.admin.web.service;

import com.ixhong.common.pojo.Result;

/**
 * Created by chenguang on 15/4/22.
 */
public interface InterestRateService {

    /**
     * 获取利率列表
     * @return
     */
    public Result list();

    /**
     * 添加利率与期限组合
     * @param timeLimit
     * @param rate
     * @param description
     * @return
     */
    public Result add(Integer timeLimit, Double rate, String description);

    public Result disable(Integer id);
}
