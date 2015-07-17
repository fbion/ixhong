package com.ixhong.admin.web.service;

import com.ixhong.common.pojo.Result;
import com.ixhong.domain.query.BiddingQuery;
import com.ixhong.domain.query.StudentBillStageQuery;

/**
 * Created by cuichengrui on 2015/4/22.
 */
public interface StudentService {
    /**
     * 通过条件查询学生借款审批列表
     * @param query 查询对象
     * @return
     */
    public Result list(BiddingQuery query);

    /**
     * 管理后台学生借款审批--发标审
     * @param id 学生id
     * @param status 审批状态
     * @param auditNote 审批备注（审批留言）
     * @return
     */
    public Result auditBidding(Integer id, Integer status, String auditNote);

    /**
     * 管理后台学生借款审批--满标审
     * @param id 学生id
     * @param status 审批状态
     * @param auditNote 审批备注（审批留言）
     * @return
     */
    public Result auditFullBidding(Integer id, Integer status, String auditNote);

    /**
     * 管理后台查看学生详情
     * @param biddingId 标的id
     * @param studentId 学生id
     * @return
     */
    public Result detail(Integer biddingId, Integer studentId);

    public Result getById(Integer studentId);

    /**
     * 实名认证
     * @param name
     * @param cardId
     * @param photoUrl
     * @return
     */
    public Result certify(Integer id, String name, String cardId, String photoUrl);

    /**
     * 学好贷后台查看学生还款单
     * @param query
     * @return
     */
    public Result billStageList(StudentBillStageQuery query);

    /**
     * 学生下载合同时得到合同详情
     * @param biddingId 标的id
     * @return
     */
    public Result contractDetail(Integer biddingId);

    /**
     * 推迟标的流标时间三天
     * @param id
     * @return
     */
    public Result biddingInvalidDelay(Integer id);

}
