<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="java.util.Date, java.text.SimpleDateFormat"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%
	SimpleDateFormat dateFormat		= new SimpleDateFormat("yyMMdd_HHmmssSSS");
	String orderid					= "TZTBINDPAY" + dateFormat.format(new Date());
	
	String transtime				= String.valueOf((int)(System.currentTimeMillis()/1000));
%>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html;charset=UTF-8" />
	<title>支付接口演示</title>
</head>
	<body>
		<br>
		<table width="80%" border="0" align="center" cellpadding="5" cellspacing="0" style="border:solid 1px #107929">
			<tr>
		  		<th align="center" height="20" colspan="5" bgcolor="#6BBE18">
					支付接口演示	
				</th>
		  	</tr> 

			<form method="post" action="../jsp/sendDirectBindPay.jsp" target="_blank" accept-charset="UTF-8">
				<tr >
					<td width="20%" align="left">&nbsp;商户订单号</td>
					<td width="5%"  align="center"> : &nbsp;</td> 
					<td width="55%" align="left"> 
						<input size="70" type="text" name="orderid" value="<%=orderid%>"/>
						<span style="color:#FF0000;font-weight:100;">*</span>
					</td>
					<td width="5%"  align="center"> - </td> 
					<td width="15%" align="left">orderid</td> 
				</tr>

				<tr >
					<td width="20%" align="left">&nbsp;交易时间</td>
					<td width="5%"  align="center"> : &nbsp;</td> 
					<td width="55%" align="left"> 
						<input size="70" type="text" name="transtime" value="<%=transtime%>" />
						<span style="color:#FF0000;font-weight:100;">*</span>
					</td>
					<td width="5%"  align="center"> - </td> 
					<td width="15%" align="left">transtime</td> 
				</tr>
				
				<tr >
					<td width="20%" align="left">&nbsp;交易币种</td>
					<td width="5%"  align="center"> : &nbsp;</td> 
					<td width="55%" align="left"> 
						<input size="70" type="text" name="currency" value="156" />
					</td>
					<td width="5%"  align="center"> - </td> 
					<td width="15%" align="left">currency</td> 
				</tr>
				
				<tr >
					<td width="20%" align="left">&nbsp;交易金额</td>
					<td width="5%"  align="center"> : &nbsp;</td> 
					<td width="55%" align="left"> 
						<input size="70" type="text" name="amount" value="" />
						<span style="color:#FF0000;font-weight:100;">*</span>
					</td>
					<td width="5%"  align="center"> - </td> 
					<td width="15%" align="left">amount</td> 
				</tr>
				
				<tr >
					<td width="20%" align="left">&nbsp;商品名称</td>
					<td width="5%"  align="center"> : &nbsp;</td> 
					<td width="55%" align="left"> 
						<input size="70" type="text" name="productname" value="productname" />
						<span style="color:#FF0000;font-weight:100;">*</span>
					</td>
					<td width="5%"  align="center"> - </td> 
					<td width="15%" align="left">productname</td> 
				</tr>
				
				<tr >
					<td width="20%" align="left">&nbsp;商品描述</td>
					<td width="5%"  align="center"> : &nbsp;</td> 
					<td width="55%" align="left"> 
						<input size="70" type="text" name="productdesc" value="" />
					</td>
					<td width="5%"  align="center"> - </td> 
					<td width="15%" align="left">productdesc</td> 
				</tr>
				
				<tr >
					<td width="20%" align="left">&nbsp;用户标识</td>
					<td width="5%"  align="center"> : &nbsp;</td> 
					<td width="55%" align="left"> 
						<input size="70" type="text" name="identityid" value="" />
						<span style="color:#FF0000;font-weight:100;">*</span>
					</td>
					<td width="5%"  align="center"> - </td> 
					<td width="15%" align="left">identityid</td> 
				</tr>
				
				<tr >
					<td width="20%" align="left">&nbsp;用户标识类型</td>
					<td width="5%"  align="center"> : &nbsp;</td> 
					<td width="55%" align="left"> 
						<input size="70" type="text" name="identitytype" value="2" />
						<span style="color:#FF0000;font-weight:100;">*</span>
					</td>
					<td width="5%"  align="center"> - </td> 
					<td width="15%" align="left">identitytype</td> 
				</tr>
				
				<tr >
					<td width="20%" align="left">&nbsp;卡号前6位</td>
					<td width="5%"  align="center"> : &nbsp;</td> 
					<td width="55%" align="left"> 
						<input size="70" type="text" name="card_top" value="" />
						<span style="color:#FF0000;font-weight:100;">*</span>
					</td>
					<td width="5%"  align="center"> - </td> 
					<td width="15%" align="left">card_top</td> 
				</tr>
				
				<tr >
					<td width="20%" align="left">&nbsp;卡号后4位</td>
					<td width="5%"  align="center"> : &nbsp;</td> 
					<td width="55%" align="left"> 
						<input size="70" type="text" name="card_last" value="" />
						<span style="color:#FF0000;font-weight:100;">*</span>
					</td>
					<td width="5%"  align="center"> - </td> 
					<td width="15%" align="left">card_last</td> 
				</tr>
				
				<tr >
					<td width="20%" align="left">&nbsp;订单有效期</td>
					<td width="5%"  align="center"> : &nbsp;</td> 
					<td width="55%" align="left"> 
						<input size="70" type="text" name="orderexpdate" value="60" />
					</td>
					<td width="5%"  align="center"> - </td> 
					<td width="15%" align="left">orderexpdate</td> 
				</tr>
				
				<tr >
					<td width="20%" align="left">&nbsp;回调地址</td>
					<td width="5%"  align="center"> : &nbsp;</td> 
					<td width="55%" align="left"> 
						<input size="70" type="text" name="callbackurl" value="" />
						<span style="color:#FF0000;font-weight:100;">*</span>
					</td>
					<td width="5%"  align="center"> - </td> 
					<td width="15%" align="left">callbackurl</td> 
				</tr>
				
				<tr >
					<td width="20%" align="left">&nbsp;设备唯一标识</td>
					<td width="5%"  align="center"> : &nbsp;</td> 
					<td width="55%" align="left"> 
						<input size="70" type="text" name="imei" value="" />
					</td>
					<td width="5%"  align="center"> - </td> 
					<td width="15%" align="left">imei</td> 
				</tr>
				
				<tr >
					<td width="20%" align="left">&nbsp;用户请求ip</td>
					<td width="5%"  align="center"> : &nbsp;</td> 
					<td width="55%" align="left"> 
						<input size="70" type="text" name="userip" value="192.168.0.1" />
						<span style="color:#FF0000;font-weight:100;">*</span>
					</td>
					<td width="5%"  align="center"> - </td> 
					<td width="15%" align="left">userip</td> 
				</tr>
				
				<tr >
					<td width="20%" align="left">&nbsp;用户UA</td>
					<td width="5%"  align="center"> : &nbsp;</td> 
					<td width="55%" align="left"> 
						<input size="70" type="text" name="ua" value="" />
					</td>
					<td width="5%"  align="center"> - </td> 
					<td width="15%" align="left">ua</td> 
				</tr>
				
				<tr >
					<td width="20%" align="left">&nbsp;</td>
					<td width="5%"  align="center">&nbsp;</td> 
					<td width="55%" align="left"> 
						<input type="submit" value="单击提交" />
					</td>
					<td width="5%"  align="center">&nbsp;</td> 
					<td width="15%" align="left">&nbsp;</td> 
				</tr>

			</form>
		</table>
</body>
</html>
