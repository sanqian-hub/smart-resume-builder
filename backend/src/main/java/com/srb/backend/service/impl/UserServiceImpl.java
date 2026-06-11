package com.srb.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.srb.backend.common.BusinessException;
import com.srb.backend.common.ErrorCode;
import com.srb.backend.common.SessionUtils;
import com.srb.backend.mapper.UserMapper;
import com.srb.backend.model.dto.UserLoginRequest;
import com.srb.backend.model.dto.UserRegisterRequest;
import com.srb.backend.model.dto.UserUpdateRequest;
import com.srb.backend.model.entity.User;
import com.srb.backend.model.vo.LoginUserVO;
import com.srb.backend.service.CosService;
import com.srb.backend.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final CosService cosService;

    private static final String SALT = "srb_salt";
    private static final List<String> DEFAULT_AVATAR_URLS = List.of(
            "https://api.dicebear.com/9.x/lorelei/svg?eyebrows=variant01,variant05,variant06,variant07,variant08,variant09,variant12&seed=Sophia",
            "https://api.dicebear.com/9.x/lorelei/svg?eyebrows=variant01,variant05,variant06,variant07,variant08,variant09,variant12&seed=Mason",
            "https://api.dicebear.com/9.x/lorelei/svg?eyebrows=variant01,variant05,variant06,variant07,variant08,variant09,variant12&seed=George",
            "https://api.dicebear.com/9.x/lorelei/svg?eyebrows=variant01,variant05,variant06,variant07,variant08,variant09,variant12&seed=Aiden",
            "https://api.dicebear.com/9.x/lorelei/svg?eyebrows=variant01,variant05,variant06,variant07,variant08,variant09,variant12&seed=Riley",
            "https://api.dicebear.com/9.x/lorelei/svg?eyebrows=variant01,variant05,variant06,variant07,variant08,variant09,variant12&seed=Liliana"
    );

    @Override
    public long register(UserRegisterRequest request) {
        String userAccount = request.getUserAccount();
        if (userAccount.contains(" ") || userAccount.contains("@") || userAccount.contains(",") || userAccount.contains("、")) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号包含非法字符");
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        long count = this.baseMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号已存在");
        }
        String encryptedPassword = DigestUtils.md5DigestAsHex((SALT + request.getUserPassword()).getBytes());
        User user = new User();
        user.setUsername(request.getUsername());
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptedPassword);
        user.setAvatarUrl(pickDefaultAvatarUrl());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        int result = this.baseMapper.insert(user);
        if (result == 0) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败");
        }
        return user.getId();
    }

    @Override
    public LoginUserVO login(UserLoginRequest request) {
        String userAccount = request.getUserAccount();
        String userPassword = request.getUserPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号或密码为空");
        }
        String encryptedPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encryptedPassword);
        User user = this.baseMapper.selectOne(queryWrapper);
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号或密码错误");
        }
        if (user.getStatus() == 1) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "账号已被禁用");
        }
        return getLoginUserVO(user);
    }

    @Override
    public LoginUserVO getCurrentUser(HttpServletRequest request) {
        User user = SessionUtils.getLoginUser(request, this.baseMapper);
        return getLoginUserVO(user);
    }

    @Override
    public void logout(HttpServletRequest request) {
        SessionUtils.removeLoginUser(request.getSession(false));
    }

    @Override
    public LoginUserVO updateMyInfo(HttpServletRequest request, UserUpdateRequest updateRequest) {
        User user = SessionUtils.getLoginUser(request, this.baseMapper);
        if (updateRequest.getUsername() != null) {
            user.setUsername(updateRequest.getUsername());
        }
        if (updateRequest.getAvatarUrl() != null) {
            user.setAvatarUrl(updateRequest.getAvatarUrl());
        }
        if (updateRequest.getGender() != null) {
            user.setGender(updateRequest.getGender());
        }
        if (updateRequest.getEmail() != null) {
            user.setEmail(updateRequest.getEmail());
        }
        if (updateRequest.getPhone() != null) {
            user.setPhone(updateRequest.getPhone());
        }
        if (updateRequest.getNoticeEnabled() != null) {
            user.setNoticeEnabled(updateRequest.getNoticeEnabled());
        }
        this.baseMapper.updateById(user);
        return getLoginUserVO(user);
    }

    @Override
    public String uploadAvatar(HttpServletRequest request, MultipartFile file) {
        User user = SessionUtils.getLoginUser(request, this.baseMapper);
        String url = cosService.upload(file, "avatar");
        user.setAvatarUrl(url);
        this.baseMapper.updateById(user);
        return url;
    }

    @Override
    public String uploadImage(HttpServletRequest request, MultipartFile file, String folder) {
        SessionUtils.getLoginUser(request, this.baseMapper);
        return cosService.upload(file, folder != null && !folder.isBlank() ? folder : "portfolio");
    }

    private LoginUserVO getLoginUserVO(User user) {
        if (user == null) {
            return null;
        }
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtils.copyProperties(user, loginUserVO);
        return loginUserVO;
    }

    private String pickDefaultAvatarUrl() {
        int index = ThreadLocalRandom.current().nextInt(DEFAULT_AVATAR_URLS.size());
        return DEFAULT_AVATAR_URLS.get(index);
    }
}
