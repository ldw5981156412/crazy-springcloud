package com.crazymaker.springcloud.base.security.provider;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.crazymaker.springcloud.base.security.token.JwtAuthenticationToken;
import com.crazymaker.springcloud.common.dto.UserDTO;
import com.crazymaker.springcloud.common.util.JsonUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.www.NonceExpiredException;
import org.springframework.session.Session;
import org.springframework.session.data.redis.RedisOperationsSessionRepository;

import java.util.Calendar;

import static com.crazymaker.springcloud.common.context.SessionHolder.G_USER;

public class JwtAuthenticationProvider implements AuthenticationProvider {

    //用于通过session id查找用户信息
    private RedisOperationsSessionRepository sessionRepository;

    public JwtAuthenticationProvider(RedisOperationsSessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        //判断JWT令牌是否过期
        JwtAuthenticationToken jwtToken = (JwtAuthenticationToken)authentication;
        DecodedJWT jwt = jwtToken.getDecodedJWT();
        if(jwt.getExpiresAt().after(Calendar.getInstance().getTime())){
            throw new NonceExpiredException("认证过期");
        }
        //获取session id
        String sid = jwt.getSubject();
        //获取令牌字符串，此变量将用于验证是否重复登录
        String newToken = jwt.getToken();
        //获取会话
        Session session = null;
        try {
            session = sessionRepository.findById(sid);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(null == session){
            throw new NonceExpiredException("还没有登录,请登录系统！");
        }
        String json = session.getAttribute(G_USER);
        if (StringUtils.isBlank(json)){
            throw new NonceExpiredException("认证有误,请重新登录");
        }
        //取得session 中的用户信息
        UserDTO userDTO = JsonUtil.jsonToPojo(json, UserDTO.class);
        if(null == userDTO){
            throw new NonceExpiredException("认证有误,请重新登录");
        }
        return null;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.isAssignableFrom(JwtAuthenticationToken.class);
    }
}
