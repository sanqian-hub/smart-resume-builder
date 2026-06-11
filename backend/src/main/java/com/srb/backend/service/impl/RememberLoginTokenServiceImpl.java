package com.srb.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.srb.backend.mapper.RememberLoginTokenMapper;
import com.srb.backend.mapper.UserMapper;
import com.srb.backend.model.entity.User;
import com.srb.backend.model.entity.RememberLoginToken;
import com.srb.backend.common.SessionUtils;
import com.srb.backend.service.RememberLoginTokenService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
public class RememberLoginTokenServiceImpl implements RememberLoginTokenService {

    static final String REMEMBER_ME_COOKIE = "remember_me";

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int SELECTOR_BYTES = 16;
    private static final int VALIDATOR_BYTES = 32;

    private final RememberLoginTokenMapper rememberLoginTokenMapper;
    private final UserMapper userMapper;

    @Value("${remember-me.max-age-seconds:2592000}")
    private int rememberMeMaxAgeSeconds;

    @Override
    public void issueTokenForCurrentDevice(Long userId, HttpServletRequest request, HttpServletResponse response) {
        revokeCurrentDeviceToken(request);

        String selector = randomHex(SELECTOR_BYTES);
        String validator = randomHex(VALIDATOR_BYTES);
        LocalDateTime now = LocalDateTime.now();

        RememberLoginToken token = new RememberLoginToken();
        token.setUserId(userId);
        token.setSelector(selector);
        token.setValidatorHash(sha256Hex(validator));
        token.setExpiresAt(now.plusSeconds(rememberMeMaxAgeSeconds));
        token.setLastUsedAt(now);
        token.setRevoked(0);
        token.setUserAgent(truncate(request.getHeader("User-Agent"), 512));
        token.setClientIp(truncate(request.getRemoteAddr(), 64));
        rememberLoginTokenMapper.insert(token);

        writeCookie(response, selector + ":" + validator, rememberMeMaxAgeSeconds, request.isSecure());
    }

    @Override
    public void clearCurrentDeviceToken(HttpServletRequest request, HttpServletResponse response) {
        revokeCurrentDeviceToken(request);
        writeCookie(response, "", 0, request.isSecure());
    }

    @Override
    public boolean restoreSessionIfPossible(HttpServletRequest request, HttpServletResponse response) {
        if (SessionUtils.getLoginUserIdIfPresent(request) != null) {
            return false;
        }

        String cookieValue = readRememberCookie(request);
        if (cookieValue == null) {
            return false;
        }
        String[] parts = splitCookieValue(cookieValue);
        if (parts == null) {
            writeCookie(response, "", 0, request.isSecure());
            return false;
        }
        String selector = parts[0];
        String validator = parts[1];

        QueryWrapper<RememberLoginToken> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("selector", selector);
        RememberLoginToken token = rememberLoginTokenMapper.selectOne(queryWrapper);
        if (token == null || token.getRevoked() == 1 || token.getExpiresAt() == null
                || token.getExpiresAt().isBefore(LocalDateTime.now())) {
            writeCookie(response, "", 0, request.isSecure());
            return false;
        }

        if (!sha256Hex(validator).equals(token.getValidatorHash())) {
            token.setRevoked(1);
            token.setUpdatedAt(LocalDateTime.now());
            rememberLoginTokenMapper.updateById(token);
            writeCookie(response, "", 0, request.isSecure());
            return false;
        }

        User user = userMapper.selectById(token.getUserId());
        if (user == null) {
            token.setRevoked(1);
            token.setUpdatedAt(LocalDateTime.now());
            rememberLoginTokenMapper.updateById(token);
            writeCookie(response, "", 0, request.isSecure());
            return false;
        }

        SessionUtils.setLoginUserId(request.getSession(true), token.getUserId());

        String newValidator = randomHex(VALIDATOR_BYTES);
        token.setValidatorHash(sha256Hex(newValidator));
        token.setLastUsedAt(LocalDateTime.now());
        token.setUpdatedAt(LocalDateTime.now());
        rememberLoginTokenMapper.updateById(token);
        writeCookie(response, selector + ":" + newValidator, rememberMeMaxAgeSeconds, request.isSecure());
        return true;
    }

    private void revokeCurrentDeviceToken(HttpServletRequest request) {
        String cookieValue = readRememberCookie(request);
        if (cookieValue == null) {
            return;
        }
        String selector = extractSelector(cookieValue);
        if (selector == null) {
            return;
        }
        QueryWrapper<RememberLoginToken> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("selector", selector);
        RememberLoginToken token = rememberLoginTokenMapper.selectOne(queryWrapper);
        if (token == null || token.getRevoked() == 1) {
            return;
        }
        token.setRevoked(1);
        token.setUpdatedAt(LocalDateTime.now());
        rememberLoginTokenMapper.updateById(token);
    }

    private static String readRememberCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (REMEMBER_ME_COOKIE.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    private static String extractSelector(String cookieValue) {
        String[] parts = splitCookieValue(cookieValue);
        if (parts == null) {
            return null;
        }
        return parts[0];
    }

    private static String[] splitCookieValue(String cookieValue) {
        if (cookieValue == null || cookieValue.isBlank()) {
            return null;
        }
        String[] parts = cookieValue.split(":", 2);
        if (parts.length != 2 || parts[0].isBlank() || parts[1].isBlank()) {
            return null;
        }
        return parts;
    }

    private void writeCookie(HttpServletResponse response, String value, int maxAge, boolean secure) {
        Cookie cookie = new Cookie(REMEMBER_ME_COOKIE, value);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);
        cookie.setSecure(secure);
        response.addCookie(cookie);
    }

    private static String randomHex(int bytes) {
        byte[] buffer = new byte[bytes];
        SECURE_RANDOM.nextBytes(buffer);
        return HexFormat.of().formatHex(buffer);
    }

    private static String sha256Hex(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashed);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }

    private static String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }
}
