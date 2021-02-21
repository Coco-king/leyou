package com.leyou.auth.controller;

import com.leyou.auth.config.JwtProperties;
import com.leyou.auth.service.AuthService;
import com.leyou.commom.utils.CookieUtils;
import com.leyou.common.pojo.UserInfo;
import com.leyou.common.utils.JwtUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
@EnableConfigurationProperties(JwtProperties.class)
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 根据用户名密码生成token存入cookie
     */
    @PostMapping("/accredit")
    public ResponseEntity<Void> accredit(
            @RequestParam("username") String username,
            @RequestParam("password") String password,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        String token = authService.accredit(username, password);
        if (StringUtils.isBlank(token)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        CookieUtils.setCookie(request, response, jwtProperties.getCookieName(), token, jwtProperties.getExpire() * 60);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/verify")
    public ResponseEntity<UserInfo> verify(
            @CookieValue("LY_TOKEN") String token,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        if (StringUtils.isBlank(token))
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        try {
            UserInfo userInfo = JwtUtils.getInfoFromToken(token, jwtProperties.getPublicKey());
            //重置token和cookie的有效时间
            token = JwtUtils.generateToken(userInfo, jwtProperties.getPrivateKey(), jwtProperties.getExpire());
            CookieUtils.setCookie(request, response, jwtProperties.getCookieName(), token, jwtProperties.getExpire() * 60);
            return ResponseEntity.ok(userInfo);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @GetMapping("/logout")
    public ResponseEntity<Void> logout(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        CookieUtils.setCookie(request, response, jwtProperties.getCookieName(), "", 0);
        return ResponseEntity.ok().build();
    }
}
