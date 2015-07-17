package com.ixhong.dao.impl;

import com.ixhong.dao.LenderDao;
import com.ixhong.domain.pojo.LenderDO;
import com.ixhong.domain.query.LenderQuery;
import com.ixhong.domain.query.QueryResult;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by jenny on 5/11/15.
 */
public class LenderDaoImpl extends BaseDao implements LenderDao {

    @Override
    public void insert(LenderDO lender) {
        this.sqlSession.insert("LenderMapper.insert", lender);
    }

    @Override
    public LenderDO validate(String phone, String password) {
        Map<String,Object> params = new HashMap<String, Object>();
        params.put("phone",phone);
        params.put("password", password);
        return this.sqlSession.selectOne("LenderMapper.validate",params);
    }

    @Override
    public LenderDO getByTelephone(String telephone) {
        return this.sqlSession.selectOne("LenderMapper.getByTelephone",telephone);
    }

    @Override
    public LenderDO getByName(String name){
        return this.sqlSession.selectOne("LenderMapper.getByName",name);
    }

    @Override
    public LenderDO getByCardId(String cardId) {
        return this.sqlSession.selectOne("LenderMapper.getByCardId",cardId);
    }

    @Override
    public LenderDO getById(int id) {
        return this.sqlSession.selectOne("LenderMapper.getById",id);
    }

    @Override
    public void updatePassword(int id, String password) {

    }

    @Override
    public void updatePasswordByPhone(String phone, String password) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("phone", phone);
        params.put("password", password);
        this.sqlSession.update("LenderMapper.updatePasswordByPhone", params);
    }

    @Override
    public void updateStatus(Integer id, Integer status) {

    }

    @Override
    public void updateLoginTime(Integer id) {
        this.sqlSession.update("LenderMapper.updateLoginTime", id);
    }

    @Override
    public void updateCertified(LenderDO lender) {
        this.sqlSession.update("LenderMapper.updateCertified", lender);
    }

    @Override
    public QueryResult<LenderDO> query(LenderQuery query) {
        QueryResult qr = new QueryResult();
        qr.setQuery(query);
        Integer amount = this.countAll(query);
        qr.setAmount(amount);
        if(amount == 0) {
            qr.setResultList(Collections.EMPTY_LIST);
            return qr;
        }
        List<LenderDO> lenders = this.sqlSession.selectList("LenderMapper.query", query);
        qr.setResultList(lenders);
        return qr;
    }

    @Override
    public List<Integer> getLenderIds(String phone, String realName) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("phone", phone);
        params.put("realName", realName);
        return this.sqlSession.selectList("LenderMapper.getLenderIds", params);
    }

    public Integer countAll(LenderQuery query) {
        return this.sqlSession.selectOne("LenderMapper.countAll", query);
    }
}
