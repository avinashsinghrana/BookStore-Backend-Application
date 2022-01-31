package com.bridgelabz.bookstore.response;

import com.bridgelabz.bookstore.enums.RoleType;

import java.io.Serializable;

public class LoginResponse implements Serializable {
    private static final long SerialVersionUID = 10l;
    private String token;
    private String name;
    private RoleType roleType;
    public LoginResponse(String token,String name,RoleType roleType){
        this.token = token;
        this.name = name;
        this.roleType = roleType;
    }

}
