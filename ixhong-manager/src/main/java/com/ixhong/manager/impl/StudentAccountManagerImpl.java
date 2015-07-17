package com.ixhong.manager.impl;

import com.ixhong.dao.StudentAccountDao;
import com.ixhong.domain.pojo.StudentAccountDO;
import com.ixhong.manager.StudentAccountManager;

/**
 * Created by Chengrui on 2015/5/13.
 */
public class StudentAccountManagerImpl implements StudentAccountManager {

    private StudentAccountDao studentAccountDao;

    public void setStudentAccountDao(StudentAccountDao studentAccountDao) {
        this.studentAccountDao = studentAccountDao;
    }

    @Override
    public void update(StudentAccountDO studentAccount) {
        this.studentAccountDao.update(studentAccount);
    }

    @Override
    public void insert(StudentAccountDO studentAccount) {
        this.studentAccountDao.insert(studentAccount);
    }

    @Override
    public StudentAccountDO getById(Integer id) {
        return this.studentAccountDao.getById(id);
    }

    @Override
    public StudentAccountDO getByStudentId(Integer studentId) {
        return this.studentAccountDao.getByStudentId(studentId);
    }

    @Override
    public void updateBalanceFrozen(Integer studentId, String balanceFrozen) {
        this.updateBalanceFrozen(studentId, balanceFrozen);
    }

    @Override
    public StudentAccountDO getByStudentIdForUpdate(Integer studentId) {
        return this.studentAccountDao.getByStudentIdForUpdate(studentId);
    }

    @Override
    public void updateBalance(StudentAccountDO studentAccount) {
        this.studentAccountDao.updateBalance(studentAccount);
    }
}
