package com.srb.backend.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface RememberLoginTokenService {
    void issueTokenForCurrentDevice(Long userId, HttpServletRequest request, HttpServletResponse response);
    void clearCurrentDeviceToken(HttpServletRequest request, HttpServletResponse response);
    boolean restoreSessionIfPossible(HttpServletRequest request, HttpServletResponse response);
}
