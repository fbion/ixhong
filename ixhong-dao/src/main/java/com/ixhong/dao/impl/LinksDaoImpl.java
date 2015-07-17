package com.ixhong.dao.impl;

import com.ixhong.dao.LinksDao;
import com.ixhong.domain.pojo.LinksDO;

import java.util.List;

/**
 * Created by Chengrui on 2015/7/15.
 */
public class LinksDaoImpl extends BaseDao implements LinksDao {

    @Override
    public List<LinksDO> list() {
        return this.sqlSession.selectList("LinksMapper.list");
    }

    @Override
    public void insert(LinksDO link) {
        this.sqlSession.insert("LinksMapper.insert", link);
    }

    @Override
    public void update(LinksDO link) {
        this.sqlSession.update("LinksMapper.update", link);
    }

    @Override
    public LinksDO getById(Integer id) {
        return this.sqlSession.selectOne("LinksMapper.getById", id);
    }

    @Override
    public void delete(Integer id) {
        this.sqlSession.delete("LinksMapper.delete", id);
    }
}
