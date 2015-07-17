package com.ixhong.dao;

import com.ixhong.domain.pojo.StudentEducationDO;

/**
 * Created by jenny on 4/22/15.
 * 学生教育经历
 */
public interface StudentEducationDao {

    public void insert(StudentEducationDO studentEducation);

    public void update(StudentEducationDO studentEducation);

    public StudentEducationDO getByStudentId(Integer studentId);
}
