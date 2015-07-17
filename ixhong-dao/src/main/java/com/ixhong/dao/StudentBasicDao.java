package com.ixhong.dao;

import com.ixhong.domain.pojo.StudentBasicDO;

import java.util.List;

/**
 * Created by jenny on 4/22/15.
 * 学生基本信息
 */
public interface StudentBasicDao {

    public void insert(StudentBasicDO studentBasic);

    public void update(StudentBasicDO studentBasic);

    public StudentBasicDO getByStudentId(Integer studentId);

    public List<StudentBasicDO> getByStudentIds(List<Integer> studentIds);
}
