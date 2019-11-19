package com.leyou.common.threadlocal;

import com.leyou.common.auth.UserInfo;

/**
 * @author 
 */
public class UserHolder {
    private static final ThreadLocal<UserInfo> TL = new ThreadLocal<>();

    public static void setUser(UserInfo user) {
        TL.set(user);
    }

    public static UserInfo getUser() {
        return TL.get();
    }

    public static void removeUser() {
        TL.remove();
    }
}