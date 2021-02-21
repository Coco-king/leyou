package com.leyou.gateway.filter;

import com.leyou.commom.utils.CookieUtils;
import com.leyou.common.utils.JwtUtils;
import com.leyou.gateway.config.FilterProperties;
import com.leyou.gateway.config.JwtProperties;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.TimeZone;

@Component
@EnableConfigurationProperties({JwtProperties.class, FilterProperties.class})
public class LoginFilter extends ZuulFilter {

    @Autowired
    private JwtProperties jwtProperties;

    @Autowired
    private FilterProperties filterProperties;

    @Override
    public String filterType() {
        return "pre";
    }

    @Override
    public int filterOrder() {
        return 20;
    }

    @Override
    public boolean shouldFilter() {
        //添加白名单过滤规则
        List<String> allowPaths = filterProperties.getAllowPaths();
        //初始化zuul网关运行上下文并获取request
        HttpServletRequest request = RequestContext.getCurrentContext().getRequest();
        //获取请求的路径
        String url = request.getRequestURL().toString();
        for (String allowPath : allowPaths) {
            //如果请求路径中包含白名单的内容那么就不执行run方法
            if (StringUtils.contains(url, allowPath))
                return false;
        }
        return true;
    }

    @Override
    public Object run() throws ZuulException {
        //初始化zuul网关运行上下文
        RequestContext context = RequestContext.getCurrentContext();
        //获取request
        HttpServletRequest request = context.getRequest();
        //获取token
        String token = CookieUtils.getCookieValue(request, jwtProperties.getCookieName());
        //判断token是否为空
        if (StringUtils.isBlank(token)) {
            //设置不转发
            context.setSendZuulResponse(false);
            //设置响应状态码
            context.setResponseStatusCode(HttpStatus.UNAUTHORIZED.value());
            return null;
        }
        //解析token
        try {
            JwtUtils.getInfoFromToken(token, jwtProperties.getPublicKey());
        } catch (Exception e) {
            e.printStackTrace();
            //设置不转发
            context.setSendZuulResponse(false);
            //设置响应状态码
            context.setResponseStatusCode(HttpStatus.UNAUTHORIZED.value());
        }
        return null;
    }
}
