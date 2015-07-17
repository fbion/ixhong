package com.ixhong.dao;

import com.ixhong.domain.pojo.OrganizationMiscDO;

/**
 * Created by shenhongxi on 15/4/13.
 */
public interface OrganizationMiscDao {

    public void insert(OrganizationMiscDO misc);

    public OrganizationMiscDO getByOrganizationId(int organizationId);

    public void update(OrganizationMiscDO misc);
}
