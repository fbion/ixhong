package com.ixhong.manager.impl;

import com.ixhong.dao.OrganizationAccountFlowDao;
import com.ixhong.domain.pojo.OrganizationAccountFlowDO;
import com.ixhong.domain.query.OrganizationAccountFlowQuery;
import com.ixhong.domain.query.QueryResult;
import com.ixhong.manager.OrganizationAccountFlowManager;

import java.util.List;

/**
 * Created by duanxiangchao on 2015/5/13.
 */
public class OrganizationAccountFlowManagerImpl implements OrganizationAccountFlowManager {

    private OrganizationAccountFlowDao organizationAccountFlowDao;

    public void setOrganizationAccountFlowDao(OrganizationAccountFlowDao organizationAccountFlowDao) {
        this.organizationAccountFlowDao = organizationAccountFlowDao;
    }

    @Override
    public void insert(OrganizationAccountFlowDO orgainzationAccountFlow) {
        this.organizationAccountFlowDao.insert(orgainzationAccountFlow);
    }

    @Override
    public List<OrganizationAccountFlowDO> getByOrganizationId(Integer organizationId) {
        return this.organizationAccountFlowDao.getByOrganizationId(organizationId);
    }

    @Override
    public QueryResult<OrganizationAccountFlowDO> query(OrganizationAccountFlowQuery query) {
        return this.organizationAccountFlowDao.query(query);
    }

    @Override
    public List<OrganizationAccountFlowDO> getByStatus(Integer status, Integer type) {
        return this.organizationAccountFlowDao.getByStatus(status, type);
    }

    @Override
    public OrganizationAccountFlowDO getById(int flowId) {
        return this.organizationAccountFlowDao.getById(flowId);
    }

    @Override
    public void update(int id, int status) {
        this.organizationAccountFlowDao.update(id, status);
    }

    @Override
    public OrganizationAccountFlowDO getByOrderId(String orderId) {
        return this.organizationAccountFlowDao.getByOrderId(orderId);
    }
}
