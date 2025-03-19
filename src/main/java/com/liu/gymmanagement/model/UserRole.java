package com.liu.gymmanagement.model;

import jakarta.persistence.*;

@Entity
@Table(name = "user_roles")
public class UserRole {

    @EmbeddedId  // 使用复合主键
    private UserRoleId id;

    // 默认构造函数
    public UserRole() {}
    public UserRole(UserRoleId id) {
        this.id = id;  // 使用复合主键构造器
    }

    public UserRole(String userID, int roleID) {
        this.id = new UserRoleId(userID, roleID);
    }

    // Getters 和 Setters
    public UserRoleId getId() {
        return id;
    }

    public void setId(UserRoleId id) {
        this.id = id;
    }

    public String getUserID() {
        return id.getUserID();
    }

    public void setUserID(String userID) {
        id.setUserID(userID);
    }

    public int getRoleID() {
        return id.getRoleID();
    }

    public void setRoleID(int roleID) {
        id.setRoleID(roleID);
    }
}
//package com.liu.gymmanagement.model;
//import jakarta.persistence.*;
//
//@Entity
//@Table(name = "user_roles")
//public class UserRole{
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @Column(name = "userID")
//    private String userID;
//
//    @Column(name = "roleID")
//    private int roleID;
//
//    public UserRole() {}
//
//    public UserRole(String userID, int roleID) {
//        this.userID = userID;
//        this.roleID = roleID;
//    }
//
//    public String getUserID() {
//        return userID;
//    }
//
//    public void setUserID(String userID) {
//        this.userID = userID;
//    }
//
//    public int getRoleID() {
//        return roleID;
//    }
//
//    public void setRoleID(int roleID) {
//        this.roleID = roleID;
//    }
//}
