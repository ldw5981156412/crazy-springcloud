package com.crazymaker.springcloud.base.security.token;

import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class JwtAuthenticationToken extends AbstractAuthenticationToken {

    private static final long serialVersionUID = 3981518947978158945L;

    //封装用户信息：用户id、密码
    private UserDetails userDetails;
    //封装的JWT认证信息
    private DecodedJWT decodedJWT;

    public JwtAuthenticationToken(DecodedJWT decodedJWT) {
        super(Collections.emptyList());
        this.decodedJWT = decodedJWT;
    }

    public JwtAuthenticationToken(UserDetails userDetails, DecodedJWT decodedJWT, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.userDetails = userDetails;
        this.decodedJWT = decodedJWT;
    }

    @Override
    public Object getCredentials() {
        return userDetails;
    }

    @Override
    public Object getPrincipal() {
        return decodedJWT.getSubject();
    }

    public DecodedJWT getDecodedJWT() {
        return decodedJWT;
    }

    @Override
    public void setDetails(Object details) {
        super.setDetails(details);
        this.setAuthenticated(true);
    }
}
