package com.ixhong.dao;

import com.ixhong.domain.pojo.StudentRelativesDO;

/**
 * Created by jenny on 4/22/15.
 * 学生亲属
 */
public interface StudentRelativesDao {

    public void insert(StudentRelativesDO studentRelatives);

    public void update(StudentRelativesDO studentRelatives);

    public StudentRelativesDO getByStudentId(Integer studentId);
}
