package com.liu.gymmanagement.model;

import jakarta.persistence.*;
import java.io.Serializable;

@Embeddable  // 表明这是一个可以嵌入到实体中的复合主键类
public class UserRoleId implements Serializable {

    @Column(name = "userID")
    private String userID;

    @Column(name = "roleID")
    private int roleID;

    // 默认构造函数
    public UserRoleId() {}

    public UserRoleId(String userID, int roleID) {
        this.userID = userID;
        this.roleID = roleID;
    }

    // Getters 和 Setters
    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public int getRoleID() {
        return roleID;
    }

    public void setRoleID(int roleID) {
        this.roleID = roleID;
    }

    // 需要实现 equals 和 hashCode 方法，用于 JPA 和 Hibernate 正确比较复合主键对象
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserRoleId that = (UserRoleId) o;
        return roleID == that.roleID && userID.equals(that.userID);
    }

    @Override
    public int hashCode() {
        return 31 * userID.hashCode() + roleID;
    }
}
