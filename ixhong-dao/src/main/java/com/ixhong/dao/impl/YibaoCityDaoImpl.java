package com.ixhong.dao.impl;

import com.ixhong.dao.YibaoCityDao;
import com.ixhong.domain.pojo.YibaoCityDO;

import java.util.List;

/**
 * Created by duanxiangchao on 2015/4/14.
 */
public class YibaoCityDaoImpl extends BaseDao implements YibaoCityDao{
    @Override
    public List<YibaoCityDO> getByPid(String pid) {
        return this.sqlSession.selectList("YibaoCityMapper.getByPid",pid);
    }

}
