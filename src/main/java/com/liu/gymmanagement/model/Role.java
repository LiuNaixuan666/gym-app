package com.liu.gymmanagement.model;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "roles")
@Data
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "RoleID", nullable = false)
    private int roleID;

    @Column(name = "RoleName", nullable = false)
    private String roleName;

//    // 角色与用户的关系
//    private Set<User> users = new HashSet<>();
//    @JsonIgnoreProperties("roles")
//    @ManyToMany(mappedBy = "roles")
//    public Set<User> getUsers() {
//        return this.users;
//    }
      // 角色与用户的关系
      @JsonIgnoreProperties("roles")
      @ManyToMany(mappedBy = "roles")
      private Set<User> users = new HashSet<>();


    public String getRoleName() {
        return roleName;
    }
    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public Integer getRoleID() {
        return roleID;
    }

    public void setRoleID(Integer roleID) {
        this.roleID = roleID;
    }
}