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

	String requestid            	= formatString(request.getParameter("requestid"));
	String ybdrawflowid         	= formatString(request.getParameter("ybdrawflowid"));

	Map<String, String> result		= TZTService.queryWithdraw(requestid, ybdrawflowid);
	String merchantaccount			= formatString(result.get("merchantaccount"));
	String requestidFromYeepay  	= formatString(result.get("requestid"));
	String ybdrawflowidFromYeepay	= formatString(result.get("ybdrawflowid"));
	String amount           		= formatString(result.get("amount"));
	String card_top           		= formatString(result.get("card_top"));
	String card_last           		= formatString(result.get("card_last"));
	String status           		= formatString(result.get("status"));
	String sign             		= formatString(result.get("sign"));
	String error_code       		= formatString(result.get("error_code"));
	String error_msg            	= formatString(result.get("error_msg"));
	String customError        		= formatString(result.get("customError"));

	if(!"".equals(error_code)) {
		out.println("error_code : " + error_code);
		out.println("error_msg : " + error_msg);
		return;
	} else if(!"".equals(customError)) {
		out.println("customError : " + customError);
		return;
	}
%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>提现查询结果</title>
</head>
	<body>
		<br /> <br />
		<table width="70%" border="0" align="center" cellpadding="5" cellspacing="0" 
							style="word-break:break-all; border:solid 1px #107929">
			<tr>
		  		<th align="center" height="30" colspan="5" bgcolor="#6BBE18">
					提现查询结果
				</th>
		  	</tr>

			<tr>
				<td width="25%" align="left">&nbsp;商户编号</td>
				<td width="5%"  align="center"> : </td> 
				<td width="50%" align="left"> <%=merchantaccount%> </td>
				<td width="5%"  align="center"> - </td> 
				<td width="15%" align="left">merchantaccount</td> 
			</tr>

			<tr>
				<td width="25%" align="left">&nbsp;提现请求号</td>
				<td width="5%"  align="center"> : </td> 
				<td width="50%" align="left"> <%=requestidFromYeepay%> </td>
				<td width="5%"  align="center"> - </td> 
				<td width="15%" align="left">requestid</td> 
			</tr>

			<tr>
				<td width="25%" align="left">&nbsp;易宝提现流水号</td>
				<td width="5%"  align="center"> : </td> 
				<td width="50%" align="left"> <%=ybdrawflowidFromYeepay%> </td>
				<td width="5%"  align="center"> - </td> 
				<td width="15%" align="left">ybdrawflowid</td> 
			</tr>

			<tr>
				<td width="25%" align="left">&nbsp;提现金额「分」</td>
				<td width="5%"  align="center"> : </td> 
				<td width="50%" align="left"> <%=amount%> </td>
				<td width="5%"  align="center"> - </td> 
				<td width="15%" align="left">amount</td> 
			</tr>
			
			<tr>
				<td width="15%" align="left">&nbsp;卡号前6位</td>
				<td width="5%"  align="center"> : </td> 
				<td width="50%" align="left"> <%=card_top%> </td>
				<td width="5%"  align="center"> - </td> 
				<td width="25%" align="left">card_top</td> 
			</tr>

			<tr>
				<td width="15%" align="left">&nbsp;卡号后4位</td>
				<td width="5%"  align="center"> : </td> 
				<td width="50%" align="left"> <%=card_last%> </td>
				<td width="5%"  align="center"> - </td> 
				<td width="25%" align="left">card_last</td> 
			</tr>

			<tr>
				<td width="15%" align="left">&nbsp;提现状态</td>
				<td width="5%"  align="center"> : </td> 
				<td width="50%" align="left"> <%=status%> </td>
				<td width="5%"  align="center"> - </td> 
				<td width="25%" align="left">status</td> 
			</tr>
				
		</table>

	</body>
</html>
