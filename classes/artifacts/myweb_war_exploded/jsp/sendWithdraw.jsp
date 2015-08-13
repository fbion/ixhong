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

	String requestid        = formatString(request.getParameter("requestid"));
	String identityid       = formatString(request.getParameter("identityid"));
	String identitytype     = formatString(request.getParameter("identitytype"));
	String card_top         = formatString(request.getParameter("card_top"));
	String card_last        = formatString(request.getParameter("card_last"));
	String amount           = formatString(request.getParameter("amount"));
	String currency         = formatString(request.getParameter("currency"));
	String drawtype         = formatString(request.getParameter("drawtype"));
	String imei             = formatString(request.getParameter("imei"));
	String userip           = formatString(request.getParameter("userip"));
	String ua               = formatString(request.getParameter("ua"));

	Map<String, String> params 	= new HashMap<String, String>();
	params.put("requestid", 		requestid);
	params.put("identityid", 		identityid);
	params.put("identitytype", 		identitytype);
	params.put("card_top", 			card_top);
	params.put("card_last", 		card_last);
	params.put("amount", 			amount);
	params.put("currency", 			currency);
	params.put("drawtype", 			drawtype);
	params.put("imei", 				imei);
	params.put("userip", 			userip);
	params.put("ua", 				ua);

	Map<String, String> result			= TZTService.withdraw(params);
	String merchantaccount				= formatString(result.get("merchantaccount")); 
    String requestidFromYeepay  	 	= formatString(result.get("requestid")); 
    String ybdrawflowid 	   			= formatString(result.get("ybdrawflowid")); 
    String amountFromYeepay 			= formatString(result.get("amount")); 
    String card_topFromYeepay 	   		= formatString(result.get("card_top")); 
    String card_lastFromYeepay 			= formatString(result.get("card_last")); 
    String status 			   			= formatString(result.get("status")); 
    String signFromYeepay 		   		= formatString(result.get("sign")); 
    String error_code	   				= formatString(result.get("error_code")); 
    String error_msg	   				= formatString(result.get("error_msg")); 
    String customError	   				= formatString(result.get("customError")); 

	if(!"".equals(error_code)) {
		out.println("error_code : " + error_code);
		out.println("<br>error_msg: " + error_msg);
		return;
	} else if(!"".equals(customError)) {
		out.println("signError : " + customError);
		return;
	}
%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>提现接口同步返回</title>
</head>
	<body>
		<br /> <br />
		<table width="80%" border="0" align="center" cellpadding="10" cellspacing="0" 
							style="word-break:break-all; border:solid 1px #107929">
			<tr>
		  		<th align="center" height="30" colspan="5" bgcolor="#6BBE18">
					提现接口同步返回
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
				<td width="15%" align="left">&nbsp;提现请求号</td>
				<td width="5%"  align="center"> : </td> 
				<td width="50%" align="left"> <%=requestidFromYeepay%> </td>
				<td width="5%"  align="center"> - </td> 
				<td width="25%" align="left">requestid</td> 
			</tr>

			<tr>
				<td width="15%" align="left">&nbsp;易宝提现流水号</td>
				<td width="5%"  align="center"> : </td> 
				<td width="50%" align="left"> <%=ybdrawflowid%> </td>
				<td width="5%"  align="center"> - </td> 
				<td width="25%" align="left">ybdrawflowid</td> 
			</tr>

			<tr>
				<td width="15%" align="left">&nbsp;提现金额</td>
				<td width="5%"  align="center"> : </td> 
				<td width="50%" align="left"> <%=amountFromYeepay%> </td>
				<td width="5%"  align="center"> - </td> 
				<td width="25%" align="left">amount</td> 
			</tr>

			<tr>
				<td width="15%" align="left">&nbsp;卡号前6位</td>
				<td width="5%"  align="center"> : </td> 
				<td width="50%" align="left"> <%=card_topFromYeepay%> </td>
				<td width="5%"  align="center"> - </td> 
				<td width="25%" align="left">card_top</td> 
			</tr>

			<tr>
				<td width="15%" align="left">&nbsp;卡号后4位</td>
				<td width="5%"  align="center"> : </td> 
				<td width="50%" align="left"> <%=card_lastFromYeepay%> </td>
				<td width="5%"  align="center"> - </td> 
				<td width="25%" align="left">card_last</td> 
			</tr>

			<tr>
				<td width="15%" align="left">&nbsp;提现请求状态</td>
				<td width="5%"  align="center"> : </td> 
				<td width="50%" align="left"> <%=status%> </td>
				<td width="5%"  align="center"> - </td> 
				<td width="25%" align="left">status</td> 
			</tr>
		</table>

	</body>
</html>
