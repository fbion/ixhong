package com.ixhong.manager.impl;

import com.ixhong.dao.ArticleDao;
import com.ixhong.domain.pojo.ArticleDO;
import com.ixhong.domain.query.ArticleQuery;
import com.ixhong.domain.query.QueryResult;
import com.ixhong.manager.ArticleManager;

import java.util.List;

/**
 * Created by duanxiangchao on 2015/6/26.
 */
public class ArticleManagerImpl implements ArticleManager{

    private ArticleDao articleDao;

    public void setArticleDao(ArticleDao articleDao) {
        this.articleDao = articleDao;
    }

    @Override
    public void insert(ArticleDO article) {
        this.articleDao.insert(article);
    }

    @Override
    public List<ArticleDO> query(Integer id,Integer type){
        return articleDao.query(id,type);
    }

    @Override
    public QueryResult<ArticleDO> list(ArticleQuery articleQuery) {
        return articleDao.list(articleQuery);
    }

    @Override
    public ArticleDO getById(Integer id) {
        return articleDao.getById(id);
    }

    @Override
    public void updateStatus(Integer id, int status) {
        articleDao.updateStatus(id,status);
    }

    @Override
    public void update(ArticleDO article) {
        articleDao.update(article);
    }

    @Override
    public void delete(Integer id) {
        this.articleDao.delete(id);
    }
}
