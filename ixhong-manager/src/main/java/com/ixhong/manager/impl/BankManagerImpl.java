package com.ixhong.manager.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.ixhong.dao.BankDao;
import com.ixhong.domain.pojo.BankDO;
import com.ixhong.manager.BankManager;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * Created by shenhongxi on 15/4/24.
 */
public class BankManagerImpl implements BankManager {
    private BankDao bankDao;

    private static Cache<String,List<BankDO>> cache = CacheBuilder.newBuilder().maximumSize(2).expireAfterAccess(15, TimeUnit.MINUTES).build();

    public void setBankDao(BankDao bankDao) {
        this.bankDao = bankDao;
    }


    @Override
    public List<BankDO> getAll() {
        try {
            return cache.get("CACHE_ALL_BANK", new Callable<List<BankDO>>() {
                @Override
                public List<BankDO> call() throws Exception {
                    return load();
                }
            });
        }catch (Exception e) {
            //如果数据库不存在此值，将会返回一个空的集合
            return Collections.EMPTY_LIST;
        }

    }

    private List<BankDO> load() {
        return this.bankDao.getAll();
    }

}
