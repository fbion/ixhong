function check_code(id) {
    var code = $('#'+id).val();
    var len = _check_len(code);
    var reg = /^[a-zA-Z0-9]{6,10}$/;
    if (code == '') {
        alert('请输入学校编号');
        return false;
    } else if (!reg.test(code)) {
        alert('学校编号由6到10位字母或数字组成');
        return false;
    }
    return true;
}
function check_bail_percent(id) {
    var bail_percent = $('#'+id).val();
    var reg = /^(([1-9]\d{0,9})|0)(\.\d{1,2})?$/;
    if (bail_percent == '') {
        var msg = '';
        if (id == 'undergraduate') {
            msg = '本科及以上';
        }
        if (id == 'junior') {
            msg = '大专学历';
        }
        if (id == 'junior_lower') {
            msg = '大专以下';
        }
        alert('请输入保证金比例(' + msg + ')');
        return false;
    } else if (!reg.test(bail_percent)) {
        alert('保证金比例为最多两位小数正数')
        return false;
    } else if (bail_percent < 0 || bail_percent > 40) {
        alert('保证金比例必须在0-40之间');
        return false;
    }
    return true;
}
function check_lending_quotas(id) {
    var bail_percent = $('#'+id).val();
    var reg = /^(([1-9]\d{0,9})|0)(\.\d{1,2})?$/;
    if (bail_percent == '') {
        alert('请输入额度限制');
        return false;
    } else if (!reg.test(bail_percent)) {
        alert('额度为最多两位小数正数')
        return false;
    } else if (bail_percent > 1000) {
        alert('额度限制必须在1000之内');
        return false;
    }
    return true;
}
function check_audit_note(id) {
    var audit_note = $('#'+id).val();
    var len = _check_len(audit_note);
    if (audit_note == '') {
        alert('请输入审核留言');
        return false;
    } else if (len < 4) {
        alert('审核留言请至少输入4个字符，一个中文为2个字符');
        return false;
    } else if (len > 122) {
        alert('审核留言最多输入122个字符，一个中文为2个字符');
        return false;
    }
    return true;
}

function check_level(name) {
    if ($('input[name="level"]:checked ').length == 0) {
        alert('请选择机构类别');
        return false;
    }
    return true;
}
function check_type(name) {
    if ($('input[name="check_type"]:checked ').length == 0) {
        alert('请选择复检频率');
        return false;
    }
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