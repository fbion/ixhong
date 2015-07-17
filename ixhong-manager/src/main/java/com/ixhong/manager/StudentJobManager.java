package com.ixhong.manager;

import com.ixhong.domain.pojo.StudentJobDO;

/**
 * Created by jenny on 4/23/15.
 */
public interface StudentJobManager {

    public void insert(StudentJobDO studentJob);

    public void update(StudentJobDO studentJobDo);

    public StudentJobDO getByStudentId(Integer studentId);
}
