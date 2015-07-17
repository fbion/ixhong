package com.ixhong.admin.web.controller;

import com.ixhong.admin.web.service.OrganizationService;
import com.ixhong.common.pojo.Result;
import com.ixhong.domain.constants.Constants;
import com.ixhong.domain.enums.CourseStatusEnum;
import com.ixhong.domain.enums.OperationsLogTypeEnum;
import com.ixhong.domain.enums.OrganizationStatusEnum;
import com.ixhong.domain.enums.UserTypeEnum;
import com.ixhong.domain.permission.Permission;
import com.ixhong.domain.query.OperationsLogQuery;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenguang on 2015/4/29 0029.
 */

@Controller
@RequestMapping("/operations_log")
@Permission(roles = {UserTypeEnum.ADMIN})
public class OperationsLogController extends BaseController {

    @Autowired
    private OrganizationService organizationService;

    private static final String[] pattern = {"yyyy-MM-dd"};

    @RequestMapping("/audit_list")
    public ModelAndView list(@RequestParam(value = "page",required = false,defaultValue = "1") Integer page,
                             @RequestParam(value = "operation", required = false, defaultValue = "-1") Integer operation,
                             @RequestParam(value = "begin_date", required = false) String beginDate,
                             @RequestParam(value = "end_date", required = false) String endDate,
                             @RequestParam(value = "name", required = false) String name,
                             @RequestParam(value = "operator", required = false) String operator) {
        ModelAndView mav = new ModelAndView();
        List<Integer> operations = new ArrayList<Integer>();
        if(operation == -1) {
            operations.add(OperationsLogTypeEnum.AUDIT_COURSE.code);
            operations.add(OperationsLogTypeEnum.AUDIT_ORGANIZATION.code);
            operations.add(OperationsLogTypeEnum.UPDATE_BAIL_PERCENT.code);
        }else {
            operations.add(operation);
        }
        OperationsLogQuery query = new OperationsLogQuery();
        query.setCurrentPage(page);
        query.setOperation(operation);
        query.setOperations(operations);
        query.setQueryBeginDate(beginDate);
        query.setQueryEndDate(endDate);
        try {
            if (StringUtils.isNotBlank(beginDate)) {
                query.setBeginDate(DateUtils.parseDate(beginDate, pattern));
            }
            if (StringUtils.isNotBlank(endDate)) {
                query.setEndDate(DateUtils.addDays(DateUtils.parseDate(endDate, Constants.DATE_PATTERN), 1));
            }
        } catch (Exception e) {
            //
            mav.setViewName("redirect:/error.jhtml?messages=paramter-error");
            return mav;
        }
        if(StringUtils.isNotBlank(name)) {
            query.setName(name);
        }
        if(StringUtils.isNotBlank(operator)) {
            query.setOperator(operator);
        }
        Result result = organizationService.auditOperationsLog(query);
        mav.addAllObjects(result.getAll());
        mav.addObject("operationsLogTypeEnums", OperationsLogTypeEnum.values());
        mav.addObject("organizationStatusEnums", OrganizationStatusEnum.values());
        mav.addObject("courseStatusEnums", CourseStatusEnum.values());
        mav.addObject("auditCourseCode", OperationsLogTypeEnum.AUDIT_COURSE.code);
        mav.addObject("auditOrganizationCode", OperationsLogTypeEnum.AUDIT_ORGANIZATION.code);
        mav.addObject("updateBailPercentCode", OperationsLogTypeEnum.UPDATE_BAIL_PERCENT.code);

        return mav;
    }
}
