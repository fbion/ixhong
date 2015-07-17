package com.ixhong.admin.web.controller;

import com.ixhong.admin.web.service.LenderService;
import com.ixhong.common.pojo.Result;
import com.ixhong.common.utils.ResultHelper;
import com.ixhong.domain.constants.Constants;
import com.ixhong.domain.enums.UserTypeEnum;
import com.ixhong.domain.permission.Permission;
import com.ixhong.domain.query.LenderAccountFlowQuery;
import com.ixhong.domain.query.LenderQuery;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

/**
 * Created by chenguang on 2015/5/25 0025.
 */

@Controller
@RequestMapping("/lender")
@Permission(roles = {UserTypeEnum.ADMIN})
public class LenderController extends BaseController {

    @Autowired
    private LenderService lenderService;

    @RequestMapping("/list")
    public ModelAndView lenderList(@RequestParam(value = "begin_date", required = false)String beginDate,
                                   @RequestParam(value = "end_date", required = false)String endDate,
                                   @RequestParam(value = "name", required = false)String name,
                                   @RequestParam(value = "phone", required = false)String phone,
                                   @RequestParam(value = "real_name", required = false)String realName,
                                   @RequestParam(value = "page", defaultValue = "1")Integer page) {
        ModelAndView mav = new ModelAndView();
        LenderQuery query = new LenderQuery();
        query.setCurrentPage(page);
        query.setPageSize(6);
        try {
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
        if(StringUtils.isNotBlank(name)) {
            query.setName(name);
        }
        if(StringUtils.isNotBlank(phone)) {
            query.setPhone(phone);
        }
        if(StringUtils.isNotBlank(realName)) {
            query.setRealName(realName);
        }

        Result result = lenderService.list(query);
        mav.addAllObjects(result.getAll());
        return mav;
    }

    @RequestMapping("/flow_list")
    public ModelAndView lenderFlowList(@RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
                                       @RequestParam(value = "name", required = false) String name,
                                       @RequestParam(value = "phone", required = false) String phone,
                                       @RequestParam(value = "real_name", required = false) String realName,
                                       @RequestParam(value = "type", required = false, defaultValue = "-1") Integer type,
                                       @RequestParam(value = "begin_date", required = false) String beginDate,
                                       @RequestParam(value = "end_date", required = false) String endDate,
                                       @RequestParam(value = "id",required = false) Integer id
    ) {
        ModelAndView modelAndView = new ModelAndView();
        LenderAccountFlowQuery lenderFlowQuery = new LenderAccountFlowQuery();
        lenderFlowQuery.setCurrentPage(page);
        lenderFlowQuery.setType(type);
        lenderFlowQuery.setLenderId(id);
        try {
            if (StringUtils.isNotBlank(name)) {
                lenderFlowQuery.setLenderName(name.trim());
            }
            if (StringUtils.isNotBlank(phone)) {
                lenderFlowQuery.setLenderPhone(phone.trim());
            }
            if (StringUtils.isNotBlank(realName)) {
                lenderFlowQuery.setLenderRealName(realName.trim());
            }
            if (StringUtils.isNotBlank(beginDate)) {
                lenderFlowQuery.setBeginDate(beginDate);
            }
            if (StringUtils.isNotBlank(endDate)) {
                lenderFlowQuery.setEndDate(endDate);
            }
            Result result = lenderService.lenderAccountFlow(lenderFlowQuery);
            modelAndView.addAllObjects(result.getAll());
        } catch (Exception e) {
            modelAndView.setViewName("redirect:/error.jhtml?messages=paramter-error");
            return modelAndView;
        }

        return modelAndView;
    }

    @RequestMapping("/bidding_item_list")
    public ModelAndView biddingItemList(@RequestParam(value = "phone", required = false)String phone,
                                        @RequestParam(value = "real_name", required = false)String realName,
                                        @RequestParam(value = "page", defaultValue = "1")Integer page) {
        ModelAndView mav = new ModelAndView();
        Result result = lenderService.biddingItemList(phone, realName, page);
        mav.addAllObjects(result.getAll());
        return mav;
    }

    @RequestMapping("/unbind_bank_card")
    public ModelAndView unbindBankCard() {
        ModelAndView mav = new ModelAndView();
        return mav;
    }

    @RequestMapping("/unbind_bank_card_action")
    @ResponseBody
    public String unbindBankCardAction(@RequestParam("card_id")String cardId,
                                 @RequestParam("card_top")String cardTop,
                                 @RequestParam("card_last")String cardLast){
        Result result = lenderService.unbindBankCard(cardId, cardTop, cardLast);
        return ResultHelper.renderAsJson(result);
    }

    @RequestMapping("/refund")
    public ModelAndView refund() {
        ModelAndView mav = new ModelAndView();
        return mav;
    }

    @RequestMapping("/refund_action")
    @ResponseBody
    public String refundAction(@RequestParam("yeepay_order_id")String yeepayOrderId,
                                       @RequestParam("amount")Double amount,
                                       @RequestParam("cause")String cause) {
        Result result = lenderService.refund(yeepayOrderId, amount, cause);
        return ResultHelper.renderAsJson(result);
    }

    @RequestMapping("/query_refund")
    @ResponseBody
    public String queryRefund(@RequestParam("yeepay_order_id")String yeepayOrderId) {
        Result result = lenderService.queryRefund(yeepayOrderId);
        return ResultHelper.renderAsJson(result);
    }

    @RequestMapping("/query_withdraw")
    public ModelAndView queryWithdraw() {
        ModelAndView mav = new ModelAndView();
        return mav;
    }

    @RequestMapping("/query_withdraw_action")
    @ResponseBody
    public String queryWithdrawAction(@RequestParam("order_id")String orderId) {
        Result result = lenderService.queryWithdraw(orderId);
        return ResultHelper.renderAsJson(result);
    }

    @RequestMapping("/query_recharge_action")
    @ResponseBody
    public String queryRechargeAction(@RequestParam("order_id")String orderId) {
        Result result = lenderService.queryRecharge(orderId);
        return ResultHelper.renderAsJson(result);
    }
}
