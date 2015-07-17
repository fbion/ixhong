package com.ixhong.dao;

import com.ixhong.domain.pojo.LenderAccountFlowDO;
import com.ixhong.domain.query.LenderAccountFlowQuery;
import com.ixhong.domain.query.QueryResult;

import java.util.List;

/**
 * Created by jenny on 5/15/15.
 */
public interface LenderAccountFlowDao {

    public void insert(LenderAccountFlowDO lenderAccountFlow);

    public void update(LenderAccountFlowDO lenderAccountFlow);

    public LenderAccountFlowDO getByOrderId(String orderId);

    public QueryResult<LenderAccountFlowDO> query(LenderAccountFlowQuery query);

    public List<LenderAccountFlowDO> getByStatus(Integer status, Integer type);

    public LenderAccountFlowDO getById(int flowId);

    public void update(Integer id, String errorMessage);
}
