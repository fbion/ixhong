package com.ixhong.dao;

import com.ixhong.domain.pojo.ArticleDO;
import com.ixhong.domain.query.ArticleQuery;
import com.ixhong.domain.query.QueryResult;

import java.util.List;

/**
 * Created by duanxiangchao on 2015/6/26.
 */
public interface ArticleDao {

    public void insert(ArticleDO article);

    public List<ArticleDO> query(Integer id, Integer type);

    public QueryResult<ArticleDO> list(ArticleQuery articleQuery);

    public ArticleDO getById(Integer id);

    public void updateStatus(Integer id, int status);

    public void update(ArticleDO article);

    public void delete(Integer id);

}
