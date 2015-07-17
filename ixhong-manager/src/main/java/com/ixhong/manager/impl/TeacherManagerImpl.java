package com.ixhong.manager.impl;

import com.ixhong.dao.TeacherDao;
import com.ixhong.domain.pojo.TeacherDO;
import com.ixhong.domain.query.QueryResult;
import com.ixhong.domain.query.TeacherQuery;
import com.ixhong.manager.TeacherManager;

/**
 * Created by liuguanqing on 15/4/7.
 */
public class TeacherManagerImpl implements TeacherManager {

    private TeacherDao teacherDao;

    public void setTeacherDao(TeacherDao teacherDao) {
        this.teacherDao = teacherDao;
    }

    @Override
    public void insert(TeacherDO teacher) {
        this.teacherDao.insert(teacher);
    }

    @Override
    public TeacherDO validate(String name, String password) {
        return this.teacherDao.validate(name,password);
    }

    @Override
    public void delete(Integer id) {
        this.teacherDao.delete(id);
    }

    @Override
    public TeacherDO getById(int id) {
        return this.teacherDao.getById(id);
    }

    @Override
    public void updatePassword(int id, String password, int status) {
        this.teacherDao.updatePassword(id, password, status);
    }

    @Override
    public TeacherDO getByTelephone(String phone) {
        return this.teacherDao.getByTelephone(phone);
    }

    @Override
    public QueryResult<TeacherDO> query(Integer organizationId,TeacherQuery query) {
        return this.teacherDao.query(organizationId, query);
    }

}
