package com.leyou.user.service;

import com.leyou.commom.utils.NumberUtils;
import com.leyou.user.mapper.UserMapper;
import com.leyou.user.pojo.User;
import com.leyou.user.utils.CodecUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class UserService {

    private static final String KEY_PREFIX = "user:verify:";

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private AmqpTemplate amqpTemplate;

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 根据type校检用户名或手机号
     *
     * @param type 1：校检用户名 2：校检手机号
     * @return true：通过 false：不通过
     */
    public Boolean checkUserData(String data, Integer type) {
        User record = new User();
        if (type == 1) {
            record.setUsername(data);
        } else if (type == 2) {
            record.setPhone(data);
        } else {
            return null;
        }
        return userMapper.selectCount(record) == 0;
    }

    /**
     * 向指定手机号发送验证码，并保存在redis中
     */
    public void sendVerifyCode(String phone) {
        if (StringUtils.isBlank(phone)) return;
        //生成验证码
        String code = NumberUtils.generateCode(6);
        //发送消息到rabbitMQ
        Map<String, String> msg = new HashMap<>();
        msg.put("phone", phone);
        msg.put("code", code);
        amqpTemplate.convertAndSend("LEYOU.SMS.EXCHANGE", "verify.code", msg);
        //保存到redis中
        redisTemplate.opsForValue().set(KEY_PREFIX + phone, code, 5L, TimeUnit.MINUTES);
    }

    /**
     * 用户注册
     */
    public void register(User user, String code) {
        //获取缓存中的验证码
        String redisCode = redisTemplate.opsForValue().get(KEY_PREFIX + user.getPhone());
        //比对验证码是否正确
        if (!StringUtils.equals(code, redisCode)) return;
        //生成盐
        String salt = CodecUtils.generateSalt();
        //加盐加密
        user.setPassword(CodecUtils.md5Hex(user.getPassword(), salt));
        //添加用户
        user.setId(null);
        user.setCreated(new Date());
        user.setSalt(salt);
        userMapper.insertSelective(user);
        //删除缓存
        redisTemplate.delete(KEY_PREFIX + user.getPhone());
    }

    /**
     * 用户查询/登录
     */
    public User queryUser(String username, String password) {
        //校检用户名密码是否为空
        if (StringUtils.isBlank(username) || StringUtils.isBlank(password)) return null;
        //根据用户名在数据库中查询用户
        User user = new User();
        user.setUsername(username);
        user = userMapper.selectOne(user);
        //判断user是否为空
        if (user == null) return null;
        //对用户输入的密码加盐加密
        password = CodecUtils.md5Hex(password, user.getSalt());
        //对比数据库中密码
        if (StringUtils.equals(password, user.getPassword())) return user;
        return null;
    }
}
