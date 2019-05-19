package com.pinyougou.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.common.utils.CookieUtil;
import com.pinyougou.pojo.Cart;
import com.pinyougou.pojo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@RequestMapping("/cart")
@CrossOrigin(origins="http://localhost:9105",allowCredentials="true")
public class CartController {
    @Reference
    private CartService cartService;
    @Autowired
    private HttpServletRequest request;
    @Autowired
    private HttpServletResponse response;

    /**
     * 查询购物车
     *
     * @return
     */
    @RequestMapping("/findCartList")
    public List<Cart> findCartList() {
        //得到登陆人账号,判断当前是否有人登陆
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        String cartList = CookieUtil.getCookieValue(request, "cartList", "UTF-8");
        if (cartList == null || cartList.equals("")) {
            cartList = "[]";
        }
        List<Cart> carts = JSON.parseArray(cartList, Cart.class);
        if (username.equals("anonymousUser")) {
            return carts;
        } else {
            List<Cart> cartList_redis = cartService.findCartListFromRedis(username);//从redis中提取
            if (carts != null && carts.size() > 0) {
                cartList_redis= cartService.mergeCartList(carts, cartList_redis);
            }
            CookieUtil.deleteCookie(request,response,"cartList");
            cartService.saveCartListToRedis(username, cartList_redis);
            return cartList_redis;
        }

    }

    @RequestMapping("/addCartList")
    public Result addCartList(@RequestParam("itemId") Long itemId, @RequestParam("num") Integer num) {
        //得到登陆人账号,判断当前是否有人登陆
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        try {
            List<Cart> cartList = findCartList();
            cartList = cartService.addGoodsToCartList(cartList, itemId, num);
            if (username.equals("anonymousUser")) { //如果是未登录，保存到cookie
                CookieUtil.setCookie(request, response, "cartList", JSON.toJSONString(cartList), 3600 * 24, "UTF-8");
            } else {
                cartService.saveCartListToRedis(username, cartList);
            }

            return new Result(true, "添加购物车成功");
        } catch (Exception e) {
            return new Result(false, "添加购物车失败");
        }
    }
}
