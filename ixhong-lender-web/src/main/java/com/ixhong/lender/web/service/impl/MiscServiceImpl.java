package com.ixhong.lender.web.service.impl;

import com.ixhong.common.pojo.Result;
import com.ixhong.common.pojo.ResultCode;
import com.ixhong.domain.enums.ArticleTypeEnum;
import com.ixhong.domain.pojo.ArticleDO;
import com.ixhong.lender.web.service.MiscService;
import com.ixhong.manager.ArticleManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by chenguang on 2015/6/26 0026.
 */

@Service
public class MiscServiceImpl implements MiscService {

    private Logger logger = Logger.getLogger(MiscService.class);

    @Autowired
    private ArticleManager articleManager;

    @Override
    public Result noticeList() {
        Result result = new Result();
        try {
            List<ArticleDO> articles = articleManager.query(null, ArticleTypeEnum.NOTICE.code);
            result.addModel("articles", articles);
            result.setSuccess(true);
        }catch (Exception e) {
            logger.error("notice list error", e);
            result.setResultCode(ResultCode.SYSTEM_ERROR);
            result.setMessage("系统错误");
        }
        return result;
    }

    @Override
    public Result noticeDetail(Integer noticeId) {
        Result result = new Result();
        try {
            List<ArticleDO> articles = articleManager.query(noticeId, ArticleTypeEnum.NOTICE.code);
            result.addModel("article", articles.get(0));
            result.setSuccess(true);
        }catch (Exception e) {
            logger.error("notice detail error", e);
            result.setResultCode(ResultCode.SYSTEM_ERROR);
            result.setMessage("系统错误");
        }
        return result;
    }

    @Override
    public Result reportList() {
        Result result = new Result();
        try {
            List<ArticleDO> articles = articleManager.query(null, ArticleTypeEnum.MEDIA.code);
            result.addModel("articles", articles);
            result.setSuccess(true);
        }catch (Exception e) {
            logger.error("report list error", e);
            result.setResultCode(ResultCode.SYSTEM_ERROR);
            result.setMessage("系统错误");
        }
        return result;
    }

    @Override
    public Result reportDetail(Integer reportId) {
        Result result = new Result();
        try {
            List<ArticleDO> articles = articleManager.query(reportId, ArticleTypeEnum.MEDIA.code);
            result.addModel("article", articles.get(0));
            result.setSuccess(true);
        }catch (Exception e) {
            logger.error("report list error", e);
            result.setResultCode(ResultCode.SYSTEM_ERROR);
            result.setMessage("系统错误");
        }
        return result;
    }
}
