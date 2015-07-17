package com.ixhong.admin.web.service.impl;

import com.ixhong.admin.web.service.ArticleService;
import com.ixhong.common.pojo.Result;
import com.ixhong.common.pojo.ResultCode;
import com.ixhong.domain.pojo.ArticleDO;
import com.ixhong.domain.query.ArticleQuery;
import com.ixhong.domain.query.QueryResult;
import com.ixhong.manager.ArticleManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by gongzaifei on 15/6/29.
 */
@Service
public class ArticleServiceImpl implements ArticleService {

    private Logger logger = Logger.getLogger(ArticleServiceImpl.class);
    @Autowired
    private ArticleManager articleManager;
    @Override
    public Result add(ArticleDO article) {
        Result result = new Result();
        result.setSuccess(true);
        try{
            articleManager.insert(article);
        }catch (Exception e){
            result.setSuccess(false);
            result.setMessage("添加失败");
            logger.error("insert Article error" + e);
        }
        return result;

    }

    @Override
    public Result list(ArticleQuery  articleQuery) {
        Result result = new Result();
        try {
            QueryResult<ArticleDO> qr = articleManager.list(articleQuery);
            result.addModel("query",articleQuery);
            result.addModel("queryResult", qr);
            result.addModel("articles", qr.getResultList());
            result.setSuccess(true);
        } catch (Exception e) {
            logger.error("query article  error", e);
            result.setResultCode(ResultCode.SYSTEM_ERROR);
        }
        return result;

    }

    @Override
    public Result getById(int id) {
        Result result = new Result();
        try{
            ArticleDO article =  articleManager.getById(id);
            if(article != null){
                result.setSuccess(true);
                result.addModel("article",article);
            }
        }catch (Exception e){
            logger.error("article detail error"+e);
            result.setMessage("查询失败");
        }
        return result;
    }

    @Override
    public Result updateStatus(Integer id, int status) {
        Result result = new Result();
        try{
            this.articleManager.updateStatus(id, status);
            if(status == 1){
                result.setMessage("启用成功！");
            }else{
                result.setMessage("停用成功！");
            }
            result.setSuccess(true);
        }catch (Exception e){
            result.setSuccess(false);
            result.setMessage("更新状态失败");
            logger.error("Article update status error" + e);
        }
        return result;
    }

    @Override
    public Result update(ArticleDO article){
        Result result = new Result();
        try{
            articleManager.update(article);
            result.setSuccess(true);
        }catch (Exception e){
           logger.error("Article update error" + e);
            result.setMessage("更新失败");
        }
        return result;
    }

    @Override
    public Result delete(Integer id) {
        Result result = new Result();
        try{
            articleManager.delete(id);
            result.setMessage("删除成功！");
            result.setSuccess(true);
        }catch (Exception e){
            result.setSuccess(false);
            result.setMessage("删除文章失败");
            logger.error("Article delete error" + e);
        }
        return result;
    }
}
