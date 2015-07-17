package com.ixhong.dao.impl;

import com.ixhong.dao.ArticleDao;
import com.ixhong.domain.pojo.ArticleDO;
import com.ixhong.domain.query.ArticleQuery;
import com.ixhong.domain.query.QueryResult;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by duanxiangchao on 2015/6/26.
 */
public class ArticleDaoImpl extends BaseDao implements ArticleDao{

    @Override
    public void insert(ArticleDO article) {
        this.sqlSession.insert("ArticleMapper.insert", article);
    }

    @Override
    public List<ArticleDO> query(Integer id, Integer type) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("id", id);
        params.put("type", type);
        return this.sqlSession.selectList("ArticleMapper.query", params);
    }
    @Override
    public QueryResult<ArticleDO> list(ArticleQuery articleQuery) {
        QueryResult<ArticleDO> qr = new QueryResult<ArticleDO>();
        qr.setQuery(articleQuery);
        int amount = this.countAll(articleQuery);
        qr.setAmount(amount);
        if(amount == 0) {
            qr.setResultList(Collections.EMPTY_LIST);
            return qr;
        }
        List<ArticleDO> articleDOs = this.sqlSession.selectList("ArticleMapper.list", articleQuery);
        qr.setResultList(articleDOs);
        return qr;
    }

    @Override
    public ArticleDO getById(Integer id) {
        return this.sqlSession.selectOne("ArticleMapper.getById", id);
    }

    @Override
    public void updateStatus(Integer id, int status) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("id", id);
        params.put("status", status);
        this.sqlSession.update("ArticleMapper.updateStatus",params);
    }

    @Override
    public void update(ArticleDO article) {
        this.sqlSession.update("ArticleMapper.update",article);
    }

    @Override
    public void delete(Integer id) {
        this.sqlSession.update("ArticleMapper.delete",id);
    }

    public int countAll(ArticleQuery query) {
        return this.sqlSession.selectOne("ArticleMapper.countAll", query);
    }
}
