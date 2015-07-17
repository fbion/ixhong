package com.ixhong.manager;

import com.ixhong.domain.pojo.LenderDO;
import com.ixhong.domain.query.LenderQuery;
import com.ixhong.domain.query.QueryResult;

import java.util.List;

/**
 * Created by jenny on 5/11/15.
 */
public interface LenderManager {

    public void insert(LenderDO lender);

    public LenderDO validate(String phone, String password);

    public LenderDO getByName(String name);

    public LenderDO getById(int id);

    public void updatePassword(int id, String password);

    public void updatePasswordByPhone(String phone, String password);

    public LenderDO getByTelephone(String phone);

    public LenderDO getByCardId(String cardId);

    public void updateLoginTime(Integer id);

    public void updateCertified(LenderDO lender);

    public QueryResult<LenderDO> query(LenderQuery query);

    public List<Integer> getLenderIds(String phone, String realName);
}
