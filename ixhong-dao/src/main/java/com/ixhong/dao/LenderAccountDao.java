package com.ixhong.dao;

import com.ixhong.domain.pojo.LenderAccountDO;

/**
 * Created by chenguang on 2015/5/14 0014.
 */
public interface LenderAccountDao {

    public LenderAccountDO getByLenderId(Integer lenderId);

    public void insert(Integer lenderId);

    public void update(LenderAccountDO lenderAccount);

    public void updateBalance(LenderAccountDO lenderAccount);

    public void updateDealPassword(String password, Integer lenderId);

    public LenderAccountDO validateDealPassword(Integer lenderId, String dealPassword);

}
