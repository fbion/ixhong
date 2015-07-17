package com.ixhong.common.yeepay;

import com.ixhong.common.utils.HttpUtils;
import org.apache.commons.lang.StringUtils;

import java.io.BufferedReader;
import java.io.StringReader;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by cuichengrui on 2015/5/11.
 */
public class YeepayUtils {

    private static final String CHARSET_GBK = "GBK";

    private static final String CHARSET_UTF8 = "UTF-8";
    /**
     * 根据参数生成hmac
     * @param requestEntry
     * @return
     */
    public static String getHmacForRecharge(YeepayRequestEntity requestEntry) {
        StringBuffer sb = new StringBuffer();
        // 业务类型
        sb.append(requestEntry.getP0Cmd())
                .append(requestEntry.getP1MerId())
                .append(requestEntry.getP2Order())
                .append(requestEntry.getP3Amt())
                .append(requestEntry.getP4Cur())
                .append(requestEntry.getP5Pid())
                .append(requestEntry.getP6Pcat())
                .append(requestEntry.getP7Pdesc())
                .append(requestEntry.getP8Url())
                .append(requestEntry.getP9SAF())
                .append(requestEntry.getPaMP())
                .append(requestEntry.getPdFrpId())
                .append(requestEntry.getPrNeedResponse());

        return hmacSign(sb.toString(), requestEntry.getKeyValue());
    }



    /**
     * 订单查询请求参数
     * 该方法是根据《易宝支付产品通用接口（HTML版）文档 v3.0》怎样查询订单进行的封装
     * 具体参数含义请仔细阅读《易宝支付产品通用接口（HTML版）文档 v3.0》
     * 商户订单号
     * @param orderId
     * @return queryResult
     */
    public static YeepayResult queryByOrderId(String orderId) {

        String queryCmd = "QueryOrdDetail";
        String joined = StringUtils.join(new String[] {queryCmd,PropertiesContainer.B2C_MERCHANT_ID,orderId});
        String hmac = hmacSign(joined, PropertiesContainer.B2C_MERCHANT_PRIVATE_KEY);

        Map<String,String> params = new HashMap<String,String>();
        params.put("p0_Cmd", queryCmd);
        params.put("p1_MerId", PropertiesContainer.B2C_MERCHANT_ID);
        params.put("p2_Order", orderId);
        params.put("hmac", hmac);

        try {
            String response = HttpUtils.httpGet(YeepayConstants.QUERY_REFUND_REQUEST_URL,params, CHARSET_GBK);
            if(StringUtils.isBlank(response)) {
                throw new NullPointerException("yeepay response empty!");
            }
            BufferedReader reader = new BufferedReader(new StringReader(response));
            YeepayResult qr = new YeepayResult();
            while (true) {
                String line = reader.readLine();
                if(line == null) {
                    break;
                }
                int index = line.indexOf("=");
                String key = line.substring(0, index);
                String value = line.substring(index + 1);

                if (key.equals("r0_Cmd")) {
                    qr.setR0Cmd(value);
                } else if (key.equals("r1_Code")) {
                    qr.setR1Code(value);
                } else if (key.equals("r2_TrxId")) {
                    qr.setR2TrxId(value);
                } else if (key.equals("r3_Amt")) {
                    qr.setR3Amt(value);
                } else if (key.equals("r4_Cur")) {
                    qr.setR4Cur(value);
                } else if (key.equals("r5_Pid")) {
                    qr.setR5Pid(value);
                } else if (key.equals("r6_Order")) {
                    qr.setR6Order(value);
                } else if (key.equals("r8_MP")) {
                    qr.setR8MP(value);
                } else if (key.equals("rb_PayStatus")) {
                    qr.setRbPayStatus(value);
                } else if (key.equals("rc_RefundCount")) {
                    qr.setRcRefundCount(value);
                } else if (key.equals("rd_RefundAmt")) {
                    qr.setRdRefundAmt(value);
                } else if (key.equals("hmac")) {
                    qr.setHmac(value);
                }

            }
            reader.close();

            joined = StringUtils.join(new String[] { qr.getR0Cmd(), qr.getR1Code(), qr.getR2TrxId(),
                    qr.getR3Amt(), qr.getR4Cur(), qr.getR5Pid(), qr.getR6Order(), qr.getR8MP(),
                    qr.getRbPayStatus(), qr.getRcRefundCount(),qr.getRdRefundAmt()});
            String currentMac = hmacSign(joined, PropertiesContainer.B2C_MERCHANT_PRIVATE_KEY);
            if (!currentMac.equals(qr.getHmac())) {
                throw new RuntimeException("Hmac error.");
            }
            return qr;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }


    /**
     * 商户编号
     * @param p1MerId
     * 业务类型
     * @param r0Cmd
     * 支付结果
     * @param r1Code
     * 易宝支付交易流水号
     * @param r2TrxId
     * 支付金额
     * @param r3Amt
     * 交易币种
     * @param r4Cur
     * 商品名称
     * @param r5Pid
     * 商户订单号
     * @param r6Order
     * 易宝支付会员ID
     * @param r7Uid
     * 商户扩展信息
     * @param r8MP
     * 交易结果返回类型
     * @param r9BType
     * @return
     */
    public static String getHmacForCallback(String p1MerId,
                                             String r0Cmd, String r1Code, String r2TrxId, String r3Amt,
                                             String r4Cur, String r5Pid, String r6Order, String r7Uid,
                                             String r8MP, String r9BType) {
        StringBuffer sb = new StringBuffer();

        sb.append(p1MerId)
        .append(r0Cmd)
        .append(r1Code)
        .append(r2TrxId)
        .append(r3Amt)
        .append(r4Cur)
        .append(r5Pid)
        .append(r6Order)
        .append(r7Uid)
        .append(r8MP)
        .append(r9BType);

        return hmacSign(sb.toString(), PropertiesContainer.B2C_MERCHANT_PRIVATE_KEY);
    }


    /**
     * @param value
     * @param key
     * @return
     */
    private static String hmacSign(String value, String key) {
        try {
            byte[] keyBytes = key.getBytes(CHARSET_UTF8);

            byte[] ipad = new byte[64];
            byte[] opad = new byte[64];
            Arrays.fill(ipad, keyBytes.length, 64, (byte) 54);
            Arrays.fill(opad, keyBytes.length, 64, (byte) 92);

            for (int i = 0; i < keyBytes.length; i++) {
                ipad[i] = (byte) (keyBytes[i] ^ 0x36);
                opad[i] = (byte) (keyBytes[i] ^ 0x5c);
            }

            MessageDigest md = MessageDigest.getInstance("MD5");

            md.update(ipad);
            md.update(value.getBytes(CHARSET_UTF8));
            byte dg[] = md.digest();
            md.reset();
            md.update(opad);
            md.update(dg, 0, 16);
            dg = md.digest();
            return toHex(dg);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String toHex(byte input[]) {
        if (input == null) {
            return null;
        }
        StringBuffer output = new StringBuffer(input.length * 2);
        for (int i = 0; i < input.length; i++) {
            int current = input[i] & 0xff;
            if (current < 16) {
                output.append("0");
            }
            output.append(Integer.toString(current, 16));
        }

        return output.toString();
    }


    public static void main(String[] args) {
        String[] test = {"1","2","3","5"};
        System.out.println(StringUtils.join(test));
    }

}
