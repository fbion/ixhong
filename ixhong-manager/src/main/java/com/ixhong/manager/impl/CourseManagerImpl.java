package com.ixhong.manager.impl;

import com.ixhong.dao.CourseDao;
import com.ixhong.domain.pojo.CourseDO;
import com.ixhong.domain.query.CourseQuery;
import com.ixhong.domain.query.QueryResult;
import com.ixhong.manager.CourseManager;

/**
 * Created by cuichengrui on 15/4/10.
 */
public class CourseManagerImpl implements CourseManager {

    private CourseDao courseDao;

    public void setCourseDao(CourseDao courseDao) {
        this.courseDao = courseDao;
    }

    @Override
    public void insert(CourseDO course) {
        this.courseDao.insert(course);
    }

    @Override
    public CourseDO getById(Integer id) {
        return this.courseDao.getById(id);
    }

    @Override
    public void disable(Integer id) {
        this.courseDao.disable(id);
    }

    @Override
    public void audit(CourseDO course) {
        this.courseDao.audit(course);
    }

    @Override
    public QueryResult<CourseDO> query(Integer organizationId,CourseQuery query) {
        return this.courseDao.query(organizationId, query);
    }

    @Override
    public CourseDO getByName(Integer organizationId, String name) {
        return this.courseDao.getByName(organizationId, name);
    }
}
