package com.ixhong.manager;

import com.ixhong.domain.pojo.OrganizationWithdrawDO;
import com.ixhong.domain.query.OrganizationWithdrawQuery;
import com.ixhong.domain.query.QueryResult;

/**
 * Created by gongzaifei on 15/6/3.
 */
public interface OrganizationWithdrawManager {

    /**
     *保存提现记录
     * @param organizationWithdrawDO
     */
    public void insert(OrganizationWithdrawDO organizationWithdrawDO);

    /**
     * 根据条件查询提现记录
     * @param organizationWithdrawQuery
     * @return
     */
    public QueryResult<OrganizationWithdrawDO> query(OrganizationWithdrawQuery organizationWithdrawQuery);

    /**
     * 根据条件统计提现记录数
     * @param status
     * @param organizationId
     * @return
     */

    public int countByStatus(Integer status, Integer organizationId);

    public OrganizationWithdrawDO getById(Integer id);

    /**
     * 更新提现状态
     * @param id
     * @param status
     */
    public void updateAuditStatus(Integer id, Integer status);
}
