package com.ixhong.dao.impl;

import com.ixhong.dao.StudentDao;
import com.ixhong.domain.pojo.StudentDO;
import org.apache.commons.collections.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by duanxiangchao on 2015/4/22.
 */
public class StudentDaoImpl extends BaseDao implements StudentDao{
    @Override
    public void insert(StudentDO student) {
        this.sqlSession.insert("StudentMapper.insert",student);
    }

    @Override
    public StudentDO validate(String phone, String password) {
        Map<String,Object> params = new HashMap<String, Object>();
        params.put("phone",phone);
        params.put("password", password);
        return this.sqlSession.selectOne("StudentMapper.validate",params);
    }

    @Override
    public StudentDO getByTelephone(String telephone) {
        return this.sqlSession.selectOne("StudentMapper.getByTelephone",telephone);
    }

    @Override
    public StudentDO getByCardId(String cardId) {
        return this.sqlSession.selectOne("StudentMapper.getByCardId",cardId);
    }

    @Override
    public StudentDO getById(int id) {
        return this.sqlSession.selectOne("StudentMapper.getById",id);
    }

    @Override
    public void updatePassword(int id, String password) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("id", id);
        params.put("password", password);
        this.sqlSession.update("StudentMapper.updatePassword", params);
    }

    @Override
    public void updatePasswordByPhone(String phone, String password) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("phone", phone);
        params.put("password", password);
        this.sqlSession.update("StudentMapper.updatePasswordByPhone", params);
    }

    @Override
    public void updateStatus(Integer id, Integer status) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("id", id);
        params.put("status", status);
        this.sqlSession.update("StudentMapper.updateStatus",params);
    }

    @Override
    public void updateScore(Integer id, Integer score) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("id", id);
        params.put("score", score);
        this.sqlSession.update("StudentMapper.updateScore",params);
    }

    @Override
    public void updateLoginTime(Integer id) {
        this.sqlSession.update("StudentMapper.updateLoginTime",id);
    }

    @Override
    public void updateContactInformation(Map contactMap) {
        this.sqlSession.update("StudentMapper.updateContactInformation",contactMap);
    }

    @Override
    public void updateCertified(Integer id, int certified) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("id", id);
        params.put("certified", certified);
        this.sqlSession.update("StudentMapper.updateCertified",params);
    }

    @Override
    public List<StudentDO> getByStudentIds(List<Integer> studentIds) {
        if(CollectionUtils.isEmpty(studentIds)){
            return null;
        }
        return this.sqlSession.selectList("StudentMapper.getByStudentIds",studentIds);
    }

}
