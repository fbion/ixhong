package com.ixhong.manager;

import com.ixhong.domain.pojo.ArticleDO;
import com.ixhong.domain.query.ArticleQuery;
import com.ixhong.domain.query.QueryResult;

import java.util.List;

/**
 * Created by duanxiangchao on 2015/6/26.
 */
public interface ArticleManager {

    /**
     *  添加媒体公告
     * @param article
     */

    public void insert(ArticleDO article);

    /**
     * 不带分页查询默认10条
     * @param id
     * @param type
     * @return
     */
    public List<ArticleDO> query(Integer id, Integer type);

    /**
     * 管理员后台查询带分页
     * @param articleQuery
     * @return
     */

    public QueryResult<ArticleDO> list(ArticleQuery articleQuery);

    /**
     * 根据ID获取媒体公告信息
     * @param id
     * @return
     */
    public ArticleDO getById(Integer id);

    /**
     * 根据ID修改媒体公告状态
     * @param id
     * @param status
     */
    public void updateStatus(Integer id, int status);


    /**
     * 更新媒体公告信息
     * @param article
     */
    public void update(ArticleDO article);


    public void delete(Integer id);

}
