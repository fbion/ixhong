package com.ixhong.domain.misc;


import com.ixhong.domain.pojo.UserDO;

/**
 * Author: qing
 * Date: 14-10-29
 */
public class LoginContextHolder {

    private static final ThreadLocal<LoginContext> holder = new ThreadLocal<LoginContext>();

    public static LoginContext get() {
        return holder.get();
    }

    public static void set(LoginContext context) {
        holder.set(context);
    }

    public static void clear() {
        holder.remove();
    }

    public static UserDO getLoginUser() {
        LoginContext context = holder.get();
        return context == null ? null : context.getUser();
    }
}
