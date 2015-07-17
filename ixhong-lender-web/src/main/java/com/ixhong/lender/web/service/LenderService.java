package com.ixhong.lender.web.service;

import com.ixhong.common.pojo.Result;
import com.ixhong.domain.pojo.LenderAccountDO;
import com.ixhong.domain.pojo.LenderDO;
import com.ixhong.domain.query.LenderBillStageQuery;


/**
 * Created by jenny on 5/11/15.
 */
public interface LenderService {

    /**
     * 投资人注册
     * @param lender
     * @param code
     * @return
     */
    public Result register(LenderDO lender, String code);

    /**
     * 更新密码
     * @param phone
     * @param password
     * @return
     */
    public Result updatePassword(String phone, String oldPassword, String password);


    /**
     * 查询手机号是否已经注册
     * @param phone
     * @return
     */
    public Result isPhoneExisted(String phone);


    /**
     * 通过手机号找回密码
     * @param phone
     * @param password
     * @return
     */
    public Result resetPassword(String phone, String password);



    /**
     * 投资人登录
     * @param phone
     * @param password
     * @return
     */
    public Result login(String phone, String password);


    /**
     * 发送手机验证码
     * @param phone
     * @return
     */
    public Result registerSendPhoneCode(String phone);

    /**
     * 提现时发送手机验证码
     * @return
     */
    public Result withdrawSendPhoneCode();


    /**
     * 验证短信验证码是否正确，找回密码时使用
     * @param phone
     * @param code
     * @return
     */
    public Result checkPhoneCode(String phone, String code, String type);


    /**
     * 发送找回密码验证码
     * @param phone
     * @param type
     * @return
     */
    public Result sendPhoneCode(String phone, String cardId, String type);


    /**
     * 查看投资人的的身份证号码是否已存在
     * @param cardId
     * @return
     */
    public Result isCardIdExisted(String cardId);


    public Result isNameExisted(String name);


    public Result getById(Integer id);

    /**
     * 获取省市
     * @param pid
     * @return
     */
    public Result getCities(String pid);

    /**
     * 获取银行卡详细信息
     * @return
     */
    public Result lenderAccountDetail();

    /**
     * 添加银行卡
     * @param lenderAccount
     * @return
     */
    public Result addBankCard(LenderAccountDO lenderAccount, String phoneCode, String requestId);

    /**
     * 根据投资人的ID获取投资人详情信息
     * @param id
     * @return
     */
    public Result getDetailById(Integer id);

    /**
     * 实名认证
     * @param realName
     * @param cardId
     * @return
     */
    public Result certify(String realName, String cardId);

    /**
     * 返回lenderAccount、lender的信息
     * @return
     */
    public Result bankCardList();

    /**
     * 是否实名认证
     * @return
     */
    public Result isCertify();

    /**
     * 易宝支付投资通绑卡请求
     * @param bankCardId
     * @param phone
     * @param clientIp
     * @return
     */
    public Result bindBankCard(String bankCardId, String phone, String clientIp);

    /**
     * 充值
     * @param
     * @param recharge
     * @return
     */
    public Result recharge(double recharge, String clientIp, String dealPassword);


    /**
     * 获取理财人账户信息详情
     * @return
     */
    public Result accountDetail();

    /**
     * 理财人提现
     * @param withdraw
     * @param clientIp
     * @param dealPassword
     * @return
     */
    Result withdraw(Double withdraw, String clientIp, String dealPassword, String phoneCode);

    /**
     * 充值回调
     * 易宝异步通知商户支付请求传过来的callback_url地址,每2秒通知一次，共通知3次。
     * 商户收到通知后需要回写，建议内容为”success”，否则会一直通知多次。
     * @param data
     * @param encryptKey
     * @return
     */
    public Result rechargeCallback(String data, String encryptKey);

    /**
     * 重置交易密码
     * @param phone
     * @param password
     * @return
     */
    public Result resetDealPassword(String phone, String password);


    /**
     * 收款计划
     * @param query
     * @return
     */
    public Result billStageList(LenderBillStageQuery query);


}
