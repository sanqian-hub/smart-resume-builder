package com.smart.resume.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smart.resume.common.ErrorCode;
import com.smart.resume.constant.Const;
import com.smart.resume.constant.UserConstant;
import com.smart.resume.exception.BusinessException;
import com.smart.resume.model.domain.User;
import com.smart.resume.model.domain.request.AdminUserUpdateRequest;
import com.smart.resume.model.domain.request.UserQueryRequest;
import com.smart.resume.model.domain.request.UserUpdateRequetst;
import com.smart.resume.service.UserService;
import com.smart.resume.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static com.smart.resume.constant.UserConstant.ADMIN_ROLE;

/**
 * 用户服务实现类
 * @author 三千
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService{

    @Autowired
    private UserMapper userMapper;

    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            // 暂时return-1，后面可以用别的处理方法，比如抛异常
            // 暂时返回一个负数表示非法的

            // todo: 后面修改为自定义异常
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }

        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号太短");
        }

        if (userPassword.length() < 6 || checkPassword.length() < 6) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码太短");
        }

        // 账户不能包含特殊字符
        // 这里用正则表达式进行校验
        // 用户账号只能包含大小写和数字和.
        String regex = "^[a-zA-Z0-9]+$";
        // 3. 匹配正则：不匹配则说明包含特殊字符
        if (!userAccount.matches(regex)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账户不能包含特殊字符");
        }

        // 密码和校验密码相同
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户两次输入的密码不匹配");
        }

        // 账户不能重复，所以我们要先去查数据库中是否已经存在userAccount
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        // 第一个参数 "userAccount"：数据库表的列名
        // 第二个参数 userAccount：要匹配的具体值
        queryWrapper.eq("userAccount", userAccount);
        // SELECT COUNT(*) FROM user WHERE userAccount = ?
        long count = this.count(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号已存在");
        }

        // 2. 加密
        // 这里加密直接用工具库就行了，做的比较简单
        // 复杂一点的项目中可以用一些复杂的加密算法
        String encryptPwd = DigestUtils.md5DigestAsHex((UserConstant.SALT + userPassword)
                .getBytes(StandardCharsets.UTF_8));

        // 3. 向数据库插入数据
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPwd);

        String defaultAvatarUrl = "https://user-avatar-1333069720.cos.ap-guangzhou." +
                "myqcloud.com/avatar/27c4523dea6e9215fa7d72670266339b.jpg";
        // 新用户默认头像，随机id
        user.setUsername("用户_" + UUID.randomUUID().toString().substring(0, 6));
        if (user.getAvatarUrl() == null) {
            user.setAvatarUrl(defaultAvatarUrl);
        }

        boolean result = this.save(user);

        // 这里为什么要加一个判断，如果直接返回用户id，可能会报错
        // 我们这个方法是long类型，但是对象里用的是Long，如果返回一个null，null无法放入long中
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "服务器异常，用户注册失败");
        }
        return user.getId();
    }

    @Override
    public User userLogin(String userAccount, String userPassword, HttpServletRequest request) {

        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            // 暂时return-1，后面可以用别的处理方法，比如抛异常
            // 暂时返回一个负数表示非法的
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号或密码为空");
        }

        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号不能小于4位");
        }

        if (userPassword.length() < 6) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码不能小于6位");
        }

        // 账户不能包含特殊字符
        // 这里用正则表达式进行校验
        // 用户账号只能包含大小写和数字
        String regex = "^[a-zA-Z0-9]+$";
        // 3. 匹配正则：不匹配则说明包含特殊字符
        if (!userAccount.matches(regex)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号不能包含特殊字符");
        }


        // 2. 对用户输入的密码进行加密
        String encryptPwd = DigestUtils.md5DigestAsHex((UserConstant.SALT + userPassword)
                .getBytes(StandardCharsets.UTF_8));

        // 3. 数据库里查询用户，看账户和密码是否同时成立，则判断为有效用户
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        // 第一个参数 "userAccount"：数据库表的列名
        // 第二个参数 userAccount：要匹配的具体值
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encryptPwd);
        // SELECT COUNT(*) FROM user WHERE userAccount = ?
        // 虽然实例确实是UserMapper
        User user = userMapper.selectOne(queryWrapper);

        // 用户不存在
        if (user == null) {
            log.info("User login failed, userAccount cannot match userPassword");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号和密码不匹配");
        }

        // 4. 对用户信息进行脱敏
        User safetyUser = getSafetyUser(user);


        // 5. 记录用户的登录态(Session)
        request.getSession().setAttribute(UserConstant.USER_LOGIN_STATE, safetyUser);

        return safetyUser;
    }

    /**
     * 用户脱敏
     * @param originUser 原始的用户对象
     * @return 脱敏后的用户对象
     */
    @Override
    public User getSafetyUser(User originUser) {
        if (originUser == null) {
            return null;
        }
        User safetyUser = new User();
        safetyUser.setId(originUser.getId());
        safetyUser.setUsername(originUser.getUsername());
        safetyUser.setUserAccount(originUser.getUserAccount());
        safetyUser.setAvatarUrl(originUser.getAvatarUrl());
        safetyUser.setEmail(originUser.getEmail());
        safetyUser.setUserRole(originUser.getUserRole());
        safetyUser.setUserStatus(0);
        safetyUser.setCreateTime(originUser.getCreateTime());
        return safetyUser;
    }

    /**
     * 用户注销
     *
     * @param request HttpServletRequest
     * @return 目前没多大用
     */
    @Override
    public int userLogout(HttpServletRequest request) {
        // 移除用户登录态
        // TODO - 用户注销这种操作其实不需要返回数据
        // 直接用BaseResponse包装，有code和message就足够了
        request.getSession().removeAttribute
                (UserConstant.USER_LOGIN_STATE);
        return 1;
    }


    public User getLoginUser(HttpServletRequest request) {
        Object loginUserObj = request.getSession()
                .getAttribute(UserConstant.USER_LOGIN_STATE);

        if (loginUserObj == null) {
            throw new BusinessException(ErrorCode.NO_LOGIN, "无法获取用户登录信息, 请先登录");
        }

        return (User) loginUserObj;
    }

    @Override
    public void updateUser(UserUpdateRequetst request,
                           HttpServletRequest httpServletRequest) {

        // 1. 从session中获取当前登录用户
        User loginUser = getLoginUser(httpServletRequest);

        // 2. 查询数据库
        User user = userMapper.selectById(loginUser.getId());
        if (user == null) {
            throw new BusinessException(ErrorCode.NO_LOGIN, "用户不存在");
        }

        // 3. 更新字段
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());

        // 4. 更新数据库
        userMapper.updateById(user);
    }

    @Override
    public boolean updatePassword(Long userId, String oldPassword, String newPassword) {
        // 1. 查用户
        User user = this.getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NO_FOUND_ERROR, "用户不存在");
        }

        // 2. 校验旧密码
        String encryptPwd = DigestUtils.md5DigestAsHex((UserConstant.SALT + oldPassword)
                .getBytes(StandardCharsets.UTF_8));
        if (!encryptPwd.equals(user.getUserPassword())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "旧密码错误");
        }

        // 3. 校验新密码
        if (newPassword.length() < 6) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码太短");
        }

        // 4. 加密新密码
        encryptPwd = DigestUtils.md5DigestAsHex((UserConstant.SALT + newPassword)
                .getBytes(StandardCharsets.UTF_8));

        // 5. 更新用户
        user.setUserPassword(encryptPwd);
        return this.updateById(user);
    }

    @Override
    public Page<User> listUserByPage(UserQueryRequest request) {

        long current = request.getCurrent();
        long pageSize = request.getPageSize();

        QueryWrapper<User> queryWrapper = new QueryWrapper<>();

        // 校验当前页
        if (current < Const.MIN_CURRENT) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "非法分页参数");
        }

        // 校验每页条数
        if (pageSize < Const.MIN_PAGE_SIZE || pageSize > Const.MAX_PAGE_SIZE) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "非法分页参数");
        }

        // 👇 模糊查询（重点）
        if (StringUtils.isNotBlank(request.getUsername())) {
            queryWrapper.like("username", request.getUsername());
        }

        if (StringUtils.isNotBlank(request.getUserAccount())) {
            queryWrapper.like("userAccount", request.getUserAccount());
        }

        // 👇 精确查询
        if (request.getUserStatus() != null) {
            queryWrapper.eq("userStatus", request.getUserStatus());
        }

        if (request.getUserRole() != null) {
            queryWrapper.eq("userRole", request.getUserRole());
        }

        if (request.getStartTime() != null && request.getEndTime() != null) {
            if (request.getStartTime().isAfter(request.getEndTime())) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "开始时间不能大于结束时间");
            }
        }

        // ⭐ 时间范围查询
        if (request.getStartTime() != null) {
            queryWrapper.ge("createTime", request.getStartTime().atStartOfDay());
        }

        if (request.getEndTime() != null) {
            queryWrapper.lt("createTime", request.getEndTime().
                    plusDays(1).atStartOfDay());
        }

        return this.page(
                new Page<>(current, pageSize),
                queryWrapper
        );
    }

    @Override
    public boolean updateUserByAdmin(AdminUserUpdateRequest request, HttpServletRequest httpServletRequest) {
        // 1. 判断要更新信息的用户是否存在
        Long id = request.getId();
        User oldUser = this.getById(id);
        if (oldUser == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在");
        }

        // 2. 防止直接编辑自己的状态
        User loginUser = getLoginUser(httpServletRequest);

        // 是否修改了敏感字段
        boolean modifySensitiveField =
                request.getUserRole() != null || request.getUserStatus() != null;
        // 是否是自己
        boolean isSelf = id.equals(loginUser.getId());

        if (isSelf && modifySensitiveField) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "管理员不能修改自己的角色/状态");
        }

        // 不能修改其他管理员
        if (oldUser.getUserRole() == ADMIN_ROLE && !isSelf) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "不能修改其他管理员信息");
        }

        // 3. 构造更新对象
        User updateUser = new User();
        updateUser.setId(id);
        if (request.getUsername() != null) {
            updateUser.setUsername(request.getUsername());
        }

        if (request.getEmail() != null) {
            updateUser.setEmail(request.getEmail());
        }

        if (request.getUserStatus() != null) {
            updateUser.setUserStatus(request.getUserStatus());
        }

        if (request.getUserRole() != null) {
            updateUser.setUserRole(request.getUserRole());
        }
        
        // 4. 更新
        return this.updateById(updateUser);
    }
}




