package com.ixhong.dao.impl;

import com.ixhong.dao.OrganizationAccountDao;
import com.ixhong.domain.pojo.OrganizationAccountDO;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by shenhongxi on 2015/4/13.
 */
public class OrganizationAccountDaoImpl extends BaseDao implements OrganizationAccountDao {
    @Override
    public void insert(OrganizationAccountDO account) {
        this.sqlSession.insert("OrganizationAccountMapper.insert",account);
    }

    @Override
    public OrganizationAccountDO getByOrganizationId(int organizationId) {
        return this.sqlSession.selectOne("OrganizationAccountMapper.getByOrganizationId", organizationId);
    }

    @Override
    public List<OrganizationAccountDO> getAll() {
        return this.sqlSession.selectList("OrganizationAccountMapper.getAll");
    }

    @Override
    public void updateBailPercent(Integer organizationId, float bailPercent) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("organizationId", organizationId);
        params.put("bailPercent", bailPercent);
        this.sqlSession.update("OrganizationAccountMapper.updateBailPercent", params);
    }

    @Override
    public void update(OrganizationAccountDO account) {
        this.sqlSession.update("OrganizationAccountMapper.update",account);
    }

    @Override
    public void updateBalance(OrganizationAccountDO account) {
        int record = this.sqlSession.update("OrganizationAccountMapper.updateBalance",account);
        if(record == 0) {
            throw new RuntimeException("version is modified by other!");
        }
    }
}
