package com.ixhong.admin.web.service;

import com.ixhong.common.pojo.Result;
import com.ixhong.domain.pojo.OrganizationAccountDO;
import com.ixhong.domain.pojo.OrganizationDO;
import com.ixhong.domain.query.OperationsLogQuery;
import com.ixhong.domain.query.OrganizationQuery;

/**
 * Created by shenhongxi on 15/4/13.
 */
public interface OrganizationService {

    public Result list(OrganizationQuery query);

    /**
     * 审核机构
     * @param organization
     * @param account
     * @param remoteIp
     * @return
     */
    public Result audit(OrganizationDO organization, OrganizationAccountDO account, String remoteIp);

    /**
     * 修改机构保证金
     * @param organization
     * @param account
     * @param remoteIp
     * @return
     */
    public Result update(OrganizationDO organization, OrganizationAccountDO account, String remoteIp);

    /**
     * 查询机构资料
     * @param organizationId
     * @return
     */
    public Result detail(Integer organizationId);

    /**
     * 查询管理员审核日志
     * @param query
     * @return
     */
    public Result auditOperationsLog(OperationsLogQuery query);

    /**
     * 提现审核
     * @param id
     * @param status
     * @return
     */
    public Result auditWithdraw(Integer id, Integer status);

    /**
     * 代付查询
     * @param orderId
     * @param batchNo
     * @return
     */
    public Result withdrawQuery(String orderId, String batchNo);

    /**
     * 获取审核信息
     * @param organizationId
     * @return
     */
     public Result getAuditInfo(Integer organizationId);

    /**
     * 修改银行账户信息
     * @param account
     * @return
     */
    public Result updateAccount(OrganizationAccountDO account);
}
