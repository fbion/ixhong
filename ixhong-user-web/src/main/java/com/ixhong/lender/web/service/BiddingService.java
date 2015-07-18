package com.ixhong.lender.web.service;

import com.ixhong.common.pojo.Result;
import com.ixhong.domain.query.BiddingQuery;

/**
 * Created by jenny on 5/14/15.
 */
public interface BiddingService {

    /**
     * 获取最新五条发标审通过的标
     * @return
     */
    public Result list();

    /**
     * 根据bidding 的Id的获取其详情
     * @param biddingId
     * @return
     */
    public Result detail(Integer biddingId);


    /**
     * 分页查询
     * @param query
     * @return
     */
    public Result query(BiddingQuery query);

    /**
     * 投资人投资
     * @param money
     * @param biddingId
     * @param dealPassword
     * @return
     */
    public Result bidding(Double money, Integer biddingId, String dealPassword);
}
