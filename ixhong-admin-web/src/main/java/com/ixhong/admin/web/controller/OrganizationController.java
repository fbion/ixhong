package com.ixhong.admin.web.controller;

import com.ixhong.admin.web.service.AccountService;
import com.ixhong.admin.web.service.OrganizationService;
import com.ixhong.common.pojo.Result;
import com.ixhong.common.pojo.ResultCode;
import com.ixhong.common.utils.ResultHelper;
import com.ixhong.domain.constants.Constants;
import com.ixhong.domain.enums.*;
import com.ixhong.domain.permission.Permission;
import com.ixhong.domain.pojo.OrganizationAccountDO;
import com.ixhong.domain.pojo.OrganizationDO;
import com.ixhong.domain.query.OrganizationQuery;
import com.ixhong.domain.query.OrganizationWithdrawQuery;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
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
 * Created by shenhongxi on 15/4/13.
 */
@Controller
@RequestMapping(value = "/organization")
@Permission(roles = {UserTypeEnum.ADMIN})
public class OrganizationController extends BaseController{

    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private AccountService accountService;

    @RequestMapping(value = "/list")
    public ModelAndView list(@RequestParam(value = "status",defaultValue = "-1") Integer status,
                             @RequestParam(value = "name",required = false) String name,
                             @RequestParam(value = "email",required = false) String email,
                             @RequestParam(value = "code",required = false) String code,
                             @RequestParam(value = "page",required = false,defaultValue = "1") Integer page) {
        ModelAndView mav = new ModelAndView();
        OrganizationQuery query = new OrganizationQuery();
        query.setCurrentPage(page);
        if (StringUtils.isNotBlank(name)) {
            query.setName(name);
        }
        if (StringUtils.isNotBlank(email)) {
            query.setEmail(email);
        }
        if (StringUtils.isNotBlank(code)) {
            query.setCode(code);
        }
        query.setStatus(status);
        Result result = this.organizationService.list(query);
        mav.addAllObjects(result.getAll());
        mav.addObject("statusEnums", OrganizationStatusEnum.values());
        mav.addObject("levelEnums", OrganizationLevelEnum.values());
        mav.addObject("emailValidating", OrganizationStatusEnum.EMAIL_VALIDATING.code);
        mav.addObject("notSubmited", OrganizationStatusEnum.MATERIAL_NOT_SUBMITTED.code);
        mav.addObject("auditing", OrganizationStatusEnum.MATERIAL_AUDITING.code);
        mav.addObject("auditedSuccess", OrganizationStatusEnum.MATERIAL_AUDITED_SUCCESS.code);
        mav.addObject("auditedFailed", OrganizationStatusEnum.MATERIAL_AUDITED_FAILED.code);
        return mav;
    }

    @RequestMapping(value = "/audit")
    public ModelAndView audit(@RequestParam(value = "organization_id") Integer organizationId,
                              @RequestParam(value = "status",defaultValue = "-1") Integer status,
                              @RequestParam(value = "name",required = false) String name,
                              @RequestParam(value = "email",required = false) String email,
                              @RequestParam(value = "code",required = false) String code,
                              @RequestParam(value = "page",required = false,defaultValue = "1") Integer page,
                               HttpServletRequest request,
                               HttpServletResponse response) {
        ModelAndView mav = new ModelAndView();
        mav.addObject("token", this.createToken(request,response));
        mav.addObject("auditedSuccess", OrganizationStatusEnum.MATERIAL_AUDITED_SUCCESS.code);
        mav.addObject("auditedFailed", OrganizationStatusEnum.MATERIAL_AUDITED_FAILED.code);
        mav.addObject("organizationId", organizationId);
        mav.addObject("checkTypeEnums", OrganizationCheckTypeEnum.values());
        mav.addObject("levelEnums", OrganizationLevelEnum.values());
        mav.addObject("quarterCheck", OrganizationCheckTypeEnum.QUARTER.code);
        mav.addObject("firstLevel", OrganizationLevelEnum.A.code);

        Result result = this.organizationService.detail(organizationId);
        mav.addAllObjects(result.getAll());
        mav.addObject("foundTypeEnums", OrganizationFoundTypeEnum.values());
        mav.addObject("propertyTypeEnums", OrganizationPropertyTypeEnum.values());
        mav.addObject("imgHost", systemConfig.getImageHostName());

        mav.addObject("status", status);
        mav.addObject("name", name);
        mav.addObject("email", email);
        mav.addObject("code", code);
        mav.addObject("page", page);
        return mav;
    }

    @RequestMapping(value = "/audit_action",method = {RequestMethod.POST})
    @ResponseBody
    public String auditAction(@RequestParam(value = "organization_id") Integer organizationId,
                               @RequestParam(value = "status") Integer status,
                               @RequestParam(value = "code", required = false) String code,
                               @RequestParam(value = "level", required = false) int level,
                               @RequestParam(value = "bail_percentage", required = false) String bailPercentage,
                               @RequestParam(value = "audit_note",required = false) String auditNote,
                               @RequestParam(value = "check_type",required = false) Integer checkType,
                               @RequestParam(value = "lending_quotas",required = false) Double lendingQuotas,
                               @RequestParam(value = "token") String token,
                               HttpServletRequest request,
                               HttpServletResponse response) {
        //验证token
        String sourceToken = this.getToken(request);
        if(!token.equals(sourceToken)) {
            return ResultHelper.renderAsJson(ResultCode.EXPIRED,"表单过期，请刷新页面");
        }
        if(OrganizationStatusEnum.value(status) == null) {
            return ResultHelper.renderAsJson(ResultCode.PARAMETER_ERROR);
        }
        if (status == OrganizationStatusEnum.MATERIAL_AUDITED_SUCCESS.code) {
            if (!code.matches("[a-zA-Z0-9]{6,10}")) {
                return ResultHelper.renderAsJson(ResultCode.PARAMETER_ERROR, "学校编号由6到10位字母或数字组成，请重新输入！");
            }
            JSONObject bailJson = JSONObject.fromObject(bailPercentage);
            Double juniorLowerPercentage = bailJson.getDouble("juniorLower");
            Double juniorPercentage = bailJson.getDouble("junior");
            Double undergraduatePercentage = bailJson.getDouble("undergraduate");
            String reg = "[0-9]+(\\.[0-9]{1,2})?";
            if (!juniorLowerPercentage.toString().matches(reg)
                    || !juniorPercentage.toString().matches(reg)
                    || !undergraduatePercentage.toString().matches(reg)) {
                return ResultHelper.renderAsJson(ResultCode.PARAMETER_ERROR, "保证金比例为最多两位小数正数，请重新输入！");
            }
            if(juniorLowerPercentage < 0 || juniorLowerPercentage > 40.00F
                    || juniorPercentage < 0 || juniorPercentage > 40.00F
                    || undergraduatePercentage < 0 || undergraduatePercentage > 40.00F) {
                return ResultHelper.renderAsJson(ResultCode.PARAMETER_ERROR, "保证金比例必须为0-40之间，请重新输入！");
            }if (!lendingQuotas.toString().matches("[0-9]+(\\.[0-9]{1,2})?")) {
                return ResultHelper.renderAsJson(ResultCode.PARAMETER_ERROR, "额度限制为最多两位小数正数，请重新输入！");
            }
            if(lendingQuotas > 1000.00F) {
                return ResultHelper.renderAsJson(ResultCode.PARAMETER_ERROR, "额度限制必须为1000之间，请重新输入！");
            }
        }
        if (StringUtils.length(auditNote) > 122) {
            return ResultHelper.renderAsJson(ResultCode.PARAMETER_ERROR);
        }

        OrganizationDO organization = new OrganizationDO();
        OrganizationAccountDO account = null;
        organization.setId(organizationId);
        organization.setStatus(status);
        organization.setAuditNote(auditNote);
        if (status == OrganizationStatusEnum.MATERIAL_AUDITED_SUCCESS.code) {
            organization.setCode(code);
            organization.setLevel(level);
            organization.setCheckType(checkType);
            account = new OrganizationAccountDO();
            account.setOrganizationId(organizationId);
            account.setBailPercentage(bailPercentage);
            account.setLendingQuotas(lendingQuotas);
        }

        Result result = this.organizationService.audit(organization, account, this.getClientIP(request));
        this.removeToken(request, response);
        return ResultHelper.renderAsJson(result);
    }

    @RequestMapping(value = "/update_bail_percent")
    public ModelAndView update_bail_percent(@RequestParam(value = "organization_id") Integer organizationId,
                                            @RequestParam(value = "status",defaultValue = "-1") Integer status,
                                            @RequestParam(value = "name",required = false) String name,
                                            @RequestParam(value = "email",required = false) String email,
                                            @RequestParam(value = "code",required = false) String code,
                                            @RequestParam(value = "page",required = false,defaultValue = "1") Integer page,
                                            HttpServletRequest request,
                                            HttpServletResponse response
                                            ) {
        ModelAndView mav = new ModelAndView();
        mav.addObject("token", this.createToken(request, response));
        Result result = this.organizationService.getAuditInfo(organizationId);
        mav.addAllObjects(result.getAll());
        mav.addObject("levelEnums", OrganizationLevelEnum.values());
        mav.addObject("checkTypeEnums", OrganizationCheckTypeEnum.values());

        mav.addObject("status", status);
        mav.addObject("name", name);
        mav.addObject("email", email);
        mav.addObject("code", code);
        mav.addObject("page", page);
        return mav;
    }

    @RequestMapping(value = "/update_bail_percent_action",method = {RequestMethod.POST})
    @ResponseBody
    public String updateBailPercentAction(@RequestParam(value = "organization_id") Integer organizationId,
                                          @RequestParam(value = "bail_percentage") String bailPercentage,
                                          @RequestParam(value = "level") int level,
                                          @RequestParam(value = "check_type") int checkType,
                                          @RequestParam(value = "lending_quotas") Double lendingQuotas,
                                          @RequestParam(value = "token") String token,
                                          HttpServletRequest request,
                                          HttpServletResponse response) {
        //验证token
        String sourceToken = this.getToken(request);
        if(!token.equals(sourceToken)) {
            return ResultHelper.renderAsJson(ResultCode.EXPIRED,"表单过期，请刷新页面");
        }
        JSONObject bailJson = JSONObject.fromObject(bailPercentage);
        Double juniorLowerPercentage = bailJson.getDouble("juniorLower");
        Double juniorPercentage = bailJson.getDouble("junior");
        Double undergraduatePercentage = bailJson.getDouble("undergraduate");
        String reg = "[0-9]+(\\.[0-9]{1,2})?";
        if (!juniorLowerPercentage.toString().matches(reg)
                || !juniorPercentage.toString().matches(reg)
                || !undergraduatePercentage.toString().matches(reg)) {
            return ResultHelper.renderAsJson(ResultCode.PARAMETER_ERROR, "保证金比例为最多两位小数正数，请重新输入！");
        }
        if(juniorLowerPercentage < 0 || juniorLowerPercentage > 40.00F
                || juniorPercentage < 0 || juniorPercentage > 40.00F
                || undergraduatePercentage < 0 || undergraduatePercentage > 40.00F) {
            return ResultHelper.renderAsJson(ResultCode.PARAMETER_ERROR, "保证金比例必须为0-40之间，请重新输入！");
        }if (!lendingQuotas.toString().matches("[0-9]+(\\.[0-9]{1,2})?")) {
            return ResultHelper.renderAsJson(ResultCode.PARAMETER_ERROR, "额度限制为最多两位小数正数，请重新输入！");
        }
        if(lendingQuotas > 1000.00F) {
            return ResultHelper.renderAsJson(ResultCode.PARAMETER_ERROR, "额度限制必须为1000之间，请重新输入！");
        }

        OrganizationDO organization = new OrganizationDO();
        organization.setId(organizationId);
        organization.setLevel(level);
        organization.setCheckType(checkType);
        OrganizationAccountDO account = new OrganizationAccountDO();
        account.setBailPercentage(bailPercentage);
        account.setLendingQuotas(lendingQuotas);
        Result result = this.organizationService.update(organization, account, this.getClientIP(request));
        this.removeToken(request, response);
        return ResultHelper.renderAsJson(result);
    }

    @RequestMapping(value = "/detail")
    public ModelAndView detail(@RequestParam(value = "organization_id") Integer organizationId) {
        ModelAndView mav = new ModelAndView();
        Result result = this.organizationService.detail(organizationId);
        mav.addAllObjects(result.getAll());
        mav.addObject("foundTypeEnums", OrganizationFoundTypeEnum.values());
        mav.addObject("chainTypeEnums", ChainTypeEnum.values());
        mav.addObject("propertyTypeEnums", OrganizationPropertyTypeEnum.values());
        mav.addObject("checkTypeEnums", OrganizationCheckTypeEnum.values());
        mav.addObject("auditedSuccess", OrganizationStatusEnum.MATERIAL_AUDITED_SUCCESS.code);
        mav.addObject("auditedFailed", OrganizationStatusEnum.MATERIAL_AUDITED_FAILED.code);
        mav.addObject("imgHost", systemConfig.getImageHostName());
        return mav;
    }

    @RequestMapping(value = "/withdraw_list")
    public ModelAndView withdrawList(@RequestParam(value = "begin_date", required = false)String beginDate,
                                     @RequestParam(value = "end_date", required = false)String endDate,
                                     @RequestParam(value = "organization_name", required = false)String organizationName,
                                     @RequestParam(value = "organization_code", required = false)String organizationCode,
                                     @RequestParam(value = "status", defaultValue = "-1")Integer status,
                                     @RequestParam(value = "page", defaultValue = "1")Integer page) {
        ModelAndView mav = new ModelAndView();
        OrganizationWithdrawQuery query = new OrganizationWithdrawQuery();
        query.setPageSize(6);
        query.setCurrentPage(page);
        query.setStatus(status);
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
        if(StringUtils.isNotBlank(organizationName)) {
            query.setOrganizationName(organizationName);
        }
        if(StringUtils.isNotBlank(organizationCode)) {
            query.setOrganizationCode(organizationCode);
        }
        Result result = accountService.withdrawList(query);
        mav.addAllObjects(result.getAll());
        mav.addObject("statusEnums", OrganizationWithdrawStatusEnum.values());
        mav.addObject("auditing", OrganizationWithdrawStatusEnum.AUDITING.code);
        mav.addObject("success", OrganizationWithdrawStatusEnum.SUCCESS.code);
        mav.addObject("auditingSuccess", OrganizationWithdrawStatusEnum.AUDITING_SUCCESS.code);
        mav.addObject("auditingFailed", OrganizationWithdrawStatusEnum.AUDITING_FAILED.code);
        return mav;
    }

    @RequestMapping("/audit_withdraw")
    @ResponseBody
    public String auditWithdraw(@RequestParam("id")Integer id,
                                @RequestParam("status")Integer status) {
        Result result = organizationService.auditWithdraw(id, status);
        return ResultHelper.renderAsJson(result);
    }

    @RequestMapping(value = "/withdraw_query")
    public ModelAndView withdrawQuery(HttpServletRequest request,
                                      HttpServletResponse response) {
        ModelAndView mav = new ModelAndView();
        mav.addObject("token", this.createToken(request, response));
        return mav;
    }

    @RequestMapping("/withdraw_query_action")
    @ResponseBody
    public String withdrawQueryAction(@RequestParam("order_id")String orderId,
                                @RequestParam("batch_no")String batchNo,
                                      HttpServletRequest request,
                                      HttpServletResponse response,
                                      @RequestParam("token")String token) {
        String sourceToken = this.getToken(request);
        if(!token.equals(sourceToken)) {
            return ResultHelper.renderAsJson(ResultCode.EXPIRED,"表单过期，请刷新页面");
        }
        Result result = organizationService.withdrawQuery(orderId, batchNo);
        if (result.isSuccess()) {
            this.removeToken(request, response);
        }
        return ResultHelper.renderAsJson(result);
    }

    @RequestMapping(value = "/update_account")
    public ModelAndView updateAccount(HttpServletRequest request,
                                      HttpServletResponse response,
                                      @RequestParam("organization_id")String organizationId,
                                      @RequestParam("bank_name")String bankName,
                                      @RequestParam("bank_branch")String bankBranch,
                                      @RequestParam("name")String name) {
        ModelAndView mav = new ModelAndView();
        mav.addObject("token", this.createToken(request, response));
        mav.addObject("organizationId", organizationId);
        mav.addObject("bankName", bankName);
        mav.addObject("bankBranch", bankBranch);
        mav.addObject("name", name);
        return mav;
    }

    @RequestMapping("/update_account_action")
    @ResponseBody
    public String updateAccountAction(@RequestParam("organization_id")Integer organizationId,
                                      @RequestParam("bank_name")String bankName,
                                      @RequestParam("bank_branch")String bankBranch,
                                      @RequestParam("name")String name,
                                      HttpServletRequest request,
                                      HttpServletResponse response,
                                      @RequestParam("token")String token) {
        String sourceToken = this.getToken(request);
        if(!token.equals(sourceToken)) {
            return ResultHelper.renderAsJson(ResultCode.EXPIRED,"表单过期，请刷新页面");
        }
        OrganizationAccountDO account = new OrganizationAccountDO();
        account.setOrganizationId(organizationId);
        account.setBankName(bankName);
        account.setBankBranch(bankBranch);
        account.setName(name);
        Result result = organizationService.updateAccount(account);
        if (result.isSuccess()) {
            this.removeToken(request, response);
        }
        return ResultHelper.renderAsJson(result);
    }

}
