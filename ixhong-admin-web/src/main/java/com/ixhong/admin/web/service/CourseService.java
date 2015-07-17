package com.ixhong.admin.web.service;

import com.ixhong.common.pojo.Result;
import com.ixhong.domain.pojo.CourseDO;
import com.ixhong.domain.query.CourseQuery;

/**
 * Created by cuichengrui on 15/4/14.
 */
public interface CourseService {

    /**
     * 通过条件查询待审核课程列表
     * 当参数均为null的时候，默认查询全部
     * @return
     */
    public Result list(CourseQuery query);

    /**
     * 课程信息审核
     * @param course 课程DO
     * @param remoteIp
     * @return
     */
    public Result audit(CourseDO course, String remoteIp);

    /**
     * 查看课程详情
     * @param id
     * @return
     */
    public Result detail(Integer id);


}
