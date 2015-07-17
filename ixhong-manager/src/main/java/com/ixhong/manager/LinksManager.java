package com.ixhong.manager;

import com.ixhong.domain.pojo.LinksDO;

import java.util.List;

/**
 * Created by cuichengrui on 15/7/15.
 */
public interface LinksManager {

    /**
     * 查询友情链接列表
     * @param useCache 是否使用缓存机制（true是，false否）
     * @return
     */
    public List<LinksDO> list(boolean useCache);

    public void insert(LinksDO link);

    public void update(LinksDO link);

    public LinksDO getById(Integer id);

    public void delete(Integer id);

}
