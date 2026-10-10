package com.campusmarket.util;

import com.campusmarket.model.User;

/**
 * 会话管理器 — 保存当前登录用户信息
 */
public class SessionManager {

    private static User currentUser;
    private static boolean loggedIn = false;

    /**
     * 设置当前登录用户
     */
    public static void login(User user) {
        currentUser = user;
        loggedIn = true;
    }

    /**
     * 退出登录
     */
    public static void logout() {
        currentUser = null;
        loggedIn = false;
    }

    /**
     * 获取当前登录用户
     */
    public static User getCurrentUser() {
        return currentUser;
    }

    /**
     * 是否已登录
     */
    public static boolean isLoggedIn() {
        return loggedIn;
    }

    /**
     * 当前用户是否为管理员
     */
    public static boolean isAdmin() {
        return loggedIn && currentUser != null && currentUser.getRole() == 1;
    }

    /**
     * 获取当前用户ID
     */
    public static int getCurrentUserId() {
        return loggedIn && currentUser != null ? currentUser.getUserId() : 0;
    }
}
