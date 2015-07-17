package com.ixhong.dao.impl;

import com.ixhong.dao.LenderAccountDao;
import com.ixhong.domain.pojo.LenderAccountDO;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by chenguang on 2015/5/14 0014.
 */
public class LenderAccountDaoImpl extends BaseDao implements LenderAccountDao {

    @Override
    public LenderAccountDO getByLenderId(Integer lenderId) {
        return this.sqlSession.selectOne("LenderAccountMapper.getByLenderId", lenderId);
    }

    @Override
    public void insert(Integer lenderId) {

        this.sqlSession.insert("LenderAccountMapper.insert", lenderId);
    }

    @Override
    public void update(LenderAccountDO lenderAccount) {
        this.sqlSession.update("LenderAccountMapper.update", lenderAccount);
    }

    @Override
    public void updateBalance(LenderAccountDO lenderAccount) {
        int record = this.sqlSession.update("LenderAccountMapper.updateBalance",lenderAccount);
        if(record == 0) {
            throw  new RuntimeException("version is modified by other!");
        }
    }

    @Override
    public void updateDealPassword(String password, Integer lenderId) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("password", password);
        params.put("lenderId", lenderId);
        this.sqlSession.update("LenderAccountMapper.updateDealPassword", params);
    }

    @Override
    public LenderAccountDO validateDealPassword(Integer lenderId, String dealPassword) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("dealPassword", dealPassword);
        params.put("lenderId", lenderId);
        return this.sqlSession.selectOne("LenderAccountMapper.validateDealPassword", params);
    }

}
