package com.pinyougou.cart.service;

import com.pinyougou.pojo.Cart;

import java.util.List;

public interface CartService {
    /**
     * 添加商品到购物车
     * @param cartList
     * @param itemId
     * @param num
     * @return
     */
    public List<Cart> addGoodsToCartList(List<Cart>cartList, Long itemId, Integer num );
    /**
     * 从redis中查询购物车
     * @param username
     * @return
     */
    public List<Cart> findCartListFromRedis(String username);

    /**
     * 将购物车保存到redis
     * @param username
     * @param cartList
     */
    public void saveCartListToRedis(String username,List<Cart> cartList);

    /**
     *
     * @param cartList_cookie
     * @param cartList_redis
     * @return
     */
    public List<Cart> mergeCartList(List<Cart>cartList_cookie,List<Cart>cartList_redis);

}
