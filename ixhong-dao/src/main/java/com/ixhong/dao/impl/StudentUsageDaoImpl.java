package com.ixhong.dao.impl;

import com.ixhong.dao.StudentUsageDao;
import com.ixhong.domain.pojo.StudentUsageDO;
import org.apache.commons.collections.CollectionUtils;

import java.util.List;

/**
 * Created by jenny on 4/22/15.
 * 学生贷款用途
 */
public class StudentUsageDaoImpl extends BaseDao implements StudentUsageDao {
    @Override
    public void insert(StudentUsageDO studentUsage) {
        this.sqlSession.insert("StudentUsageMapper.insert",studentUsage);
    }

    @Override
    public void update(StudentUsageDO studentUsageDo) {
        this.sqlSession.update("StudentUsageMapper.update",studentUsageDo);
    }

    @Override
    public StudentUsageDO getByStudentId(Integer studentId) {
        return this.sqlSession.selectOne("StudentUsageMapper.getByStudentId",studentId);
    }

    @Override
    public List<StudentUsageDO> getByStudentIds(List<Integer> studentIds) {
        if(CollectionUtils.isEmpty(studentIds)){
            return null;
        }
        return this.sqlSession.selectList("StudentUsageMapper.getByStudentIds",studentIds);
    }
}
