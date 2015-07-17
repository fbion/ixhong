package com.ixhong.common.yeepay.tzt;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.ixhong.common.utils.AESUtils;
import com.ixhong.common.utils.HttpUtils;
import com.ixhong.common.yeepay.PropertiesContainer;
import net.sf.json.JSONObject;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;

import java.util.*;

/**
 * Created by shenhongxi on 15/5/27.
 */
public class TZTUtils {

    /**
     * 绑卡请求
     * @param requestEntity
     * @return
     * @throws Exception
     */
    public static JSONObject bindBankCardRequest(BindBankCardRequestEntity requestEntity) throws Exception {
        TreeMap<String, Object> params	= new TreeMap<String, Object>();
        params.put("merchantaccount", requestEntity.getMerchantId());
        params.put("requestid", requestEntity.getRequestId());
        params.put("identitytype", requestEntity.getIdentityType());
        params.put("identityid", requestEntity.getIdentityId());
        params.put("cardno", requestEntity.getBankCardId());
        params.put("idcardtype", requestEntity.getIdCardType());
        params.put("idcardno", requestEntity.getCardId());
        params.put("username", requestEntity.getRealName());
        params.put("phone", requestEntity.getPhone());
        params.put("userip", requestEntity.getClientIp());

        return yeepayRequest(params, TZTConstants.BIND_BANK_CARD_URL);
    }

    /**
     * 确认绑卡
     * @param requestId
     * @param validateCode
     * @return
     * @throws Exception
     */
    public static JSONObject bindBankCard(String requestId, String validateCode) throws Exception {
        TreeMap<String, Object> params	= new TreeMap<String, Object>();
        params.put("merchantaccount", PropertiesContainer.TZT_MERCHANT_ID);
        params.put("requestid", requestId);
        params.put("validatecode", validateCode);

        return yeepayRequest(params, TZTConstants.CONFIRM_BIND_BANK_CARD_URL);
    }

    /**
     * 直连？绑定支付
     * @param requestEntity
     * @return
     * @throws Exception
     */
    public static JSONObject directBindPay(PayRequestEntity requestEntity) throws Exception {
        TreeMap<String, Object> params	= new TreeMap<String, Object>();
        params.put("merchantaccount", requestEntity.getMerchantId());
        params.put("orderid", requestEntity.getOrderId());
        params.put("transtime", requestEntity.getTransactionTime());
        params.put("amount", requestEntity.getAmount());
        params.put("productname", requestEntity.getProductName());
        params.put("identityid", requestEntity.getIdentityId());
        params.put("identitytype", requestEntity.getIdentityType());
        params.put("card_top", requestEntity.getCardTop());
        params.put("card_last", requestEntity.getCardLast());
        params.put("callbackurl", requestEntity.getCallbackUrl());
        params.put("userip", requestEntity.getClientIp());

        return yeepayRequest(params, TZTConstants.DIRECT_BIND_PAY_URL);
    }

    /**
     * 解密支付回调参数
     * @param dataFromYeepay
     * @param encryptKeyFromYeepay
     * @return
     * @throws Exception
     */
    public static JSONObject decryptCallbackData(String dataFromYeepay, String encryptKeyFromYeepay) throws Exception {
        JSONObject result = new JSONObject();
        boolean signMatch = checkDecryptAndSign(dataFromYeepay, encryptKeyFromYeepay,
                PropertiesContainer.YEEPAY_PUBLIC_KEY, PropertiesContainer.TZT_MERCHANT_PRIVATE_KEY);
        if(!signMatch) {
            result.put("clientSignError", "client sign does not match");
            return result;
        }

        String yeepayAESKey	= RSAUtils.decrypt(encryptKeyFromYeepay, PropertiesContainer.TZT_MERCHANT_PRIVATE_KEY);
        String decryptData = com.ixhong.common.yeepay.tzt.AESUtils.decryptFromBase64(dataFromYeepay, yeepayAESKey);

        return JSONObject.fromObject(decryptData);
    }

    /**
     * 提现
     * @param requestEntity
     * @return
     * @throws Exception
     */
    public static JSONObject withdraw(WithdrawRequestEntity requestEntity) throws Exception {
        TreeMap<String, Object> params	= new TreeMap<String, Object>();
        params.put("merchantaccount", requestEntity.getMerchantId());
        params.put("requestid", requestEntity.getRequestId());
        params.put("identityid", requestEntity.getIdentityId());
        params.put("identitytype", requestEntity.getIdentityType());
        params.put("card_top", requestEntity.getCardTop());
        params.put("card_last", requestEntity.getCardLast());
        params.put("amount", requestEntity.getAmount());
        params.put("drawtype", requestEntity.getDrawType());
        params.put("userip", requestEntity.getClientIp());

        return yeepayRequest(params, TZTConstants.WITHDRAW_URL);
    }

    /**
     * 支付订单查询
     * @param orderId
     * @param yeepayOrderId
     * @return
     * @throws Exception
     */
    public static JSONObject queryOrder(String orderId, String yeepayOrderId) throws Exception {
        TreeMap<String, Object> params	= new TreeMap<String, Object>();
        params.put("merchantaccount", PropertiesContainer.TZT_MERCHANT_ID);
        if (orderId != null) {
            params.put("orderid", orderId);
        }
        if (yeepayOrderId != null) {
            params.put("yborderid", yeepayOrderId);
        }

        return yeepayGetRequest(params, TZTConstants.PAYMENT_QUERY_URL);
    }

    public static JSONObject queryOrder(String orderId) throws Exception {
        return queryOrder(orderId, null);
    }

    /**
     * 提现查询
     * @param requestId
     * @param yeepayDrawFlowId
     * @return
     * @throws Exception
     */
    public static JSONObject queryWithdraw(String requestId, String yeepayDrawFlowId) throws Exception {
        TreeMap<String, Object> params	= new TreeMap<String, Object>();
        params.put("merchantaccount", PropertiesContainer.TZT_MERCHANT_ID);
        if (requestId != null) {
            params.put("requestid", requestId);
        }
        if (yeepayDrawFlowId != null) {
            params.put("ybdrawflowid", yeepayDrawFlowId);
        }

        return yeepayGetRequest(params, TZTConstants.QUERY_WITHDRAW_URL);
    }

    public static JSONObject queryWithdraw(String requestId) throws Exception {
        return queryWithdraw(requestId, null);
    }

    /**
     * 银行卡列表查询
     * @param identityId
     * @param identityType
     * @return
     * @throws Exception
     */
    public static JSONObject bankCardList(String identityId, int identityType) throws Exception{
        TreeMap<String, Object> params	= new TreeMap<String, Object>();
        params.put("merchantaccount", PropertiesContainer.TZT_MERCHANT_ID);
        params.put("identityid", identityId);
        params.put("identitytype", identityType);

        return yeepayGetRequest(params, TZTConstants.QUERY_AUTH_BIND_LIST_URL);
    }

    /**
     * 银行卡信息查询
     * @param bankCardId
     * @return
     * @throws Exception
     */
    public static JSONObject bankCardDetail(String bankCardId) throws Exception {
        TreeMap<String, Object> params	= new TreeMap<String, Object>();
        params.put("merchantaccount", PropertiesContainer.TZT_MERCHANT_ID);
        params.put("cardno", bankCardId);

        return yeepayRequest(params, TZTConstants.BANK_CARD_CHECK_URL);
    }

    public static JSONObject unbindBankCard(String cardTop, String cardLast, String identityId) throws Exception {
        TreeMap<String, Object> params	= new TreeMap<String, Object>();
        params.put("merchantaccount", PropertiesContainer.TZT_MERCHANT_ID);
        params.put("card_top", cardTop);
        params.put("card_last", cardLast);
        params.put("identityid", identityId);
        params.put("identitytype", 2);

        return yeepayRequest(params, TZTConstants.UNBIND_BANK_CARD_URL);
    }

    public static JSONObject directRefund(RefundEntity entity) throws Exception {
        TreeMap<String, Object> params	= new TreeMap<String, Object>();
        params.put("amount", entity.getAmount());
        params.put("currency", entity.getCurrency());
        params.put("cause", entity.getCause());
        params.put("merchantaccount", entity.getMerchantId());
        params.put("orderid", entity.getOrderId());
        params.put("origyborderid", entity.getYeepayOrderId());

        return yeepayRequest(params, TZTConstants.DIRECT_REFUND_URL);
    }

    public static JSONObject queryRefund(String yeepayOrderId) throws Exception {
        TreeMap<String, Object> params	= new TreeMap<String, Object>();
        params.put("merchantaccount", PropertiesContainer.TZT_MERCHANT_ID);
        params.put("yborderid", yeepayOrderId);

        return yeepayGetRequest(params, TZTConstants.QUERY_REFUND_URL);
    }

    /**
     * POST请求
     * @param params
     * @param url
     * @return
     * @throws Exception
     */
    private static JSONObject yeepayRequest(TreeMap<String, Object> params, String url) throws Exception {
        JSONObject result = new JSONObject();

        String sign = handleRSA(params, PropertiesContainer.TZT_MERCHANT_PRIVATE_KEY);
        params.put("sign", sign);

        String json = simpleMap2Json(params);
        String merchantAESKey = RandomStringUtils.random(16, true, true);
        String data = AESUtils.encryptToBase64(json, merchantAESKey);
        String encryptKey = RSAUtils.encrypt(merchantAESKey, PropertiesContainer.YEEPAY_PUBLIC_KEY);
        List<NameValuePair> pairs = new ArrayList<NameValuePair>();
        pairs.add(new BasicNameValuePair("merchantaccount", params.get("merchantaccount").toString()));
        pairs.add(new BasicNameValuePair("data", data));
        pairs.add(new BasicNameValuePair("encryptkey", encryptKey));

        String response = HttpUtils.httpPost(url, null, new UrlEncodedFormEntity(pairs, "UTF-8"));

        JSONObject responseJson = JSONObject.fromObject(response);
        if(responseJson.containsKey("error_code")) {
            return responseJson;
        }

        String dataFromYeepay = responseJson.getString("data");
        String encryptKeyFromYeepay = responseJson.getString("encryptkey");
        boolean signMatch = checkDecryptAndSign(dataFromYeepay, encryptKeyFromYeepay, PropertiesContainer.YEEPAY_PUBLIC_KEY, PropertiesContainer.TZT_MERCHANT_PRIVATE_KEY);
        if(!signMatch) {
            result.put("clientSignError", "client sign does not match");
            return result;
        }

        String yeepayAESKey	= RSAUtils.decrypt(encryptKeyFromYeepay, PropertiesContainer.TZT_MERCHANT_PRIVATE_KEY);
        String decryptData = com.ixhong.common.yeepay.tzt.AESUtils.decryptFromBase64(dataFromYeepay, yeepayAESKey);

        return JSONObject.fromObject(decryptData);
    }

    /**
     * GET请求
     * @param params
     * @param url
     * @return
     * @throws Exception
     */
    private static JSONObject yeepayGetRequest(TreeMap<String, Object> params, String url) throws Exception {
        JSONObject result = new JSONObject();

        String sign = handleRSA(params, PropertiesContainer.TZT_MERCHANT_PRIVATE_KEY);
        params.put("sign", sign);

        String json = simpleMap2Json(params);
        String merchantAESKey = RandomStringUtils.random(16, true, true);
        String data = AESUtils.encryptToBase64(json, merchantAESKey);
        String encryptKey = RSAUtils.encrypt(merchantAESKey, PropertiesContainer.YEEPAY_PUBLIC_KEY);

        Map<String, String> _params = new HashMap<String, String>();
        _params.put("merchantaccount", params.get("merchantaccount").toString());
        _params.put("data", data);
        _params.put("encryptkey", encryptKey);

        String response = HttpUtils.httpGet(url, _params);

        JSONObject responseJson = JSONObject.fromObject(response);
        if(responseJson.containsKey("error_code")) {
            return responseJson;
        }

        String dataFromYeepay = responseJson.getString("data");
        String encryptKeyFromYeepay = responseJson.getString("encryptkey");
        boolean signMatch = checkDecryptAndSign(dataFromYeepay, encryptKeyFromYeepay,
                 PropertiesContainer.YEEPAY_PUBLIC_KEY, PropertiesContainer.TZT_MERCHANT_PRIVATE_KEY);
        if(!signMatch) {
            result.put("clientSignError", "client sign does not match");
            return result;
        }

        String yeepayAESKey	= RSAUtils.decrypt(encryptKeyFromYeepay, PropertiesContainer.TZT_MERCHANT_PRIVATE_KEY);
        String decryptData = com.ixhong.common.yeepay.tzt.AESUtils.decryptFromBase64(dataFromYeepay, yeepayAESKey);

        return JSONObject.fromObject(decryptData);
    }

    /**
     * 简单 map 对象转成 json 字符串
     * @param map
     * @return
     */
    private static String simpleMap2Json(Map<String, Object> map) {
        JSONObject json = new JSONObject();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            json.put(entry.getKey(), entry.getValue());
        }
        return json.toString();
    }

    /**
     * 生成RSA签名
     */
    public static String handleRSA(TreeMap<String, Object> map, String privateKey) {
        StringBuffer buf = new StringBuffer();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            buf.append(entry.getValue());
        }
        String _sign = buf.toString();

        String sign = "";
        if (StringUtils.isNotEmpty(privateKey)) {
            sign = RSAUtils.sign(_sign, privateKey);
        }
        return sign;
    }

    /**
     * 对易宝支付返回的结果进行验签
     *
     * @param data
     *            易宝支付返回的业务数据密文
     * @param encryptKey
     *            易宝支付返回的对ybAesKey加密后的密文
     * @param yeepayPublicKey
     *            易宝支付提供的公钥
     * @param merchantPrivateKey
     *            商户自己的私钥
     * @return 验签是否通过
     * @throws Exception
     */
    public static boolean checkDecryptAndSign(String data, String encryptKey,
                                              String yeepayPublicKey, String merchantPrivateKey) throws Exception {

        /** 1.使用YBprivatekey解开aesEncrypt。 */
        String AESKey = "";
        try {
            AESKey = RSAUtils.decrypt(encryptKey, merchantPrivateKey);
        } catch (Exception e) {
            /** AES密钥解密失败 */
            e.printStackTrace();
            return false;
        }

        /** 2.用aeskey解开data。取得data明文 */
        String realData = com.ixhong.common.yeepay.tzt.AESUtils.decryptFromBase64(data, AESKey);

        TreeMap<String, String> map = JSON.parseObject(realData,
                new TypeReference<TreeMap<String, String>>() {});

        /** 3.取得data明文sign。 */
        String sign = StringUtils.trimToEmpty(map.get("sign"));

        /** 4.对map中的值进行验证 */
        StringBuffer signData = new StringBuffer();
        Iterator<Map.Entry<String, String>> iter = map.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String, String> entry = iter.next();

            /** 把sign参数隔过去 */
            if (StringUtils.equals((String) entry.getKey(), "sign")) {
                continue;
            }
            signData.append(entry.getValue() == null ? "" : entry.getValue());
        }

        /** 5. result为true时表明验签通过 */
        boolean result = RSAUtils.checkSign(signData.toString(), sign, yeepayPublicKey);

        return result;
    }

}
