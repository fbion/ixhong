package com.ixhong.manager;

import com.ixhong.domain.pojo.StudentAccountFlowDO;
import com.ixhong.domain.query.QueryResult;
import com.ixhong.domain.query.StudentAccountFlowQuery;

import java.util.List;

/**
 * Created by Chengrui on 2015/5/13.
 */
public interface StudentAccountFlowManager {

    public void insert(StudentAccountFlowDO studentAccountFlow);

    public void update(StudentAccountFlowDO studentAccountFlow);

    public StudentAccountFlowDO getById(Integer id);

    public StudentAccountFlowDO getByOrderId(String orderId);

    public QueryResult<StudentAccountFlowDO> query(StudentAccountFlowQuery query);

    public List<StudentAccountFlowDO> getByStatus(Integer status, Integer type);
}
