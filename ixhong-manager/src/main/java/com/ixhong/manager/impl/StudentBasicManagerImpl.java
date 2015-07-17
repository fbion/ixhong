package com.ixhong.manager.impl;

import com.ixhong.dao.StudentBasicDao;
import com.ixhong.domain.pojo.StudentBasicDO;
import com.ixhong.manager.StudentBasicManager;

import java.util.List;

/**
 * Created by jenny on 4/23/15.
 */
public class StudentBasicManagerImpl implements StudentBasicManager {

    private StudentBasicDao studentBasicDao;

    public void setStudentBasicDao(StudentBasicDao studentBasicDao){
        this.studentBasicDao = studentBasicDao;
    }

    @Override
    public void insert(StudentBasicDO studentBasic) {
        this.studentBasicDao.insert(studentBasic);
    }

    @Override
    public void update(StudentBasicDO studentBasic) {
        this.studentBasicDao.update(studentBasic);
    }

    @Override
    public StudentBasicDO getByStudentId(Integer studentId) {
        return this.studentBasicDao.getByStudentId(studentId);
    }

    @Override
    public List<StudentBasicDO> getByStudentIds(List<Integer> studentIds) {
        return this.studentBasicDao.getByStudentIds(studentIds);
    }

}
