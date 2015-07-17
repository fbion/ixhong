package com.ixhong.manager.impl;

import com.ixhong.dao.StudentUsageDao;
import com.ixhong.domain.pojo.StudentUsageDO;
import com.ixhong.manager.StudentUsageManager;

import java.util.List;

/**
 * Created by jenny on 4/23/15.
 */
public class StudentUsageManagerImpl implements StudentUsageManager {

    private StudentUsageDao studentUsageDao;

    public void setStudentUsageDao(StudentUsageDao studentUsageDao){
        this.studentUsageDao = studentUsageDao;
    }

    @Override
    public void insert(StudentUsageDO studentUsage) {
        this.studentUsageDao.insert(studentUsage);
    }

    @Override
    public void update(StudentUsageDO studentUsageDo) {
        this.studentUsageDao.update(studentUsageDo);
    }

    @Override
    public StudentUsageDO getByStudentId(Integer studentId) {
        return this.studentUsageDao.getByStudentId(studentId);
    }

    @Override
    public List<StudentUsageDO> getByStudentIds(List<Integer> studentIds) {

        return this.studentUsageDao.getByStudentIds(studentIds);
    }

}

