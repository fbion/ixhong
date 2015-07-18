package com.ixhong.lender.web.controller;


import com.ixhong.common.pojo.Result;
import com.ixhong.common.utils.DESUtils;
import com.ixhong.common.utils.ValidateCodeUtils;
import com.ixhong.domain.constants.Constants;
import com.ixhong.domain.enums.BiddingItemEnum;
import com.ixhong.lender.web.service.BiddingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
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
import java.util.Map;


/**
 * Created by jenny on 15/5/13.
 */
@Controller
public class IndexController extends BaseController{

    @Autowired
    private BiddingService biddingService;

    @RequestMapping("/index")
    public ModelAndView index(){
        ModelAndView mav = new ModelAndView();
        Result result = this.biddingService.list();
        mav.addObject("biddingList",result.getModel("biddingList"));
        mav.addObject("basicMap",result.getModel("basicMap"));
        mav.addObject("ageMap", result.getModel("ageMap"));
        mav.addObject("salaryMap", result.getModel("salaryMap"));
        mav.addObject("titleMap", result.getModel("titleMap"));
        mav.addObject("article", result.getModel("article"));
        mav.addObject("biddingItemEnum", BiddingItemEnum.getMap());
        mav.addObject("index_flag",1);
        return mav;
    }

    @RequestMapping("/error")
    public ModelAndView error(@RequestParam(value = "messages",required = false)String messages) {
        ModelAndView mav = new ModelAndView();
        mav.addObject("messages",messages);
        return mav;
    }



    @RequestMapping("/get_validate_code")
    @ResponseBody
    public void getValidateCode(HttpServletRequest request, HttpServletResponse response) {

        Map.Entry<String, BufferedImage> entry = ValidateCodeUtils.generate();
        String code = entry.getKey();

        Cookie cookie = new Cookie(Constants.VALIDATE_CODE_COOKIE_KEY, DESUtils.encrypt(code, Constants.HTTP_ENCRYPT_KEY));
        cookie.setMaxAge(60 * 60);
        cookie.setPath(this.getRequestPath(request));
        response.addCookie(cookie);

        //禁止图像缓存。
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);
        response.setContentType("image/jpeg");
        try {
            ServletOutputStream output = response.getOutputStream();
            ImageIO.write(entry.getValue(), "jpeg", output);
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
