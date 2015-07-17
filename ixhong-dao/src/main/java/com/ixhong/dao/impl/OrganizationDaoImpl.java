package com.ixhong.dao.impl;

import com.ixhong.dao.OrganizationDao;
import com.ixhong.domain.pojo.*;
import com.ixhong.domain.query.OrganizationQuery;
import com.ixhong.domain.query.QueryResult;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by liuguanqing on 15/4/7.
 */
public class OrganizationDaoImpl extends BaseDao implements OrganizationDao {

    @Override
    public void insert(OrganizationDO organization) {
        this.sqlSession.insert("OrganizationMapper.insert", organization);
    }

    @Override
    public void updatePassword(int organizationId,String password) {
        Map<String,Object> params = new HashMap<String, Object>();
        params.put("id",organizationId);
        params.put("password",password);
        this.sqlSession.update("OrganizationMapper.updatePassword", params);
    }

    @Override
    public OrganizationDO validate(String email, String password) {
        Map<String,Object> params = new HashMap<String, Object>();
        params.put("email",email);
        params.put("password", password);
        return this.sqlSession.selectOne("OrganizationMapper.validate",params);
    }


    @Override
    public OrganizationDO getByEmail(String email) {
        return this.sqlSession.selectOne("OrganizationMapper.getByEmail", email);
    }

    @Override
    public OrganizationDO getByName(String name) {
        return this.sqlSession.selectOne("OrganizationMapper.getByName",name);
    }

    @Override
    public OrganizationDO getById(Integer id) {
        return this.sqlSession.selectOne("OrganizationMapper.getById",id);
    }

    @Override
    public OrganizationDO getById(String id) {
        return sqlSession.selectOne("OrganizationMapper.getById", id);
    }

    @Override
    public void updateStatus(int organizationId, int status) {
        Map<String,Object> params = new HashMap<String, Object>();
        params.put("id",organizationId);
        params.put("status",status);
        this.sqlSession.update("OrganizationMapper.updateStatus", params);
    }

    @Override
    public void deleteByEmail(String email, int status) {
        Map<String,Object> params = new HashMap<String, Object>();
        params.put("email",email);
        params.put("status",status);
        this.sqlSession.delete("OrganizationMapper.deleteByEmail", params);
    }

    @Override
    public QueryResult<OrganizationDO> query(OrganizationQuery query) {
        QueryResult<OrganizationDO> qr = new QueryResult<OrganizationDO>();
        qr.setQuery(query);

        Map<String,Object> params = query.build();
        int amount = this.countAll(params);
        qr.setAmount(amount);
        if(amount == 0) {
            qr.setResultList(Collections.EMPTY_LIST);
            return qr;
        }

        List<OrganizationDO> organizations =  this.sqlSession.selectList("OrganizationMapper.query", params);
        qr.setResultList(organizations);
        return qr;
    }

    @Override
    public void update(OrganizationDO organization) {
        this.sqlSession.update("OrganizationMapper.update", organization);
    }

    @Override
    public OrganizationDO getByCode(String code) {
        return this.sqlSession.selectOne("OrganizationMapper.getByCode", code);
    }

    private int countAll(Map<String,Object> params) {
        return this.sqlSession.selectOne("OrganizationMapper.countAll",params);
    }

}
