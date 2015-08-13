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

	String requestid 			= formatString(request.getParameter("requestid"));
	String identitytype 		= formatString(request.getParameter("identitytype"));
	String identityid 			= formatString(request.getParameter("identityid"));
	String cardno 				= formatString(request.getParameter("cardno"));
	String idcardtype 			= formatString(request.getParameter("idcardtype"));
	String idcardno 			= formatString(request.getParameter("idcardno"));
	String username 			= formatString(request.getParameter("username"));
	String phone 				= formatString(request.getParameter("phone"));
	String registerphone 		= formatString(request.getParameter("registerphone"));
	String registerdate 		= formatString(request.getParameter("registerdate"));
	String registerip 			= formatString(request.getParameter("registerip"));
	String registeridcardtype	= formatString(request.getParameter("registeridcardtype"));
	String registeridcardno 	= formatString(request.getParameter("registeridcardno"));
	String registercontact 		= formatString(request.getParameter("registercontact"));
	String os 					= formatString(request.getParameter("os"));
	String imei 				= formatString(request.getParameter("imei"));
	String userip 				= formatString(request.getParameter("userip"));
	String ua 					= formatString(request.getParameter("ua"));

	Map<String, String> params 	= new HashMap<String, String>();
	params.put("requestid", 		requestid);
	params.put("identitytype", 		identitytype);
	params.put("identityid", 		identityid);
	params.put("cardno", 			cardno);
	params.put("idcardtype", 		idcardtype);
	params.put("idcardno", 			idcardno);
	params.put("username", 			username);
	params.put("phone", 			phone);
	params.put("registerphone", 	registerphone);
	params.put("registerdate",	 	registerdate);
	params.put("registerip", 		registerip);
	params.put("registeridcardno", 	registeridcardno);
	params.put("registercontact", 	registercontact);
	params.put("os", 				os);
	params.put("imei", 				imei);
	params.put("userip", 			userip);
	params.put("ua", 				ua);
	params.put("registeridcardtype", registeridcardtype);

	Map<String, String> result			= TZTService.bindBankcard(params);
	String merchantaccount				= formatString(result.get("merchantaccount")); 
    String requestidFromYeepay 	   		= formatString(result.get("requestid")); 
    String codesender 			   		= formatString(result.get("codesender")); 
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
<meta http-equiv="Content-Type" content="text/html; charset=GBK">
<title>绑卡请求接口</title>
</head>
	<body>
		<br /> <br />
		<table width="80%" border="0" align="center" cellpadding="10" cellspacing="0" 
							style="word-break:break-all; border:solid 1px #107929">
			<tr>
		  		<th align="center" height="30" colspan="5" bgcolor="#6BBE18">
					绑卡请求接口返回参数
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
				<td width="15%" align="left">&nbsp;绑卡请求号</td>
				<td width="5%"  align="center"> : </td> 
				<td width="50%" align="left"> <%=requestidFromYeepay%> </td>
				<td width="5%"  align="center"> - </td> 
				<td width="25%" align="left">requestid</td> 
			</tr>

			<tr>
				<td width="15%" align="left">&nbsp;短信发送方</td>
				<td width="5%"  align="center"> : </td> 
				<td width="50%" align="left"> <%=codesender%> </td>
				<td width="5%"  align="center"> - </td> 
				<td width="25%" align="left">codesender</td> 
			</tr>
		</table>

	</body>
</html>
