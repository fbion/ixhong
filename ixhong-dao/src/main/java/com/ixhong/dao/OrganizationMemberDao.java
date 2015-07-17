package com.ixhong.dao;

import com.ixhong.domain.pojo.OrganizationMemberDO;

/**
 * Created by shenhongxi on 15/4/13.
 */
public interface OrganizationMemberDao {

    public void insert(OrganizationMemberDO member);

    public OrganizationMemberDO getByOrganizationId(int organizationId);

    public void update(OrganizationMemberDO member);
}
