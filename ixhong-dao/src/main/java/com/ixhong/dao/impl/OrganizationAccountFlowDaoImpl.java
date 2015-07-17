package com.ixhong.dao.impl;

import com.ixhong.dao.OrganizationAccountFlowDao;
import com.ixhong.domain.pojo.OrganizationAccountFlowDO;
import com.ixhong.domain.query.OrganizationAccountFlowQuery;
import com.ixhong.domain.query.QueryResult;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by duanxiangchao on 2015/5/13.
 */
public class OrganizationAccountFlowDaoImpl extends BaseDao implements OrganizationAccountFlowDao {
    @Override
    public void insert(OrganizationAccountFlowDO organizationAccountFlow) {
        this.sqlSession.insert("OrganizationAccountFlowMapper.insert",organizationAccountFlow);
    }

    @Override
    public List<OrganizationAccountFlowDO> getByOrganizationId(Integer organizationId) {
        return this.sqlSession.selectList("OrganizationAccountFlowMapper.getByOrganizationId", organizationId);
    }

    @Override
    public QueryResult<OrganizationAccountFlowDO> query(OrganizationAccountFlowQuery query) {
        QueryResult<OrganizationAccountFlowDO> qr = new QueryResult<OrganizationAccountFlowDO>();
        qr.setQuery(query);

        Map<String,Object> params = query.build();
        int amount = this.countAll(params);
        qr.setAmount(amount);
        if(amount == 0) {
            qr.setResultList(Collections.EMPTY_LIST);
            return qr;
        }

        List<OrganizationAccountFlowDO> flows =  this.sqlSession.selectList("OrganizationAccountFlowMapper.query", params);
        qr.setResultList(flows);
        return qr;
    }

    @Override
    public List<OrganizationAccountFlowDO> getByStatus(Integer status, Integer type) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("status", status);
        params.put("type", type);
        return this.sqlSession.selectList("OrganizationAccountFlowMapper.getByStatus", params);
    }

    @Override
    public OrganizationAccountFlowDO getById(int flowId) {
        return this.sqlSession.selectOne("OrganizationAccountFlowMapper.getById", flowId);
    }

    @Override
    public void update(int id, int status) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("id", id);
        params.put("status", status);
        this.sqlSession.update("OrganizationAccountFlowMapper.update", params);
    }

    @Override
    public OrganizationAccountFlowDO getByOrderId(String orderId) {
        return this.sqlSession.selectOne("OrganizationAccountFlowMapper.getByOrderId", orderId);
    }

    private int countAll(Map<String,Object> params) {
        return this.sqlSession.selectOne("OrganizationAccountFlowMapper.countAll",params);
    }
}
