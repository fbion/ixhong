package com.ixhong.admin.web.service;

import com.ixhong.common.pojo.Result;
import com.ixhong.domain.pojo.ArticleDO;
import com.ixhong.domain.query.ArticleQuery;

/**
 * Created by gongzaifei on 15/6/29.
 */
public interface ArticleService {
     /**
      * 添加媒体公告信息
      * @param article
      * @return
      */
     public Result  add(ArticleDO article);

     /**
      * 公告媒体列表查询
      * @param articleQuery
      * @return
      */

     public Result list(ArticleQuery articleQuery);

     /**
      * 根据ID获取文章详情
      * @param id
      * @return
      */
     public Result getById(int id);

     /**
      * 更新文章是否可用
      * @param id
      * @param status
      */
     public Result updateStatus(Integer id, int status);

     /**
      * 更新媒体公告信息
      * @param article
      */
     public Result update(ArticleDO article);
     /**
      * 删除文章
      * @param id
      * @return
      */
     public Result delete(Integer id);

}
