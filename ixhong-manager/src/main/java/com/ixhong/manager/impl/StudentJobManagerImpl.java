package com.ixhong.manager.impl;

import com.ixhong.dao.StudentJobDao;
import com.ixhong.domain.pojo.StudentJobDO;
import com.ixhong.manager.StudentJobManager;

/**
 * Created by jenny on 4/23/15.
 */
public class StudentJobManagerImpl implements StudentJobManager {

    private StudentJobDao studentJobDao;

    public void setStudentJobDao(StudentJobDao studentJobDao){
        this.studentJobDao = studentJobDao;
    }

    @Override
    public void insert(StudentJobDO studentJob) {
        this.studentJobDao.insert(studentJob);
    }

    @Override
    public void update(StudentJobDO StudentJobDO) {
        this.studentJobDao.update(StudentJobDO);
    }

    @Override
    public StudentJobDO getByStudentId(Integer studentId) {
        return this.studentJobDao.getByStudentId(studentId);
    }

}
