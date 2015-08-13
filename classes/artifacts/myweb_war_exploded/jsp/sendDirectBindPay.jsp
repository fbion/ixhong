<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="java.util.Map" import="java.util.HashMap" import="com.yeepay.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%! String formatString(String text){
		return text==null ? "" : text.trim();
	}
%>
<%
	request.setCharacterEncoding("UTF-8");

	String orderid              = formatString(request.getParameter("orderid"));
	String transtime            = formatString(request.getParameter("transtime"));
	String currency             = formatString(request.getParameter("currency"));
	String amount               = formatString(request.getParameter("amount"));
	String productname          = formatString(request.getParameter("productname"));
	String productdesc          = formatString(request.getParameter("productdesc"));
	String identityid           = formatString(request.getParameter("identityid"));
	String identitytype         = formatString(request.getParameter("identitytype"));
	String card_top             = formatString(request.getParameter("card_top"));
	String card_last            = formatString(request.getParameter("card_last"));
	String orderexpdate         = formatString(request.getParameter("orderexpdate"));
	String callbackurl          = formatString(request.getParameter("callbackurl"));
	String imei                 = formatString(request.getParameter("imei"));
	String userip               = formatString(request.getParameter("userip"));
	String ua                   = formatString(request.getParameter("ua"));

	Map<String, String> params 	= new HashMap<String, String>();
	params.put("orderid", 			orderid);
	params.put("transtime", 		transtime);
	params.put("currency", 			currency);
	params.put("amount", 			amount);
	params.put("productname", 		productname);
	params.put("productdesc", 		productdesc);
	params.put("identityid", 		identityid);
	params.put("identitytype", 		identitytype);
	params.put("card_top", 			card_top);
	params.put("card_last", 		card_last);
	params.put("orderexpdate", 		orderexpdate);
	params.put("callbackurl", 		callbackurl);
	params.put("imei", 				imei);
	params.put("userip", 			userip);
	params.put("ua", 				ua);

	Map<String, String> result			= TZTService.directBindPay(params);
	String merchantaccount				= formatString(result.get("merchantaccount")); 
    String orderidFromYeepay 	   		= formatString(result.get("orderid")); 
    String yborderid 			   		= formatString(result.get("yborderid")); 
    String amountFromYeepay 			= formatString(result.get("amount")); 
    String signFromYeepay 		   		= formatString(result.get("sign")); 
    String error_code	   				= formatString(result.get("error_code")); 
    String error_msg	   				= formatString(result.get("error_msg")); 
    String customError	   				= formatString(result.get("customError")); 

	if(!"".equals(error_code)) {
		out.println("error_code : " + error_code);
		out.println("<br>error_msg: " + error_msg);
		return;
	} else if(!"".equals(customError)) {
		out.println("customError : " + customError);
		return;
	}
%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>接口参数返回</title>
</head>
	<body>
		<br /> <br />
		<table width="80%" border="0" align="center" cellpadding="10" cellspacing="0" 
							style="word-break:break-all; border:solid 1px #107929">
			<tr>
		  		<th align="center" height="30" colspan="5" bgcolor="#6BBE18">
					接口参数返回
				</th>
		  	</tr>

			<tr>
				<td width="15%" align="left">&nbsp;商户编号</td>
				<td width="5%"  align="center"> : </td> 
				<td width="50%" align="left"> <%=merchantaccount%> </td>
				<td width="5%"  align="center"> - </td> 
				<td width="25%" align="left">merchantaccount</td> 
			</tr>

			<tr>
				<td width="15%" align="left">&nbsp;商户订单号</td>
				<td width="5%"  align="center"> : </td> 
				<td width="50%" align="left"> <%=orderidFromYeepay%> </td>
				<td width="5%"  align="center"> - </td> 
				<td width="25%" align="left">orderid</td> 
			</tr>

			<tr>
				<td width="15%" align="left">&nbsp;易宝流水号</td>
				<td width="5%"  align="center"> : </td> 
				<td width="50%" align="left"> <%=yborderid%> </td>
				<td width="5%"  align="center"> - </td> 
				<td width="25%" align="left">yborderid</td> 
			</tr>

			<tr>
				<td width="15%" align="left">&nbsp;订单金额</td>
				<td width="5%"  align="center"> : </td> 
				<td width="50%" align="left"> <%=amountFromYeepay%> </td>
				<td width="5%"  align="center"> - </td> 
				<td width="25%" align="left">amount</td> 
			</tr>
		</table>

	</body>
</html>
