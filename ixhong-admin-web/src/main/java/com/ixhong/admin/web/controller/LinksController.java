package com.ixhong.admin.web.controller;

import com.ixhong.admin.web.service.LinksService;
import com.ixhong.common.pojo.Result;
import com.ixhong.common.pojo.ResultCode;
import com.ixhong.common.utils.ResultHelper;
import com.ixhong.domain.enums.UserTypeEnum;
import com.ixhong.domain.permission.Permission;
import com.ixhong.domain.pojo.LinksDO;
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
 * 友情链接
 * Created by Chengrui on 2015/7/15.
 */
@Controller
@RequestMapping("/links")
@Permission(roles = {UserTypeEnum.ADMIN})
public class LinksController extends BaseController {

    @Autowired
    private LinksService linksService;

    @RequestMapping("/list")
    public ModelAndView list(){
        ModelAndView mav = new ModelAndView();
        Result result = this.linksService.list();
        if (!result.isSuccess()){
            mav.setViewName("redirect:/error.jhtml?messages=parameter-error");
            return mav;
        }
        mav.addAllObjects(result.getAll());
        return mav;
    }

    @RequestMapping("/add")
    public ModelAndView add(@RequestParam(value = "id",required = false)Integer id,
                            HttpServletRequest request,
                            HttpServletResponse response){
        ModelAndView mav = new ModelAndView();
        if (id == null){
            mav.addObject("token", this.createToken(request, response));
            return mav;
        }
        Result result = this.linksService.getById(id);
        mav.addAllObjects(result.getAll());
        return mav;
    }

    @RequestMapping(value = "/add_action", method = {RequestMethod.POST})
    @ResponseBody
    public String addAction(@RequestParam("token")String token,
                            @RequestParam("name")String name,
                            @RequestParam("link")String link,
                            HttpServletRequest request,
                            HttpServletResponse response){
        if(!this.getToken(request).equals(token)) {
            return ResultHelper.renderAsJson(ResultCode.VALIDATE_FAILURE, "表单过期，请重新刷新页面");
        }
        LinksDO linksDO = new LinksDO();
        linksDO.setName(name);
        linksDO.setLink(link);
        Result result = this.linksService.add(linksDO);
        if (result.isSuccess()){
            this.removeToken(request, response);
        }
        return ResultHelper.renderAsJson(result);
    }

    @RequestMapping(value = "/update_action", method = {RequestMethod.POST})
    @ResponseBody
    public String updateAction(@RequestParam("id")Integer id,
                               @RequestParam("name")String name,
                               @RequestParam("link")String link){
        Result result = this.linksService.getById(id);
        if (!result.isSuccess()){
            return ResultHelper.renderAsJson(result);
        }
        LinksDO linksDO = (LinksDO)result.getModel("link");
        linksDO.setName(name);
        linksDO.setLink(link);
        result = this.linksService.update(linksDO);
        return ResultHelper.renderAsJson(result);
    }

    @RequestMapping(value = "/delete",method = {RequestMethod.POST})
    @ResponseBody
    public String delete(@RequestParam("id")Integer id){
        Result result = this.linksService.delete(id);
        return ResultHelper.renderAsJson(result);
    }

}
