package com.ixhong.manager.impl;

import com.ixhong.dao.StudentAccountFlowDao;
import com.ixhong.domain.pojo.StudentAccountFlowDO;
import com.ixhong.domain.query.QueryResult;
import com.ixhong.domain.query.StudentAccountFlowQuery;
import com.ixhong.manager.StudentAccountFlowManager;

import java.util.List;

/**
 * Created by Chengrui on 2015/5/13.
 */
public class StudentAccountFlowManagerImpl implements StudentAccountFlowManager {

    private StudentAccountFlowDao studentAccountFlowDao;

    public void setStudentAccountFlowDao(StudentAccountFlowDao studentAccountFlowDao) {
        this.studentAccountFlowDao = studentAccountFlowDao;
    }

    @Override
    public void update(StudentAccountFlowDO studentAccountFlow) {
        this.studentAccountFlowDao.update(studentAccountFlow);
    }

    @Override
    public void insert(StudentAccountFlowDO studentAccountFlow) {
        this.studentAccountFlowDao.insert(studentAccountFlow);
    }

    @Override
    public StudentAccountFlowDO getById(Integer id) {
        return this.studentAccountFlowDao.getById(id);
    }

    @Override
    public StudentAccountFlowDO getByOrderId(String orderId) {
        return this.studentAccountFlowDao.getByOrderId(orderId);
    }

    @Override
    public QueryResult<StudentAccountFlowDO> query(StudentAccountFlowQuery query) {
        return this.studentAccountFlowDao.query(query);
    }

    @Override
    public List<StudentAccountFlowDO> getByStatus(Integer status, Integer type) {
        return this.studentAccountFlowDao.getByStatus(status, type);
    }
}
