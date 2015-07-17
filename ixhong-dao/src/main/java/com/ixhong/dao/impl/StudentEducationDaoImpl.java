package com.ixhong.dao.impl;

import com.ixhong.dao.StudentEducationDao;
import com.ixhong.domain.pojo.StudentEducationDO;

/**
 * Created by jenny on 4/22/15.
 * 学生教育
 */
public class StudentEducationDaoImpl extends BaseDao implements StudentEducationDao {
    @Override
    public void insert(StudentEducationDO studentEducation) {
        this.sqlSession.insert("StudentEducationMapper.insert",studentEducation);
    }

    @Override
    public void update(StudentEducationDO studentEducation) {
        this.sqlSession.update("StudentEducationMapper.update",studentEducation);
    }

    @Override
    public StudentEducationDO getByStudentId(Integer studentId) {
        return this.sqlSession.selectOne("StudentEducationMapper.getByStudentId",studentId);
    }
}
