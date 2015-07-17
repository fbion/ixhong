package com.ixhong.manager;

import com.ixhong.domain.pojo.StudentUsageDO;

import java.util.List;

/**
 * Created by jenny on 4/23/15.
 */
public interface StudentUsageManager {

    public void insert(StudentUsageDO studentUsage);

    public void update(StudentUsageDO studentUsageDo);

    public StudentUsageDO getByStudentId(Integer studentId);

    /**
     * 根据学生ID批量查询学生借款用途表 默认最大查询10条
     * @param studentIds
     * @return
     */
    public List<StudentUsageDO> getByStudentIds(List<Integer> studentIds);
}
