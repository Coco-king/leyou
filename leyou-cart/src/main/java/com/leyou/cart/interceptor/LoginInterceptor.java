package com.leyou.cart.interceptor;

import com.leyou.cart.config.JwtProperties;
import com.leyou.commom.utils.CookieUtils;
import com.leyou.common.pojo.UserInfo;
import com.leyou.common.utils.JwtUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
@EnableConfigurationProperties(JwtProperties.class)
public class LoginInterceptor extends HandlerInterceptorAdapter {

    private static final ThreadLocal<UserInfo> THREAD_LOCAL = new ThreadLocal<>();

    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 在controller之前执行
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //根据cookie名字获取cookie中的jwt类型的token
        String token = CookieUtils.getCookieValue(request, jwtProperties.getCookieName());
        if (StringUtils.isBlank(token)) return false;
        //使用公钥解析token
        UserInfo userInfo = JwtUtils.getInfoFromToken(token, jwtProperties.getPublicKey());
        //绑定到当前线程
        THREAD_LOCAL.set(userInfo);
        return true;
    }

    /**
     * 后台业务逻辑完毕后，渲染视图前执行
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        //由于使用的是tomcat线程池，线程永远不会销毁，所以要手动解绑线程上的对象
        THREAD_LOCAL.remove();
    }

    /**
     * 获取当前线程绑定的userInfo
     */
    public static UserInfo getUserInfo() {
        return THREAD_LOCAL.get();
    }
}
