package com.crazymaker.springcloud.base.security.configurer;

import com.crazymaker.springcloud.base.security.filter.JwtAuthenticationFilter;
import com.crazymaker.springcloud.base.security.handler.AuthFailureHandler;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.HttpSecurityBuilder;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.authentication.logout.LogoutFilter;

public class JwtAuthConfigurer<T extends JwtAuthConfigurer<T, B>, B extends HttpSecurityBuilder<B>> extends AbstractHttpConfigurer<T, B> {

    private JwtAuthenticationFilter jwtAuthenticationFilter;

    public JwtAuthConfigurer() {
        //创建认证过滤器
        this.jwtAuthenticationFilter = new JwtAuthenticationFilter();
    }
    //将过滤器加入http过滤处理责任链
    @Override
    public void configure(B http) throws Exception {
        //获取Spring Security共享的AuthenticationManager认证提供者实例
        //将其设置到jwtAuthenticationFilter认证过滤器
        jwtAuthenticationFilter.setAuthenticationManager(http.getSharedObject(AuthenticationManager.class));
        jwtAuthenticationFilter.setFailureHandler(new AuthFailureHandler());
        JwtAuthenticationFilter filter = postProcess(jwtAuthenticationFilter);
        http.addFilterBefore(filter, LogoutFilter.class);
    }
}
