package com.ixhong.dao;

import com.ixhong.domain.pojo.*;
import com.ixhong.domain.query.OrganizationQuery;
import com.ixhong.domain.query.QueryResult;

/**
 * Created by liuguanqing on 15/4/7.
 */
public interface OrganizationDao {

    public void insert(OrganizationDO organization);

    public void updatePassword(int organizationId, String password);

    public void updateStatus(int organizationId, int status);

    public OrganizationDO validate(String email, String password);

    public OrganizationDO getByEmail(String email);


    /**
     * 根据机构名称查询机构是否已经存在
     * @param name
     * @return
     */
    public OrganizationDO getByName(String name);

    public OrganizationDO getById(Integer id);

    public OrganizationDO getById(String id);

    public void deleteByEmail(String email, int status);

    public QueryResult<OrganizationDO> query(OrganizationQuery query);

    public void update(OrganizationDO organization);

    public OrganizationDO getByCode(String code);
}
