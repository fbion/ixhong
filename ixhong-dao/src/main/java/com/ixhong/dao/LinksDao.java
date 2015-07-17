package com.ixhong.dao;

import com.ixhong.domain.pojo.LinksDO;

import java.util.List;

/**
 * 友情链接
 * Created by cuichengrui on 15/7/15.
 */
public interface LinksDao {

    public List<LinksDO> list();

    public void insert(LinksDO link);

    public void update(LinksDO link);

    public LinksDO getById(Integer id);

    public void delete(Integer id);
}
