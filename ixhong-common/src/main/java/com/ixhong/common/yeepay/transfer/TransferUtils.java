package com.ixhong.common.yeepay.transfer;

import com.cfca.util.pki.api.CertUtil;
import com.cfca.util.pki.api.KeyUtil;
import com.cfca.util.pki.api.SignatureUtil;
import com.cfca.util.pki.cert.X509Cert;
import com.cfca.util.pki.cipher.JCrypto;
import com.cfca.util.pki.cipher.JKey;
import com.ixhong.common.utils.HttpUtils;
import com.ixhong.common.yeepay.PropertiesContainer;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by shenhongxi on 15/6/3.
 * 打款（转账），走委托结算
 */
public class TransferUtils {

    private static final String DEFAULT_ENCODING = "GBK";

    //证书
    private static final String PFX_PATH = "/home/ext/xuehaodai_private.pfx";

    private static final String PFX_PASSWORD = "hongxi890309";

    //需要参加签名的参数
    private static final String[] digest = {"cmd","mer_Id","batch_No","order_Id","amount","account_Number","page_No"};
    private static final String[] backDigest = {"cmd","ret_Code","mer_Id","batch_No","total_Amt","total_Num","r1_Code","end_Flag"};

    //打款状态码
    public static Map<String, String> transferCodes = new HashMap<String, String>();
    //银行状态码ong
    public static Map<String, String> bankCodes = new HashMap<String, String>();

    static {
        transferCodes.put("0025", "已接收");
        transferCodes.put("0026", "已汇出");
        transferCodes.put("0027", "已退款");
        transferCodes.put("0028", "已拒绝");
        transferCodes.put("0029", "待复核");
        transferCodes.put("0030", "未知");

        bankCodes.put("S", "已成功");
        bankCodes.put("I", "银行处理中");
        bankCodes.put("F", "出款失败");
        bankCodes.put("W", "未出款");
        bankCodes.put("U", "未知");
    }

    /**
     * 单笔打款
     * @param entity
     */
    public static JSONObject singleTransfer(SingleTransferEntity entity) throws Exception {
        LinkedHashMap<String, String> params = new LinkedHashMap<String, String>();
        params.put("cmd", entity.getCmd());
        params.put("mer_Id", entity.getMerchantId());
        params.put("group_Id", entity.getGroupId());
        params.put("batch_No", entity.getBatchNo());
        params.put("order_Id", entity.getOrderId());
        params.put("bank_Name", entity.getBankName());
        params.put("branch_Bank_Name", entity.getBranchBankName());
        params.put("amount", entity.getAmount().toString());
        params.put("account_Name", entity.getAccountName());
        params.put("account_Number", entity.getAccountNumber());
        params.put("province", entity.getBranchBankProvince());
        params.put("city", entity.getBranchBankCity());
        params.put("fee_Type", entity.getFeeType());
        params.put("urgency", entity.getUrgency().toString());
        params.put("version", entity.getVersion());
        return singleTransfer(params);

    }

    /**
     * 打款结果查询
     * @param entity
     * @return
     * @throws Exception
     */
    public static JSONObject transferQuery(TransferQueryEntity entity) throws Exception {
        LinkedHashMap<String, String> params = new LinkedHashMap<String, String>();
        params.put("cmd", entity.getCmd());
        params.put("version", entity.getVersion());
        params.put("group_Id", entity.getGroupId());
        params.put("mer_Id", entity.getMerchantId());
        params.put("query_Mode", entity.getQueryMode());
        params.put("order_Id", entity.getOrderId());
        params.put("batch_No", entity.getBatchNo());
        params.put("page_No", entity.getPageNo().toString());
        return transferQuery(params);
    }

    /**
     * 单笔打款
     * @param params
     * @return
     * @throws Exception
     */
    private static JSONObject singleTransfer(LinkedHashMap<String, String> params) throws Exception {
        JSONObject result = new JSONObject();

        //下面用数字证书进行签名
        String ALGORITHM = SignatureUtil.SHA1_RSA;
        //初始化加密库，获得会话session
        //多线程的应用可以共享一个session,不需要重复,只需初始化一次
        //初始化加密库并获得session。
        //系统退出后要jcrypto.finalize()，释放加密库
        JCrypto jcrypto = JCrypto.getInstance();
        jcrypto.initialize(JCrypto.JSOFT_LIB, null);
        com.cfca.util.pki.cipher.Session session = jcrypto.openSession(JCrypto.JSOFT_LIB);

        System.out.println();
        System.out.println("--------------------" + PFX_PATH + "------------------");
        System.out.println();
        JKey jkey = KeyUtil.getPriKey(PFX_PATH, PFX_PASSWORD);
        X509Cert cert = CertUtil.getCert(PFX_PATH, PFX_PASSWORD);
        System.out.println(cert.getSubject());
        X509Cert[] cs=new X509Cert[1];
        cs[0]=cert;
        String sign ="";
        SignatureUtil signUtil =null;
        // 第二步:对请求的串进行MD5对数据进行签名
        String yphs = hmacSign(buildHmac(params, digest));
        signUtil = new SignatureUtil();
        byte[] b64SignData;
        // 第三步:对MD5签名之后数据调用CFCA提供的api方法用商户自己的数字证书进行签名
        b64SignData = signUtil.p7SignMessage(true, yphs.getBytes(),ALGORITHM, jkey, cs, session);
        if(jcrypto!=null){
            jcrypto.finalize (JCrypto.JSOFT_LIB,null);
        }
        sign = new String(b64SignData, "UTF-8");
        System.out.println("经过md5和数字证书签名之后的数据为---||"+sign+"||");

        String xml = buildXml(params, sign);
        System.out.println(xml);
        String response = null;
        try {
            response = postBodyAsStream(TransferConstants.TRANSFER_URL, xml, DEFAULT_ENCODING);
        } catch (Exception e) {
            result.put("error", "调用支付系统超时，请稍后再试");
            return result;
        }

        if (StringUtils.isBlank(response)) {
            result.put("error", "支付系统繁忙或超时，请稍后再试");
            return result;
        }
        Document document = DocumentHelper.parseText(response);
        if (document == null) {
            result.put("error", "系统错误");
            return result;
        }
        Element rootElement = document.getRootElement();
        String hmac = rootElement.elementText("hmac");
        if (StringUtils.isBlank(hmac)) {
            result.put("error", "系统错误");
            return result;
        }

        //对易宝响应报文中的签名进行验证

        boolean isSignValid = signUtil.p7VerifySignMessage(hmac.getBytes(), session);
        if (!isSignValid) {
            result.put("error", "证书验签失败");
            return result;
        }

        String[] responseParams = {"cmd", "ret_Code", "order_Id", "r1_Code", "bank_Status", "error_Msg"};

        LinkedHashMap<String, String> responseMap = new LinkedHashMap<String, String>();
        for (String param : responseParams) {
            responseMap.put(param, rootElement.elementText(param));
        }

        String backHmac = buildHmac(responseMap, backDigest);

        String newMd5hmac = hmacSign(backHmac);

        String backMd5Hmac = new String(signUtil.getSignedContent());

        if (!newMd5hmac.equals(backMd5Hmac)) {
            result.put("error", "md5验签失败");
            return result;
        }
        if(signUtil.getSigerCert()[0].getSubject().toUpperCase().indexOf("OU=YEEPAY,") == -1){
            result.put("error", "证书DN不是易宝的");
            return result;
        }

        result.put("transferCode", rootElement.elementText("r1_Code"));
        result.put("bankStatus", rootElement.elementText("bank_Status"));
        result.put("returnCode", rootElement.elementText("ret_Code"));
        result.put("errorMsg", rootElement.elementText("error_Msg"));
        result.put("orderId", rootElement.elementText("order_Id"));

        return result;
    }

    /**
     * 打款结果查询
     * @param params
     * @return
     * @throws Exception
     */
    private static JSONObject transferQuery(LinkedHashMap<String, String> params) throws Exception {
        JSONObject result = new JSONObject();

        //下面用数字证书进行签名
        String ALGORITHM = SignatureUtil.SHA1_RSA;
        //初始化加密库，获得会话session
        //多线程的应用可以共享一个session,不需要重复,只需初始化一次
        //初始化加密库并获得session。
        //系统退出后要jcrypto.finalize()，释放加密库
        JCrypto jcrypto = JCrypto.getInstance();
        jcrypto.initialize(JCrypto.JSOFT_LIB, null);
        com.cfca.util.pki.cipher.Session session = jcrypto.openSession(JCrypto.JSOFT_LIB);

        JKey jkey = KeyUtils.getPriKey(PFX_PATH, PFX_PASSWORD);
        X509Cert cert = CertUtils.getCert(PFX_PATH, PFX_PASSWORD);
        X509Cert[] cs=new X509Cert[1];
        cs[0]=cert;
        String sign ="";
        SignatureUtil signUtil =null;
        // 第二步:对请求的串进行MD5对数据进行签名

        String yphs = hmacSign(buildHmac(params, new String[]{"cmd","mer_Id","batch_No","order_Id","page_No"}));
        signUtil = new SignatureUtil();
        byte[] b64SignData;
        // 第三步:对MD5签名之后数据调用CFCA提供的api方法用商户自己的数字证书进行签名
        b64SignData = signUtil.p7SignMessage(true, yphs.getBytes(),ALGORITHM, jkey, cs, session);
        if(jcrypto!=null){
            jcrypto.finalize (JCrypto.JSOFT_LIB,null);
        }
        sign = new String(b64SignData, "UTF-8");
        System.out.println("经过md5和数字证书签名之后的数据为---||"+sign+"||");

        String xml = buildXml(params, sign);

        String response = postBodyAsStream(TransferConstants.TRANSFER_URL, xml, DEFAULT_ENCODING);

        if (StringUtils.isBlank(response)) {
            result.put("error", "支付系统繁忙或超时，请稍后再试");
            return result;
        }
        Document document = DocumentHelper.parseText(response);
        if (document == null) {
            result.put("error", "系统错误");
            return result;
        }
        Element rootElement = document.getRootElement();
        String hmac = rootElement.elementText("hmac");
        if (StringUtils.isBlank(hmac)) {
            result.put("error", "系统错误");
            return result;
        }

        //对易宝响应报文中的签名进行验证

        boolean isSignValid = signUtil.p7VerifySignMessage(hmac.getBytes(), session);
        if (!isSignValid) {
            result.put("error", "证书验签失败");
            return result;
        }

        String[] responseParams = {"cmd", "ret_Code", "batch_No", "total_Num", "end_Flag", "error_msg"};
        LinkedHashMap<String, String> responseMap = new LinkedHashMap<String, String>();
        for (String param : responseParams) {
            responseMap.put(param, rootElement.elementText(param));
        }

        String backHmac = buildHmac(responseMap, new String[]{"cmd","ret_Code","batch_No","total_Num","end_Flag"});

        String newMd5hmac = hmacSign(backHmac);

        String backMd5Hmac = new String(signUtil.getSignedContent());

        if (!newMd5hmac.equals(backMd5Hmac)) {
            result.put("error", "md5验签失败");
            return result;
        }
        if(signUtil.getSigerCert()[0].getSubject().toUpperCase().indexOf("OU=YEEPAY,") == -1){
            result.put("error", "证书DN不是易宝的");
            return result;
        }

        result.put("returnCode", rootElement.elementText("ret_Code"));
        result.put("errorMsg", rootElement.elementText("error_msg"));

        //解析 items
        List<Element> items =  rootElement.element("list").element("items").elements("item");
        JSONArray array = new JSONArray();
        for (Element item : items) {
            JSONObject json = new JSONObject();
            json.put("orderId", item.elementText("order_Id"));
            json.put("transferCode", item.elementText("r1_Code"));
            json.put("bankStatus", item.elementText("bank_Status"));
            json.put("requestDate", item.elementText("request_Date"));
            json.put("payeeName", item.elementText("payee_Name"));
            json.put("payeeBankName", item.elementText("payee_BankName"));
            json.put("payeeAccountNumber", item.elementText("payee_Bank_Account"));
            json.put("amount", item.elementText("amount"));
            json.put("fee", item.elementText("fee"));
            json.put("realAmount", item.elementText("real_pay_amount"));
            json.put("note", item.elementText("note"));
            json.put("completeDate", item.elementText("complete_Date"));
            json.put("refundDate", item.elementText("refund_Date"));
            json.put("failDescription", item.elementText("fail_Desc"));
            json.put("abstractInfo", item.elementText("abstractInfo"));
            json.put("remark", item.elementText("remarksInfo"));
            array.add(json);
        }
        result.put("items", array);

        return result;
    }

    /**
     * 按顺序将相关字段进行hmac验签
     * @param map 字段值组成的 map
     * @param params 参与 hmac 验签的字段
     * @return
     */
    private static String buildHmac(LinkedHashMap<String, String> map, String[] params) {
        StringBuilder sb = new StringBuilder();
        for (String d : params) {
            if (map.containsKey(d)) {
                sb.append(map.get(d));
            }
        }
        return sb.toString() + PropertiesContainer.TRANSFER_MERCHANT_PRIVATE_KEY;
    }

    /**
     * map 转 xml 字符串
     * @param params
     * @param sign 签名
     * @return
     * @throws Exception
     */
    private static String buildXml(LinkedHashMap<String, String> params, String sign) throws Exception {
        Document document = DocumentHelper.createDocument();
        document.setXMLEncoding(DEFAULT_ENCODING);
        Element root = document.addElement("data");

        String cmd = params.get("cmd");
        root.addElement("cmd").addText(cmd);

        if (cmd.equals(TransferConstants.CMD_TRANSFER_SINGLE)) {
            root.addElement("version").addText(params.get("version"));
            root.addElement("mer_Id").addText(params.get("mer_Id"));
            root.addElement("group_Id").addText(params.get("group_Id"));
            root.addElement("batch_No").addText(params.get("batch_No"));
            root.addElement("order_Id").addText(params.get("order_Id"));
            root.addElement("bank_Code").addText("");
            root.addElement("cnaps").addText("");
            root.addElement("bank_Name").addText(params.get("bank_Name"));
            root.addElement("branch_Bank_Name").addText(params.get("branch_Bank_Name"));
            root.addElement("amount").addText(params.get("amount"));
            root.addElement("account_Name").addText(params.get("account_Name"));
            root.addElement("account_Number").addText(params.get("account_Number"));
            root.addElement("province").addText(params.get("province"));
            root.addElement("city").addText(params.get("city"));
            root.addElement("fee_Type").addText(params.get("fee_Type"));
            root.addElement("payee_Email").addText("");
            root.addElement("payee_Mobile").addText("");
            root.addElement("leave_Word").addText("");
            root.addElement("abstractInfo").addText("");
            root.addElement("remarksInfo").addText("");
            root.addElement("urgency").addText(params.get("urgency"));
        } else if (cmd.equals(TransferConstants.CMD_TRANSFER_QUERY)) {
            root.addElement("version").addText(params.get("version"));
            root.addElement("mer_Id").addText(params.get("mer_Id"));
            root.addElement("group_Id").addText(params.get("group_Id"));
            root.addElement("query_Mode").addText(params.get("query_Mode"));
            root.addElement("order_Id").addText(params.get("order_Id"));
            root.addElement("batch_No").addText(params.get("batch_No"));
            root.addElement("page_No").addText(params.get("page_No"));

        }

        root.addElement("hmac").addText(sign);

        return document.asXML();
    }

    /**
     * 直接用MD5签名对数据签名，不需要密钥
     * @param aValue
     * @return
     */
    public static String hmacSign(String aValue) throws Exception {
        byte[] input = aValue.getBytes();
        MessageDigest md = MessageDigest.getInstance("MD5");
        return toHex(md.digest(input));
    }

    /**
     * 16进制处理
     * @param input
     * @return
     */
    public static String toHex(byte input[]){
        if(input == null)
            return null;
        StringBuffer output = new StringBuffer(input.length * 2);
        for(int i = 0; i < input.length; i++){
            int current = input[i] & 0xff;
            if(current < 16)
                output.append("0");
            output.append(Integer.toString(current, 16));
        }

        return output.toString();
    }

    private static String postBodyAsStream(String url, String xml, String encoding) throws Exception{
        InputStream inputStream = new ByteArrayInputStream(xml.getBytes(encoding));
        String response = HttpUtils.postBodyAsStream(url, inputStream, encoding);
        return response;
    }

    public static void main(String[] args) throws Exception {



    }
}
