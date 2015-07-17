package com.ixhong.manager.impl;

import com.ixhong.dao.OrganizationAccountDao;
import com.ixhong.domain.pojo.OrganizationAccountDO;
import com.ixhong.manager.OrganizationAccountManager;

import java.util.List;

/**
 * Created by shenhongxi on 2015/4/13.
 */
public class OrganizationAccountManagerImpl implements OrganizationAccountManager {

    private OrganizationAccountDao organizationAccountDao;

    public void setOrganizationAccountDao(OrganizationAccountDao organizationAccountDao) {
        this.organizationAccountDao = organizationAccountDao;
    }

    @Override
    public void insert(OrganizationAccountDO account) {
        this.organizationAccountDao.insert(account);
    }

    @Override
    public OrganizationAccountDO getByOrganizationId(int organizationId) {
        return this.organizationAccountDao.getByOrganizationId(organizationId);
    }

    @Override
    public List<OrganizationAccountDO> getAll() {
        return organizationAccountDao.getAll();
    }

    @Override
    public void updateBailPercent(Integer organizationId, float bailPercent) {
        this.organizationAccountDao.updateBailPercent(organizationId, bailPercent);
    }

    @Override
    public void update(OrganizationAccountDO account) {
        this.organizationAccountDao.update(account);
    }

    @Override
    public void updateBalance(OrganizationAccountDO account) {
        this.organizationAccountDao.updateBalance(account);
    }

}
