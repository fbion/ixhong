package com.ixhong.dao;

import com.ixhong.domain.pojo.OrganizationAccountFlowDO;
import com.ixhong.domain.query.OrganizationAccountFlowQuery;
import com.ixhong.domain.query.QueryResult;

import java.util.List;

/**
 * Created by duanxiangchao on 2015/5/13.
 */
public interface OrganizationAccountFlowDao {

    public void insert(OrganizationAccountFlowDO organizationAccountFlow);

    public List<OrganizationAccountFlowDO> getByOrganizationId(Integer organizationId);

    public QueryResult<OrganizationAccountFlowDO> query(OrganizationAccountFlowQuery query);

    public List<OrganizationAccountFlowDO> getByStatus(Integer status, Integer type);

    public OrganizationAccountFlowDO getById(int flowId);

    public void update(int id, int status);

    public OrganizationAccountFlowDO getByOrderId(String orderId);
}
