package com.ixhong.manager;

import com.ixhong.domain.pojo.OrganizationMemberDO;

/**
 * Created by shenhongxi on 2015/4/13.
 */
public interface OrganizationMemberManager {

    public void insert(OrganizationMemberDO member);

    public OrganizationMemberDO getByOrganizationId(int organizationId);

    public void update(OrganizationMemberDO member);
}
