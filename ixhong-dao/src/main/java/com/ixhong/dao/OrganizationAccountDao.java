package com.ixhong.dao;

import com.ixhong.domain.pojo.OrganizationAccountDO;

import java.util.List;

/**
 * Created by shenhongxi on 15/4/13.
 */
public interface OrganizationAccountDao {

    public void insert(OrganizationAccountDO account);

    public OrganizationAccountDO getByOrganizationId(int organizationId);

    public List<OrganizationAccountDO> getAll();

    public void updateBailPercent(Integer organizationId, float bailPercent);

    public void update(OrganizationAccountDO account);

    public void updateBalance(OrganizationAccountDO account);
}
