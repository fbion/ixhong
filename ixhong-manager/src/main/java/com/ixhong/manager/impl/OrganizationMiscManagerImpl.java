package com.ixhong.manager.impl;

import com.ixhong.dao.OrganizationMiscDao;
import com.ixhong.domain.pojo.OrganizationMiscDO;
import com.ixhong.manager.OrganizationMiscManager;

/**
 * Created by shenhongxi on 2015/4/13.
 */
public class OrganizationMiscManagerImpl implements OrganizationMiscManager {

    private OrganizationMiscDao organizationMiscDao;

    public void setOrganizationMiscDao(OrganizationMiscDao organizationMiscDao) {
        this.organizationMiscDao = organizationMiscDao;
    }

    @Override
    public void insert(OrganizationMiscDO misc) {
        this.organizationMiscDao.insert(misc);
    }

    @Override
    public OrganizationMiscDO getByOrganizationId(int organizationId) {
        return this.organizationMiscDao.getByOrganizationId(organizationId);
    }

    @Override
    public void update(OrganizationMiscDO misc) {
        this.organizationMiscDao.update(misc);
    }
}
