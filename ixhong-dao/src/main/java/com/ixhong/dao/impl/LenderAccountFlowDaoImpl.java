package com.ixhong.dao.impl;

import com.ixhong.dao.LenderAccountFlowDao;
import com.ixhong.domain.pojo.LenderAccountFlowDO;
import com.ixhong.domain.query.LenderAccountFlowQuery;
import com.ixhong.domain.query.QueryResult;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by jenny on 5/15/15.
 */
public class LenderAccountFlowDaoImpl extends BaseDao implements LenderAccountFlowDao {
    @Override
    public void insert(LenderAccountFlowDO studentAccountFlow) {
        this.sqlSession.insert("LenderAccountFlowMapper.insert", studentAccountFlow);
    }

    @Override
    public void update(LenderAccountFlowDO studentAccountFlow) {
        this.sqlSession.update("LenderAccountFlowMapper.update", studentAccountFlow);
    }

    @Override
    public LenderAccountFlowDO getByOrderId(String orderId) {
        return this.sqlSession.selectOne("LenderAccountFlowMapper.getByOrderId", orderId);
    }

    @Override
    public QueryResult<LenderAccountFlowDO> query( LenderAccountFlowQuery query) {
        QueryResult<LenderAccountFlowDO> qr = new QueryResult<LenderAccountFlowDO>();
        qr.setQuery(query);

        Map<String,Object> params = query.build();
        int amount = this.countAll(params);
        qr.setAmount(amount);
        if(amount == 0) {
            qr.setResultList(Collections.EMPTY_LIST);
            return qr;
        }

        List<LenderAccountFlowDO> flows =  this.sqlSession.selectList("LenderAccountFlowMapper.query", params);
        qr.setResultList(flows);
        return qr;
    }

    @Override
    public List<LenderAccountFlowDO> getByStatus(Integer status, Integer type) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("status", status);
        params.put("type", type);
        return this.sqlSession.selectList("LenderAccountFlowMapper.getByStatus", params);
    }

    @Override
    public LenderAccountFlowDO getById(int flowId) {
        return this.sqlSession.selectOne("LenderAccountFlowMapper.getById", flowId);
    }

    @Override
    public void update(Integer id, String errorMessage) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("id", id);
        params.put("errorMessage", errorMessage);
        this.sqlSession.update("LenderAccountFlowMapper.updateStatus", params);
    }

    private int countAll(Map<String,Object> params) {
        return this.sqlSession.selectOne("LenderAccountFlowMapper.countAll",params);
    }
}
