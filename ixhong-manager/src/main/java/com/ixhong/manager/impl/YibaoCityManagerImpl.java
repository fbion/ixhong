package com.ixhong.manager.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.ixhong.dao.YibaoCityDao;
import com.ixhong.domain.pojo.YibaoCityDO;
import com.ixhong.manager.YibaoCityManager;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * Created by duanxiangchao on 2015/4/14.
 */
public class YibaoCityManagerImpl implements YibaoCityManager{


    private YibaoCityDao yibaoCityDao;

    private static Cache<String,List<YibaoCityDO>> cache = CacheBuilder.newBuilder().maximumSize(2048).expireAfterAccess(15, TimeUnit.MINUTES).build();

    public void setYibaoCityDao(YibaoCityDao yibaoCityDao) {
        this.yibaoCityDao = yibaoCityDao;
    }



    @Override
    public List<YibaoCityDO> getByPid(final String pid) {
        try {
            //cache.get方法首先从Cache中查询key是否存在，如果存在则返回对应的value。
            //如果不存在，则调用Callable.call方法。并把call方法返回的结果保存在cache中。
            return cache.get(pid, new Callable<List<YibaoCityDO>>() {
                @Override
                public List<YibaoCityDO> call() throws Exception {
                    return load(pid);
                }
            });
        }catch (Exception e) {
            //如果数据库不存在此值，将会返回一个空的集合
            return Collections.EMPTY_LIST;
        }
    }

    /**
     * 如果后台DAO返回结果为null，需要抛出异常。因为guava cache中不能存储null值。
     *
     * Callable的返回值将会被保存在cache中，然后返回给调用方法。
     * 如果抛出异常，异常将会通过cache.get方法抛出。
     * @param pid
     * @return
     * @throws Exception
     */
    protected List<YibaoCityDO> load(final String pid) throws Exception{
        List<YibaoCityDO> result = this.yibaoCityDao.getByPid(pid);
        if(result == null) {
            throw new NullPointerException("pid not existed");
        }
        return result;
    }

}
