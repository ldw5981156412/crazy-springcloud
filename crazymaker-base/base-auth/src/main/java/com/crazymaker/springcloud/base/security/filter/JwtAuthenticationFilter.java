package com.crazymaker.springcloud.base.security.filter;

import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.crazymaker.springcloud.base.security.token.JwtAuthenticationToken;
import com.crazymaker.springcloud.common.constants.SessionConstants;
import com.crazymaker.springcloud.common.context.SessionHolder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.*;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private AuthenticationManager authenticationManager;

    private AuthenticationSuccessHandler successHandler = new SavedRequestAwareAuthenticationSuccessHandler();
    private AuthenticationFailureHandler failureHandler = new SimpleUrlAuthenticationFailureHandler();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        Authentication passToken = null;
        AuthenticationException failed = null;
        //从HTTP请求获取JWT令牌的头部字段
        String token = null;
        //用户端存放的JWT的HTTP头部字段为token
        String sessionIDStore = SessionHolder.getSessionIDStore();
        if (sessionIDStore.equals(SessionConstants.SESSION_STORE)) {
            token = request.getHeader(SessionConstants.AUTHORIZATION_HEAD);
            //管理端存放的JWT的HTTP头部字段为Authorization
        } else if (sessionIDStore.equals(SessionConstants.ADMIN_SESSION_STORE)) {
            token = request.getHeader(SessionConstants.ADMIN_AUTHORIZATION_HEAD);
        } else {
            failed = new InsufficientAuthenticationException("请求头认证消息为空");
            unsuccessfulAuthentication(request, response, failed);
            return;
        }
        token = StringUtils.removeStart(token,"Bearer ");

        try {
            if(StringUtils.isNotBlank(token)) {
                //组装令牌
                JwtAuthenticationToken authToken = new JwtAuthenticationToken(JWT.decode(token));
                //提交给AuthenticationManager认证管理者进行令牌验证，获取认证后的令牌
                passToken = this.getAuthenticationManager().authenticate(authToken);
                //获取认证后的用户信息，主要是用户id
                UserDetails details = (UserDetails) passToken.getDetails();
                //通过details.getUsername()获取用户id，并作为请求属性进行缓存
                request.setAttribute(SessionConstants.USER_IDENTIFIER,details.getUsername());
            }else{
                failed = new InsufficientAuthenticationException("请求头认证消息为空");
            }
        } catch (JWTDecodeException e) {
            throw new RuntimeException(e);
        } catch (AuthenticationException e) {
            throw new RuntimeException(e);
        }
        filterChain.doFilter(request,response);
    }

    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws ServletException, IOException {
        SecurityContextHolder.clearContext();
        failureHandler.onAuthenticationFailure(request, response, failed);
    }

    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, Authentication authResult) throws ServletException, IOException {
        SecurityContextHolder.getContext().setAuthentication(authResult);
        successHandler.onAuthenticationSuccess(request, response, authResult);
    }

    public AuthenticationManager getAuthenticationManager() {
        return authenticationManager;
    }

    public void setAuthenticationManager(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    public AuthenticationFailureHandler getFailureHandler() {
        return failureHandler;
    }

    public void setFailureHandler(AuthenticationFailureHandler failureHandler) {
        this.failureHandler = failureHandler;
    }
}
