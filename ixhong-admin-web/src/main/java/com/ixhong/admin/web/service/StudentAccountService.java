package com.ixhong.admin.web.service;

import com.ixhong.common.pojo.Result;
import com.ixhong.domain.query.StudentAccountFlowQuery;

/**
 * Created by shenhongxi on 15/5/14.
 */
public interface StudentAccountService {

    public Result listFlow(StudentAccountFlowQuery query);
}
