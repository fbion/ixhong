package com.ixhong.dao.impl;

import com.ixhong.dao.CourseDao;
import com.ixhong.domain.pojo.CourseDO;
import com.ixhong.domain.query.CourseQuery;
import com.ixhong.domain.query.QueryResult;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by cuichengrui on 15/4/10.
 */
public class CourseDaoImpl extends BaseDao implements CourseDao {

    @Override
    public void insert(CourseDO course) {
        this.sqlSession.insert("CourseMapper.insert",course);
    }


    @Override
    public CourseDO getById(Integer id) {
        Map<String,Object> params = new HashMap<String, Object>();
        params.put("id",id);
        return this.sqlSession.selectOne("CourseMapper.getById", params);
    }

    @Override
    public void disable(Integer id) {
        Map<String,Object> params = new HashMap<String, Object>();
        params.put("id",id);
        this.sqlSession.update("CourseMapper.disable", params);
    }

    @Override
    public void audit(CourseDO course) {
        this.sqlSession.update("CourseMapper.update", course);
    }

    @Override
    public QueryResult<CourseDO> query(Integer organizationId,CourseQuery query) {

        QueryResult<CourseDO> qr = new QueryResult<CourseDO>();
        qr.setQuery(query);

        Map<String,Object> params = query.build();
        params.put("organizationId",organizationId);
        int amount = this.countAll(params);
        qr.setAmount(amount);
        if(amount == 0) {
            qr.setResultList(Collections.EMPTY_LIST);
            return qr;
        }

        List<CourseDO> courses =  this.sqlSession.selectList("CourseMapper.query", params);
        qr.setResultList(courses);
        return qr;
    }

    /**
     * 总条数
     * @param params
     * @return
     */
    private int countAll(Map<String,Object> params) {
        return this.sqlSession.selectOne("CourseMapper.countAll", params);
    }

    @Override
    public CourseDO getByName(Integer organizationId,String name) {
        Map<String,Object> params = new HashMap<String, Object>();
        params.put("organizationId",organizationId);
        params.put("name", name);
        return this.sqlSession.selectOne("CourseMapper.getByName",params);
    }
}
