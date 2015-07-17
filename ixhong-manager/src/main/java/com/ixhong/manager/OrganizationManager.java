package com.ixhong.manager;

import com.ixhong.domain.pojo.*;
import com.ixhong.domain.query.OrganizationQuery;
import com.ixhong.domain.query.QueryResult;

/**
 * Created by liuguanqing on 15/4/7.
 */
public interface OrganizationManager {

    public void insert(OrganizationDO organization);

    public OrganizationDO validate(String email, String password);

    public void updatePassword(int organizationId, String password);

    /**
     * 根据邮箱查询机构是否已经存在
     * @param email
     * @return
     */
    public OrganizationDO getByEmail(String email);

    /**
     * 根据名称查询机构是否已存在
     * @param name
     * @return
     */

    public OrganizationDO getByName(String name);

    public OrganizationDO getById(Integer id);

    public void updateStatus(int organizationId, int status);

    /**
     * 根据邮箱删除
     * @param email
     */
    public void deleteByEmail(String email, int status);

    public QueryResult<OrganizationDO> query(OrganizationQuery query);

    public void update(OrganizationDO organization);

    public OrganizationDO getByCode(String code);
}
