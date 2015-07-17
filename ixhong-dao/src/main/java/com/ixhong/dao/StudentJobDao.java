package com.ixhong.dao;

import com.ixhong.domain.pojo.StudentJobDO;

/**
 * Created by jenny on 4/22/15.
 *
 */
public interface StudentJobDao {

    public void insert(StudentJobDO studentJob);

    public void update(StudentJobDO studentJobDo);

    public StudentJobDO getByStudentId(Integer studentId);

}
