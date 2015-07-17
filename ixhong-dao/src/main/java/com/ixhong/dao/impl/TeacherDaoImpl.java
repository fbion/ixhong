package com.ixhong.dao.impl;

import com.ixhong.dao.TeacherDao;
import com.ixhong.domain.pojo.TeacherDO;
import com.ixhong.domain.query.QueryResult;
import com.ixhong.domain.query.TeacherQuery;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by liuguanqing on 15/4/7.
 */
public class TeacherDaoImpl extends BaseDao implements TeacherDao {


    @Override
    public void insert(TeacherDO teacher) {
        this.sqlSession.insert("TeacherMapper.insert", teacher);
    }

    @Override
    public TeacherDO validate(String name, String password) {
        Map<String,Object> params = new HashMap<String, Object>();
        params.put("phone",name);
        params.put("password", password);
        return this.sqlSession.selectOne("TeacherMapper.validate", params);
    }

    @Override
    public void delete(Integer id) {
        this.sqlSession.delete("TeacherMapper.delete",id);
    }

    @Override
    public TeacherDO getByTelephone(String telephone) {
        return sqlSession.selectOne("TeacherMapper.getByTelephone", telephone);
    }

    @Override
    public void update(TeacherDO teacher) {
        sqlSession.update("TeacherMapper.update", teacher);
    }

    @Override
    public TeacherDO getById(int id) {
        return sqlSession.selectOne("TeacherMapper.getById", id);
    }

    @Override
    public void updatePassword(int id, String password, int status) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("id", id);
        params.put("password", password);
        params.put("status", status);
        this.sqlSession.update("TeacherMapper.updatePassword", params);
    }

    @Override
    public QueryResult<TeacherDO> query(Integer organizationId, TeacherQuery query) {
        QueryResult<TeacherDO> qr = new QueryResult<TeacherDO>();
        qr.setQuery(query);

        Map<String,Object> params = query.build();
        params.put("organizationId",organizationId);
        int amount = this.countAll(params);
        qr.setAmount(amount);
        if(amount == 0) {
            qr.setResultList(Collections.EMPTY_LIST);
            return qr;
        }

        List<TeacherDO> teachers =  this.sqlSession.selectList("TeacherMapper.query", params);
        qr.setResultList(teachers);
        return qr;
    }

    private int countAll(Map<String,Object> params) {
        return this.sqlSession.selectOne("TeacherMapper.countAll",params);
    }
}
