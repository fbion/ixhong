package com.ixhong.common.yeepay;

import java.util.Properties;

/**
 * Created by liuguanqing on 15/6/9.
 */
public class PropertiesContainer {

    protected  final static Properties properties = new Properties();

    static {
        try {
            properties.load(PropertiesContainer.class.getClassLoader().getResourceAsStream("/yeepay.properties"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    //网银商户号
    public static final String B2C_MERCHANT_ID = properties.getProperty("B2C_MERCHANT_ID");
    //网银商户私钥
    public static final String B2C_MERCHANT_PRIVATE_KEY = properties.getProperty("B2C_MERCHANT_PRIVATE_KEY");
    //投资通商户号
    public static final String TZT_MERCHANT_ID = properties.getProperty("TZT_MERCHANT_ID");
    //投资通商户私钥
    public static final String TZT_MERCHANT_PRIVATE_KEY = properties.getProperty("TZT_MERCHANT_PRIVATE_KEY");
    //易宝 RSA 公钥
    public static final String YEEPAY_PUBLIC_KEY = properties.getProperty("YEEPAY_PUBLIC_KEY");
    //代付商户号
    public static final String TRANSFER_MERCHANT_ID = properties.getProperty("TRANSFER_MERCHANT_ID");
    //代付商户私钥
    public static final String TRANSFER_MERCHANT_PRIVATE_KEY = properties.getProperty("TRANSFER_MERCHANT_PRIVATE_KEY");


}
