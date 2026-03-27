package com.smart.resume.constant;

/**
 * 用户常量
 */
public interface UserConstant {

    // 接口里面的变量默认是public static final的

    /**
     * 盐值SALT
     * 用来混淆密码
     * 这样相同的密码经过加密后得到的密文是不一样的
     */
    String SALT = "sanqian";

    /**
     * USER_LOGIN_STATE => 用户登录态键
     * 用来做Session的attribute的键
     * 用来记录用户的一些登录状态
     */
    String USER_LOGIN_STATE = "userLoginState";

    /*
        ------ 用户权限 ------  role
        0 - 普通用户
        1 - 管理员
     */
    int DEFAULT_ROLE = 0;  // 默认用户（普通用户）权限
    int ADMIN_ROLE = 1;  // 管理员权限
}
