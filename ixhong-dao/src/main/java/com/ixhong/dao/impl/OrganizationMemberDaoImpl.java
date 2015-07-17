package com.ixhong.dao.impl;

import com.ixhong.dao.OrganizationMemberDao;
import com.ixhong.domain.pojo.OrganizationMemberDO;

/**
 * Created by shenhongxi on 2015/4/13.
 */
public class OrganizationMemberDaoImpl extends BaseDao implements OrganizationMemberDao {
    @Override
    public void insert(OrganizationMemberDO member) {
        this.sqlSession.insert("OrganizationMemberMapper.insert",member);
    }

    @Override
    public OrganizationMemberDO getByOrganizationId(int organizationId) {
        return this.sqlSession.selectOne("OrganizationMemberMapper.getByOrganizationId", organizationId);
    }

    @Override
    public void update(OrganizationMemberDO member) {
        this.sqlSession.update("OrganizationMemberMapper.update", member);
    }
}
