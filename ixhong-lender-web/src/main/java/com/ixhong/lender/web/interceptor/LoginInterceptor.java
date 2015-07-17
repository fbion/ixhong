package com.ixhong.lender.web.interceptor;

import com.ixhong.common.utils.DESUtils;
import com.ixhong.domain.misc.LoginContext;
import com.ixhong.domain.misc.LoginContextHolder;
import com.ixhong.domain.misc.SystemConfig;
import com.ixhong.domain.pojo.LenderAccountDO;
import com.ixhong.domain.pojo.LenderDO;
import com.ixhong.domain.pojo.LinksDO;
import com.ixhong.domain.pojo.UserDO;
import com.ixhong.manager.LenderAccountManager;
import com.ixhong.manager.LenderManager;
import com.ixhong.manager.LinksManager;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;

/**
 * Author: qing
 * Date: 14-10-29
 * 用于控制接口是否为需要“登陆”，如果需要登陆，则检测登陆信息是否完备。
 */
public class LoginInterceptor implements HandlerInterceptor{

    protected static final Logger logger = Logger.getLogger(LoginInterceptor.class);

    private SystemConfig systemConfig;

    public void setSystemConfig(SystemConfig systemConfig) {
        this.systemConfig = systemConfig;
    }

    private LenderManager lenderManager;

    public void setLenderManager(LenderManager lenderManager) {
        this.lenderManager = lenderManager;
    }

    private LinksManager linksManager;

    public void setLinksManager(LinksManager linksManager){
        this.linksManager = linksManager;
    }

    private LenderAccountManager lenderAccountManager;

    public void setLenderAccountManager(LenderAccountManager lenderAccountManager) {
        this.lenderAccountManager = lenderAccountManager;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Cookie[] cookies = request.getCookies();
        if(cookies == null) {
            return true;
        }
        //解析cookie
        String cookieKey = systemConfig.getCookieKey();
        for(Cookie cookie : cookies) {
            String key = cookie.getName();
            String content = cookie.getValue();
            if(key.equalsIgnoreCase(cookieKey)) {
               if(StringUtils.isNotBlank(content)) {
                   String source = DESUtils.decrypt(content, systemConfig.getCookieSecurityKey());
                   UserDO user = this.decoder(source);
                   LoginContext context = new LoginContext();
                   context.setUser(user);
                   LoginContextHolder.set(context);
               }
            }
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        if(modelAndView != null) {
            UserDO user = LoginContextHolder.getLoginUser();
            if(user != null) {
                LenderDO lender = lenderManager.getById(user.getId());
                if(lender != null ){
                    LenderAccountDO lenderAccount = lenderAccountManager.getByLenderId(user.getId());
                    modelAndView.addObject("isCertified", lender.getCertified() == 1 ? true : false);
                    modelAndView.addObject("isDealPassword", StringUtils.isNotBlank(lenderAccount.getDealPassword()) ? true : false);
                    modelAndView.addObject("isBankCardId", StringUtils.isNotBlank(lenderAccount.getBankCardId()) ? true : false);
                }
            }
            modelAndView.addObject("loginUser", user);
            //友情链接
            List<LinksDO> links = this.linksManager.list(true);
            modelAndView.addObject("links", links);
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        LoginContextHolder.clear();
    }


    protected UserDO decoder(String content){
        if(StringUtils.isBlank(content)){
            return null;
        }
        JSONObject json = JSONObject.fromObject(content);
        if(json.isNullObject()){
            return null;
        }
        UserDO user = new UserDO();
        user.setId(json.getInt("id"));

        user.setName(json.getString("name"));
        user.setType(json.getInt("type"));
        if(json.has("phone")){
            user.setPhone(json.getString("phone"));
        }
        if(json.has("loginTime")) {
            user.setLoginTime(new Date(json.getLong("loginTime")));
        }
        return user;
    }


}
