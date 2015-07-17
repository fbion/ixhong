package com.ixhong.manager;

import com.ixhong.domain.pojo.OrganizationMiscDO;

/**
 * Created by shenhongxi on 2015/4/13.
 */
public interface OrganizationMiscManager {

    public void insert(OrganizationMiscDO misc);

    public OrganizationMiscDO getByOrganizationId(int organizationId);

    public void update(OrganizationMiscDO misc);
    
}
