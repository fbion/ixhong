package com.yeepay;

import java.util.Map;
import java.util.TreeMap;
import java.util.HashMap;
import java.net.URLEncoder;
import java.net.URLDecoder;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.File;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.IOException;

import com.yeepay.AES;
import com.yeepay.RSA;
import com.yeepay.EncryUtil;
import com.yeepay.RandomUtil;
import com.yeepay.Configuration;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.TypeReference;

import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;

/**
 * 投资通接口范例--API版
 * @author	：yingjie.wang    
 * @since	：2015-05-13 
 */

public class TZTService {
	
	/**
	 * 取得商户编号
	 */
	public static String getMerchantAccount() {
		return Configuration.getInstance().getValue("merchantAccount");
	}
	
	/**
	 * 取得商户私钥
	 */
	public static String getMerchantPrivateKey() {
		return Configuration.getInstance().getValue("merchantPrivateKey");
	}

	/**
	 * 取得商户AESKey
	 */
	public static String getMerchantAESKey() {
		return (RandomUtil.getRandom(16));
	}

	/**
	 * 取得易宝公玥
	 */
	public static String getYeepayPublicKey() {
		return Configuration.getInstance().getValue("yeepayPublicKey");
	}

	/**
	 * 格式化字符串
	 */
	public static String formatString(String text) {
		return (text == null ? "" : text.trim());
	}

	/**
	 * String2Integer
	 */
	public static int String2Int(String text) throws NumberFormatException {
		return text == null ? 0 : Integer.valueOf(text);
	}

	/**
	 * 绑卡请求接口请求地址
	 */
	public static String getBindBankcardURL() {
		return Configuration.getInstance().getValue("bindBankcardURL");
	}

	/**
	 * 绑卡确认接口请求地址
	 */
	public static String getConfirmBindBankcardURL() {
		return Configuration.getInstance().getValue("confirmBindBankcardURL");
	}

	/**
	 * 支付接口请求地址
	 */
	public static String getDirectBindPayURL() {
		return Configuration.getInstance().getValue("directBindPayURL");
	}

	/**
	 * 订单查询请求地址
	 */
	public static String getPaymentQueryURL() {
		return Configuration.getInstance().getValue("paymentQueryURL");
	}

	/**
	 * 取现接口请求地址
	 */
	public static String getWithdrawURL() {
		return Configuration.getInstance().getValue("withdrawURL");
	}

	/**
	 * 取现查询请求地址
	 */
	public static String getQueryWithdrawURL() {
		return Configuration.getInstance().getValue("queryWithdrawURL");
	}

	/**
	 * 取现查询请求地址
	 */
	public static String getQueryAuthbindListURL() {
		return Configuration.getInstance().getValue("queryAuthbindListURL");
	}

	/**
	 * 银行卡信息查询请求地址 
	 */
	public static String getBankCardCheckURL() {
		return Configuration.getInstance().getValue("bankCardCheckURL");
	}

	/**
	 * 清算文件下载请求地址 
	 */
	public static String getPayClearDataURL() {
		return Configuration.getInstance().getValue("payClearDataURL");
	}

	/**
	 * 解析http请求返回
	 */
	public static Map<String, String> parseHttpResponseBody(int statusCode, String responseBody) throws Exception {

		String merchantPrivateKey	= getMerchantPrivateKey();
		String yeepayPublicKey		= getYeepayPublicKey();

		Map<String, String> result	= new HashMap<String, String>();
		String customError			= "";

		if(statusCode != 200) {
			customError	= "Request failed, response code : " + statusCode;
			result.put("customError", customError);
			return (result);
		}

		Map<String, String> jsonMap	= JSON.parseObject(responseBody, 
											new TypeReference<TreeMap<String, String>>() {});

		if(jsonMap.containsKey("error_code")) {
			result	= jsonMap;
			return (result);
		}

		String dataFromYeepay		= formatString(jsonMap.get("data"));
		String encryptkeyFromYeepay	= formatString(jsonMap.get("encryptkey"));

		boolean signMatch = EncryUtil.checkDecryptAndSign(dataFromYeepay, encryptkeyFromYeepay, 
										yeepayPublicKey, merchantPrivateKey);
		if(!signMatch) {
			customError	= "Sign not match error";
			result.put("customError",	customError);
			return (result);
		}

		String yeepayAESKey		= RSA.decrypt(encryptkeyFromYeepay, merchantPrivateKey);
		String decryptData		= AES.decryptFromBase64(dataFromYeepay, yeepayAESKey);

		result	= JSON.parseObject(decryptData, new TypeReference<TreeMap<String, String>>() {});

		return(result);
	}


	/**
	 * bindBankcard() : 绑卡请求接口
	 */

	public static Map<String, String> bindBankcard(Map<String, String> params) {

		System.out.println("##### bindBankcard() #####");

		Map<String, String> result			= new HashMap<String, String>();
        String customError	   				= "";	//自定义，非接口返回
				
		String merchantaccount		= getMerchantAccount();
		String merchantPrivateKey	= getMerchantPrivateKey();
		String merchantAESKey		= getMerchantAESKey();
		String yeepayPublicKey		= getYeepayPublicKey();
		String bindBankcardURL		= getBindBankcardURL();

		String requestid            = formatString(params.get("requestid"));
		String identityid           = formatString(params.get("identityid"));
		String cardno               = formatString(params.get("cardno"));
		String idcardtype           = formatString(params.get("idcardtype"));
		String idcardno             = formatString(params.get("idcardno"));
		String username             = formatString(params.get("username"));
		String phone                = formatString(params.get("phone"));
		String registerphone        = formatString(params.get("registerphone"));
		String registerdate         = formatString(params.get("registerdate"));
		String registerip           = formatString(params.get("registerip"));
		String registeridcardno     = formatString(params.get("registeridcardno"));
		String registercontact      = formatString(params.get("registercontact"));
		String os                   = formatString(params.get("os"));
		String imei                 = formatString(params.get("imei"));
		String userip               = formatString(params.get("userip"));
		String ua                   = formatString(params.get("ua"));
		String registeridcardtype   = formatString(params.get("registeridcardtype"));

		int identitytype			= 2;

		try {
			if(params.get("identitytype") == null) {
				throw new Exception("identitytype is null!!!!!");
			} else {
				identitytype        	= String2Int(params.get("identitytype"));
			}
		} catch(Exception e) {
			StringBuffer buffer		= new StringBuffer();
			buffer.append("error - NumberFormatException : ");
			buffer.append("[identitytype = " + formatString(params.get("identitytype")) + "]. ");
			e.printStackTrace();
			customError				= buffer.toString();
			result.put("customError", customError);
			return (result);
		}

		TreeMap<String, Object> dataMap	= new TreeMap<String, Object>();
		dataMap.put("merchantaccount", 	merchantaccount);
		dataMap.put("requestid", 		requestid);
		dataMap.put("identitytype", 	identitytype);
		dataMap.put("identityid", 		identityid);
		dataMap.put("cardno", 			cardno);
		dataMap.put("idcardtype", 		idcardtype);
		dataMap.put("idcardno", 		idcardno);
		dataMap.put("username", 		username);
		dataMap.put("phone", 			phone);
		dataMap.put("registerphone", 	registerphone);
		dataMap.put("registerdate", 	registerdate);
		dataMap.put("registerip", 		registerip);
		dataMap.put("registeridcardno", registeridcardno);
		dataMap.put("registercontact", 	registercontact);
		dataMap.put("os", 				os);
		dataMap.put("imei", 			imei);
		dataMap.put("userip", 			userip);
		dataMap.put("ua", 				ua);
		dataMap.put("registeridcardtype", registeridcardtype);
						
		String sign						= EncryUtil.handleRSA(dataMap, merchantPrivateKey);
		dataMap.put("sign", sign);

		System.out.println("bindBankcardURL : " + bindBankcardURL);
		System.out.println("dataMap : " + dataMap);

		HttpClient httpClient			= new HttpClient();
		PostMethod postMethod			= new PostMethod(bindBankcardURL);

		try {
			String jsonStr				= JSON.toJSONString(dataMap);
			String data					= AES.encryptToBase64(jsonStr, merchantAESKey);
			String encryptkey			= RSA.encrypt(merchantAESKey, yeepayPublicKey);

			NameValuePair[] datas		= {new NameValuePair("merchantaccount", merchantaccount),
										   new NameValuePair("data", data),
										   new NameValuePair("encryptkey", encryptkey)};

			postMethod.setRequestBody(datas);

			int statusCode				= httpClient.executeMethod(postMethod);
			byte[] responseByte			= postMethod.getResponseBody();
			String responseBody			= new String(responseByte, "UTF-8");

			result = parseHttpResponseBody(statusCode, responseBody);

		} catch(Exception e) {
			customError	= "Caught Exception!" + e.toString();
			result.put("customError", customError);
			e.printStackTrace();
		} finally {
			postMethod.releaseConnection();
		}

		System.out.println("result : " + result);

		return (result);
	}

	
	/**
	 * cofirmBindBankcard() : 绑卡确认接口
	 */

	public static Map<String, String> confirmBindBankcard(Map<String, String> params) {

		System.out.println("##### confirmBindBankcard() #####");
				
		String merchantaccount			= getMerchantAccount();
		String merchantPrivateKey		= getMerchantPrivateKey();
		String merchantAESKey			= getMerchantAESKey();
		String yeepayPublicKey			= getYeepayPublicKey();
		String confirmBindBankcardURL	= getConfirmBindBankcardURL();

		String requestid            	= formatString(params.get("requestid"));
		String validatecode      		= formatString(params.get("validatecode"));

		TreeMap<String, Object> dataMap	= new TreeMap<String, Object>();
		dataMap.put("merchantaccount", 	merchantaccount);
		dataMap.put("requestid", 		requestid);
		dataMap.put("validatecode", 	validatecode);
						
		String sign						= EncryUtil.handleRSA(dataMap, merchantPrivateKey);
		dataMap.put("sign", sign);

		System.out.println("confirmBindBankcardURL : " + confirmBindBankcardURL);
		System.out.println("dataMap : " + dataMap);

		Map<String, String> result			= new HashMap<String, String>();
        String customError	   				= "";	//自定义，非接口返回

		HttpClient httpClient			= new HttpClient();
		PostMethod postMethod			= new PostMethod(confirmBindBankcardURL);

		try {
			String jsonStr				= JSON.toJSONString(dataMap);
			String data					= AES.encryptToBase64(jsonStr, merchantAESKey);
			String encryptkey			= RSA.encrypt(merchantAESKey, yeepayPublicKey);

			NameValuePair[] datas		= {new NameValuePair("merchantaccount", merchantaccount),
										   new NameValuePair("data", data),
										   new NameValuePair("encryptkey", encryptkey)};

			postMethod.setRequestBody(datas);

			int statusCode				= httpClient.executeMethod(postMethod);
			byte[] responseByte			= postMethod.getResponseBody();
			String responseBody			= new String(responseByte, "UTF-8");

			result = parseHttpResponseBody(statusCode, responseBody);

		} catch(Exception e) {
			customError	= "Caught Exception!" + e.toString();
			result.put("customError", customError);
			e.printStackTrace();
		} finally {
			postMethod.releaseConnection();
		}

		System.out.println("result : " + result);

		return (result);
	}


		
	/**
	 * directBindPay() : 支付接口
	 */

	public static Map<String, String> directBindPay(Map<String, String> params) {

		System.out.println("##### directBindPay() #####");

		Map<String, String> result			= new HashMap<String, String>();
        String customError	   				= "";	//自定义，非接口返回
				
		String merchantaccount				= getMerchantAccount();
		String merchantPrivateKey			= getMerchantPrivateKey();
		String merchantAESKey				= getMerchantAESKey();
		String yeepayPublicKey				= getYeepayPublicKey();
		String directBindPayURL				= getDirectBindPayURL();

		String orderid              		= formatString(params.get("orderid"));
		String productname          		= formatString(params.get("productname"));
		String productdesc          		= formatString(params.get("productdesc"));
		String identityid           		= formatString(params.get("identityid"));
		String card_top             		= formatString(params.get("card_top"));
		String card_last            		= formatString(params.get("card_last"));
		String callbackurl          		= formatString(params.get("callbackurl"));
		String imei                 		= formatString(params.get("imei"));
		String userip               		= formatString(params.get("userip"));
		String ua                   		= formatString(params.get("ua"));

		int transtime						= 0; 
		int amount              			= 0; 
		int identitytype        			= 0; 
		int orderexpdate        			= 0; 
		int currency	        			= 0; 
		
		try {
			//transtime、identitytype、amount是必填参数
			if(params.get("transtime") == null) {
				throw new Exception("transtime is null!!!!!");
			} else {
				transtime			= String2Int(params.get("transtime"));
			}

			if(params.get("identitytype") == null) {
				throw new Exception("identitytype is null!!!!!");
			} else {
				identitytype        	= String2Int(params.get("identitytype"));
			}

			if(params.get("amount") == null) {
				throw new Exception("amount is null!!!!!");
			} else {
				amount              	= String2Int(params.get("amount"));
			}

			orderexpdate        	= String2Int(params.get("orderexpdate"));
			currency	        	= String2Int(params.get("currency"));

		} catch(Exception e) {
			StringBuffer buffer		= new StringBuffer();
			buffer.append("error - the following parameters must be int  : ");
			buffer.append("[transtime = " + formatString(params.get("transtime")) + "], ");
			buffer.append("[amount = " + formatString(params.get("amount")) + "], ");
			buffer.append("[identitytype = " + formatString(params.get("identitytype")) + "], ");
			buffer.append("[orderexpdate = " + formatString(params.get("orderexpdate")) + "], ");
			buffer.append("[currency = " + formatString(params.get("currency")) + "]. ");
			e.printStackTrace();
			customError				= buffer.toString();
			result.put("customError", customError);
			return (result);
		}

		TreeMap<String, Object> dataMap	= new TreeMap<String, Object>();
		dataMap.put("merchantaccount", 	merchantaccount);
		dataMap.put("orderid", 			orderid);
		dataMap.put("transtime", 		transtime);
		dataMap.put("currency", 		currency);
		dataMap.put("amount", 			amount);
		dataMap.put("productname", 		productname);
		dataMap.put("productdesc", 		productdesc);
		dataMap.put("identityid", 		identityid);
		dataMap.put("identitytype", 	identitytype);
		dataMap.put("card_top", 		card_top);
		dataMap.put("card_last", 		card_last);
		dataMap.put("orderexpdate", 	orderexpdate);
		dataMap.put("callbackurl", 		callbackurl);
		dataMap.put("imei", 			imei);
		dataMap.put("userip", 			userip);
		dataMap.put("ua", 				ua);
						
		String sign						= EncryUtil.handleRSA(dataMap, merchantPrivateKey);
		dataMap.put("sign", sign);

		System.out.println("directBindPayURL : " + directBindPayURL);
		System.out.println("dataMap : " + dataMap);

		HttpClient httpClient			= new HttpClient();
		PostMethod postMethod			= new PostMethod(directBindPayURL);

		try {
			String jsonStr				= JSON.toJSONString(dataMap);
			String data					= AES.encryptToBase64(jsonStr, merchantAESKey);
			String encryptkey			= RSA.encrypt(merchantAESKey, yeepayPublicKey);

			System.out.println("jsonStr : " + jsonStr);
			System.out.println("data : " + data);
			System.out.println("encryptkey : " + encryptkey);

			NameValuePair[] datas		= {new NameValuePair("merchantaccount", merchantaccount),
										   new NameValuePair("data", data),
										   new NameValuePair("encryptkey", encryptkey)};

			postMethod.setRequestBody(datas);

			int statusCode				= httpClient.executeMethod(postMethod);
			byte[] responseByte			= postMethod.getResponseBody();
			String responseBody			= new String(responseByte, "UTF-8");

			result						= parseHttpResponseBody(statusCode, responseBody);

		} catch(Exception e) {
			customError	= "Caught Exception!" + e.toString();
			result.put("customError", customError);
			e.printStackTrace();
		} finally {
			postMethod.releaseConnection();
		}

		System.out.println("result : " + result);

		return (result);
	}

	/**
	 * decryptCallbackData() : 解密支付回调参数data
	 *
	 */

	public static Map<String, String> decryptCallbackData(String data, String encryptkey) {
		
		System.out.println("##### decryptCallbackData() #####");
		
		String merchantaccount		= getMerchantAccount();
		String merchantPrivateKey	= getMerchantPrivateKey();
		String merchantAESKey		= getMerchantAESKey();
		String yeepayPublicKey		= getYeepayPublicKey();
		
		System.out.println("data : " + data);
		System.out.println("encryptkey : " + encryptkey);
		
		Map<String, String> callbackResult	= new HashMap<String, String>();
		String customError			= "";
		
		try {
			boolean signMatch = EncryUtil.checkDecryptAndSign(data, encryptkey, yeepayPublicKey, merchantPrivateKey);

			if(!signMatch) {
				customError	= "Sign not match error";
				callbackResult.put("customError",	customError);
				return callbackResult;
			}

			String yeepayAESKey	= RSA.decrypt(encryptkey, merchantPrivateKey);
			String decryptData	= AES.decryptFromBase64(data, yeepayAESKey);
			callbackResult		= JSON.parseObject(decryptData, new TypeReference<TreeMap<String, String>>() {});

		} catch(Exception e) {
			customError		= "Caught an Exception. " + e.toString();
			callbackResult.put("customError", customError);
			e.printStackTrace();
		}

		System.out.println("callbackResult : " + callbackResult);

		return (callbackResult);
	}


	/**
	 * queryByOrder() : 查询接口
	 */
	
	public static Map<String, String> queryByOrder(String orderid, String yborderid) {
		
		System.out.println("##### queryByOrder() #####");
		
		String merchantaccount		= getMerchantAccount();
		String merchantPrivateKey	= getMerchantPrivateKey();
		String merchantAESKey		= getMerchantAESKey();
		String yeepayPublicKey		= getYeepayPublicKey();
		String paymentQueryURL		= getPaymentQueryURL();
		
		TreeMap<String, Object> dataMap	= new TreeMap<String, Object>();
		dataMap.put("merchantaccount", 	merchantaccount);
		dataMap.put("orderid",			orderid);
		dataMap.put("yborderid",		yborderid);

		String sign					= EncryUtil.handleRSA(dataMap, merchantPrivateKey);
		dataMap.put("sign", sign);
				
		System.out.println("paymentQueryURL : " + paymentQueryURL);
		System.out.println("dataMap : " + dataMap);
		
		Map<String, String> result	= new HashMap<String, String>();
		String customError			= "";	// 自定义，非接口返回

		HttpClient httpClient		= new HttpClient();
		GetMethod getMethod			= new GetMethod();
		
		try {
			String jsonStr			= JSON.toJSONString(dataMap);
			String data				= AES.encryptToBase64(jsonStr, merchantAESKey);
			String encryptkey		= RSA.encrypt(merchantAESKey, yeepayPublicKey);

			String url				= paymentQueryURL + 
									  "?merchantaccount=" + URLEncoder.encode(merchantaccount, "UTF-8") + 
									  "&data=" + URLEncoder.encode(data, "UTF-8") +
									  "&encryptkey=" + URLEncoder.encode(encryptkey, "UTF-8");

			System.out.println("url	 : " + url);

			getMethod				= new GetMethod(url);
			int statusCode			= httpClient.executeMethod(getMethod);
			String responseBody		= getMethod.getResponseBodyAsString();
			
			result					= parseHttpResponseBody(statusCode, responseBody);

		} catch(Exception e) {
			customError		= "Caught an Exception. " + e.toString();
			result.put("customError", customError);
			e.printStackTrace();
		} finally {
			getMethod.releaseConnection();
		}

		System.out.println("result : " + result);

		return (result);
	}

			
	/**
	 * withdraw() : 提现接口
	 */

	public static Map<String, String> withdraw(Map<String, String> params) {

		System.out.println("##### withdraw() #####");

		Map<String, String> result			= new HashMap<String, String>();
        String customError	   				= "";	//自定义，非接口返回
				
		String merchantaccount				= getMerchantAccount();
		String merchantPrivateKey			= getMerchantPrivateKey();
		String merchantAESKey				= getMerchantAESKey();
		String yeepayPublicKey				= getYeepayPublicKey();
		String withdrawURL					= getWithdrawURL();

		String requestid            		= formatString(params.get("requestid"));
		String identityid           		= formatString(params.get("identityid"));
		String card_top             		= formatString(params.get("card_top"));
		String card_last            		= formatString(params.get("card_last"));
		String drawtype             		= formatString(params.get("drawtype"));
		String imei                 		= formatString(params.get("imei"));
		String userip               		= formatString(params.get("userip"));
		String ua                   		= formatString(params.get("ua"));

		int amount              			= 0; 
		int identitytype        			= 0; 
		int currency	        			= 0; 
		
		try {
			if(params.get("identitytype") == null) {
				throw new Exception("identitytype is null!!!!!");
			} else {
				identitytype        	= String2Int(params.get("identitytype"));
			}

			if(params.get("amount") == null) {
				throw new Exception("amount is null!!!!!");
			} else {
				amount              	= String2Int(params.get("amount"));
			}

			currency	        	= String2Int(params.get("currency"));

		} catch(Exception e) {
			StringBuffer buffer		= new StringBuffer();
			buffer.append("error - the following parameters must be int  : ");
			buffer.append("[amount = " + formatString(params.get("amount")) + "], ");
			buffer.append("[identitytype = " + formatString(params.get("identitytype")) + "], ");
			buffer.append("[currency = " + formatString(params.get("currency")) + "]. ");
			e.printStackTrace();
			customError	= buffer.toString();
			result.put("customError", customError);
			return (result);
		}

		TreeMap<String, Object> dataMap	= new TreeMap<String, Object>();
		dataMap.put("merchantaccount",	merchantaccount);
		dataMap.put("requestid", 		requestid);
		dataMap.put("identityid", 		identityid);
		dataMap.put("identitytype", 	identitytype);
		dataMap.put("card_top", 		card_top);
		dataMap.put("card_last", 		card_last);
		dataMap.put("amount", 			amount);
		dataMap.put("currency", 		currency);
		dataMap.put("drawtype", 		drawtype);
		dataMap.put("imei", 			imei);
		dataMap.put("userip", 			userip);
		dataMap.put("ua", 				ua);
						
		String sign						= EncryUtil.handleRSA(dataMap, merchantPrivateKey);
		dataMap.put("sign", sign);

		System.out.println("withdrawURL : " + withdrawURL);
		System.out.println("dataMap : " + dataMap);

		HttpClient httpClient			= new HttpClient();
		PostMethod postMethod			= new PostMethod(withdrawURL);

		try {
			String jsonStr				= JSON.toJSONString(dataMap);
			String data					= AES.encryptToBase64(jsonStr, merchantAESKey);
			String encryptkey			= RSA.encrypt(merchantAESKey, yeepayPublicKey);

			NameValuePair[] datas		= {new NameValuePair("merchantaccount", merchantaccount),
										   new NameValuePair("data", data),
										   new NameValuePair("encryptkey", encryptkey)};

			postMethod.setRequestBody(datas);

			int statusCode				= httpClient.executeMethod(postMethod);
			byte[] responseByte			= postMethod.getResponseBody();
			String responseBody			= new String(responseByte, "UTF-8");

			System.out.println("responseBody : " + responseBody);

			result					= parseHttpResponseBody(statusCode, responseBody);

		} catch(Exception e) {
			customError		= "Caught an Exception. " + e.toString();
			result.put("customError", customError);
			e.printStackTrace();
		} finally {
			postMethod.releaseConnection();
		}

		System.out.println("result : " + result);

		return (result);
	}

	
	/**
	 * queryWithdraw() : 提现查询接口
	 */
	
	public static Map<String, String> queryWithdraw(String requestid, String ybdrawflowid) {
		
		System.out.println("##### queryWithdraw() #####");
		
		String merchantaccount		= getMerchantAccount();
		String merchantPrivateKey	= getMerchantPrivateKey();
		String merchantAESKey		= getMerchantAESKey();
		String yeepayPublicKey		= getYeepayPublicKey();
		String queryWithdrawURL		= getQueryWithdrawURL();
		
		TreeMap<String, Object> dataMap	= new TreeMap<String, Object>();
		dataMap.put("merchantaccount", 	merchantaccount);
		dataMap.put("requestid",		requestid);
		dataMap.put("ybdrawflowid",		ybdrawflowid);

		String sign					= EncryUtil.handleRSA(dataMap, merchantPrivateKey);
		dataMap.put("sign", sign);
				
		System.out.println("requestid : " + requestid);
		System.out.println("ybdrawflowid : " + ybdrawflowid);
		System.out.println("merchantaccount : " + merchantaccount);
		System.out.println("merchantPrivateKey : " + merchantPrivateKey);
		System.out.println("yeepayPublicKey : " + yeepayPublicKey);
		System.out.println("queryWithdrawURL : " + queryWithdrawURL);
		System.out.println("dataMap : " + dataMap);
		
		Map<String, String> result		= new HashMap<String, String>();
        String customError     			= "";  // 自定义参数，非接口返回。

		HttpClient httpClient		= new HttpClient();
		GetMethod getMethod			= new GetMethod();
		
		try {
			String jsonStr				= JSON.toJSONString(dataMap);
			String data					= AES.encryptToBase64(jsonStr, merchantAESKey);
			String encryptkey			= RSA.encrypt(merchantAESKey, yeepayPublicKey);

			String url					= queryWithdrawURL + 
										  "?merchantaccount=" + URLEncoder.encode(merchantaccount, "UTF-8") + 
										  "&data=" + URLEncoder.encode(data, "UTF-8") +
										  "&encryptkey=" + URLEncoder.encode(encryptkey, "UTF-8");

			getMethod					= new GetMethod(url);
			int statusCode				= httpClient.executeMethod(getMethod);
			String responseBody			= getMethod.getResponseBodyAsString();

			System.out.println("url	 : " + url);
			System.out.println("responseBody : " + responseBody);

			result					= parseHttpResponseBody(statusCode, responseBody);

		} catch(Exception e) {
			customError		= "Caught an Exception. " + e.toString();
			result.put("customError", customError);
			e.printStackTrace();
		} finally {
			getMethod.releaseConnection();
		}
		
		System.out.println("result : " + result);

		return (result);
	}

		
	/**
	 * queryAuthbindList() : 绑卡查询接口 
	 *
	 */

	public static Map<String, String> queryAuthbindList(String identityid, String identitytype) {
		
		System.out.println("##### queryAuthbindList() #####");
		
		Map<String, String> result		= new HashMap<String, String>();
		String customError				= "";	//自定义，非接口返回 

		String merchantaccount		= getMerchantAccount();
		String merchantPrivateKey	= getMerchantPrivateKey();
		String merchantAESKey		= getMerchantAESKey();
		String yeepayPublicKey		= getYeepayPublicKey();
		String queryAuthbindListURL	= getQueryAuthbindListURL();
		
		int identitytype2Int		= -1;
		
		try {
			identitytype2Int	= String2Int(identitytype);
		} catch(Exception e) {
			e.printStackTrace();
			customError			= "String 2 Int Error!!! - identitytype = [" + identitytype + "]";
			result.put("customError", customError);
			return (result);
		}

		TreeMap<String, Object> dataMap	= new TreeMap<String, Object>();
		dataMap.put("merchantaccount", merchantaccount);
		dataMap.put("identityid", identityid);
		dataMap.put("identitytype", identitytype2Int);

		String sign					= EncryUtil.handleRSA(dataMap, merchantPrivateKey);
		dataMap.put("sign", sign);
				
		System.out.println("queryAuthbindListURL : " + queryAuthbindListURL);
		System.out.println("dataMap : " + dataMap);
		
		HttpClient httpClient		= new HttpClient();
		GetMethod getMethod			= new GetMethod();
		
		try {
			String jsonStr				= JSON.toJSONString(dataMap);
			String data					= AES.encryptToBase64(jsonStr, merchantAESKey);
			String encryptkey			= RSA.encrypt(merchantAESKey, yeepayPublicKey);

			String url					= queryAuthbindListURL + 
										  "?merchantaccount=" + URLEncoder.encode(merchantaccount, "UTF-8") + 
										  "&data=" + URLEncoder.encode(data, "UTF-8") +
										  "&encryptkey=" + URLEncoder.encode(encryptkey, "UTF-8");

			getMethod					= new GetMethod(url);

			int statusCode				= httpClient.executeMethod(getMethod);
			String responseBody			= getMethod.getResponseBodyAsString();

			System.out.println("url	 : " + url);
			System.out.println("responseBody : " + responseBody);

			result					= parseHttpResponseBody(statusCode, responseBody);

		} catch(Exception e) {
			customError		= "Caught an Exception. " + e.toString();
			result.put("customError", customError);
			e.printStackTrace();
		} finally {
			getMethod.releaseConnection();
		}
		
		System.out.println("result : " + result);

		return (result);
	}

				
	/**
	 * bankCardCheck() : 银行卡信息查询接口 
	 *
	 */

	public static Map<String, String> bankCardCheck(String cardno) {
		
		System.out.println("##### bankCardCheck() #####");
		
		String merchantaccount		= getMerchantAccount();
		String merchantPrivateKey	= getMerchantPrivateKey();
		String merchantAESKey		= getMerchantAESKey();
		String yeepayPublicKey		= getYeepayPublicKey();
		String bankCardCheckURL		= getBankCardCheckURL();
		
		TreeMap<String, Object> dataMap	= new TreeMap<String, Object>();
		dataMap.put("merchantaccount", 	merchantaccount);
		dataMap.put("cardno", 			cardno);
				
		String sign					= EncryUtil.handleRSA(dataMap, merchantPrivateKey);
		dataMap.put("sign", sign);

		System.out.println("bankCardCheckURL : " + bankCardCheckURL);
		System.out.println("dataMap : " + dataMap);

		Map<String, String> result	= new HashMap<String, String>();
		String customError			= "";

		HttpClient httpClient		= new HttpClient();
		PostMethod postMethod		= new PostMethod(bankCardCheckURL);

		try {
			String jsonStr			= JSON.toJSONString(dataMap);
			String data				= AES.encryptToBase64(jsonStr, merchantAESKey);
			String encryptkey		= RSA.encrypt(merchantAESKey, yeepayPublicKey);

			NameValuePair[] datas	= {new NameValuePair("merchantaccount", merchantaccount),
									   new NameValuePair("data", data),
									   new NameValuePair("encryptkey", encryptkey)};

			postMethod.setRequestBody(datas);

			int statusCode			= httpClient.executeMethod(postMethod);
			byte[] responseByte		= postMethod.getResponseBody();
			String responseBody		= new String(responseByte, "UTF-8");

			System.out.println("responseBody : " + responseBody);

			result					= parseHttpResponseBody(statusCode, responseBody);

		} catch(Exception e) {
			customError		= "Caught an Exception. " + e.toString();
			result.put("customError", customError);
			e.printStackTrace();
		} finally {
			postMethod.releaseConnection();
		}

		System.out.println("result : " + result);

		return (result);
	}

				
	/**
	 * getPathOfPayClearData()：获取清算数据
	 *
 	 * 返回说明：
	 *
	 * filePath				- 批量查询结果文件的路径
	 * error_code			- 错误返回码
	 * error				- 错误信息
	 * customError			- 自定义，非接口返回
	 *
	 */

	public static Map<String, String> getPathOfPayClearData(String startdate, String enddate, String sysPath) {
		
		System.out.println("##### getPathOfPayClearData() #####");

		
		String merchantaccount		= getMerchantAccount();
		String merchantPrivateKey	= getMerchantPrivateKey();
		String merchantAESKey		= getMerchantAESKey();
		String yeepayPublicKey		= getYeepayPublicKey();
		String payClearDataURL		= getPayClearDataURL();
		
		TreeMap<String, Object> dataMap	= new TreeMap<String, Object>();
		dataMap.put("merchantaccount", 	merchantaccount);
		dataMap.put("startdate", 		startdate);
		dataMap.put("enddate", 			enddate);

		String sign					= EncryUtil.handleRSA(dataMap, merchantPrivateKey);
		dataMap.put("sign", sign);
				
		Map<String, String> queryResult	= new HashMap<String, String>();
		String filePath					= "";
		String error_code              	= "";
		String error                   	= "";
		String customError				= "";

		HttpClient httpClient			= new HttpClient();
		GetMethod getMethod				= new GetMethod();
		
		try {
			String jsonStr				= JSON.toJSONString(dataMap);
			String data					= AES.encryptToBase64(jsonStr, merchantAESKey);
			String encryptkey			= RSA.encrypt(merchantAESKey, yeepayPublicKey);

			String url					= payClearDataURL + 
										  "?merchantaccount=" + URLEncoder.encode(merchantaccount, "UTF-8") + 
										  "&data=" + URLEncoder.encode(data, "UTF-8") +
										  "&encryptkey=" + URLEncoder.encode(encryptkey, "UTF-8");

			getMethod					= new GetMethod(url);

			int statusCode				= httpClient.executeMethod(getMethod);

			if(statusCode != 200) {
				customError = "Get request failed, response code = " + statusCode;
				queryResult.put("customError", customError);
				return (queryResult);
			}

			InputStream	responseStream	= getMethod.getResponseBodyAsStream();
			BufferedReader	reader		= new BufferedReader(new InputStreamReader(responseStream, "UTF-8"));
			//BufferedReader	reader		= new BufferedReader(new InputStreamReader(responseStream));

			String line					= reader.readLine();
			if(line.startsWith("{")) {
				Map<String, Object> jsonMap	= JSON.parseObject(line, TreeMap.class);

				if(jsonMap.containsKey("error_code")) {
					error_code					= formatString((String)jsonMap.get("error_code"));
					error						= formatString((String)jsonMap.get("error"));
				} else {
					String dataFromYeepay		= formatString((String)jsonMap.get("data"));
					String encryptkeyFromYeepay	= formatString((String)jsonMap.get("encryptkey")); 

					String yeepayAESKey					= RSA.decrypt(encryptkeyFromYeepay, merchantPrivateKey);
					String decryptData					= AES.decryptFromBase64(dataFromYeepay, yeepayAESKey);
					Map<String, Object> decryptDataMap	= JSON.parseObject(decryptData, TreeMap.class);

					error_code 					= formatString((String)decryptDataMap.get("error_code"));
					error						= formatString((String)decryptDataMap.get("error"));

					System.out.println("decryptData : " + decryptData);
				}
			} else {
				String outputFilePath				= sysPath + File.separator + "clearData";
				File file							= new File(outputFilePath);
				file.mkdir();

				String time							= String.valueOf(System.currentTimeMillis());
				String fileName						= "payClearData_" + startdate + "_" + enddate + "_" + time + ".txt";
				String absolutePathOfOutputFile		= outputFilePath + File.separator + fileName;
				filePath							= absolutePathOfOutputFile;

				File outputFile						= new File(absolutePathOfOutputFile);
				FileWriter fileWriter				= new FileWriter(outputFile);
				BufferedWriter writer				= new BufferedWriter(fileWriter);

				System.out.println("filePath : " + filePath);

				writer.write(line);
				writer.write(System.getProperty("line.separator"));
				while((line = reader.readLine()) != null) {
					writer.write(line);
					writer.write(System.getProperty("line.separator"));
				}
				
				writer.close();
			}
		} catch(Exception e) {
			customError = "Caught an Exception. " + e.toString();
			e.printStackTrace();
		} finally {
			getMethod.releaseConnection();
		}

		queryResult.put("filePath",		filePath);
		queryResult.put("error_code",	error_code);
		queryResult.put("error",		error);
		queryResult.put("customError",	customError);

		return (queryResult);
	}	

}
