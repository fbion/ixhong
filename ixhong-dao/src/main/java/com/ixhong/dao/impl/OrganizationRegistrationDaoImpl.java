package com.ixhong.dao.impl;

import com.ixhong.dao.OrganizationRegistrationDao;
import com.ixhong.domain.pojo.OrganizationRegistrationDO;

/**
 * Created by shenhongxi on 2015/4/13.
 */
public class OrganizationRegistrationDaoImpl extends BaseDao implements OrganizationRegistrationDao {
    @Override
    public void insert(OrganizationRegistrationDO registration) {
        this.sqlSession.insert("OrganizationRegistrationMapper.insert",registration);
    }

    @Override
    public OrganizationRegistrationDO getByOrganizationId(int organizationId) {
        return this.sqlSession.selectOne("OrganizationRegistrationMapper.getByOrganizationId", organizationId);
    }

    @Override
    public void update(OrganizationRegistrationDO registration) {
        this.sqlSession.update("OrganizationRegistrationMapper.update", registration);
    }
}
