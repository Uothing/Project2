package com.leyou.web;

import com.leyou.entity.Cart;
import com.leyou.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @version V1.0
 * @author: weiyuan
 * @date: 2019/11/19 11:01
 * @description:
 */
@RestController
@RequestMapping
public class CartController {

    @Autowired
    private CartService cartService;

    /**
     * 添加购物车
     * @param cart 前端购物车数据
     * @return 无
     */
    @PostMapping
    public ResponseEntity<Void> addItemToCart(@RequestBody Cart cart) {

        cartService.addItemToCart(cart);
        return ResponseEntity.ok().build();
    }

    /**
     * 查询购物车
     * @return
     */
    @GetMapping("/list")
    public ResponseEntity<List<Cart>> queryCartList() {
        List<Cart> carts = cartService.queryCartList();
        if (carts == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        return ResponseEntity.ok(carts);
    }
}
