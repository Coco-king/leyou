package com.leyou.user.controller;

import com.leyou.user.pojo.User;
import com.leyou.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;

@Controller
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 根据type校检用户名或手机号
     *
     * @param type 1：校检用户名 2：校检手机号
     * @return true：通过 false：不通过
     */
    @GetMapping("/check/{data}/{type}")
    public ResponseEntity<Boolean> checkUserData(@PathVariable("data") String data, @PathVariable("type") Integer type) {
        Boolean bool = userService.checkUserData(data, type);
        if (bool == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(bool);
    }

    /**
     * 向指定手机号发送验证码，并保存在redis中
     */
    @PostMapping("/code")
    public ResponseEntity<Void> sendVerifyCode(@RequestParam("phone") String phone) {
        userService.sendVerifyCode(phone);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public ResponseEntity<Void> register(@Valid User user, @RequestParam("code") String code) {
        userService.register(user, code);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 用户查询/登录
     */
    @GetMapping("/query")
    public ResponseEntity<User> queryUser(@RequestParam("username") String username, @RequestParam("password") String password) {
        User user = userService.queryUser(username, password);
        if (user == null)
            return ResponseEntity.badRequest().build();
        return ResponseEntity.ok(user);
    }
}
