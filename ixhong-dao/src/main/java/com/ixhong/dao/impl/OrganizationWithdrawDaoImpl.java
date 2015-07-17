package com.ixhong.dao.impl;

import com.ixhong.dao.OrganizationWithdrawDao;
import com.ixhong.domain.pojo.OrganizationWithdrawDO;
import com.ixhong.domain.query.OrganizationWithdrawQuery;
import com.ixhong.domain.query.QueryResult;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by gongzaifei on 15/6/3.
 */
public class OrganizationWithdrawDaoImpl extends BaseDao implements OrganizationWithdrawDao {

    @Override
    public void insert(OrganizationWithdrawDO organizationWithdrawDO) {
            this.sqlSession.insert("OrganizationWithdrawMapper.insert",organizationWithdrawDO);
    }

    @Override
    public QueryResult<OrganizationWithdrawDO> getOrganizationDrawDo(OrganizationWithdrawQuery organizationWithdrawQuery) {
        QueryResult<OrganizationWithdrawDO> qr = new QueryResult<OrganizationWithdrawDO>();
        qr.setQuery(organizationWithdrawQuery);

        Map<String,Object> params = organizationWithdrawQuery.build();
        int amount = this.countAll(params);
        qr.setAmount(amount);
        if(amount == 0) {
            qr.setResultList(Collections.EMPTY_LIST);
            return qr;
        }

        List<OrganizationWithdrawDO> withdraws = this.sqlSession.selectList("OrganizationWithdrawMapper.query", params);

        qr.setResultList(withdraws);
        return qr;
    }


    public int countByStatus(Integer status,Integer organizationId){
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("status", status);
        params.put("organizationId", organizationId);
        return  this.sqlSession.selectOne("OrganizationWithdrawMapper.countByStatus",params);
    }

    @Override
    public OrganizationWithdrawDO getById(Integer id) {
        return this.sqlSession.selectOne("OrganizationWithdrawMapper.getById", id);
    }

    @Override
    public void updateAuditStatus(Integer id, Integer status) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("id", id);
        params.put("status", status);
        sqlSession.update("OrganizationWithdrawMapper.updateAuditStatus", params);
    }

    /**
     * 总条数
     * @param params
     * @return
     */
    private int countAll(Map<String, Object> params) {
        return this.sqlSession.selectOne("OrganizationWithdrawMapper.countAll", params);
    }
}
