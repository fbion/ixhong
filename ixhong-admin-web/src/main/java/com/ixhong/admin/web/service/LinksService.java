package com.ixhong.admin.web.service;

import com.ixhong.common.pojo.Result;
import com.ixhong.domain.pojo.LinksDO;

/**
 * Created by Chengrui on 2015/7/15.
 */
public interface LinksService {

    public Result list();

    public Result add(LinksDO link);

    public Result update(LinksDO link);

    public Result getById(Integer id);

    public Result delete(Integer id);
}
