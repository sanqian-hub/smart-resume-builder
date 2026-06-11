package com.srb.backend.common;

import com.srb.backend.mapper.UserMapper;
import com.srb.backend.model.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import static com.srb.backend.constant.UserConstant.USER_LOGIN_STATE;

public class SessionUtils {

    private SessionUtils() {
    }

    public static Long getLoginUserId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        Long userId = (Long) session.getAttribute(USER_LOGIN_STATE);
        if (userId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return userId;
    }

    public static User getLoginUser(HttpServletRequest request, UserMapper userMapper) {
        Long userId = getLoginUserId(request);
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        }
        return user;
    }

    public static Long getLoginUserIdIfPresent(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        Object userId = session.getAttribute(USER_LOGIN_STATE);
        return userId instanceof Long ? (Long) userId : null;
    }

    public static void setLoginUserId(HttpSession session, Long userId) {
        session.setAttribute(USER_LOGIN_STATE, userId);
    }

    public static void removeLoginUser(HttpSession session) {
        session.removeAttribute(USER_LOGIN_STATE);
    }
}
