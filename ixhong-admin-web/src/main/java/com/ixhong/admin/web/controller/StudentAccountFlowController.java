package com.ixhong.admin.web.controller;

import com.ixhong.admin.web.service.StudentAccountService;
import com.ixhong.admin.web.service.StudentService;
import com.ixhong.common.pojo.Result;
import com.ixhong.domain.constants.Constants;
import com.ixhong.domain.enums.StudentFlowTypeEnum;
import com.ixhong.domain.query.StudentAccountFlowQuery;
import com.ixhong.domain.query.StudentBillStageQuery;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.text.ParseException;

/**
 * Created by shenhongxi on 15/5/14.
 */
@Controller
@RequestMapping("/student_account_flow")
public class StudentAccountFlowController extends BaseController {

    @Autowired
    private StudentAccountService studentAccountService;

    @Autowired
    private StudentService studentService;

    @RequestMapping("/recharge_list")
    public ModelAndView rechargeList(
                             @RequestParam(value = "student_name",required = false)String studentName,
                             @RequestParam(value = "student_phone",required = false)String studentPhone,
                             @RequestParam(value = "begin_date",required = false)String beginDate,
                             @RequestParam(value = "end_date",required = false)String endDate,
                             @RequestParam(value = "page",required = false,defaultValue = "1")Integer page) {
        ModelAndView mav = new ModelAndView();
        StudentAccountFlowQuery query = new StudentAccountFlowQuery();
        query.setCurrentPage(page);
        query.setType(StudentFlowTypeEnum.RECHARGE.code);
        if (StringUtils.isNotBlank(studentName)) {
            query.setStudentName(studentName);
        }
        if (StringUtils.isNotBlank(studentPhone)) {
            query.setStudentPhone(studentPhone);
        }
        try {
            if (StringUtils.isNotBlank(beginDate)){
                query.setBeginDate(DateUtils.parseDate(beginDate, Constants.DATE_PATTERN));
                query.setQueryBeginDate(beginDate);
            }
            if (StringUtils.isNotBlank(endDate)){
                query.setEndDate(DateUtils.addDays(DateUtils.parseDate(endDate, Constants.DATE_PATTERN), 1));
                query.setQueryEndDate(endDate);
            }
        } catch (ParseException e) {
            mav.setViewName("redirect:/error.jhtml?messages=paramter-error");
            return mav;
        }
        Result result = this.studentAccountService.listFlow(query);
        mav.addAllObjects(result.getAll());
        return mav;
    }

    @RequestMapping("/repaid_list")
    public ModelAndView repaidList(
            @RequestParam(value = "student_name",required = false)String studentName,
            @RequestParam(value = "student_phone",required = false)String studentPhone,
            @RequestParam(value = "organization_name",required = false)String organizationName,
            @RequestParam(value = "begin_date",required = false)String beginDate,
            @RequestParam(value = "end_date",required = false)String endDate,
            @RequestParam(value = "page",required = false,defaultValue = "1")Integer page) {
        ModelAndView mav = new ModelAndView();
        StudentAccountFlowQuery query = new StudentAccountFlowQuery();
        query.setCurrentPage(page);
        query.setType(StudentFlowTypeEnum.REPAYMENT.code);
        if (StringUtils.isNotBlank(studentName)) {
            query.setStudentName(studentName);
        }
        if (StringUtils.isNotBlank(studentPhone)) {
            query.setStudentPhone(studentPhone);
        }
        if (StringUtils.isNotBlank(organizationName)) {
            query.setOrganizationName(organizationName);
        }
        try {
            if (StringUtils.isNotBlank(beginDate)){
                query.setBeginDate(DateUtils.parseDate(beginDate, Constants.DATE_PATTERN));
                query.setQueryBeginDate(beginDate);
            }
            if (StringUtils.isNotBlank(endDate)){
                query.setEndDate(DateUtils.addDays(DateUtils.parseDate(endDate, Constants.DATE_PATTERN), 1));
                query.setQueryEndDate(endDate);
            }
        } catch (ParseException e) {
            mav.setViewName("redirect:/error.jhtml?messages=paramter-error");
            return mav;
        }
        Result result = this.studentAccountService.listFlow(query);
        mav.addAllObjects(result.getAll());
        return mav;
    }

    @RequestMapping("/bill_stage_list")
    public ModelAndView billStageList(@RequestParam(value = "phone",required = false)String phone,
                                      @RequestParam(value = "studentName",required = false)String name,
                                      @RequestParam(value = "page",required = false,defaultValue = "1")Integer page) {
        ModelAndView mav = new ModelAndView();
        StudentBillStageQuery query = new StudentBillStageQuery();
        if(StringUtils.isNotBlank(phone)){
            query.setPhone(phone);
        }
        if(StringUtils.isNotBlank(name)){
            query.setStudentName(name);
        }
        query.setCurrentPage(page);
        Result result = this.studentService.billStageList(query);
        mav.addAllObjects(result.getAll());
        return mav;
    }

}
