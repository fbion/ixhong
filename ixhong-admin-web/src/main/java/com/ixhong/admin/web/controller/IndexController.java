package com.ixhong.admin.web.controller;

import com.ixhong.admin.web.service.AdminService;
import com.ixhong.common.pojo.Result;
import com.ixhong.common.pojo.ResultCode;
import com.ixhong.common.utils.DESUtils;
import com.ixhong.common.utils.ResultHelper;
import com.ixhong.common.utils.ValidateCodeUtils;
import com.ixhong.domain.constants.Constants;
import com.ixhong.domain.enums.OrganizationStatusEnum;
import com.ixhong.domain.misc.LoginContextHolder;
import com.ixhong.domain.pojo.UserDO;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by shenhongxi on 15/4/13.
 */
@Controller
public class IndexController extends BaseController{

    @Autowired
    private AdminService adminService;

    @RequestMapping("/login")
    public ModelAndView login(HttpServletRequest request,HttpServletResponse response) {
        ModelAndView mav = new ModelAndView();
        if (LoginContextHolder.getLoginUser() != null){
            mav.setViewName("redirect:/organization/list.jhtml");
        }
        return mav;
    }

    @RequestMapping(value = "login_action", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public String loginAction(@RequestParam("name") String name, @RequestParam("password") String password,
                              @RequestParam("validate_code") String validateCode,
                              HttpServletRequest request,HttpServletResponse response) {

        if(LoginContextHolder.getLoginUser() != null){
            Result result = new Result();
            result.setSuccess(true);
            return ResultHelper.renderAsJson(result);
        }

        //验证码校验
        if(!this.getCurrentValidateCode(request).equals(validateCode)){
            return ResultHelper.renderAsJson(ResultCode.PARAMETER_ERROR,"验证码错误");
        }

        Result result = this.adminService.login(name.trim(), password);
        if (result.isSuccess()) {
            UserDO user = (UserDO) result.getModel("user");
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("id", user.getId());
            params.put("name", user.getName());
            params.put("type", user.getType());
            params.put("adminId",user.getAdminId());
            String json = this.encoder(params);
            String value = DESUtils.encrypt(json, systemConfig.getCookieSecurityKey());
            Cookie cookie = new Cookie(systemConfig.getCookieKey(), value);
            cookie.setSecure(false);
            cookie.setDomain(systemConfig.getCookieDomain());
            cookie.setPath("/");
            response.addCookie(cookie);
        }
        result.addModel("status", OrganizationStatusEnum.MATERIAL_AUDITING.code);
        return ResultHelper.renderAsJson(result);
    }

    protected String encoder(Map<String, Object> params) {
        if (params == null) {
            return null;
        }
        return JSONObject.fromObject(params).toString();
    }

    @RequestMapping("/logout")
    public ModelAndView logout(HttpServletRequest request,HttpServletResponse response) {
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

    @RequestMapping("/get_validate_code")
    @ResponseBody
    public void getValidateCode(HttpServletRequest request, HttpServletResponse response) {

        Map.Entry<String,BufferedImage> entry = ValidateCodeUtils.generate();
        String code = entry.getKey();

        Cookie cookie = new Cookie(Constants.VALIDATE_CODE_COOKIE_KEY, DESUtils.encrypt(code, Constants.HTTP_ENCRYPT_KEY));
        //cookie.setMaxAge(60 * 60);
        cookie.setPath(this.getRequestPath(request));
        response.addCookie(cookie);

        //禁止图像缓存。
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);
        response.setContentType("image/jpeg");
        try {
            ServletOutputStream output = response.getOutputStream();
            ImageIO.write(entry.getValue(),"jpeg", output);
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @RequestMapping("/error")
    public ModelAndView error(@RequestParam(value = "messages",required = false)String messages) {
        ModelAndView mav = new ModelAndView();
        mav.addObject("messages",messages);
        return mav;
    }

}
