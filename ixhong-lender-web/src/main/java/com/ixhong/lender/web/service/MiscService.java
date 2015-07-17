package com.ixhong.lender.web.service;

import com.ixhong.common.pojo.Result;

/**
 * Created by chenguang on 2015/6/26 0026.
 */
public interface MiscService {

    /**
     * 最新公告列表
     * @return
     */
    public Result noticeList();

    /**
     * 获取公告详情
     * @param noticeId
     * @return
     */
    public Result noticeDetail(Integer noticeId);

    /**
     * 媒体报道列表
     * @return
     */
    public Result reportList();

    /**
     * 媒体报道详情
     * @return
     */
    public Result reportDetail(Integer reportId);
}
