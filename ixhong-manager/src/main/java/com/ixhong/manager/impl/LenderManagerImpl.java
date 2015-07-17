package com.ixhong.manager.impl;

import com.ixhong.dao.LenderDao;
import com.ixhong.domain.pojo.LenderDO;
import com.ixhong.domain.query.LenderQuery;
import com.ixhong.domain.query.QueryResult;
import com.ixhong.manager.LenderManager;

import java.util.List;

/**
 * Created by jenny on 5/11/15.
 */
public class LenderManagerImpl implements LenderManager {

    private LenderDao lenderDao;

    public void setLenderDao(LenderDao lenderDao) {
        this.lenderDao = lenderDao;
    }

    @Override
    public void insert(LenderDO lender) {
        this.lenderDao.insert(lender);
    }

    @Override
    public LenderDO validate(String phone, String password) {
        return this.lenderDao.validate(phone, password);
    }

    @Override
    public LenderDO getByName(String name) {
        return this.lenderDao.getByName(name);
    }

    @Override
    public LenderDO getById(int id) {
        return this.lenderDao.getById(id);
    }

    @Override
    public void updatePassword(int id, String password) {

    }

    @Override
    public void updatePasswordByPhone(String phone, String password) {
        lenderDao.updatePasswordByPhone(phone, password);
    }

    @Override
    public LenderDO getByTelephone(String phone) {
        return lenderDao.getByTelephone(phone);
    }

    @Override
    public LenderDO getByCardId(String cardId) {
        return lenderDao.getByCardId(cardId);
    }

    @Override
    public void updateLoginTime(Integer id) {
        this.lenderDao.updateLoginTime(id);
    }

    @Override
    public void updateCertified(LenderDO lender) {
        lenderDao.updateCertified(lender);
    }

    @Override
    public QueryResult<LenderDO> query(LenderQuery query) {
        return lenderDao.query(query);
    }

    @Override
    public List<Integer> getLenderIds(String phone, String realName) {
        return lenderDao.getLenderIds(phone, realName);
    }

}
