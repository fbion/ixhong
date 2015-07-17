package com.ixhong.admin.web.service.impl;

import com.ixhong.admin.web.service.AccountService;
import com.ixhong.common.pojo.Result;
import com.ixhong.common.pojo.ResultCode;
import com.ixhong.domain.pojo.*;
import com.ixhong.domain.query.AccountFlowQuery;
import com.ixhong.domain.query.OrganizationWithdrawQuery;
import com.ixhong.domain.query.QueryResult;
import com.ixhong.manager.*;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by chenguang on 2015/6/4 0004.
 */
@Service
public class AccountServiceImpl implements AccountService {

    private Logger logger = Logger.getLogger(AccountService.class);

    @Autowired
    private OrganizationWithdrawManager organizationWithdrawManager;

    @Autowired
    private OrganizationManager organizationManager;

    @Autowired
    private OrganizationAccountManager organizationAccountManager;

    @Autowired
    private AccountFlowManager accountFlowManager;

    @Autowired
    private LenderManager lenderManager;

    @Autowired
    private StudentManager studentManager;

    @Autowired
    private AccountManager accountManager;

    @Override
    public Result withdrawList(OrganizationWithdrawQuery query) {
        Result result = new Result();
        try {
            QueryResult<OrganizationWithdrawDO> qr = organizationWithdrawManager.query(query);
            result.addModel("query",query);
            result.addModel("queryResult", qr);
            result.addModel("organizationWithdraws", qr.getResultList());
            result.setSuccess(true);
        } catch (Exception e) {
            logger.error("query organization withdraw error",e);
            result.setResultCode(ResultCode.SYSTEM_ERROR);
        }
        return result;
    }

    @Override
    public Result flowList(AccountFlowQuery query) {
        Result result = new Result();
        try {
            QueryResult<AccountFlowDO> qr = accountFlowManager.query(query);
            result.addModel("query", query);
            result.addModel("queryResult", qr);

            List<AccountFlowDO> adminAccountFlows = (List<AccountFlowDO>)qr.getResultList();
            for(AccountFlowDO adminAccountFlow : adminAccountFlows) {
                Integer lenderId = adminAccountFlow.getLenderId();
                Integer studentId = adminAccountFlow.getStudentId();
                if(lenderId != null) {
                    LenderDO lender = lenderManager.getById(lenderId);
                    if(lender != null) {
                        adminAccountFlow.setPhone(lender.getPhone());
                    }
                }
                if(studentId != null) {
                    StudentDO student = studentManager.getById(studentId);
                    if(student != null) {
                        adminAccountFlow.setPhone(student.getPhone());
                    }
                }
            }
            result.addModel("adminAccountFlows", adminAccountFlows);
            result.addModel("balance", accountManager.get().getBalance());
        }catch (Exception e) {
            logger.error("list error", e);
            result.setMessage("系统错误");
            result.setResultCode(ResultCode.SYSTEM_ERROR);
        }
        return result;
    }
}
