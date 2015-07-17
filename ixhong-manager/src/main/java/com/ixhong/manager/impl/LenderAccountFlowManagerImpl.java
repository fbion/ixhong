package com.ixhong.manager.impl;

import com.ixhong.dao.LenderAccountFlowDao;
import com.ixhong.domain.pojo.LenderAccountFlowDO;
import com.ixhong.domain.query.LenderAccountFlowQuery;
import com.ixhong.domain.query.QueryResult;
import com.ixhong.manager.LenderAccountFlowManager;

import javax.annotation.Resource;
import java.util.List;


/**
 * Created by jenny on 5/15/15.
 */
public class LenderAccountFlowManagerImpl implements LenderAccountFlowManager {

    @Resource
    private LenderAccountFlowDao LenderAccountFlowDao;

    public void setLenderAccountFlowDao(LenderAccountFlowDao LenderAccountFlowDao) {
        this.LenderAccountFlowDao = LenderAccountFlowDao;
    }

    @Override
    public void update(LenderAccountFlowDO studentAccountFlow) {
        this.LenderAccountFlowDao.update(studentAccountFlow);
    }

    @Override
    public void insert(LenderAccountFlowDO studentAccountFlow) {
        this.LenderAccountFlowDao.insert(studentAccountFlow);
    }

    @Override
    public LenderAccountFlowDO getByOrderId(String orderId) {
        return this.LenderAccountFlowDao.getByOrderId(orderId);
    }

    @Override
    public QueryResult<LenderAccountFlowDO> query(LenderAccountFlowQuery query) {
        return this.LenderAccountFlowDao.query(query);
    }

    @Override
    public List<LenderAccountFlowDO> getByStatus(Integer status, Integer type) {
        return this.LenderAccountFlowDao.getByStatus(status, type);
    }

    @Override
    public LenderAccountFlowDO getById(int flowId) {
        return this.LenderAccountFlowDao.getById(flowId);
    }

    @Override
    public void update(Integer id, String errorMessage) {
        this.LenderAccountFlowDao.update(id, errorMessage);
    }

}
