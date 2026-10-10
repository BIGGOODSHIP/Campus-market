package com.campusmarket.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 密码加密工具类
 * 使用 SHA-256 算法对密码进行单向加密
 */
public class PasswordUtil {

    /**
     * 对密码进行 SHA-256 加密
     * @param password 明文密码
     * @return 64位十六进制密文
     */
    public static String encrypt(String password) {
        if (password == null || password.isEmpty()) {
            return "";
        }
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256算法不可用", e);
        }
    }
}
