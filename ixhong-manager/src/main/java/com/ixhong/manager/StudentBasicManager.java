package com.ixhong.manager;

import com.ixhong.domain.pojo.StudentBasicDO;

import java.util.List;

/**
 * Created by jenny on 4/23/15.
 */
public interface StudentBasicManager {

    public void insert(StudentBasicDO studentBasic);

    public void update(StudentBasicDO studentBasic);

    public StudentBasicDO getByStudentId(Integer studentId);

    /**
     * 根据学生ID批量查询学生基本信息表 默认最大是10条
     * @param studentIds
     * @return
     */
    public List<StudentBasicDO> getByStudentIds(List<Integer> studentIds);
}
