package com.ixhong.manager.impl;

import com.ixhong.dao.OrganizationDao;
import com.ixhong.domain.pojo.*;
import com.ixhong.domain.query.OrganizationQuery;
import com.ixhong.domain.query.QueryResult;
import com.ixhong.manager.OrganizationManager;

/**
 * Created by liuguanqing on 15/4/7.
 */
public class OrganizationManagerImpl implements OrganizationManager {

    private OrganizationDao organizationDao;

    public void setOrganizationDao(OrganizationDao organizationDao) {
        this.organizationDao = organizationDao;
    }

    @Override
    public void insert(OrganizationDO organization) {
        this.organizationDao.insert(organization);
    }

    @Override
    public OrganizationDO validate(String email, String password) {
        return this.organizationDao.validate(email,password);
    }

    @Override
    public void updatePassword(int organizationId,String password) {
        this.organizationDao.updatePassword(organizationId,password);
    }

    @Override
    public OrganizationDO getByEmail(String email) {
        return this.organizationDao.getByEmail(email);
    }

    @Override
    public OrganizationDO getByName(String name) {
        return this.organizationDao.getByName(name);
    }

    @Override
    public OrganizationDO getById(Integer id) {
        return  this.organizationDao.getById(id);
    }

    @Override
    public void updateStatus(int organizationId, int status) {
        this.organizationDao.updateStatus(organizationId, status);
    }

    @Override
    public void deleteByEmail(String email, int status) {
        this.organizationDao.deleteByEmail(email,status);
    }

    @Override
    public QueryResult<OrganizationDO> query(OrganizationQuery query) {
        return this.organizationDao.query(query);
    }

    @Override
    public void update(OrganizationDO organization) {
        this.organizationDao.update(organization);
    }

    @Override
    public OrganizationDO getByCode(String code) {
        return this.organizationDao.getByCode(code);
    }

}
