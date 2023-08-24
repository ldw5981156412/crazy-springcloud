package com.crazymaker.springcloud.demo.security;

import org.springframework.security.authentication.AbstractAuthenticationToken;

import java.util.Collections;

public class DemoToken extends AbstractAuthenticationToken {

    private static final long serialVersionUID = 1981518947978158945L;

    private  String userName;
    private  String password;

    public DemoToken(String userName, String password) {
        super(Collections.emptyList());
        this.userName = userName;
        this.password = password;
    }

    @Override
    public Object getCredentials() {
        return password;
    }

    @Override
    public Object getPrincipal() {
        return userName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
