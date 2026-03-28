package com.smart.resume.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smart.resume.common.BaseResponse;
import com.smart.resume.common.ErrorCode;
import com.smart.resume.constant.UserConstant;
import com.smart.resume.exception.BusinessException;
import com.smart.resume.model.domain.User;
import com.smart.resume.model.domain.request.*;
import com.smart.resume.model.domain.request.*;
import com.smart.resume.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

import static com.smart.resume.constant.UserConstant.ADMIN_ROLE;

/**
 * 用户接口
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        if (userRegisterRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "注册请求参数不能为空");
        }

        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();

        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "注册必填项不能为空");
        }

        long result = userService.userRegister(userAccount, userPassword, checkPassword);
        return BaseResponse.success(result);
    }

    @PostMapping("/login")
    public BaseResponse<User> userLogin(@RequestBody UserLoginRequest userLoginRequest,
                          HttpServletRequest request) {
        if (userLoginRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "登录请求参数不能为空");
        }

        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();

        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "登录必填项不能为空");
        }

        User user = userService.userLogin(userAccount, userPassword, request);
        return BaseResponse.success(user);
    }

    @PostMapping("/logout")
    public BaseResponse<Integer> userLogout(HttpServletRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,
                    "HttpServletRequest 对象为空，请求上下文异常");
        }
        int result = userService.userLogout(request);
        return BaseResponse.success(result);
    }

    @GetMapping("/search")
    public BaseResponse<List<User>> searchUsers(String username, HttpServletRequest request) {

        if (!isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH, "用户没有查询权限");
        }

        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotBlank(username)) {
            queryWrapper.like("username", username);
        }
        /*
            如果有传username，那就会做模糊查询
            如果没有传值，username为null，那就相当于条件为空，会返回所有用户
         */
        List<User> userList = userService.list(queryWrapper);
        List<User> result = userList.stream()
                .map(userService::getSafetyUser)
                .collect(Collectors.toList());
        return BaseResponse.success(result);
    }


    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteUser(@RequestBody @Valid DeleteRequest request, HttpServletRequest httpServletRequest) {

        if (!isAdmin(httpServletRequest)) {
            throw new BusinessException(ErrorCode.NO_AUTH, "用户没有删除权限");
        }

        Long id = request.getId();

        // 查询用户是否存在
        User user = userService.getById(id);
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在");
        }

        User loginUser = userService.getLoginUser(httpServletRequest);

        if (id.equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "不能删除自己");
        }

        if (user.getUserRole() == ADMIN_ROLE) {
            throw new BusinessException(ErrorCode.NO_AUTH, "不能删除管理员");
        }

        /*
            MBP框架开启了逻辑删除，在删除时会转变为更新
            把isDelete置1，而不是真的去删除这条数据
         */
        boolean result = userService.removeById(id);
        return BaseResponse.success(result);
    }

    /**
     * 管理员鉴权公共方法
     * 判断是否为管理员
     */
    private boolean isAdmin(HttpServletRequest request) {
        Object userObj = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        User user = (User) userObj;

        return user != null && user.getUserRole().equals(ADMIN_ROLE);
    }

    @GetMapping("/current")
    public BaseResponse<User> getCurrentUser(HttpServletRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,
                    "HttpServletRequest 对象为空，请求上下文异常");
        }
        Object loginUserInSession = request.getSession().
                getAttribute(UserConstant.USER_LOGIN_STATE);
        User loginUser = (User) loginUserInSession;
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NO_LOGIN, "无法获取用户登录信息, 请先登录");
        }
        Long userId = loginUser.getId();
        // todo: 校验用户是否合法（用户可能有被封号的状态）
        // 这个东西可以后面来补充完成，目前只要是不被删除就可以了
        // 而mybatis-plus的框架中查用户时有对isDelete逻辑删除的逻辑
        User dbUser = userService.getById(userId);
        User safetyUser = userService.getSafetyUser(dbUser);
        return BaseResponse.success(safetyUser);
    }

    // 用户更新信息：用户名和邮箱
    @PutMapping("/update")
    public BaseResponse<Void> updateUser(@RequestBody @Valid UserUpdateRequetst request,
                                         HttpServletRequest httpServletRequest) {
        if (httpServletRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,
                    "HttpServletRequest 对象为空，请求上下文异常");
        }
        userService.updateUser(request, httpServletRequest);
        return BaseResponse.success(null);
    }

    @PostMapping("/update/password")
    public BaseResponse<Boolean> updatePassword(@RequestBody @Valid PasswordUpdateRequest request,
                                                HttpServletRequest httpServletRequest) {
        User loginUser = userService.getLoginUser(httpServletRequest);

        boolean result = userService.updatePassword(
                loginUser.getId(),
                request.getOldPassword(),
                request.getNewPassword()
        );

        return BaseResponse.success(result);

    }

    /*
        分页查询
     */
    @PostMapping("/list/page")
    public BaseResponse<Page<User>> listUserByPage(
            @RequestBody @Valid UserQueryRequest request,
            HttpServletRequest httpServletRequest) {

        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数不能为空");
        }

        if (!isAdmin(httpServletRequest)) {
            throw new BusinessException(ErrorCode.NO_AUTH, "没有查询权限");
        }

        Page<User> userPage = userService.listUserByPage(request);

        return BaseResponse.success(userPage);
    }

    @PostMapping("/update-by-admin")
    public BaseResponse<Boolean> updateUserByAdmin(
            @RequestBody @Valid AdminUserUpdateRequest request,
            HttpServletRequest httpServletRequest
    ) {

        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数不能为空");
        }

        if (!isAdmin(httpServletRequest)) {
            throw new BusinessException(ErrorCode.NO_AUTH, "没有更新权限");
        }

        if (request.getUsername() == null
                && request.getEmail() == null
                && request.getUserStatus() == null
                && request.getUserRole() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "没有需要更新的字段");
        }

        boolean result = userService.updateUserByAdmin(request, httpServletRequest);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新用户信息失败");
        }
        return BaseResponse.success(result);
    }

}
