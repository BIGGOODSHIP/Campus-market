package com.campusmarket.model;

import java.sql.Timestamp;

/**
 * 用户实体类
 */
public class User {
    private int userId; 
    private String username;
    private String password;
    private String realName;
    private String phone;
    private int role;       // 0=学生, 1=管理员
    private int status;     // 0=正常, 1=禁用
    private Timestamp createTime;

    public User() {}

    public User(int userId, String username, String password, String realName,
                String phone, int role, int status, Timestamp createTime) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.realName = realName;
        this.phone = phone;
        this.role = role;
        this.status = status;
        this.createTime = createTime;
    }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRealName() { return realName; }
    public void setRealName(String realName) { this.realName = realName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public int getRole() { return role; }
    public void setRole(int role) { this.role = role; }

    public String getRoleName() { return role == 1 ? "管理员" : "学生"; }

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    public String getStatusName() { return status == 0 ? "正常" : "禁用"; }

    public Timestamp getCreateTime() { return createTime; }
    public void setCreateTime(Timestamp createTime) { this.createTime = createTime; }
}
