package com.ixhong.dao;

import com.ixhong.domain.pojo.YibaoCityDO;

import java.util.List;

/**
 * Created by duanxiangchao on 2015/4/14.
 */
public interface YibaoCityDao {

    public List<YibaoCityDO> getByPid(String pid);

}
