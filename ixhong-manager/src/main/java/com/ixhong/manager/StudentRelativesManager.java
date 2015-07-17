package com.ixhong.manager;

import com.ixhong.domain.pojo.StudentRelativesDO;

/**
 * Created by jenny on 4/23/15.
 */
public interface StudentRelativesManager {

    public void insert(StudentRelativesDO studentRelatives);

    public void update(StudentRelativesDO studentRelatives);

    public StudentRelativesDO getByStudentId(Integer studentId);
}
