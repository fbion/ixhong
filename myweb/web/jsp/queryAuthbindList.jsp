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

	String identityid           	= formatString(request.getParameter("identityid"));
	String identitytype         	= formatString(request.getParameter("identitytype"));

	Map<String, String> result		= TZTService.queryAuthbindList(identityid, identitytype);
	String merchantaccount			= formatString(result.get("merchantaccount"));
	String identityidFromYeepay		= formatString(result.get("identityid"));
	String identitytypeFromYeeapy	= formatString(result.get("identitytype"));
	String cardlist					= formatString(result.get("cardlist"));
	String error_code				= formatString(result.get("error_code"));
	String error_msg				= formatString(result.get("error_msg"));
	String sign						= formatString(result.get("sign"));
	String customError				= formatString(result.get("customError"));

	if(!"".equals(error_code)) {
		out.println("error_code : " + error_code);
		out.println("error_msg : " + error_msg);
		return;
	} else if(!"".equals(customError)) {
		out.println("customError : " + customError);
		return;
	}

	if(cardlist.length() < 3) {
		cardlist	= "该用户标识[" + identityid + "]下，无绑定银行卡!";
	}
%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>绑卡查询结果</title>
</head>
	<body>
		<br /> <br />
		<table width="80%" border="0" align="center" cellpadding="5" cellspacing="0" 
							style="word-break:break-all; border:solid 1px #107929">
			<tr>
		  		<th align="center" height="30" colspan="5" bgcolor="#6BBE18">
					绑卡查询结果
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
				<td width="15%" align="left">&nbsp;用户标识</td>
				<td width="5%"  align="center"> : </td> 
				<td width="50%" align="left"> <%=identityidFromYeepay%> </td>
				<td width="5%"  align="center"> - </td> 
				<td width="25%" align="left">identityid</td> 
			</tr>

			<tr>
				<td width="15%" align="left">&nbsp;用户标识类型</td>
				<td width="5%"  align="center"> : </td> 
				<td width="50%" align="left"> <%=identitytypeFromYeeapy%> </td>
				<td width="5%"  align="center"> - </td> 
				<td width="25%" align="left">identitytype</td> 
			</tr>

			<tr>
				<td width="15%" align="left" rowspan="6">&nbsp;已绑定的银行卡</td>
				<td width="5%"  align="center" rowspan="6"> : </td> 
				<td width="60%" align="left" rowspan="6">
					<%=cardlist%>
					<%--span style="font-size:12px; color:#FF0000; font-weight:100;"> 
						→ cardlist格式：JSON	
					</span--%>
				</td>
				<td width="5%"  align="center" rowspan="6"> - </td> 
				<td width="25%" align="left" rowspan="6">cardlist</td> 
			</tr>

		</table>

	</body>
</html>
