package com.ixhong.admin.web.service.impl;

import com.ixhong.admin.web.service.StudentAccountService;
import com.ixhong.common.pojo.Result;
import com.ixhong.common.pojo.ResultCode;
import com.ixhong.domain.pojo.StudentAccountFlowDO;
import com.ixhong.domain.query.QueryResult;
import com.ixhong.domain.query.StudentAccountFlowQuery;
import com.ixhong.manager.StudentAccountFlowManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by shenhongxi on 15/5/14.
 */
@Service
public class StudentAccountServiceImpl implements StudentAccountService {

    private Logger logger = Logger.getLogger(StudentAccountService.class);

    @Autowired
    private StudentAccountFlowManager studentAccountFlowManager;

    @Override
    public Result listFlow(StudentAccountFlowQuery query) {
        Result result = new Result();
        try {
            QueryResult<StudentAccountFlowDO> qr = this.studentAccountFlowManager.query(query);
            result.addModel("query",query);
            result.addModel("queryResult", qr);
            result.addModel("flows", qr.getResultList());
            result.setSuccess(true);
        } catch (Exception e) {
            logger.error("query student account flow error",e);
            result.setResultCode(ResultCode.SYSTEM_ERROR);
        }
        return result;
    }
}
