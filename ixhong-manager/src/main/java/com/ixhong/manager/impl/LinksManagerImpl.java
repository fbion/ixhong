package com.ixhong.manager.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.ixhong.dao.LinksDao;
import com.ixhong.domain.pojo.LinksDO;
import com.ixhong.manager.LinksManager;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * Created by Chengrui on 2015/7/15.
 */
public class LinksManagerImpl implements LinksManager {

    private LinksDao linksDao;

    public void setLinksDao(LinksDao linksDao) {
         this.linksDao = linksDao;
    }

    private static Cache<String,List<LinksDO>> cache = CacheBuilder.newBuilder().maximumSize(1).expireAfterWrite(10, TimeUnit.MINUTES).build();

    @Override
    public List<LinksDO> list(boolean useCache) {
        if (!useCache){
            return this.load();
        }
        try {
            return cache.get("CACHE_LINKS", new Callable<List<LinksDO>>() {
                @Override
                public List<LinksDO> call() throws Exception {
                    return load();
                }
            });
        }catch (Exception e) {
            //如果数据库不存在此值，将会返回一个空的集合
            return Collections.EMPTY_LIST;
        }
    }

    private List<LinksDO> load(){
        return this.linksDao.list();
    }

    @Override
    public void insert(LinksDO link) {
        this.linksDao.insert(link);
    }

    @Override
    public void update(LinksDO link) {
        this.linksDao.update(link);
    }

    @Override
    public LinksDO getById(Integer id) {
        return  this.linksDao.getById(id);
    }

    @Override
    public void delete(Integer id) {
        this.linksDao.delete(id);
    }
}
