package com.ixhong.common.utils;

import com.ixhong.common.pojo.ResultCode;
import net.sf.json.JSONObject;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by shenhongxi on 15/5/16.
 */
public class CertifyUtils {

    private static final String AES = "AES";
    private static final String PADDING = "AES/CBC/NoPadding";//"算法/模式/补码方式"
    private static final String DEFAULT_ENCODING = "utf-8";
    private static final String IV = "1234567812345678";
    //风控身份认证
    public static final String RISK_CONTROL_URL = "http://fengkong.haodai.com/api/data/index";
    public static final String RISK_CONTROL_KEY = "IsWq7Wk8pRqXtIXXIKFXDTgznvw8BwNT";
    public static final String RISK_CONTROL_ID = "33036123-9";

    //private static final String IMAGE = "http://stat.xuehaodai.com/ds/fhqxgjrvslh6wdprxawi4eroprdapsrn.jpg";
    private static final String IMAGE = "http://stat.xuehaodai.com/ds/x2/magbvwoql9zgjg4xjynykh8huckdfb6m.jpeg";
    //private static final String IMAGE = "http://stat.xuehaodai.com/ds/e7dah5zi4je6nnzpridigpczikkglclo.jpg";

    public static String certify(String name, String cardId) {
        return certify(name, cardId, null);
    }

    /**
     * 100000:操作成功
     * 100001:组织机构代码号错误
     * 100002:解密错误  !!!注意替换 jre/lib/security下的两个jar
     * 100003:请求参数错误，请求类型不存在
     * 100004:请求参数错误，请求类型存在，但其它参数有错误
     * 100005:系统错误
     * 100006:账户积分不足
     * 100007:IP受限

     * 返回对比结果
     * 00,10：核查成功
     * 01：库中无此号
     * 02：不一致
     * 03：身份证号不符合规则
     * 04：姓名不符合规则
     * 05：姓名身份证号照片不符合规则
     * 06：照片不符合规则
     * -1: 用户名有误
     * -2: 摘要有误
     * -3: IP地址受限
     * -4: 提交数据有误
     * -999: 系统异常
     * 其他：系统异常
     * 用户实名认证，传递一个姓名 + 身份证号 + 头像照
     * 调用接口失败返回ResultCode.SYSTEM_ERROR.code
     * 成功则返回 code,message(,score)  score为头像识别所得分数
     * @param name 姓名
     * @param cardId 身份证号
     * @param photoUrl 头像照片 URL
     * @return
     */
    public static String certify(String name, String cardId, String photoUrl) {
        InputStream inputStream = null;
        ByteArrayOutputStream outputStream = null;
        try {
            if (StringUtils.isBlank(photoUrl)) {
                photoUrl = IMAGE;
            }
            inputStream = HttpUtils.httpGetStream(photoUrl, null);
            outputStream = new ByteArrayOutputStream();
            byte[] buf = new byte[100];
            int b = 0;
            while ((b = inputStream.read(buf, 0, 100)) > 0) {
                outputStream.write(buf, 0, b);
            }
            byte[] imgBytes = outputStream.toByteArray();
            String img = new Base64().encodeToString(imgBytes);
            String _name = UnicodeUtils.native2ascii(name);
            JSONObject json = new JSONObject();
            json.put("name",_name);
            json.put("idcard",cardId);
            json.put("type","100");
            json.put("img", img);

            String jsonStr = json.toString();
            jsonStr = jsonStr.replace("\\\\", "\\");
            String encryptData = encrypt(jsonStr, RISK_CONTROL_KEY);

            List<NameValuePair> pairs = new ArrayList<NameValuePair>();
            pairs.add(new BasicNameValuePair("companyid", RISK_CONTROL_ID));
            pairs.add(new BasicNameValuePair("data", encryptData));

            String response = null;
            try {
                response = HttpUtils.httpPost(RISK_CONTROL_URL, null, new UrlEncodedFormEntity(pairs, "utf-8"));
            } catch (Exception e) {
                throw new RuntimeException(ResultCode.SYSTEM_ERROR.code);
            }

            if(StringUtils.isBlank(response)) {
                return ResultCode.SYSTEM_ERROR.code;
            }

            JSONObject responseJson = JSONObject.fromObject(response);

            if (responseJson.get("code").equals("100000")) {
                String data = responseJson.get("data").toString();
                String decodeData = decrypt(data, RISK_CONTROL_KEY);
                JSONObject obj = JSONObject.fromObject(decodeData);
                String code = obj.getString("execresult");
                String message = UnicodeUtils.ascii2native(obj.getString("message"));
                String objScore = obj.get("fs").toString(); //照片识别评分
                if (photoUrl.equals(IMAGE)) {
                    return code + "," + message;
                }
                return code + "," + message + "," + objScore;
            }
            return responseJson.get("code").toString();
        } catch (Exception e) {
            return ResultCode.SYSTEM_ERROR.code;
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static String encrypt(String code, String key) {
        code = padding(code);
        try {
            return new Base64().encodeToString(encrypt(code.getBytes(DEFAULT_ENCODING),
                    key.getBytes(DEFAULT_ENCODING)));
        } catch (Exception e) {

        }
        return null;
    }

    private static byte[] encrypt(byte[] code, byte[] key) throws Exception {
        SecretKeySpec keySpec = new SecretKeySpec(key, AES);
        Cipher cipher = Cipher.getInstance(PADDING);//"算法/模式/补码方式"
        //使用CBC模式，需要一个向量iv，可增加加密算法的强度
        IvParameterSpec iv = new IvParameterSpec(IV.getBytes());
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, iv);
        return cipher.doFinal(code);
    }

    private static String decrypt(String data, String key) {
        data = padding(data);
        try {
            return new String(decrypt(data.getBytes(DEFAULT_ENCODING),
                    key.getBytes(DEFAULT_ENCODING)), DEFAULT_ENCODING);
        } catch (Exception e) {
            //
        }
        return null;
    }

    private static byte[] decrypt(byte[] src, byte[] key) throws Exception {
        SecretKeySpec keySpec = new SecretKeySpec(key, AES);
        Cipher cipher = Cipher.getInstance(PADDING);
        IvParameterSpec iv = new IvParameterSpec(IV.getBytes(DEFAULT_ENCODING));
        cipher.init(Cipher.DECRYPT_MODE, keySpec, iv);
        byte[] _src = new Base64().decode(src);
        return cipher.doFinal(_src);
    }

    private static String padding(String code) {
        for (int i = 0; i < code.length(); i++) {
            code += " ";
            if (code.length() % 16 == 0){
                break;
            }
        }
        return code;
    }

    public static void main(String[] args) throws Exception {
//        String code = "1234567812345678"; // 必须为16位倍数，不是的可用空格补齐
//        String key = "IsWq7Wk8pRqXtIXXIKFXDTgznvw8BwNT"; // 必须为16/32位
//        System.out.println("加密前：" + code);
//        String _code = encrypt(code, key);
//        System.out.println("加密编码后：" + _code);
//        System.out.println("编码解密后：" + decrypt(_code, key));

        System.out.println(certify("聂丽苹", "130682198806206968"));
        System.out.println(certify("沈红喜", "429004198903093876"));

    }



}
