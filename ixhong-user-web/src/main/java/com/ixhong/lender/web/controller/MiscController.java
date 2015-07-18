package com.ixhong.lender.web.controller;

import com.ixhong.common.pojo.Result;
import com.ixhong.lender.web.service.MiscService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

/**
 * Created by jenny on 5/20/15.
 */
@Controller
@RequestMapping("/misc")
public class MiscController {

    @Autowired
    private MiscService miscService;

    /**
     * 注册免责协议
     * @return
     */
    @RequestMapping("/protocol")
    public ModelAndView protocol() {
        ModelAndView mav = new ModelAndView();
        return mav;
    }


    /**
     * 安全保障
     * @return
     */
    @RequestMapping("/guarantee")
    public ModelAndView guarantee() {
        ModelAndView mav = new ModelAndView();
        mav.addObject("guarantee_flag", 1);
        return mav;
    }


    /**
     * 新手指南
     * @return
     */
    @RequestMapping("/guide")
    public ModelAndView guide() {
        ModelAndView mav = new ModelAndView();
        mav.addObject("guide_flag", 1);
        return mav;
    }


    /**
     * 投资人帮助中心
     * @return
     */
    @RequestMapping("lender_help")
    public ModelAndView lenderHelp() {
        ModelAndView mav = new ModelAndView();
        mav.addObject("lender_help", 1);
        return mav;
    }


    /**
     * 账户管理
     * @return
     */
    @RequestMapping("account_help")
    public ModelAndView accountHelp() {
        ModelAndView mav = new ModelAndView();
        mav.addObject("account_help", 1);
        return mav;
    }


    /**
     * 关于我们
     * @return
     */
    @RequestMapping("/about")
    public ModelAndView about() {
        ModelAndView mav = new ModelAndView();
        return mav;
    }


    /**
     * 团队介绍
     * @return
     */
    @RequestMapping("/team")
    public ModelAndView team() {
        ModelAndView mav = new ModelAndView();
        return mav;
    }

    /**
     * 最新公告 列表
     * @return
     */
    @RequestMapping("/notices")
    public ModelAndView notices() {
        ModelAndView mav = new ModelAndView();
        Result result = miscService.noticeList();
        mav.addAllObjects(result.getAll());
        return mav;
    }


    /**
     * 最新公告 详情
     * @param noticeId
     * @return
     */
    @RequestMapping("/notice_detail")
    public ModelAndView noticeDetail(@RequestParam("notice_id")Integer noticeId) {
        ModelAndView mav = new ModelAndView();
        Result result = miscService.noticeDetail(noticeId);
        mav.addAllObjects(result.getAll());
        return mav;
    }


    /**
     * 媒体报道 列表
     * @return
     */
    @RequestMapping("reports")
    public ModelAndView reports() {
        ModelAndView mav = new ModelAndView();
        Result result = miscService.reportList();
        mav.addAllObjects(result.getAll());
        return mav;
    }


    /**
     * 媒体报道 详情
     * @param reportId
     * @return
     */
    @RequestMapping("report_detail")
    public ModelAndView reportDetail(@RequestParam("report_id")Integer reportId) {
        ModelAndView mav = new ModelAndView();
        Result result = miscService.reportDetail(reportId);
        mav.addAllObjects(result.getAll());
        return mav;
    }


    /**
     * 法律政策
     * @return
     */
    @RequestMapping("/law")
    public ModelAndView law() {
        ModelAndView mav = new ModelAndView();
        return mav;
    }

}
