package com.ixhong.manager;

import com.ixhong.domain.pojo.StudentEducationDO;

/**
 * Created by jenny on 4/23/15.
 */
public interface StudentEducationManager {

    public void insert(StudentEducationDO studentEducation);

    public void update(StudentEducationDO studentEducation);

    public StudentEducationDO getByStudentId(Integer studentId);
}
