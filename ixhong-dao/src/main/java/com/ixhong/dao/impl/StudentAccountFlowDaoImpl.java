package com.ixhong.dao.impl;

import com.ixhong.dao.StudentAccountFlowDao;
import com.ixhong.domain.pojo.StudentAccountFlowDO;
import com.ixhong.domain.query.QueryResult;
import com.ixhong.domain.query.StudentAccountFlowQuery;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Chengrui on 2015/5/13.
 */
public class StudentAccountFlowDaoImpl extends BaseDao implements StudentAccountFlowDao {

    @Override
    public void insert(StudentAccountFlowDO studentAccountFlow) {
        this.sqlSession.insert("StudentAccountFlowMapper.insert", studentAccountFlow);
    }

    @Override
    public void update(StudentAccountFlowDO studentAccountFlow) {
        this.sqlSession.update("StudentAccountFlowMapper.update", studentAccountFlow);
    }

    @Override
    public StudentAccountFlowDO getById(Integer id) {
        return this.sqlSession.selectOne("StudentAccountFlowMapper.getById", id);
    }

    @Override
    public StudentAccountFlowDO getByOrderId(String orderId) {
        return this.sqlSession.selectOne("StudentAccountFlowMapper.getByOrderId", orderId);
    }

    @Override
    public QueryResult<StudentAccountFlowDO> query( StudentAccountFlowQuery query) {
        QueryResult<StudentAccountFlowDO> qr = new QueryResult<StudentAccountFlowDO>();
        qr.setQuery(query);

        Map<String,Object> params = query.build();
        int amount = this.countAll(params);
        qr.setAmount(amount);
        if(amount == 0) {
            qr.setResultList(Collections.EMPTY_LIST);
            return qr;
        }

        List<StudentAccountFlowDO> flows = this.sqlSession.selectList("StudentAccountFlowMapper.query", params);

        qr.setResultList(flows);
        return qr;
    }

    private int countAll(Map<String,Object> params) {
        return this.sqlSession.selectOne("StudentAccountFlowMapper.countAll",params);
    }

    @Override
    public List<StudentAccountFlowDO> getByStatus(Integer status, Integer type) {
        Map<String,Object> params = new HashMap<String, Object>();
        params.put("status", status);
        params.put("type", type);
        return this.sqlSession.selectList("StudentAccountFlowMapper.getByStatus", params);
    }
}
