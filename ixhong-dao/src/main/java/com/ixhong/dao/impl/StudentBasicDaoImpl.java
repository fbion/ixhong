package com.ixhong.dao.impl;

import com.ixhong.dao.StudentBasicDao;
import com.ixhong.domain.pojo.StudentBasicDO;
import org.apache.commons.collections.CollectionUtils;

import java.util.List;

/**
 * Created by jenny on 4/22/15.
 * 学生基本信息
 */
public class StudentBasicDaoImpl extends BaseDao implements StudentBasicDao {
    @Override
    public void insert(StudentBasicDO studentBasic) {
        this.sqlSession.insert("StudentBasicMapper.insert",studentBasic);
    }

    @Override
    public void update(StudentBasicDO studentBasic) {
       this.sqlSession.update("StudentBasicMapper.update",studentBasic);
    }

    @Override
    public StudentBasicDO getByStudentId(Integer studentId) {
        return this.sqlSession.selectOne("StudentBasicMapper.getByStudentId",studentId);
    }

    @Override
    public List<StudentBasicDO> getByStudentIds(List<Integer> studentIds) {
        if(CollectionUtils.isEmpty(studentIds)){
            return null;
        }
        return this.sqlSession.selectList("StudentBasicMapper.getByStudentIds",studentIds);
    }
}
