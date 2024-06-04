//package com.crazymaker.springcloud.demo.security;
//
//import com.crazymaker.springcloud.common.constants.SessionConstants;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.authentication.AuthenticationServiceException;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.AuthenticationException;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.core.userdetails.User;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.web.authentication.AuthenticationFailureHandler;
//import org.springframework.web.filter.OncePerRequestFilter;
//
//import javax.servlet.FilterChain;
//import javax.servlet.ServletException;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import java.io.IOException;
//
//public class DemoAuthFilter extends OncePerRequestFilter {
//
//    private static final String AUTHORIZATION_HEAD = "token";
//
//    //认证失败的处理器
//    private AuthenticationFailureHandler failureHandler = new AuthFailureHandler();
//
//    //authenticationManager是认证流程的入口，接收一个Authentication令牌对象作为参数
//    private AuthenticationManager authenticationManager;
//
//    @Override
//    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain) throws ServletException, IOException {
//
//        AuthenticationException failed = null;
//
//        try {
//            Authentication returnToken;
//            boolean succeed;
//            //从请求头中获取认证信息
//            String token = httpServletRequest.getHeader(AUTHORIZATION_HEAD);
//            String[] parts = token.split(",");
//            //方式一:DemoToken 认证演示
//            //组装令牌
////            DemoToken demoToken = new DemoToken(parts[0], parts[1]);
//            //提交给AuthenticationManager认证管理者进行令牌验证
////            returnToken = this.getAuthenticationManager().authenticate(demoToken);
//            //获取认证成功标志
////            succeed = returnToken.isAuthenticated();
//
//            //方式二:数据库 认证演示
//            UserDetails userDetails = User.builder().username(parts[0])
//                    .password(parts[1])
//                    .authorities(SessionConstants.USER_INFO)
//                    .build();
//            //创建一个用户名+密码的凭证，一般情况下，令牌中的密码需要明文
//            Authentication passwordAuthentication =
//                    new UsernamePasswordAuthenticationToken(userDetails, userDetails.getPassword(), userDetails.getAuthorities());
//            //进入认证流程
//            returnToken = this.getAuthenticationManager().authenticate(passwordAuthentication);
//            succeed = passwordAuthentication.isAuthenticated();
//
//            if (succeed) {
//                //认证成功，设置上下文令牌
//                SecurityContextHolder.getContext().setAuthentication(returnToken);
//                //执行后续的操作
//                filterChain.doFilter(httpServletRequest, httpServletResponse);
//                return;
//            }
//        } catch (Exception e) {
//            logger.error("认证有误", e);
//            failed = new AuthenticationServiceException("请求头认证消息格式错误", e);
//        }
//        if (failed == null) {
//            failed = new AuthenticationServiceException("认证失败");
//        }
//        //认证失败了
//        SecurityContextHolder.clearContext();
//        failureHandler.onAuthenticationFailure(httpServletRequest, httpServletResponse, failed);
//    }
//
//    protected AuthenticationManager getAuthenticationManager() {
//        return authenticationManager;
//    }
//
//    public void setAuthenticationManager(AuthenticationManager authenticationManager) {
//        this.authenticationManager = authenticationManager;
//    }
//}
