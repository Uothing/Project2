package com.leyou.service;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.threadlocal.UserHolder;
import com.leyou.common.utils.JsonUtils;
import com.leyou.entity.Cart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @version V1.0
 * @author: weiyuan
 * @date: 2019/11/19 11:02
 * @description:
 */
@Service
public class CartService {

    private static final String KEY_PREFIX = "ly:cart:uid:";

    @Autowired
    private StringRedisTemplate redisTemplate;

    // 添加购物车
    public void addItemToCart(Cart cart) {

        // 获取当前用户
        String key = KEY_PREFIX + UserHolder.getUser().getId();

        BoundHashOperations<String, String, String> hashOps = redisTemplate.boundHashOps(key);

        // 获取商品id
        String hashKey = cart.getSkuId().toString();
        // 获取数量
        Integer num = cart.getNum();
        Boolean hasKey = hashOps.hasKey(hashKey);
        if (hasKey != null && hasKey) {
            // 存在，修改数量
            cart = JsonUtils.toBean(hashOps.get(hashKey), Cart.class);
            cart.setNum(num + cart.getNum());
        }
        // 写入redis
        hashOps.put(hashKey, JsonUtils.toString(cart));

    }

    // 查询购物车
    public List<Cart> queryCartList() {
        // 获取当前用户
        String key = KEY_PREFIX + UserHolder.getUser().getId();
        Boolean boo = redisTemplate.hasKey(key);
        if(boo == null || !boo){
            // 不存在，直接返回
            throw new LyException(ExceptionEnum.CARTS_NOT_FOUND);
        }
        BoundHashOperations<String, String, String> hashOps = this.redisTemplate.boundHashOps(key);
        // 判断是否有数据
        Long size = hashOps.size();
        if(size == null || size < 0){
            // 不存在，直接返回
            throw new LyException(ExceptionEnum.CARTS_NOT_FOUND);
        }
        List<String> carts = hashOps.values();
        // 查询购物车数据
        return carts.stream()
                .map(json -> JsonUtils.toBean(json, Cart.class))
                .collect(Collectors.toList());
    }
}
