package com.ixhong.dao.impl;

import com.ixhong.dao.BankDao;
import com.ixhong.domain.pojo.BankDO;

import java.util.List;

/**
 * Created by shenhongxi on 15/4/24.
 */
public class BankDaoImpl extends BaseDao implements BankDao {

    public List<BankDO> getAll() {
        return this.sqlSession.selectList("BankMapper.getAll");
    }

}
