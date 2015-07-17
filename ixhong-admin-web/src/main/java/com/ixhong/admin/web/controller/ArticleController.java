package com.ixhong.admin.web.controller;
import com.ixhong.admin.web.service.ArticleService;
import com.ixhong.admin.web.service.UploadService;
import com.ixhong.common.pojo.Result;
import com.ixhong.common.pojo.ResultCode;
import com.ixhong.common.utils.ResultHelper;
import com.ixhong.domain.enums.ArticleTypeEnum;
import com.ixhong.domain.pojo.ArticleDO;
import com.ixhong.domain.query.ArticleQuery;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;

/**
 * Created by gongzaifei on 15/6/29.
 */
@Controller
@RequestMapping("/article")
public class ArticleController extends BaseController {

    private Logger logger = Logger.getLogger(ArticleController.class);

    @Autowired
    private UploadService uploadService;

    @Autowired
    private ArticleService articleService;

    @RequestMapping("/add_media")
    public ModelAndView media(HttpServletRequest request,
                              HttpServletResponse response){
        ModelAndView mav = new ModelAndView();
        mav.addObject("token",this.createToken(request,response));
        mav.addObject("type",ArticleTypeEnum.MEDIA.code);
        return mav;
    }

    //添加最新公告页
    @RequestMapping("/add_notice")
    public ModelAndView addNotice( HttpServletRequest request,
                                   HttpServletResponse response){
        ModelAndView mav = new ModelAndView();
        mav.addObject("token", this.createToken(request, response));
        mav.addObject("type",ArticleTypeEnum.NOTICE.code);
        return mav;
    }

    @RequestMapping("/add_action")
    public ModelAndView addAction(@RequestParam(value = "title",required = true) String title,
                                  @RequestParam(value = "content",required = true) String content,
                                  @RequestParam(value = "summary",required = false) String summary,
                                  @RequestParam(value = "token",required = true)String token,
                                  @RequestParam(value = "type",required = true)int type,
                                  HttpServletRequest request,
                                  @RequestParam(value = "file",required = false) MultipartFile file){
        ModelAndView mav = new ModelAndView();
        String sourceToken  =  this.getToken(request);
        if(!sourceToken.equals(token)){
            if(type == ArticleTypeEnum.MEDIA.code){
                mav.setViewName("redirect:/article/media_list.jhtml");
                return  mav;
            }else{
                mav.setViewName("redirect:/article/notice_list.jhtml");
                return  mav;
            }
        }
        Result result = null;
        ArticleDO article = new ArticleDO();
        if(file != null){
            InputStream inputStream = null;
            try {
                inputStream = file.getInputStream();
                result = this.uploadService.uploadImage(inputStream);
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("imgUpload error"+e);
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                        logger.error("inputStream close error"+e);
                    }
                }
            }

            if(result != null && result.isSuccess()){
                article.setImage(result.getModel("url").toString());
            }
        }
        article.setTitle(title);
        article.setContent(content);
        article.setSummary(summary);
        article.setType(ArticleTypeEnum.NOTICE.code);
        if(type == ArticleTypeEnum.MEDIA.code){
            article.setType(ArticleTypeEnum.MEDIA.code);
        }
        Result re=  articleService.add(article);
        if(re.isSuccess()){
            if(type == ArticleTypeEnum.MEDIA.code){
                mav.setViewName("redirect:/article/media_list.jhtml");
                return  mav;
            }else{
                mav.setViewName("redirect:/article/notice_list.jhtml");
                return  mav;
            }
        }else{
            mav.setViewName("redirect:/error.jhtml?messages="+re.getMessage());
            return mav;
        }

    }

    @RequestMapping("/update_action")
    public ModelAndView updateAction(@RequestParam(value = "title",required = false)String title,
                                     @RequestParam(value = "summary",required = false)String summary,
                                     @RequestParam(value = "content",required = false)String content,
                                     @RequestParam(value = "imgUrl",required = false)String imgUrl,
                                     @RequestParam(value = "id",required = true)Integer id,
                                     @RequestParam(value = "token",required = true)String token,
                                     @RequestParam(value = "type",required = true) int type,
                                     HttpServletRequest request,
                                     @RequestParam(value = "file",required = false)MultipartFile file){

        ModelAndView mav = new ModelAndView();
        String sourceToken = this.getToken(request);
        if(!sourceToken.equals(token)){
            if(type == ArticleTypeEnum.MEDIA.code){
                mav.setViewName("redirect:/article/media_list.jhtml");
                return  mav;
            }else{
                mav.setViewName("redirect:/article/notice_list.jhtml");
                return  mav;
            }
        }
        Result result = articleService.getById(id);
        if(result.isSuccess()){
            ArticleDO  article = (ArticleDO)result.getModel("article");
            if(file != null ){
                Result re = null;
                InputStream inputStream = null;
                try {
                    inputStream = file.getInputStream();
                    re = this.uploadService.uploadImage(inputStream);
                } catch (Exception e) {
                    e.printStackTrace();
                    logger.error("imgUpload error"+e);
                } finally {
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                            logger.error("inputStream close error"+e);
                        }
                    }
                }
                if(re.isSuccess()){
                    article.setImage(re.getModel("url").toString());
                }
            }
            if(StringUtils.isNotBlank(title)){
                article.setTitle(title);
            }
            if(StringUtils.isNotBlank(summary)){
                article.setSummary(summary);
            }
            if(StringUtils.isNotBlank(content)){
                article.setContent(content);
            }
            Result result1 =  articleService.update(article);
            if(result1.isSuccess()){
                if(type == ArticleTypeEnum.MEDIA.code){
                    mav.setViewName("redirect:/article/media_list.jhtml");
                    return  mav;
                }else{
                    mav.setViewName("redirect:/article/notice_list.jhtml");
                    return  mav;
                }
            }else{
                mav.setViewName("redirect:/error.jhtml?messages="+result1.getMessage());
                return mav;
            }
        }else{
            mav.setViewName("redirect:/error.jhtml?messages="+result.getMessage());
            return mav;
        }

    }

    @RequestMapping("/media_list")
    public ModelAndView mediaList(@RequestParam(value = "begin_date",required = false)String beginDate,
                                  @RequestParam(value = "end_date",required = false)String endDate,
                                  @RequestParam(value = "title",required = false)String title,
                                  @RequestParam(value = "page",required = false ,defaultValue = "1")Integer page){
        ModelAndView mav = new ModelAndView();
        ArticleQuery query  = new ArticleQuery();
        query.setCurrentPage(page);
        query.setType(ArticleTypeEnum.MEDIA.code);
        if(StringUtils.isNotBlank(beginDate)){
            query.setBeginDate(beginDate);
        }
        if(StringUtils.isNotBlank(endDate)){
            query.setEndDate(endDate);
        }
        if(StringUtils.isNotBlank(title)){
            query.setTitle(title);
        }
        try{
            Result result =  articleService.list(query);
            mav.addAllObjects(result.getAll());
        }catch(Exception e){
            mav.setViewName("redirect:/error.jhtml?messages=parameter-error");
            return mav;
        }
        return mav;
    }
    @RequestMapping("/media_detail")
    public ModelAndView mediaDetail(@RequestParam(value = "id",required = true) Integer id,
                                    HttpServletRequest request,
                                    HttpServletResponse response){
        ModelAndView mav = new ModelAndView();
        Result result = articleService.getById(id);
        mav.addAllObjects(result.getAll());
        mav.addObject("token",this.createToken(request,response));
        return mav;
    }

    //公告列表
    @RequestMapping("/notice_list")
    public ModelAndView noticeList(@RequestParam(value = "begin_date",required = false)String beginDate,
                                   @RequestParam(value = "end_date",required = false)String endDate,
                                   @RequestParam(value = "title",required = false)String title,
                                   @RequestParam(value = "page",required = false ,defaultValue = "1")Integer page){
        ModelAndView mav = new ModelAndView();
        ArticleQuery query  = new ArticleQuery();
        query.setCurrentPage(page);
        query.setType(ArticleTypeEnum.NOTICE.code);
        if(StringUtils.isNotBlank(beginDate)){
            query.setBeginDate(beginDate);
        }
        if(StringUtils.isNotBlank(endDate)){
            query.setEndDate(endDate);
        }
        if(StringUtils.isNotBlank(title)){
            query.setTitle(title);
        }
        try{
            Result result =  articleService.list(query);
            mav.addAllObjects(result.getAll());
        }catch(Exception e){
            mav.setViewName("redirect:/error.jhtml?messages=paramter-error");
            return mav;
        }
        return mav;
    }



    @RequestMapping("/notice_detail")
    public ModelAndView noticeDetail(@RequestParam(value = "id")int id,
                                     HttpServletRequest request,
                                     HttpServletResponse response){
        ModelAndView mav = new ModelAndView();
        Result result = articleService.getById(id);
        mav.addAllObjects(result.getAll());
        mav.addObject("token",this.createToken(request,response));
        return mav;
    }


    @RequestMapping(value = "/delete_action",method = {RequestMethod.POST})
    @ResponseBody
    public String deleteAction(@RequestParam(value = "id")int id,
                               @RequestParam(value = "type")int type){
        Result result = this.articleService.delete(id);
        result.setResponseUrl("/article/notice_list.jhtml");
        if(type == ArticleTypeEnum.MEDIA.code){
            result.setResponseUrl("/article/media_list.jhtml");
        }else{
            result.setResponseUrl("/article/notice_list.jhtml");
        }
        return ResultHelper.renderAsJson(result);
    }

    @RequestMapping(value = "/update_status_action",method = {RequestMethod.POST})
    @ResponseBody
    public String updateStatusAction(@RequestParam(value = "id")int id,
                                     @RequestParam(value = "status")int status,
                                     @RequestParam(value = "type")int type){
        if(status != 0 && status != 1){
            return ResultHelper.renderAsJson(ResultCode.PARAMETER_ERROR,"参数错误");
        }
        Result result = this.articleService.updateStatus(id, status);
        result.setResponseUrl("/article/notice_list.jhtml");
        if(type == ArticleTypeEnum.MEDIA.code){
            result.setResponseUrl("/article/media_list.jhtml");
        }else{
            result.setResponseUrl("/article/notice_list.jhtml");
        }
        return ResultHelper.renderAsJson(result);
    }
    @RequestMapping("/article")
    public ModelAndView article(){
        ModelAndView  mav = new ModelAndView();
        return mav;
    }

}
