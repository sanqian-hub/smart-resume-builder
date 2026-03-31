package com.smart.resume.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.smart.resume.model.domain.User;
import com.smart.resume.model.domain.request.AdminUserUpdateRequest;
import com.smart.resume.model.domain.request.UserQueryRequest;
import com.smart.resume.model.domain.request.UserUpdateRequetst;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 用户服务接口
 * @author 三千
 */
public interface UserService extends IService<User> {

    /**
     *
     * @param userAccount   用户账号
     * @param userPassword  用户密码
     * @param checkPassword 二次校验密码
     * @return 新用户ID
     */
    long userRegister(String userAccount, String userPassword, String checkPassword);

    /**
     * 用户登录
     *
     * @param userAccount  用户账号
     * @param userPassword 用户密码
     * @param request 前端的请求
     * @return 脱敏（不包含敏感信息）的用户信息
     */
    User userLogin(String userAccount, String userPassword, HttpServletRequest request);


    /**
     * 用户脱敏
     * @param originUser 原始的用户信息
     * @return 脱敏后的用户信息
     */
    User getSafetyUser(User originUser);

    /**
     * 用户注销
     *
     * @param request HttpServletRequest
     * @return 返回一个int值（目前没多大用）
     */
    int userLogout(HttpServletRequest request);


    /**
     *  用户更新信息（邮箱和用户名）
     * @param request 封装好的用户更新对象
     * @param httpServletRequest HttpServletRequest
     */
    void updateUser(UserUpdateRequetst request,
                           HttpServletRequest httpServletRequest);


    /**
     * 根据request来拿到session中的用户信息
     * @param request HttpServletRequest
     * @return User
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 
     * @param userId 用户ID
     * @param oldPassword  用户旧密码
     * @param newPassword  用户新密码
     * @return 布尔值
     */
    boolean updatePassword(Long userId, String oldPassword, String newPassword);


    /**
     *
     * @param request 分页参数 + 模糊查询参数 + 条件查询参数 + 时间范围查询参数
     * @return 返回封装好的分页对象
     */
    Page<User> listUserByPage(UserQueryRequest request);

    /**
     *
     * @param request  管理员更新用户请求对象
     * @param httpServletRequest  HttpServletRequest
     * @return 布尔值
     */
    boolean updateUserByAdmin(AdminUserUpdateRequest request, HttpServletRequest httpServletRequest);
}
