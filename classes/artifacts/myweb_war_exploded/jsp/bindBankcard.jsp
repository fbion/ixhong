<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="java.util.Date, java.text.SimpleDateFormat"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%
	SimpleDateFormat dateFormat		= new SimpleDateFormat("yyMMdd_HHmmssSSS");
	String requestid				= "TZTBINDBANKCARD" + dateFormat.format(new Date());
%>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html;charset=UTF-8" />
	<title>银行卡绑定接口演示</title>
</head>
	<body>
		<br>
		<table width="80%" border="0" align="center" cellpadding="5" cellspacing="0" style="border:solid 1px #107929">
			<tr>
		  		<th align="center" height="20" colspan="5" bgcolor="#6BBE18">
					请输入银行卡绑定参数	
				</th>
		  	</tr> 

			<form method="post" action="../jsp/sendBindBankcard.jsp" target="_blank" accept-charset="UTF-8">
				<tr >
					<td width="20%" align="left">&nbsp;绑卡请求号</td>
					<td width="5%"  align="center"> : &nbsp;</td> 
					<td width="55%" align="left"> 
						<input size="70" type="text" name="requestid" value="<%=requestid%>"/>
						<span style="color:#FF0000;font-weight:100;">*</span>
					</td>
					<td width="5%"  align="center"> - </td> 
					<td width="15%" align="left">requestid</td> 
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
					<td width="20%" align="left">&nbsp;银行卡号</td>
					<td width="5%"  align="center"> : &nbsp;</td> 
					<td width="55%" align="left"> 
						<input size="70" type="text" name="cardno" value="" />
						<span style="color:#FF0000;font-weight:100;">*</span>
					</td>
					<td width="5%"  align="center"> - </td> 
					<td width="15%" align="left">cardno</td> 
				</tr>
				
				<tr >
					<td width="20%" align="left">&nbsp;证件类型</td>
					<td width="5%"  align="center"> : &nbsp;</td> 
					<td width="55%" align="left"> 
						<input size="70" type="text" name="idcardtype" value="01" />
						<span style="color:#FF0000;font-weight:100;">*</span>
					</td>
					<td width="5%"  align="center"> - </td> 
					<td width="15%" align="left">idcardtype</td> 
				</tr>
								
				<tr >
					<td width="20%" align="left">&nbsp;证件号</td>
					<td width="5%"  align="center"> : &nbsp;</td> 
					<td width="55%" align="left"> 
						<input size="70" type="text" name="idcardno" value="" />
						<span style="color:#FF0000;font-weight:100;">*</span>
					</td>
					<td width="5%"  align="center"> - </td> 
					<td width="15%" align="left">idcardno</td> 
				</tr>
				
				<tr >
					<td width="20%" align="left">&nbsp;持卡人姓名</td>
					<td width="5%"  align="center"> : &nbsp;</td> 
					<td width="55%" align="left"> 
						<input size="70" type="text" name="username" value="" />
						<span style="color:#FF0000;font-weight:100;">*</span>
					</td>
					<td width="5%"  align="center"> - </td> 
					<td width="15%" align="left">username</td> 
				</tr>

				<tr >
					<td width="20%" align="left">&nbsp;银行预留手机号</td>
					<td width="5%"  align="center"> : &nbsp;</td> 
					<td width="55%" align="left"> 
						<input size="70" type="text" name="phone" value="" />
						<span style="color:#FF0000;font-weight:100;">*</span>
					</td>
					<td width="5%"  align="center"> - </td> 
					<td width="15%" align="left">phone</td> 
				</tr>
				
				<tr >
					<td width="20%" align="left">&nbsp;用户注册手机号</td>
					<td width="5%"  align="center"> : &nbsp;</td> 
					<td width="55%" align="left"> 
						<input size="70" type="text" name="registerphone" value="" />
					</td>
					<td width="5%"  align="center"> - </td> 
					<td width="15%" align="left">registerphone</td> 
				</tr>
				
				<tr >
					<td width="20%" align="left">&nbsp;用户注册日期</td>
					<td width="5%"  align="center"> : &nbsp;</td> 
					<td width="55%" align="left"> 
						<input size="70" type="text" name="registerdate" value="" />
					</td>
					<td width="5%"  align="center"> - </td> 
					<td width="15%" align="left">registerdate</td> 
				</tr>
				
				<tr >
					<td width="20%" align="left">&nbsp;用户注册ip</td>
					<td width="5%"  align="center"> : &nbsp;</td> 
					<td width="55%" align="left"> 
						<input size="70" type="text" name="registerip" value="" />
					</td>
					<td width="5%"  align="center"> - </td> 
					<td width="15%" align="left">registerip</td> 
				</tr>
				
				<tr >
					<td width="20%" align="left">&nbsp;用户注册证件类型</td>
					<td width="5%"  align="center"> : &nbsp;</td> 
					<td width="55%" align="left"> 
						<input size="70" type="text" name="registeridcardtype" value="" />
					</td>
					<td width="5%"  align="center"> - </td> 
					<td width="15%" align="left">registeridcardtype</td> 
				</tr>
				
				<tr >
					<td width="20%" align="left">&nbsp;用户注册证件号</td>
					<td width="5%"  align="center"> : &nbsp;</td> 
					<td width="55%" align="left"> 
						<input size="70" type="text" name="registeridcardno" value="" />
					</td>
					<td width="5%"  align="center"> - </td> 
					<td width="15%" align="left">registeridcardno</td> 
				</tr>
				
				<tr >
					<td width="20%" align="left">&nbsp;用户注册联系方式</td>
					<td width="5%"  align="center"> : &nbsp;</td> 
					<td width="55%" align="left"> 
						<input size="70" type="text" name="registercontact" value="" />
					</td>
					<td width="5%"  align="center"> - </td> 
					<td width="15%" align="left">registercontact</td> 
				</tr>
				
				<tr >
					<td width="20%" align="left">&nbsp;用户使用的操作系统</td>
					<td width="5%"  align="center"> : &nbsp;</td> 
					<td width="55%" align="left"> 
						<input size="70" type="text" name="os" value="" />
					</td>
					<td width="5%"  align="center"> - </td> 
					<td width="15%" align="left">os</td> 
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
					<td width="20%" align="left">&nbsp;用户UA信息</td>
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
