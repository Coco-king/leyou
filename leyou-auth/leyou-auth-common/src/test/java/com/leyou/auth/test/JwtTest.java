package com.leyou.auth.test;

import com.leyou.common.pojo.UserInfo;
import com.leyou.common.utils.JwtUtils;
import com.leyou.common.utils.RsaUtils;
import org.junit.Before;
import org.junit.Test;

import java.security.PrivateKey;
import java.security.PublicKey;

public class JwtTest {

    private static final String pubKeyPath = "src/main/resources/Rsa/rsa.pub";

    private static final String priKeyPath = "src/main/resources/Rsa/rsa.pri";

    private PublicKey publicKey;

    private PrivateKey privateKey;

    @Test
    public void testRsa() throws Exception {
        RsaUtils.generateKey(pubKeyPath, priKeyPath, "234");
    }

    @Before
    public void testGetRsa() throws Exception {
        this.publicKey = RsaUtils.getPublicKey(pubKeyPath);
        this.privateKey = RsaUtils.getPrivateKey(priKeyPath);
    }

    @Test
    public void testGenerateToken() throws Exception {
        // 生成token
        String token = JwtUtils.generateToken(new UserInfo(20L, "tom"), privateKey, 5);
        System.out.println("token = " + token);
    }

    @Test
    public void testParseToken() throws Exception {
        String token = "eyJhbGciOiJSUzI1NiJ9.eyJpZCI6MjAsInVzZXJuYW1lIjoidG9tIiwiZXhwIjoxNjEwNzg3MzcyfQ.cvjnkP14TydyzN-AUWZQkgQGd92XiBghhY-8wUAZHJGBT6hiSFunvSkWS_SZsKk6aA8rS-bw4oIgPwPjN7gHzCQyZpMMg1qYNEIqvnA3uVUygtokYYZMKN3EfhJ90tQKqyCIZAPFspPwNJzLAso5ZCcpn79UdYQ8HdLB3GQ8C2Y";
        // 解析token
        UserInfo user = JwtUtils.getInfoFromToken(token, publicKey);
        System.out.println("id: " + user.getId());
        System.out.println("userName: " + user.getUsername());
    }
}