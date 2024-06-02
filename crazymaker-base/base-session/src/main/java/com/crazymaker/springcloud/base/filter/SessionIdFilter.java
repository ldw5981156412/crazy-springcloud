package com.crazymaker.springcloud.base.filter;

import com.crazymaker.springcloud.common.constants.SessionConstants;
import com.crazymaker.springcloud.common.context.SessionHolder;
import com.crazymaker.springcloud.standard.redis.RedisRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.session.Session;
import org.springframework.session.data.redis.RedisOperationsSessionRepository;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
public class SessionIdFilter extends OncePerRequestFilter {

    private RedisOperationsSessionRepository sessionRepository;

    private RedisRepository redisRepository;

    public SessionIdFilter(RedisOperationsSessionRepository sessionRepository, RedisRepository redisRepository) {
        this.sessionRepository = sessionRepository;
        this.redisRepository = redisRepository;
    }

    /**
     * 将session userIdentifier（用户ID）转成session id
     * @param request
     * @param response
     * @param filterChain
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        //从请求头中获取session userIdentifier（用户ID）
        String identifier = request.getHeader(SessionConstants.USER_IDENTIFIER);
        SessionHolder.setUserIdentifer(identifier);
        //从请求头中获取session userIdentifier（用户ID）
        String sid = redisRepository.getSessionId(identifier);
        if(StringUtils.isNotEmpty(sid)){
            //判断分布式会话是否存在
            Session session = sessionRepository.findById(sid);
            if (null != session){
                //保存session id在线程中的局部变量，供后面的过滤器使用
                SessionHolder.setSid(sid);
            }
        }
    }

    /**
     * 返回true代表不执行过滤器，false代表执行
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String identifier = request.getHeader(SessionConstants.USER_IDENTIFIER);
        if(StringUtils.isNotEmpty(identifier)){
            return false;
        }
        return true;
    }
}
