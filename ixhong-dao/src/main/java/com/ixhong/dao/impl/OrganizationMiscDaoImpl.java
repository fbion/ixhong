package com.ixhong.dao.impl;

import com.ixhong.dao.OrganizationMiscDao;
import com.ixhong.domain.pojo.OrganizationMiscDO;

/**
 * Created by shenhongxi on 2015/4/13.
 */
public class OrganizationMiscDaoImpl extends BaseDao implements OrganizationMiscDao {
    @Override
    public void insert(OrganizationMiscDO misc) {
        this.sqlSession.insert("OrganizationMiscMapper.insert",misc);
    }

    @Override
    public OrganizationMiscDO getByOrganizationId(int organizationId) {
        return this.sqlSession.selectOne("OrganizationMiscMapper.getByOrganizationId", organizationId);
    }

    @Override
    public void update(OrganizationMiscDO misc) {
        this.sqlSession.update("OrganizationMiscMapper.update", misc);
    }
}
