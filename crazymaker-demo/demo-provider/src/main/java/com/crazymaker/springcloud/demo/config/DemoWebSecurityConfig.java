//package com.crazymaker.springcloud.demo.config;
//
//import com.crazymaker.springcloud.demo.security.DemoAuthConfigurer;
//import com.crazymaker.springcloud.demo.security.DemoAuthProvider;
//import com.crazymaker.springcloud.demo.security.DemoAuthUserService;
//import org.springframework.context.annotation.Bean;
//import org.springframework.security.authentication.AuthenticationProvider;
//import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
//import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.builders.WebSecurity;
//import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
//import org.springframework.security.crypto.password.PasswordEncoder;
//
//import javax.annotation.Resource;
///**
// * 学习 SpringSecurity 时，本配置类才启用
// **/
////@EnableWebSecurity
//public class DemoWebSecurityConfig extends WebSecurityConfigurerAdapter {
//
//    //配置HTTP请求的安全策略，应用DemoAuthConfigurer配置类实例
//    @Override
//    protected void configure(HttpSecurity http) throws Exception {
//        http.csrf().disable()
//                .authorizeRequests()
//                .antMatchers(
//                        "/**/v2/api-docs",
//                        "/**/swagger-resources/configuration/ui",
//                        "/**/swagger-resources",
//                        "/**/swagger-resources/configuration/security",
//                        "/images/**",
//                        "/**/swagger-ui.html",
//                        "/**/webjars/**",
//                        "/**/favicon.ico",
//                        "/**/css/**",
//                        "/**/js/**"
//                )
//                .permitAll()
//                .anyRequest().authenticated()
//                .and()
//
//                .formLogin().disable()
//                .sessionManagement().disable()
//                .cors()
//
//                .and()
//                .apply(new DemoAuthConfigurer<>())
//                .and()
//                .sessionManagement().disable();
//    }
//
//    @Override
//    public void configure(WebSecurity web) throws Exception {
//        web.ignoring().antMatchers(
//                "/**/v2/api-docs",
//                "/**/swagger-resources/configuration/ui",
//                "/**/swagger-resources",
//                "/**/swagger-resources/configuration/security",
//                "/images/**",
//                "/**/swagger-ui.html",
//                "/**/webjars/**",
//                "/**/favicon.ico",
//                "/**/css/**",
//                "/**/js/**"
//        );
//    }
//
//    //配置认证Builder建造者，由它负责建造AuthenticationManager认证管理者实例
//    //Builder将建造AuthenticationManager管理者实例，并且将作为HTTP请求的共享对象存储在
//    //代码中，可以通过http.getSharedObject(AuthenticationManager.class) 来获取管理者实例
//    @Override
//    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
//        //加入自定义的Provider认证提供者实例
//        auth.authenticationProvider(demoAuthProvider());
//        auth.authenticationProvider(daoAuthenticationProvider());
//    }
//
//    //自定义的认证提供者实例
//    @Bean("demoAuthProvider")
//    protected DemoAuthProvider demoAuthProvider() {
//        return new DemoAuthProvider();
//    }
//
//    //注入全局BCryptPasswordEncoder加密器容器实例
//    @Resource
//    private PasswordEncoder passwordEncoder;
//    //注入数据源服务容器实例
//    @Resource
//    private DemoAuthUserService demoAuthUserService;
//
//    @Bean("daoAuthenticationProvider")
//    protected AuthenticationProvider daoAuthenticationProvider() {
//        //创建一个数据源提供者
//        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
//        //设置加密器，使用全局的 BCryptPasswordEncoder 加密器
//        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder);
//        //设置用户数据源服务
//        daoAuthenticationProvider.setUserDetailsService(demoAuthUserService);
//        return daoAuthenticationProvider;
//    }
//}
