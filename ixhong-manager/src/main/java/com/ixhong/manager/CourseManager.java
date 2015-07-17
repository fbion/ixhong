package com.ixhong.manager;

import com.ixhong.domain.pojo.CourseDO;
import com.ixhong.domain.query.CourseQuery;
import com.ixhong.domain.query.QueryResult;

/**
 * Created by cuichengrui on 15/4/10.
 */
public interface CourseManager {

    public void insert(CourseDO course);

    public CourseDO getById(Integer id);

    public void disable(Integer id);

    public void audit(CourseDO course);

    public QueryResult<CourseDO> query(Integer organizationId, CourseQuery query);

    public CourseDO getByName(Integer organizationId, String name);
}
