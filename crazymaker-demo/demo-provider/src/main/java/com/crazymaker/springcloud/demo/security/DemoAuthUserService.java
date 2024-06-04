//package com.crazymaker.springcloud.demo.security;
//
//import com.crazymaker.springcloud.common.constants.SessionConstants;
//import com.crazymaker.springcloud.standard.context.SpringContextUtil;
//import org.springframework.security.core.userdetails.User;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.core.userdetails.UserDetailsService;
//import org.springframework.security.core.userdetails.UsernameNotFoundException;
//import org.springframework.security.crypto.password.PasswordEncoder;
//
//import javax.annotation.Resource;
//import java.util.LinkedHashMap;
//import java.util.Map;
//
//public class DemoAuthUserService implements UserDetailsService {
//
//    //模拟数据源，实际从DB中获取
//    private Map<String, String> map = new LinkedHashMap<>();
//
//    //初始化模拟的数据源，放入出两个用户
//    {
//        map.put("zhangsan", "123456");
//        map.put("lisi", "123456");
//    }
//
//    @Resource
//    private PasswordEncoder passwordEncoder;
//
//    @Override
//    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
//
//        //实际场景中，需要从数据库加载用户
//        //出于演示目的：这里从模拟的数据源加载
//        String password = map.get(username);
//        if (password == null) {
//            return null;
//        }
//
//        if (null == passwordEncoder) {
//            passwordEncoder = SpringContextUtil.getBean(PasswordEncoder.class);
//        }
//
//        /**
//         * 返回一个用户详细实例，包含用户名，加密后的密码，用户权限清单，用户角色
//         */
//        UserDetails userDetails = User.builder()
//                .username(username)
//                .password(passwordEncoder.encode(password))
//                .authorities(SessionConstants.USER_INFO)
//                .roles("USER")
//                .build();
//        return userDetails;
//    }
//}
