//package com.crazymaker.springcloud.demo.security;
//
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.config.annotation.web.HttpSecurityBuilder;
//import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
//import org.springframework.security.web.authentication.logout.LogoutFilter;
//
//public class DemoAuthConfigurer<T extends DemoAuthConfigurer<T,B>, B extends HttpSecurityBuilder<B>> extends AbstractHttpConfigurer<T,B> {
//    //创建认证过滤器
//    private DemoAuthFilter demoAuthFilter = new DemoAuthFilter();
//    //将过滤器加入HTTP过滤处理责任链
//    @Override
//    public void configure(B builder) throws Exception {
//        //获取Spring Security共享的AuthenticationManager认证提供者实例
//        //设置认证过滤器
//        demoAuthFilter.setAuthenticationManager(builder.getSharedObject(AuthenticationManager.class));
//        DemoAuthFilter filter = postProcess(demoAuthFilter);
//        //将过滤器加入HTTP过滤处理责任链
//        builder.addFilterBefore(filter, LogoutFilter.class);
//    }
//}
