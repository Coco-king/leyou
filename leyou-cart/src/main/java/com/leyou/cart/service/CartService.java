package com.leyou.cart.service;

import com.leyou.cart.client.GoodsClient;
import com.leyou.cart.interceptor.LoginInterceptor;
import com.leyou.cart.pojo.Cart;
import com.leyou.commom.utils.JsonUtils;
import com.leyou.common.pojo.UserInfo;
import com.leyou.item.pojo.Sku;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class CartService {

    private static final String KEY_PREFIX = "user:cart:";

    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 添加一条购物车
     */
    public Cart addCart(Cart cart) {
        //获取线程绑定的userInfo
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        //查询购物车记录
        BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(KEY_PREFIX + userInfo.getId());
        return addCart(cart, hashOps, userInfo.getId());
    }

    /**
     * 查询当前用户的购物车
     */
    public List<Cart> findCarts(List<Cart> cartList) {
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        //判断是否有该购物车
        if (!Objects.requireNonNull(redisTemplate.hasKey(KEY_PREFIX + userInfo.getId()))) {
            return null;
        }
        //获取购物车map
        BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(KEY_PREFIX + userInfo.getId());
        //获取商品集合
        if (!CollectionUtils.isEmpty(cartList)) {
            for (Cart cart : cartList) {
                addCart(cart, hashOps, userInfo.getId());
            }
        }
        List<Object> cartsJson = hashOps.values();
        if (CollectionUtils.isEmpty(cartsJson)) return null;
        return cartsJson.stream().map(cartJson -> JsonUtils.parse(cartJson.toString(), Cart.class)).collect(Collectors.toList());
    }

    /**
     * 添加到指定的redis中
     */
    private Cart addCart(Cart cart, BoundHashOperations<String, Object, Object> hashOps, Long userId) {
        //判断当前商品是否在购物车中
        String key = cart.getSkuId().toString();
        //保存原有的数量
        Integer num = cart.getNum();
        if (Objects.requireNonNull(hashOps.hasKey(key))) {
            //在，更新数量
            cart = JsonUtils.parse(Objects.requireNonNull(hashOps.get(key)).toString(), Cart.class);
            if (cart == null) return null;
            cart.setNum(cart.getNum() + num);
        } else {
            //不在，添加，根据skuId查询sku
            Sku sku = goodsClient.findSkuBySkuId(cart.getSkuId());
            cart.setUserId(userId);
            cart.setTitle(sku.getTitle());
            cart.setOwnSpec(sku.getOwnSpec());
            cart.setPrice(sku.getPrice());
            cart.setImage(StringUtils.isBlank(sku.getImages()) ? "" : StringUtils.split(sku.getImages(), ",")[0]);
        }
        //最后存入redis
        hashOps.put(key, Objects.requireNonNull(JsonUtils.serialize(cart)));
        return new Cart(cart.getImage(), cart.getNum(), cart.getOwnSpec());
    }

    /**
     * 修改数量
     */
    public void updateNum(Cart cart) {
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        //判断是否有该购物车
        if (!Objects.requireNonNull(redisTemplate.hasKey(KEY_PREFIX + userInfo.getId()))) {
            return;
        }
        //获取购物车map
        BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(KEY_PREFIX + userInfo.getId());
        //保存数量
        Integer num = cart.getNum();
        //获取redis中的商品数据
        String cartJson = Objects.requireNonNull(hashOps.get(cart.getSkuId().toString())).toString();
        //反序列化为对象
        cart = JsonUtils.parse(cartJson, Cart.class);
        if (cart == null) return;
        cart.setNum(num);
        hashOps.put(cart.getSkuId().toString(), Objects.requireNonNull(JsonUtils.serialize(cart)));
    }

    /**
     * 删除购物车中的商品
     */
    public void deleteCart(Long skuId) {
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        //判断是否有该购物车
        if (!Objects.requireNonNull(redisTemplate.hasKey(KEY_PREFIX + userInfo.getId()))) {
            return;
        }
        //获取购物车map
        BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(KEY_PREFIX + userInfo.getId());
        hashOps.delete(skuId.toString());
    }

    /**
     * 删除购物车中选择的商品
     */
    public void deleteCarts(List<Cart> cartIds) {
        cartIds.forEach(cart -> deleteCart(cart.getSkuId()));
    }
}
