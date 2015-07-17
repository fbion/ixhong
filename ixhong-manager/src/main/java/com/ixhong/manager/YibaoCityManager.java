package com.ixhong.manager;

import com.ixhong.domain.pojo.YibaoCityDO;

import java.util.List;

/**
 * Created by duanxiangchao on 2015/4/14.
 */
public interface YibaoCityManager {

    public List<YibaoCityDO> getByPid(String pid);

}
