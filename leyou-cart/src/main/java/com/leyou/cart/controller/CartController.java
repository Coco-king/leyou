package com.leyou.cart.controller;

import com.leyou.cart.pojo.Cart;
import com.leyou.cart.pojo.CartList;
import com.leyou.cart.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class CartController {

    @Autowired
    private CartService cartService;

    /**
     * 添加一条购物车
     */
    @PostMapping("/addCart")
    public ResponseEntity<Cart> addCart(@RequestBody Cart cart) {
        cart = cartService.addCart(cart);
        return ResponseEntity.ok(cart);
    }

    /**
     * 查询当前用户的购物车
     */
    @PostMapping("/findCarts")
    public ResponseEntity<List<Cart>> findCarts(@RequestBody CartList cartList) {
        List<Cart> carts = cartService.findCarts(cartList.getCartList());
        if (CollectionUtils.isEmpty(carts))
            return ResponseEntity.notFound().build();
        return ResponseEntity.ok(carts);
    }

    /**
     * 更新数量
     */
    @PutMapping("/updateNum")
    public ResponseEntity<Void> updateNum(@RequestBody Cart cart) {
        cartService.updateNum(cart);
        return ResponseEntity.noContent().build();
    }

    /**
     * 删除购物车中的商品
     */
    @DeleteMapping("/deleteCart/{skuId}")
    public ResponseEntity<Void> deleteCart(@PathVariable("skuId") Long skuId) {
        cartService.deleteCart(skuId);
        return ResponseEntity.ok().build();
    }

    /**
     * 删除购物车中选择的商品
     */
    @PostMapping("/deleteCarts")
    public ResponseEntity<Void> deleteCarts(@RequestBody CartList cartList) {
        cartService.deleteCarts(cartList.getCartList());
        return ResponseEntity.ok().build();
    }
}
