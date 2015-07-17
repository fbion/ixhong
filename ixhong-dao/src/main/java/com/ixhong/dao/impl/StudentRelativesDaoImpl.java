package com.ixhong.dao.impl;

import com.ixhong.dao.StudentRelativesDao;
import com.ixhong.domain.pojo.StudentRelativesDO;

/**
 * Created by jenny on 4/22/15.
 * 学生亲属
 */
public class StudentRelativesDaoImpl extends BaseDao implements StudentRelativesDao {
    @Override
    public void insert(StudentRelativesDO studentRelatives) {
        this.sqlSession.insert("StudentRelativesMapper.insert",studentRelatives);
    }

    @Override
    public void update(StudentRelativesDO studentRelatives) {
         this.sqlSession.update("StudentRelativesMapper.update",studentRelatives);
    }

    @Override
    public StudentRelativesDO getByStudentId(Integer studentId) {
        return this.sqlSession.selectOne("StudentRelativesMapper.getByStudentId",studentId);
    }
}
