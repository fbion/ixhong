package com.ixhong.dao.impl;

import com.ixhong.dao.StudentAccountDao;
import com.ixhong.domain.pojo.StudentAccountDO;

/**
 * Created by Chengrui on 2015/5/13.
 */
public class StudentAccountDaoImpl extends BaseDao implements StudentAccountDao {

    @Override
    public void insert(StudentAccountDO studentAccount) {
        this.sqlSession.insert("StudentAccountMapper.insert", studentAccount);
    }

    @Override
    public void update(StudentAccountDO studentAccount) {
        this.sqlSession.update("StudentAccountMapper.update", studentAccount);
    }

    @Override
    public StudentAccountDO getById(Integer id) {
        return this.sqlSession.selectOne("StudentAccountMapper.getById", id);
    }

    @Override
    public StudentAccountDO getByStudentId(Integer studentId) {
        return this.sqlSession.selectOne("StudentAccountMapper.getByStudentId", studentId);
    }

    @Override
    public StudentAccountDO getByStudentIdForUpdate(Integer studentId) {
        return this.sqlSession.selectOne("StudentAccountMapper.getByStudentIdForUpdate", studentId);
    }

    @Override
    public void updateBalance(StudentAccountDO studentAccount) {
        this.sqlSession.update("StudentAccountMapper.updateBalance", studentAccount);
    }
}
