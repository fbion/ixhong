package com.ixhong.admin.web.controller;

import com.ixhong.admin.web.service.AccountService;
import com.ixhong.common.pojo.Result;
import com.ixhong.domain.constants.Constants;
import com.ixhong.domain.enums.AdminFlowTypeEnum;
import com.ixhong.domain.enums.UserTypeEnum;
import com.ixhong.domain.permission.Permission;
import com.ixhong.domain.query.AccountFlowQuery;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

/**
 * Created by chenguang on 2015/6/3 0003.
 */

@Controller
@RequestMapping("/account")
@Permission(roles = {UserTypeEnum.ADMIN})
public class AccountController {

    @Autowired
    private AccountService accountService;

    @RequestMapping("/flow_list")
    public ModelAndView list(@RequestParam(value = "begin_date", required = false)String beginDate,
                             @RequestParam(value = "end_date", required = false)String endDate,
                             @RequestParam(value = "type", defaultValue = "-1")Integer type,
                             @RequestParam(value = "page", defaultValue = "1")Integer page) {

        //test merge

        ModelAndView mav = new ModelAndView();
        AccountFlowQuery query = new AccountFlowQuery();
        query.setCurrentPage(page);
        if(type == -1) {
            type = null;
        }
        query.setType(type);
        try{
            if(StringUtils.isNotBlank(beginDate)) {
                query.setBeginDate(DateUtils.parseDate(beginDate, Constants.DATE_PATTERN));
                query.setQueryBeginDate(beginDate);
            }
            if(StringUtils.isNotBlank(endDate)) {
                query.setEndDate(DateUtils.addDays(DateUtils.parseDate(endDate, Constants.DATE_PATTERN), 1));
                query.setQueryEndDate(endDate);
            }
        }catch (Exception e) {
            mav.setViewName("redirect:/error.jhtml?message=parameter-error");
            return mav;
        }
        Result result = accountService.flowList(query);
        mav.addAllObjects(result.getAll());
        mav.addObject("flowTypes", AdminFlowTypeEnum.values());
        mav.addObject("receiveFee", AdminFlowTypeEnum.RECEIVE_FEE);
        mav.addObject("rechargeFee", AdminFlowTypeEnum.RECHARGE_FEE);
        mav.addObject("adminAdvanceAmount", AdminFlowTypeEnum.ADMIN_ADVANCE_AMOUNT);
        mav.addObject("receiveFine", AdminFlowTypeEnum.RECEIVE_FINE);
        mav.addObject("receiveAfterAdvance", AdminFlowTypeEnum.RECEIVE_AFTER_ADVANCE);

        //

        return mav;
    }
}
