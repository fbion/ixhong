package com.ixhong.admin.web.controller;

import com.ixhong.admin.web.service.InterestRateService;
import com.ixhong.common.pojo.Result;
import com.ixhong.common.pojo.ResultCode;
import com.ixhong.common.utils.ResultHelper;
import com.ixhong.domain.enums.MonthLimitEnum;
import com.ixhong.domain.enums.UserTypeEnum;
import com.ixhong.domain.permission.Permission;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by chenguang on 2015/4/22 0022.
 */
@Controller
@RequestMapping("/interest_rate")
@Permission(roles = {UserTypeEnum.ADMIN})
public class InterestRateController extends BaseController {

    @Autowired
    private InterestRateService interestRateService;

    @RequestMapping("/list")
    public ModelAndView list() {
        ModelAndView mav = new ModelAndView();
        Result result = interestRateService.list();
        mav.addAllObjects(result.getAll());
        return mav;
    }

    @RequestMapping("/add")
    public ModelAndView add(HttpServletRequest request,
                            HttpServletResponse response) {
        ModelAndView mav = new ModelAndView();
        mav.addObject("token", this.createToken(request, response));
        return mav;
    }

    @RequestMapping(value = "/add_action", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public String addAction(@RequestParam(value = "month_limit") Integer monthLimit,
                            @RequestParam(value = "rate") String rate,
                            @RequestParam(value = "description", required = false) String description,
                            @RequestParam(value = "token") String token,
                            HttpServletRequest request) {
        String source = this.getToken(request);
        if(!token.equals(source)) {
            return ResultHelper.renderAsJson(ResultCode.EXPIRED.EXPIRED, "表单已过期，请刷新页面");
        }
        if(MonthLimitEnum.value(monthLimit) == null) {
            return ResultHelper.renderAsJson(ResultCode.PARAMETER_ERROR, "您输入的还款期有误，请重新输入");
        }
        String[] splits = rate.split("\\.");
        int length = splits.length;
        if(length > 2) {
            return ResultHelper.renderAsJson(ResultCode.PARAMETER_ERROR, "利率保留2位小数");
        } else if(length == 2) {
            if(splits[1].length() > 2) {
                return ResultHelper.renderAsJson(ResultCode.PARAMETER_ERROR, "利率保留2位小数");
            }
        }
        Double _rate = Double.valueOf(rate);
        if(_rate < 0 || _rate > 30) {
            return ResultHelper.renderAsJson(ResultCode.PARAMETER_ERROR, "利率最低为0，最高为30");
        }
        Result result = interestRateService.add(monthLimit, _rate, description);
        if(result.isSuccess()) {
            result.setResponseUrl("/interest_rate/list.jhtml");
        }
        return ResultHelper.renderAsJson(result);
    }

    @RequestMapping(value = "/disable")
    @ResponseBody
    public String disable(@RequestParam(value = "id") Integer id) {
        Result result = interestRateService.disable(id);
        if(result.isSuccess()) {
            result.setResponseUrl("/interest_rate/list.jhtml");
        }
        return ResultHelper.renderAsJson(result);
    }
}
