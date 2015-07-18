package com.ixhong.lender.web.controller;

import com.ixhong.common.pojo.Result;
import com.ixhong.common.utils.ResultHelper;
import com.ixhong.domain.enums.BiddingStatusEnum;
import com.ixhong.domain.enums.GenderEnum;
import com.ixhong.domain.query.BiddingQuery;
import com.ixhong.lender.web.service.BiddingService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jenny on 5/20/15.
 */
@Controller
@RequestMapping("/bidding")
public class BiddingController extends BaseController {

    @Autowired
    private BiddingService biddingService;

    @RequestMapping("/detail")
    public ModelAndView detail(@RequestParam("bidding_id") Integer biddingId){
        ModelAndView mav = new ModelAndView();
        Result result = this.biddingService.detail(biddingId);
        if (result.isSuccess()) {
            mav.addAllObjects(result.getAll());
            mav.addObject("ongoing", BiddingStatusEnum.ONGOING.code);
            mav.addObject("fullAuditing", BiddingStatusEnum.FULL_ADMIN_AUDITING.code);
            mav.addObject("staging", BiddingStatusEnum.STAGING.code);
            mav.addObject("failure", BiddingStatusEnum.FAILURE.code);
            mav.addObject("paidOff", BiddingStatusEnum.PAID_OFF.code);
            mav.addObject("male", GenderEnum.MALE.code);
            mav.addObject("female", GenderEnum.FEMALE.code);
            //mav.addObject("paidOff", BiddingStatusEnum.PAID_OFF.code);
        } else {
            mav.setViewName("redirect:/error.jhtml?messages=" + result.getMessage());
        }
        return mav;
    }


    @RequestMapping("/list")
    public ModelAndView biddingQuery(@RequestParam(value = "page",required = false,defaultValue = "1")Integer page
                                     ){

        ModelAndView mav = new ModelAndView();
        BiddingQuery query = new BiddingQuery();
        query.setPageSize(16);
        query.setCurrentPage(page);
        query.setOrder("desc");
        List<Integer> statusList = new ArrayList<Integer>();
        statusList.add(BiddingStatusEnum.ONGOING.code);
        statusList.add(BiddingStatusEnum.FULL_ADMIN_AUDITING.code);
        statusList.add(BiddingStatusEnum.STAGING.code);
        query.setStatus(statusList);
        Result result = this.biddingService.query(query);
        mav.addAllObjects(result.getAll());
        mav.addObject("list_flag",1);

        return mav;
    }

    @RequestMapping("/query")
    @ResponseBody
    public String query(@RequestParam(value = "status",required = false)Integer status,
                        @RequestParam(value = "from_month_limit",required = false) Integer fromMonthLimit, @RequestParam(value = "to_month_limit",required = false) Integer toMonthLimit,
                        @RequestParam(value = "from_process",required = false) Double fromProcess, @RequestParam(value = "to_process",required = false) Double toProcess,
                        @RequestParam(value = "from_rate",required = false) Double fromRate, @RequestParam(value = "to_rate",required = false) Double toRate,
                        @RequestParam(value = "process_order",required = false) String processOrder,@RequestParam(value = "amount_order",required = false) String amountOrder,
                        @RequestParam(value = "month_limit_order",required = false) String monthLimitOrder,@RequestParam(value = "order",required = false) String order,
                        @RequestParam(value = "current_page",required = false,defaultValue = "1")Integer page){

        BiddingQuery query = buildBiddingQuery(status, fromMonthLimit, toMonthLimit, fromProcess, toProcess, fromRate, toRate, processOrder, amountOrder, monthLimitOrder, order, page);
        Result result = this.biddingService.query(query);
        return ResultHelper.renderAsJson(result);
    }

    private BiddingQuery buildBiddingQuery( Integer status,  Integer fromMonthLimit, Integer toMonthLimit,  Double fromProcess, Double toProcess,  Double fromRate, Double toRate,  String processOrder,  String amountOrder,  String monthLimitOrder,  String order, Integer page) {

        BiddingQuery biddingQuery = new BiddingQuery();
        biddingQuery.setPageSize(16);
        biddingQuery.setCurrentPage(page);

        biddingQuery.setFromMonthLimit(fromMonthLimit != null ? fromMonthLimit : null);
        biddingQuery.setToMonthLimit(toMonthLimit != null ? toMonthLimit : null);

        biddingQuery.setFromProcess(fromProcess != null ? fromProcess : null);
        biddingQuery.setToProcess(toProcess != null ? toProcess : null);

        biddingQuery.setFromRate(fromRate != null ? fromRate : null);
        biddingQuery.setToRate(toRate != null ? toRate : null);

        biddingQuery.setMonthLimitOrder(StringUtils.isNotBlank(monthLimitOrder) ? monthLimitOrder : null);
        biddingQuery.setAmountOrder(StringUtils.isNotBlank(amountOrder) ? amountOrder : null);
        biddingQuery.setProcessOrder(StringUtils.isNotBlank(processOrder) ? processOrder : null);
        biddingQuery.setOrder(StringUtils.isNotBlank(order)?order:null);


        if(status != null){
            List<Integer> statusList = new ArrayList<Integer>();
            statusList.add(status);
            biddingQuery.setStatus(statusList);
        } else {
            List<Integer> statusList = new ArrayList<Integer>();
            statusList.add(BiddingStatusEnum.ONGOING.code);
            statusList.add(BiddingStatusEnum.FULL_ADMIN_AUDITING.code);
            statusList.add(BiddingStatusEnum.STAGING.code);
            biddingQuery.setStatus(statusList);
        }

        biddingQuery.setEffective(1);
        return biddingQuery;
    }



}
