package com.ixhong.manager.impl;

import com.ixhong.dao.OrganizationRegistrationDao;
import com.ixhong.domain.pojo.OrganizationRegistrationDO;
import com.ixhong.manager.OrganizationRegistrationManager;

/**
 * Created by shenhongxi on 2015/4/13.
 */
public class OrganizationRegistrationManagerImpl implements OrganizationRegistrationManager {

    private OrganizationRegistrationDao organizationRegistrationDao;

    public void setOrganizationRegistrationDao(OrganizationRegistrationDao organizationRegistrationDao) {
        this.organizationRegistrationDao = organizationRegistrationDao;
    }

    @Override
    public void insert(OrganizationRegistrationDO registration) {
        this.organizationRegistrationDao.insert(registration);
    }

    @Override
    public OrganizationRegistrationDO getByOrganizationId(int organizationId) {
        return this.organizationRegistrationDao.getByOrganizationId(organizationId);
    }

    @Override
    public void update(OrganizationRegistrationDO registration) {
        this.organizationRegistrationDao.update(registration);
    }
}
