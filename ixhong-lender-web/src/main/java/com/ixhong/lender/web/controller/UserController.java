package com.ixhong.lender.web.controller;

import com.ixhong.common.pojo.Result;
import com.ixhong.common.pojo.ResultCode;
import com.ixhong.common.utils.DESUtils;
import com.ixhong.common.utils.ResultHelper;
import com.ixhong.common.utils.ValidateUtils;
import com.ixhong.domain.constants.Constants;
import com.ixhong.domain.enums.UserTypeEnum;
import com.ixhong.domain.misc.LoginContextHolder;
import com.ixhong.domain.pojo.LenderDO;
import com.ixhong.lender.web.service.LenderService;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by jenny on 5/14/15.
 */
@Controller
public class UserController extends BaseController {
    @Autowired
    private LenderService lenderService;

    @RequestMapping("/login")
    public ModelAndView login(@RequestParam(value = "url", required = false )String url, HttpServletRequest request,HttpServletResponse response) {
        ModelAndView mav = new ModelAndView();
        if(LoginContextHolder.getLoginUser() != null) {
            mav.setViewName("redirect:/lender/index.jhtml");
            return mav;
        }
        mav.addObject("url", StringUtils.isNotBlank(url) ? url : null);
        mav.addObject("token", this.createToken(request, response));
        return mav;
    }

    /**
     * 带验证码的登录
     * @param name
     * @param password
     * @param validateCode
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/login_action", method = {RequestMethod.POST})
    @ResponseBody
    public String loginAction(@RequestParam("username") String name,
                               @RequestParam("password") String password,
                               @RequestParam("validate_code") String validateCode,
                               @RequestParam(value = "url", required = false)String url,
                               HttpServletRequest request,
                               HttpServletResponse response) {

        if(LoginContextHolder.getLoginUser() != null){
            Result result = new Result();
            result.setSuccess(true);
            return ResultHelper.renderAsJson(result);
        }

        //检测验证码
        String sourceCode = this.getCurrentValidateCode(request);
        if (!validateCode.equals(sourceCode)) {
            return ResultHelper.renderAsJson(ResultCode.PARAMETER_ERROR, "验证码错误");
        }

        Result result = this.lenderService.login(name, password);
        result.addModel("url", url);
        if (result.isSuccess()) {
            LenderDO lender = (LenderDO) result.getModel("lender");
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("id", lender.getId());
            params.put("name", lender.getName());
            params.put("type", UserTypeEnum.INVESTOR.code);
            params.put("loginTime", lender.getLoginTime().getTime());
            params.put("phone",lender.getPhone());
            String json = this.encode(params);
            String value = DESUtils.encrypt(json, systemConfig.getCookieSecurityKey());
            Cookie cookie = new Cookie(systemConfig.getCookieKey(), value);
            cookie.setSecure(false);
            cookie.setDomain(systemConfig.getCookieDomain());
            cookie.setPath("/");
            response.addCookie(cookie);
        }
        return ResultHelper.renderAsJson(result);
    }

    @RequestMapping("/register")
    public ModelAndView register(HttpServletRequest request, HttpServletResponse response) {
        ModelAndView mav = new ModelAndView();
        mav.addObject("token", this.createToken(request, response));
        return mav;
    }

    @RequestMapping(value = "/register_action", method = {RequestMethod.POST})
    @ResponseBody
    public String registerAction(@RequestParam("phone") String phone,
                                 @RequestParam("password") String password,
                                 @RequestParam("confirm_password") String configPassword,
                                 @RequestParam("name") String name,
                                 @RequestParam(value = "phone_code", required = false) String phoneCode,
                                 @RequestParam("token") String token,
                                 HttpServletRequest request,
                                 HttpServletResponse response) {
        if (!token.equals(this.getToken(request))) {
            return ResultHelper.renderAsJson(ResultCode.EXPIRED, "表单已过期，请刷新页面！");
        }
        if (!ValidateUtils.validatePhone(phone)) {
            return ResultHelper.renderAsJson(ResultCode.PARAMETER_ERROR, "手机格式错误！");
        }
        if (!password.equals(configPassword)) {
            return ResultHelper.renderAsJson(ResultCode.PARAMETER_ERROR, "两次密码不一致！");
        }
        if (password.length() > 64) {
            return ResultHelper.renderAsJson(ResultCode.PARAMETER_ERROR, "密码格式错误！");
        }
        if (name.length() > 128) {
            return ResultHelper.renderAsJson(ResultCode.PARAMETER_ERROR, "姓名参数错误！");
        }
        LenderDO lender = builderLenderDO(phone, password, name.trim());
        Result result = this.lenderService.register(lender, phoneCode);
        if (result.isSuccess()) {
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("id", lender.getId());
            params.put("name", lender.getName());
            params.put("type", UserTypeEnum.INVESTOR.code);
            lender.setLoginTime(new Date());
            params.put("loginTime", lender.getLoginTime().getTime());
            params.put("phone",lender.getPhone());
            result.addModel("userNew", lender);
            result.setResponseUrl("/lender/register_success.jhtml");
            this.removeToken(request, response);
            if(LoginContextHolder.getLoginUser() != null) {
                return ResultHelper.renderAsJson(result);
            }
            String json = this.encode(params);
            String value = DESUtils.encrypt(json, systemConfig.getCookieSecurityKey());
            Cookie cookie = new Cookie(systemConfig.getCookieKey(), value);
            cookie.setSecure(false);
            cookie.setDomain(systemConfig.getCookieDomain());
            cookie.setPath("/");
            response.addCookie(cookie);
        }
        return ResultHelper.renderAsJson(result);
    }



    private LenderDO builderLenderDO(@RequestParam("phone") String phone, @RequestParam("password") String password, @RequestParam("name") String name) {
        LenderDO lender = new LenderDO();
        lender.setName(name);
        lender.setPhone(phone);
        lender.setPassword(password);
        lender.setEffective(1);
        return lender;
    }

    @RequestMapping(value = "/logout", method = RequestMethod.GET)
    public ModelAndView loginOut(HttpServletRequest request,HttpServletResponse response) {
        ModelAndView mav = new ModelAndView();
        Cookie[] cookies = request.getCookies();
        for(Cookie cookie : cookies) {
            cookie.setValue(null);
            cookie.setMaxAge(0);
            cookie.setSecure(false);
            cookie.setDomain(systemConfig.getCookieDomain());
            cookie.setPath("/");
            response.addCookie(cookie);
        }
        mav.setViewName("redirect:/login.jhtml");
        return mav;
    }


    @RequestMapping("/check_phone_action")
    @ResponseBody
    public String checkPhoneAction(@RequestParam("phone") String phone) {
        Result result = this.lenderService.isPhoneExisted(phone);
        return ResultHelper.renderAsJson(result);
    }

    @RequestMapping("/check_card_id_action")
    @ResponseBody
    public String checkCardIdAction(@RequestParam("card_id") String cardId) {
        Result result = this.lenderService.isCardIdExisted(cardId);
        if (result.isSuccess()) {
            result.setResultCode(ResultCode.SUCCESS);
            result.setMessage("身份证未注册");
        } else {
            result.setResultCode(ResultCode.VALIDATE_FAILURE);
            result.setMessage("身份证已注册");
        }
        return ResultHelper.renderAsJson(result);
    }

    @RequestMapping("/register/send_phone_code_action")
    @ResponseBody
    public String registerSendValidateCodeAction(@RequestParam("phone") String phone) {
        Result result = this.lenderService.registerSendPhoneCode(phone);
        return ResultHelper.renderAsJson(result);
    }

    @RequestMapping("/withdraw/send_phone_code_action")
    @ResponseBody
    public String withdrawSendValidateCodeAction() {
        Result result = this.lenderService.withdrawSendPhoneCode();
        return ResultHelper.renderAsJson(result);
    }

    //找回密码
    @RequestMapping("/find_password")
    @ResponseBody
    public ModelAndView findPassword(HttpServletRequest request, HttpServletResponse response) {
        ModelAndView mav = new ModelAndView();
        mav.addObject("token", this.createToken(request, response));
        return mav;
    }

    //发送找回密码短信
    @RequestMapping("/find_password/send_phone_code_action")
    @ResponseBody
    public String findPasswordSendPhoneCodeAction(@RequestParam("phone") String phone) {
        Result result = this.lenderService.sendPhoneCode(phone, null, "lenderFindPasswordPhoneCode");
        return ResultHelper.renderAsJson(result);
    }

    //检测找回密码短信是否正确
    @RequestMapping(value = "/find_password/check_phone_code_action", method = RequestMethod.POST)
    @ResponseBody
    public String findPasswordCheckPhoneCodeAction(@RequestParam("validate_code") String validateCode,
                                       @RequestParam("phone") String phone,
                                       @RequestParam("token") String token,
                                       HttpServletRequest request,
                                       HttpServletResponse response) {
        String source = this.getToken(request);
        if (!token.equals(source)) {
            return ResultHelper.renderAsJson(ResultCode.PARAMETER_ERROR, "表单已过期，请重新刷新页面");
        }
        Result result = this.lenderService.checkPhoneCode(phone, validateCode, "lenderFindPasswordPhoneCode");
        if (!result.isSuccess()) {
            return ResultHelper.renderAsJson(ResultCode.PARAMETER_ERROR, result.getMessage());
        }
        Cookie cookie = new Cookie(Constants.PHONE_VALIDATE_CODE_COOKIE_KEY, DESUtils.encrypt(phone, Constants.HTTP_ENCRYPT_KEY));
        cookie.setMaxAge(60 * 60 * 5);
        cookie.setPath(this.getRequestPath(request));
        response.addCookie(cookie);
        result.setResponseUrl("/reset_password.jhtml?phone=" + phone);
        result.setResultCode(ResultCode.SUCCESS);
        return ResultHelper.renderAsJson(result);
    }

    @RequestMapping(value = "/reset_password")
    @ResponseBody
    public ModelAndView resetPassword(@RequestParam("phone") String phone,
                                      HttpServletRequest request,
                                      HttpServletResponse response) {
        ModelAndView mav = new ModelAndView();
        mav.addObject("token", this.createToken(request, response));
        mav.addObject("phone", phone);
        return mav;
    }

    @RequestMapping(value = "/reset_password_action", method = RequestMethod.POST)
    @ResponseBody
    public String resetPasswordAction(@RequestParam("token") String token,
                                      @RequestParam("phone") String phone,
                                      @RequestParam("password1") String password1,
                                      @RequestParam("password2") String password2,
                                      HttpServletRequest request,
                                      HttpServletResponse response) {
        if (!token.equals(this.getToken(request))) {
            return ResultHelper.renderAsJson(ResultCode.EXPIRED, "表单已过期，请刷新页面！");
        }
        if (StringUtils.isBlank(password1) || StringUtils.isBlank(password2)) {
            return ResultHelper.renderAsJson(ResultCode.PARAMETER_ERROR, "密码不能为空");
        }
        if (!password1.equals(password2)) {
            return ResultHelper.renderAsJson(ResultCode.PARAMETER_ERROR, "两次密码不一致！");
        }
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(Constants.PHONE_VALIDATE_CODE_COOKIE_KEY)) {
                    String source = cookie.getValue();
                    String sourcePhone = DESUtils.decrypt(source, Constants.HTTP_ENCRYPT_KEY);
                    if (!sourcePhone.equals(phone)) {
                        return ResultHelper.renderAsJson(ResultCode.VALIDATE_FAILURE, "非法操作");
                    }
                    break;
                }
            }
        }
        Result result = this.lenderService.resetPassword(phone, password1);
        if (result.isSuccess()) {
            Cookie cookie = new Cookie(Constants.PHONE_VALIDATE_CODE_COOKIE_KEY, null);
            cookie.setMaxAge(0);
            response.addCookie(cookie);
        }
        result.setResponseUrl("/find_password_success.jhtml");
        return ResultHelper.renderAsJson(result);
    }

    @RequestMapping("/find_password_success")
    public ModelAndView findPasswordSuccess() {
        ModelAndView mav = new ModelAndView();
        return mav;
    }


    @RequestMapping("/check_name_action")
    @ResponseBody
    public String checkNameAction(@RequestParam("name") String name) {
        Result result = this.lenderService.isNameExisted(name);
        return ResultHelper.renderAsJson(result);

    }

    //交易密码找回
    @RequestMapping("/find_deal_password")
    public ModelAndView findDealPassword(HttpServletRequest request, HttpServletResponse response) {
        ModelAndView mav = new ModelAndView();
        mav.addObject("token", this.createToken(request, response));
        return mav;
    }

    //发送找回交易密码短信
    @RequestMapping("/find_deal_password/send_phone_code_action")
    @ResponseBody
    public String findDealPasswordSendPhoneCodeAction(@RequestParam("phone") String phone,
                                                      @RequestParam("card_id")String cardId) {
        Result result = this.lenderService.sendPhoneCode(phone, cardId, "lenderFindDealPasswordPhoneCode");
        return ResultHelper.renderAsJson(result);
    }

    //检测找回交易密码短信是否正确
    @RequestMapping(value = "/find_deal_password/check_phone_code_action", method = RequestMethod.POST)
    @ResponseBody
    public String findDealPasswordCheckPhoneCodeAction(@RequestParam("validate_code") String validateCode,
                                       @RequestParam("phone") String phone,
                                       @RequestParam("token") String token,
                                       HttpServletRequest request,
                                       HttpServletResponse response) {
        String source = this.getToken(request);
        if (!token.equals(source)) {
            return ResultHelper.renderAsJson(ResultCode.PARAMETER_ERROR, "表单已过期，请重新刷新页面");
        }
        Result result = this.lenderService.checkPhoneCode(phone, validateCode, "lenderFindDealPasswordPhoneCode");
        if (!result.isSuccess()) {
            return ResultHelper.renderAsJson(ResultCode.PARAMETER_ERROR, result.getMessage());
        }
        Cookie cookie = new Cookie(Constants.PHONE_VALIDATE_CODE_COOKIE_KEY, DESUtils.encrypt(phone, Constants.HTTP_ENCRYPT_KEY));
        cookie.setMaxAge(60 * 60 * 5);
        cookie.setPath(this.getRequestPath(request));
        response.addCookie(cookie);
        result.setResponseUrl("/reset_deal_password.jhtml?phone=" + phone);
        result.setResultCode(ResultCode.SUCCESS);
        return ResultHelper.renderAsJson(result);
    }

    @RequestMapping(value = "/reset_deal_password")
    public ModelAndView resetDealPassword(@RequestParam("phone") String phone,
                                      HttpServletRequest request,
                                      HttpServletResponse response) {
        ModelAndView mav = new ModelAndView();
        mav.addObject("token", this.createToken(request, response));
        mav.addObject("phone", phone);
        return mav;
    }

    @RequestMapping(value = "/reset_deal_password_action", method = RequestMethod.POST)
    @ResponseBody
    public String resetDealPasswordAction(@RequestParam("token") String token,
                                      @RequestParam("phone") String phone,
                                      @RequestParam("password1") String password1,
                                      @RequestParam("password2") String password2,
                                      HttpServletRequest request,
                                      HttpServletResponse response) {
        if (!token.equals(this.getToken(request))) {
            return ResultHelper.renderAsJson(ResultCode.EXPIRED, "表单已过期，请刷新页面！");
        }
        if (StringUtils.isBlank(password1) || StringUtils.isBlank(password2)) {
            return ResultHelper.renderAsJson(ResultCode.PARAMETER_ERROR, "密码不能为空");
        }
        if (!password1.equals(password2)) {
            return ResultHelper.renderAsJson(ResultCode.PARAMETER_ERROR, "两次密码不一致！");
        }
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(Constants.PHONE_VALIDATE_CODE_COOKIE_KEY)) {
                    String source = cookie.getValue();
                    String sourcePhone = DESUtils.decrypt(source, Constants.HTTP_ENCRYPT_KEY);
                    if (!sourcePhone.equals(phone)) {
                        return ResultHelper.renderAsJson(ResultCode.VALIDATE_FAILURE, "非法操作");
                    }
                    break;
                }
            }
        }
        Result result = this.lenderService.resetDealPassword(phone, password1);
        if (result.isSuccess()) {
            Cookie cookie = new Cookie(Constants.PHONE_VALIDATE_CODE_COOKIE_KEY, null);
            cookie.setMaxAge(0);
            response.addCookie(cookie);
        }
        result.setResponseUrl("/find_deal_password_success.jhtml");
        return ResultHelper.renderAsJson(result);
    }

    @RequestMapping("/find_deal_password_success")
    public ModelAndView findDealPassw() {
        ModelAndView mav = new ModelAndView();
        return mav;
    }

    protected String encode(Map<String, Object> params) {
        if (params == null) {
            return null;
        }
        return JSONObject.fromObject(params).toString();
    }
}
