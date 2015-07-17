package com.ixhong.manager;

import com.ixhong.domain.pojo.OrganizationRegistrationDO;

/**
 * Created by shenhongxi on 2015/4/13.
 */
public interface OrganizationRegistrationManager {

    public void insert(OrganizationRegistrationDO registration);

    public OrganizationRegistrationDO getByOrganizationId(int organizationId);

    public void update(OrganizationRegistrationDO registration);
}
