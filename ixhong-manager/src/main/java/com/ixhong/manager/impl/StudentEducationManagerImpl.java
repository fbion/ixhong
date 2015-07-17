package com.ixhong.manager.impl;

import com.ixhong.dao.StudentEducationDao;
import com.ixhong.domain.pojo.StudentEducationDO;
import com.ixhong.manager.StudentEducationManager;

/**
 * Created by jenny on 4/23/15.
 */
public class StudentEducationManagerImpl implements StudentEducationManager {

    private StudentEducationDao studentEducationDao;

    public void setStudentEducationDao(StudentEducationDao studentEducationDao){
        this.studentEducationDao = studentEducationDao;
    }

    @Override
    public void insert(StudentEducationDO studentEducation) {
        this.studentEducationDao.insert(studentEducation);
    }

    @Override
    public void update(StudentEducationDO studentEducation) {
        this.studentEducationDao.update(studentEducation);
    }

    @Override
    public StudentEducationDO getByStudentId(Integer studentId) {
        return this.studentEducationDao.getByStudentId(studentId);
    }

}
