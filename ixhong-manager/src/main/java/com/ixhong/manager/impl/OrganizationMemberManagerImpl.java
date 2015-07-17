package com.ixhong.manager.impl;

import com.ixhong.dao.OrganizationMemberDao;
import com.ixhong.domain.pojo.OrganizationMemberDO;
import com.ixhong.manager.OrganizationMemberManager;

/**
 * Created by shenhongxi on 2015/4/13.
 */
public class OrganizationMemberManagerImpl implements OrganizationMemberManager {

    private OrganizationMemberDao organizationMemberDao;

    public void setOrganizationMemberDao(OrganizationMemberDao organizationMemberDao) {
        this.organizationMemberDao = organizationMemberDao;
    }

    @Override
    public void insert(OrganizationMemberDO member) {
        this.organizationMemberDao.insert(member);
    }

    @Override
    public OrganizationMemberDO getByOrganizationId(int organizationId) {
        return this.organizationMemberDao.getByOrganizationId(organizationId);
    }

    @Override
    public void update(OrganizationMemberDO member) {
        this.organizationMemberDao.update(member);
    }
}
