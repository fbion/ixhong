/**
 * Created by chenguang on 2015/4/28 0028.
 */
$(document).ready(function() {
    //期限
    $("#month_limit").bind('focus', function() {
        $(this).siblings('.msg_box').css({
            'display' : "inline-block"
        });
    }).bind('blur', function() {
        _check_month_limit('month_limit');
    });

    //年利率
    $("#rate").bind('focus', function() {
        $(this).siblings('.msg_box').css({
            'display' : "inline-block"
        });
    }).bind('blur', function() {
        _check_rate('rate');
    });
})

function  _check_month_limit(id) {
    var month_limit = $('#' + id);
    var month_limit_val = month_limit.val();
    var arr_month_limit = new Array(6, 12, 18, 24);
    if(month_limit_val == "") {
        _check_error(id, "*请输入期限");
        return false;
    }else if(!isExist(arr_month_limit, month_limit_val)) {
        _check_error(id, "*您输入的还款期有误");
    }else {
        _check_right(id, "");
        return true;
    }
}

function isExist(arr, val) {
    for(var i=0; i<arr.length; i++) {
        if(arr[i] == val) {
            return true;
        }
    }
    return false;
}

function  _check_rate(id) {
    var rate = $('#' + id);
    var rate_val = rate.val();
    var splits = rate_val.split(".");
    var num = rate_val.replace(/[\d.\.]/g,"").length;

    if(rate_val == "") {
        _check_error(id, "*请输入年化利率");
        return false;
    }else if(num > 0) {
        _check_error(id, "*请输入整数或小数");
        return false;
    }else if(splits.length > 2) {
        _check_error(id, "*利率保留2位小数");
        return false;
    }else if(splits.length == 2 && splits[1].length > 2) {
        _check_error(id, "*利率保留2位小数");
        return false;
    }else if(rate_val < 0 || rate_val > 30) {
        _check_error(id, "*利率最低为0，最高为30");
        return false;
    }else {
        _check_right(id, "");
        return true;
    }
}

//错误提示
function _check_error(_name, _content) {
    var __name = $('#' + _name);
    __name.siblings('.msg_box').removeClass().addClass('msg_box error');
    __name.siblings('.msg_box').children('font').html(_content);
    __name.siblings('.msg_box').css({
        'display': 'inline-block'
    });
    return false;
}

function _check_right(_name, value) {
    var __name = $('#' + _name);
    __name.siblings('.msg_box').removeClass().addClass('msg_box right');
    __name.siblings('.msg_box').children('font').html(value);
    __name.siblings('.msg_box').css({
        'display': 'inline-block'
    });
    return true;
}




