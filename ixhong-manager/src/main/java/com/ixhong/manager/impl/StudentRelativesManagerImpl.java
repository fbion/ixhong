package com.ixhong.manager.impl;

import com.ixhong.dao.StudentRelativesDao;
import com.ixhong.domain.pojo.StudentRelativesDO;
import com.ixhong.manager.StudentRelativesManager;

/**
 * Created by jenny on 4/23/15.
 */
public class StudentRelativesManagerImpl implements StudentRelativesManager {

    private StudentRelativesDao studentRelativesDao;

    public void setStudentRelativesDao(StudentRelativesDao studentRelativesDao){
        this.studentRelativesDao = studentRelativesDao;
    }

    @Override
    public void insert(StudentRelativesDO studentRelatives) {
        this.studentRelativesDao.insert(studentRelatives);
    }

    @Override
    public void update(StudentRelativesDO studentRelatives) {
        this.studentRelativesDao.update(studentRelatives);
    }

    @Override
    public StudentRelativesDO getByStudentId(Integer studentId) {
        return this.studentRelativesDao.getByStudentId(studentId);
    }

}
