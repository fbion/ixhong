$(document).ready(function(){
	var h = $("html").height();
	$("#nav").height(h-85);
	$(".course_main").height(h-85);
	//$("select").change(function(){
	//	var checkText = $(this).find(':selected').text();
	//	$(this).prev().html(checkText);
	//});
//侧导航
	$(".style1").click(function(){
		$(this).parent(".ww").toggleClass("backchange").end().next().toggle("display:block;");
	});
//密码			     
	$('#password').bind('focus', function() {
		$(this).siblings('.tips').css({'display': 'inline-block'});
		$(this).siblings('.msg_box').css({'display': 'inline-block'});
	}).bind('blur', function() {
		$(this).siblings('.tips').hide();
		_check_pwd('password');
	})
	//check the confirm password
	$('#confirm_password').bind('focus', function() {
		$(this).siblings('.msg_box').css({'display': 'inline-block'});
	}).bind('blur', function() {
		_check_con_pwd('password', 'confirm_password');
	})
	function _check_error(_name, _content) {
		var __name = $('#' + _name);
		__name.siblings('.msg_box').removeClass().addClass('msg_box error');
		__name.siblings('.msg_box').children('font').html(_content);
		__name.siblings('.msg_box').css({
			'display' : 'inline-block'
		});
		return false;
	}

	function _check_right(_name,value) {
		var __name = $('#' + _name);
		__name.siblings('.msg_box').removeClass().addClass('msg_box right');
		__name.siblings('.msg_box').children('font').html(value);
		__name.siblings('.msg_box').css({
			'display' : 'inline-block'
		});
		return true;
	}
	function _check_len(s) {
		var l = 0;
		var a = s.split("");
		for (var i = 0; i < a.length; i++) {
			if (a[i].charCodeAt(0) < 299) {
				l++;
			} else {
				l += 2;
			}
		}
		return l;
	}
// check the password
	function _check_pwd(_password) {
		var password = $('#' + _password);
		var pwd_val = password.val();
		var pwd_len = _check_len(pwd_val);
		if(pwd_val==""){
			_check_error(_password, '密码不能为空');
			return false;
		}else if(pwd_len<6 || pwd_len>=20){
			_check_error(_password, '密码必须在6-20个字符之间');
			return false;
		}else{
			_check_right(_password,'');
			return true;
		}
	}

// check the confirm password
	function _check_con_pwd(_password, _confirm_password) {
		var pwd = $('#' + _password);
		var con_pwd = $('#' + _confirm_password);
		var pwd_val = pwd.val();
		var con_pwd_val = con_pwd.val();
		if (pwd_val != con_pwd_val) {
			_check_error(_confirm_password, '两次密码不一致');
			return false;
		} else {
			if (pwd_val == '') {
				_check_error(_confirm_password, '请先输入您的登录密码');
				return false;
			} else {
				_check_right(_confirm_password,'');
			}
		}
	}
// check the strength of the password
	function _check_pwd_stg(password) {
		// CharMode函数
		// 测试某个字符是属于哪一类
		function CharMode(iN) {
			if (iN >= 48 && iN <= 57) // 数字
				return 1;
			if (iN >= 65 && iN <= 90) // 大写字母
				return 2;
			if (iN >= 97 && iN <= 122) // 小写
				return 4;
			else
				return 8; // 特殊字符
		};
		// bitTotal函数
		// 计算出当前密码当中一共有多少种模式
		function bitTotal(num) {
			modes = 0;
			for (i = 0; i < 4; i++) {
				if (num & 1)
					modes++;
				num >>>= 1;
			}
			return modes;
		};
		// checkStrong函数
		// 返回密码的强度级别
		function checkStrong(sPW) {
			if (sPW.length <= 4)
				return 0; // 密码太短
			Modes = 0;
			for (i = 0; i < sPW.length; i++) {
				// 测试每一个字符的类别并统计一共有多少种模式
				Modes |= CharMode(sPW.charCodeAt(i));
			}
			return bitTotal(Modes);
		};
		var pwd = $('#' + password).val();
		var pw_bar = $('.pw_barr');
		if (pwd == null || pwd == '') {
			pw_bar.css({
				'width' : '0px'
			})
		} else {
			S_level = checkStrong(pwd);
			switch (S_level) {
				case 0 :
					pw_bar.css({
						'width' : '0px',
						'background' : '#FF0000'
					});
				case 1 :
					pw_bar.css({
						'width' : '90px',
						'background' : '#FF0000'
					});
					break;
				case 2 :
					pw_bar.css({
						'width' : '180px',
						'background' : '#FF9900'
					});
					break;
				case 3 :
					pw_bar.css({
						'width' : '270px',
						'background' : '#33CC00'
					});
					break;
				default :
					pw_bar.css({
						'width' : '270px',
						'background' : '#33CC00'
					});
			}
		}
		return;
	}

	/**
	 * 所有审核留言的校验
	 * @param id
	 * @returns {boolean}
	 */
	function check_audit_note(id) {
		var audit_note = $('#'+id).val();
		var len = _check_len(audit_note);
		if (audit_note == '') {
			alert('请输入审核留言');
			return false;
		} else if (len < 4) {
			alert('审核留言最少输入4个字符，一个中文占2个字符');
			return false;
		} else if (len > 122) {
			alert('审核留言最多输入122个字符，一个中文占2个字符');
			return false;
		}
		return true;
	}

	/**
	 * 返回顶部按钮效果
	 */
	//当滚动条的位置处于距顶部100像素以下时，跳转链接出现，否则消失
	$(window).scroll(function(){
		if ($(window).scrollTop()>100){
			$("#back-to-top").fadeIn(500);
		}
		else
		{
			$("#back-to-top").fadeOut(500);
		}
	});

	//当点击跳转链接后，回到页面顶部位置
	$("#back-to-top").click(function(){
		$('body,html').animate({scrollTop:0},600);
		return false;
	});

	//设置时间控件的皮肤
	laydate.skin('molv');

	$(".navs>ul>li").click(function(e){
		// var url = e.target.href;
		// if(/\./.test(url)){
		//         return;
		// }

		if ($(this).children('ul').is(':hidden')){
			$(this).addClass('curr').siblings().removeClass('curr');
			$(this).children('ul').show();
			$(this).siblings().children('ul').hide();
		}

		// else{
		// 	$(this).siblings().removeClass('curr');
		// 	$(this).children('ul').hide();
		// }
	})

});