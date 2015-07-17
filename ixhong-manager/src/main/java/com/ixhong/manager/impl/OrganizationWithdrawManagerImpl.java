package com.ixhong.manager.impl;

import com.ixhong.dao.OrganizationWithdrawDao;
import com.ixhong.domain.pojo.OrganizationWithdrawDO;
import com.ixhong.domain.query.OrganizationWithdrawQuery;
import com.ixhong.domain.query.QueryResult;
import com.ixhong.manager.OrganizationWithdrawManager;

/**
 * Created by gongzaifei on 15/6/3.
 */
public class OrganizationWithdrawManagerImpl implements OrganizationWithdrawManager {

    private OrganizationWithdrawDao organizationWithdrawDao;

    public void setOrganizationWithdrawDao(OrganizationWithdrawDao organizationWithdrawDao) {
        this.organizationWithdrawDao = organizationWithdrawDao;
    }

    @Override
    public void insert(OrganizationWithdrawDO organizationWithdrawDO) {
        organizationWithdrawDao.insert(organizationWithdrawDO);
    }

    @Override
    public QueryResult<OrganizationWithdrawDO> query(OrganizationWithdrawQuery organizationWithdrawQuery) {
        return organizationWithdrawDao.getOrganizationDrawDo(organizationWithdrawQuery);
    }

    @Override
    public int countByStatus(Integer status,Integer organizationId) {
        return organizationWithdrawDao.countByStatus(status, organizationId);
    }

    @Override
    public OrganizationWithdrawDO getById(Integer id) {
        return organizationWithdrawDao.getById(id);
    }

    @Override
    public void updateAuditStatus(Integer id, Integer status) {
        organizationWithdrawDao.updateAuditStatus(id, status);
    }

}
