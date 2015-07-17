package com.ixhong.admin.web.controller;

import com.ixhong.domain.enums.FeeEnum;
import com.ixhong.domain.enums.UserTypeEnum;
import com.ixhong.domain.permission.Permission;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * Created by chenguang on 2015/4/22 0022.
 */
@Controller
@RequestMapping("/fee")
@Permission(roles = {UserTypeEnum.ADMIN})
public class FeeController extends BaseController {

    @RequestMapping("/list")
    public ModelAndView list1() {
        ModelAndView mav = new ModelAndView();
        mav.addObject("fees", FeeEnum.values());
        return mav;
    }
}
