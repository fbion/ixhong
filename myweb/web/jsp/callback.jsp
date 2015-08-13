<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.Map" import="java.util.HashMap" import="com.yeepay.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%! String formatString(String text){
	return text==null ? "" : text.trim();
	}
%>
<%
	String data					= formatString(request.getParameter("data"));
	String encryptkey			= formatString(request.getParameter("encryptkey"));
	
	Map<String, String>	result	= TZTService.decryptCallbackData(data, encryptkey);
	String merchantaccount 		= result.get("merchantaccount");
	String orderid              = result.get("orderid");
	String yborderid            = result.get("yborderid");
	String amount               = result.get("amount");
	String identityid           = result.get("identityid");
	String card_top	            = result.get("card_top	");
	String card_last            = result.get("card_last");
	String status               = result.get("status");
	String bankcode             = result.get("bankcode");
	String bank		            = result.get("bank");
	String bankcardtype         = result.get("bankcardtype");
	String errorcode            = result.get("errorcode");
	String errormsg             = result.get("errormsg");
	String sign					= result.get("sign");
	String customError			= result.get("customError");

	if(!"".equals(customError)) {
		out.println("sign 验证失败!!!");
		out.println("<br>customError : " + customError);
		return;
	} else {
		out.println("SUCCESS");
	}
%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>投资通-通知回调</title>
</head>
	<body>
		<br /> <br />
		<table width="70%" border="0" align="center" cellpadding="5" cellspacing="0" 
							style="word-break:break-all; border:solid 1px #107929">
			<tr>
		  		<th align="center" height="30" colspan="5" bgcolor="#6BBE18">
					投资通-通知回调
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
				<td width="25%" align="left">&nbsp;商户订单号</td>
				<td width="5%"  align="center"> : </td> 
				<td width="50%" align="left"> <%=orderid%> </td>
				<td width="5%"  align="center"> - </td> 
				<td width="15%" align="left">orderid</td> 
			</tr>

			<tr>
				<td width="25%" align="left">&nbsp;易宝流水号</td>
				<td width="5%"  align="center"> : </td> 
				<td width="50%" align="left"> <%=yborderid%> </td>
				<td width="5%"  align="center"> - </td> 
				<td width="15%" align="left">yborderid</td> 
			</tr>

			<tr>
				<td width="25%" align="left">&nbsp;订单金额</td>
				<td width="5%"  align="center"> : </td> 
				<td width="50%" align="left"> <%=amount%> </td>
				<td width="5%"  align="center"> - </td> 
				<td width="15%" align="left">amount</td> 
			</tr>

			<tr>
				<td width="25%" align="left">&nbsp;用户标识</td>
				<td width="5%"  align="center"> : </td> 
				<td width="50%" align="left"> <%=identityid%> </td>
				<td width="5%"  align="center"> - </td> 
				<td width="15%" align="left">identityid</td> 
			</tr>

			<tr>
				<td width="25%" align="left">&nbsp;卡号前6位</td>
				<td width="5%"  align="center"> : </td> 
				<td width="50%" align="left"> <%=card_top%> </td>
				<td width="5%"  align="center"> - </td> 
				<td width="15%" align="left">card_top</td> 
			</tr>

			<tr>
				<td width="25%" align="left">&nbsp;卡号后4位</td>
				<td width="5%"  align="center"> : </td> 
				<td width="50%" align="left"> <%=card_last%> </td>
				<td width="5%"  align="center"> - </td> 
				<td width="15%" align="left">card_last</td> 
			</tr>

			<tr>
				<td width="25%" align="left">&nbsp;支付状态</td>
				<td width="5%"  align="center"> : </td> 
				<td width="50%" align="left"> <%=status%> </td>
				<td width="5%"  align="center"> - </td> 
				<td width="15%" align="left">status</td> 
			</tr>

			<tr>
				<td width="25%" align="left">&nbsp;错误码</td>
				<td width="5%"  align="center"> : </td> 
				<td width="50%" align="left"> <%=errorcode%> </td>
				<td width="5%"  align="center"> - </td> 
				<td width="15%" align="left">errorcode</td> 
			</tr>

			<tr>
				<td width="25%" align="left">&nbsp;错误信息</td>
				<td width="5%"  align="center"> : </td> 
				<td width="50%" align="left"> <%=errormsg%> </td>
				<td width="5%"  align="center"> - </td> 
				<td width="15%" align="left">errormsg</td> 
			</tr>

		</table>

	</body>
</html>
