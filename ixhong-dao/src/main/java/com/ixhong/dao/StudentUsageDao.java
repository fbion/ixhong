package com.ixhong.dao;

import com.ixhong.domain.pojo.StudentUsageDO;

import java.util.List;

/**
 * Created by jenny on 4/22/15.
 * 学生贷款用途
 */
public interface StudentUsageDao {
    
    public void insert(StudentUsageDO studentUsage);

    public void update(StudentUsageDO studentUsageDo);

    public StudentUsageDO getByStudentId(Integer studentId);

    public List<StudentUsageDO> getByStudentIds(List<Integer> studentIds);

}
