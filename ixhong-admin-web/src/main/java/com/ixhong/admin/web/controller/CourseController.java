package com.ixhong.admin.web.controller;

import com.ixhong.admin.web.service.CourseService;
import com.ixhong.common.pojo.Result;
import com.ixhong.common.pojo.ResultCode;
import com.ixhong.common.utils.ResultHelper;
import com.ixhong.domain.enums.CourseStatusEnum;
import com.ixhong.domain.enums.UserTypeEnum;
import com.ixhong.domain.permission.Permission;
import com.ixhong.domain.pojo.CourseDO;
import com.ixhong.domain.query.CourseQuery;
import com.ixhong.domain.utils.CalculatorUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by cuichengrui on 15/4/14.
 */
@Controller
@RequestMapping("/course")
@Permission(roles = {UserTypeEnum.ADMIN})
public class CourseController extends BaseController{

    @Autowired
    private CourseService courseService;

    private static final String REG_COURSE = "(?!^[0]{2,})(?!^0[1-9]+)(?!^0\\.[0]*$)^(([1-9]+[0-9]*)|0)(\\.\\d{1,2})?$";

    @RequestMapping("/list")
    public ModelAndView list(@RequestParam(value = "status",required = false,defaultValue = "-1")Integer status,
                             @RequestParam(value = "name",required = false)String name,
                             @RequestParam(value = "organization_name",required = false)String organizationName,
                             @RequestParam(value = "page",required = false,defaultValue = "1")Integer page){
        ModelAndView mav = new ModelAndView();
        CourseQuery query = new CourseQuery();
        query.setCurrentPage(page);
        query.setName(name != null ? name.trim() : name);
        query.setOrganizationName(organizationName != null ? organizationName.trim() : organizationName);
        //当取得的课程状态status为null时，代表全部状态
        query.setStatus(status == -1 ? null : status);
        Result result = this.courseService.list(query);
        mav.addAllObjects(result.getAll());
        return mav;
    }

    @RequestMapping("/audit")
    public ModelAndView audit(@RequestParam(value = "id")Integer id,
                              HttpServletRequest request,
                              HttpServletResponse response) {
        ModelAndView mav = new ModelAndView();
        Result result = this.courseService.detail(id);
        mav.addObject("token", this.createToken(request, response));
        mav.addObject("courseCheckedSuccess", CourseStatusEnum.COURSE_CHECKED_SUCCESS.code);
        mav.addObject("courseCheckedFailure", CourseStatusEnum.COURSE_CHECKED_FAILURE.code);
        mav.addAllObjects(result.getAll());
        //要添加的token
        return mav;
    }

    @RequestMapping(value = "/audit_action",method = {RequestMethod.POST})
    @ResponseBody
    public String auditAction(@RequestParam(value = "id")Integer id,
                              @RequestParam(value = "status")Integer status,
                              @RequestParam(value = "month_limit",required = false,defaultValue = "1")Integer monthLimit,
                              @RequestParam(value = "grace_period",required = false,defaultValue = "0")Integer gracePeriod,
                              @RequestParam(value = "fee_rate",required = false,defaultValue = "0")String feeRate,
                              @RequestParam(value = "grace_rate",required = false,defaultValue = "0")String graceRate,
                              @RequestParam(value = "audit_note")String auditNote,
                              @RequestParam(value = "token")String token,
                              HttpServletRequest request,
                              HttpServletResponse response){
        //验证token
        String sourceToken = this.getToken(request);
        if(!token.equals(sourceToken)) {
            return ResultHelper.renderAsJson(ResultCode.EXPIRED, "表单过期，请刷新页面");
        }
        //验证status是否在枚举范围内
        if (CourseStatusEnum.value(status) == null){
            return ResultHelper.renderAsJson(ResultCode.PARAMETER_ERROR, "审核结果参数不合法");
        }
        //验证宽恕期限是否大于借款期限
        if (gracePeriod.compareTo(monthLimit) > 0){
            return ResultHelper.renderAsJson(ResultCode.PARAMETER_ERROR, "宽恕期限不能大于借款期限");
        }
        Pattern regex = Pattern.compile(REG_COURSE);
        Matcher matcher = regex.matcher(feeRate != null ? feeRate : "");
        //验证月基础服务费率参数是否合法
        if (!matcher.matches()){
            return ResultHelper.renderAsJson(ResultCode.PARAMETER_ERROR, "请输入正确的【月基础服务费率】,(如果有小数位最多2位)");
        }
        //验证宽恕期内月服务费率是否合法
        matcher = regex.matcher(graceRate != null ? graceRate : "");
        if (!matcher.matches()){
            return ResultHelper.renderAsJson(ResultCode.PARAMETER_ERROR, "请输入正确的【宽恕期内月服务费率】,(如果有小数位最多2位)");
        }
        //验证审核留言
        if (StringUtils.isBlank(auditNote)){
            return ResultHelper.renderAsJson(ResultCode.PARAMETER_ERROR, "审核留言不能为空");
        }
        if (auditNote.length() < 4 && auditNote.length() > 256){
            return ResultHelper.renderAsJson(ResultCode.PARAMETER_ERROR, "审核留言长度不合法，请重新输入");
        }
        Result result = courseService.detail(id);
        if (!result.isSuccess()){
            return ResultHelper.renderAsJson(result);
        }
        CourseDO course = (CourseDO)result.getModel("course");
        Integer sourceStatus = course.getStatus();
        if (sourceStatus == CourseStatusEnum.COURSE_CHECKED_SUCCESS.code || sourceStatus == CourseStatusEnum.COURSE_CHECKED_FAILURE.code){
            return ResultHelper.renderAsJson(ResultCode.PARAMETER_ERROR, "该课程已被审核过，请勿重复审核");
        }
        course.setStatus(status);
        course.setMonthLimit(monthLimit);
        course.setGracePeriod(gracePeriod);
        course.setFeeRate(NumberUtils.toDouble(feeRate));
        course.setGraceRate(NumberUtils.toDouble(graceRate));
        Double monthRate = null;
        try {
            monthRate = CalculatorUtils.calculateMonthFeeRate(gracePeriod, monthLimit, NumberUtils.toDouble(graceRate), NumberUtils.toDouble(feeRate));
        } catch (Exception e) {
            e.printStackTrace();
            return ResultHelper.renderAsJson(ResultCode.PARAMETER_ERROR, e.getMessage());
        }
        course.setMonthRate(CalculatorUtils.format(monthRate));
        course.setAuditNote(auditNote);
        result = courseService.audit(course,getClientIP(request));
        if (!result.isSuccess()){
            return ResultHelper.renderAsJson(result);
        }
        result.setResponseUrl("/course/list.jhtml");
        this.removeToken(request,response);
        return ResultHelper.renderAsJson(result);
    }

    @RequestMapping(value = "/detail")
    public ModelAndView courseDetail(@RequestParam(value="id")Integer id) {
        ModelAndView mav = new ModelAndView();
        Result result = this.courseService.detail(id);
        mav.addAllObjects(result.getAll());
        return mav;
    }


}
