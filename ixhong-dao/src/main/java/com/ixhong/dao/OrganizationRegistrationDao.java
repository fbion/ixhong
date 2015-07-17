package com.ixhong.dao;

import com.ixhong.domain.pojo.OrganizationRegistrationDO;

/**
 * Created by shenhongxi on 15/4/13.
 */
public interface OrganizationRegistrationDao {

    public void insert(OrganizationRegistrationDO registration);

    public OrganizationRegistrationDO getByOrganizationId(int organizationId);

    public void update(OrganizationRegistrationDO registration);
}
