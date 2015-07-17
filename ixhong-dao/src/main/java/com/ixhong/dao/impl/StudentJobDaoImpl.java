package com.ixhong.dao.impl;

import com.ixhong.dao.StudentJobDao;
import com.ixhong.domain.pojo.StudentJobDO;

/**
 * Created by jenny on 4/22/15.
 * 学生工作
 */
public class StudentJobDaoImpl extends BaseDao implements StudentJobDao {
    @Override
    public void insert(StudentJobDO studentJob) {
        this.sqlSession.insert("StudentJobMapper.insert",studentJob);
    }

    @Override
    public void update(StudentJobDO studentJob) {
        this.sqlSession.update("StudentJobMapper.update",studentJob);
    }

    @Override
    public StudentJobDO getByStudentId(Integer studentId) {
        return this.sqlSession.selectOne("StudentJobMapper.getByStudentId",studentId);
    }
}
