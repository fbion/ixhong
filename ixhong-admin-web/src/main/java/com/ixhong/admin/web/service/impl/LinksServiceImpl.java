package com.ixhong.admin.web.service.impl;

import com.ixhong.admin.web.service.LinksService;
import com.ixhong.common.pojo.Result;
import com.ixhong.domain.pojo.LinksDO;
import com.ixhong.manager.LinksManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by Chengrui on 2015/7/15.
 */
@Service
public class LinksServiceImpl implements LinksService {

    private Logger logger = Logger.getLogger(LinksServiceImpl.class);

    @Autowired
    private LinksManager linksManager;

    @Override
    public Result list() {
        Result result = new Result();
        try {
            List<LinksDO> links = this.linksManager.list(false);
            result.addModel("links", links);
            result.setSuccess(true);
        } catch (Exception e) {
            logger.error("query links error.", e);
        }
        return result;
    }

    @Override
    public Result add(LinksDO link) {
        Result result = new Result();
        try {
            this.linksManager.insert(link);
            result.setSuccess(true);
        } catch (Exception e) {
            logger.error("insert link error.", e);
            result.setMessage("添加失败，请稍后再试");
        }
        return result;
    }

    @Override
    public Result getById(Integer id) {
        Result result = new Result();
        try {
            LinksDO link = this.linksManager.getById(id);
            result.addModel("link", link);
            result.setSuccess(true);
        } catch (Exception e) {
            logger.error("query link detail error.", e);
        }
        return result;
    }

    @Override
    public Result update(LinksDO link) {
        Result result = new Result();
        try {
            this.linksManager.update(link);
            result.setSuccess(true);
        } catch (Exception e) {
            logger.error("update link error.", e);
            result.setMessage("更新失败，请稍后再试");
        }
        return result;
    }

    @Override
    public Result delete(Integer id) {
        Result result = new Result();
        try {
            this.linksManager.delete(id);
            result.setSuccess(true);
        } catch (Exception e) {
            logger.error("delete link error.", e);
            result.setMessage("删除失败，请稍后再试");
        }
        return result;
    }
}
