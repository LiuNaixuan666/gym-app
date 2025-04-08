package com.liu.gymmanagement.model;

import lombok.Data;
import jakarta.persistence.*;
import java.util.Set;
import java.util.HashSet;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;

// import lombok.Getter;
// import lombok.Setter;
@Data
@Entity
@Table(name = "users")
public class User {

    @Id
    @Column(name = "UserID", nullable = false, unique = true)
    private String userID;  // 使用 String 类型来映射 UserID 字段

    @Column(name = "Username", nullable = false)
    private String username;

    @Column(name = "Password", nullable = false)
    private String password;

    @Column(name = "phone", nullable = false, unique = true)
    private String phone;

    @Column(name = "age", nullable = false)
    private int age;

    @Column(name = "gender", nullable = false)
    private String gender;

    @Column(name = "lable")
    private String label;  // 使用 String 类型来映射标签

    @Getter
    @Column(name = "email")
    private String email;

    @Column(name = "is_email_verified")
    private Boolean isEmailVerified = false;  // 默认是未验证的



    // 角色的关系
    @JsonIgnoreProperties("users")
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "UserID"),
            inverseJoinColumns = @JoinColumn(name = "RoleID")
    )
    private Set<Role> roles = new HashSet<>();
//    private Set<User> users = new HashSet<>();
    // 构造方法
    public User() {
        // 无参构造方法
    }

    public User(String userID, String username, String password, String phone, int age, String gender, String label, boolean isEmailVerified, String email, Set<Role> roles) {
        this.userID = userID;
        this.username = username;
        this.password = password;
        this.phone = phone;
        this.age = age;
        this.gender = gender;
        this.label = label;
        this.email = email;
        this.isEmailVerified = isEmailVerified;
        this.roles = roles;
    }

    // getter和setter方法
    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }
    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Boolean getEmailVerified() {
        return isEmailVerified;
    }

    public void setEmailVerified(Boolean emailVerified) {
        isEmailVerified = emailVerified;
    }

    // Getter and Setter for roles
    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }
}

