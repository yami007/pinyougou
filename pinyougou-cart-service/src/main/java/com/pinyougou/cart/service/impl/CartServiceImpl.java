package com.pinyougou.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.Cart;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbOrderItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class CartServiceImpl implements CartService {
    @Autowired
    private TbItemMapper itemMapper;

    @Autowired
    private RedisTemplate redisTemplate;
    @Override
    public List<Cart> addGoodsToCartList(List<Cart> cartList, Long itemId, Integer num) {
        //1.根据商品SKU ID查询SKU商品信息
        TbItem item = itemMapper.selectByPrimaryKey(itemId);
        if (item == null) {
            throw new RuntimeException("商品不存在");
        }
        if (!item.getStatus().equals("1")) {
            throw new RuntimeException("商品状态无效");
        }
        //获取商家id
        String sellerId = item.getSellerId();
        //判断购物车中是否存在该商家商品
        Cart cart = searchCartBySellerId(cartList, sellerId);
        if (cart == null) {
            //4.1 新建购物车对象 ，
            cart = new Cart();
            cart.setSellerId(sellerId);
            cart.setSellerName(item.getSeller());
            TbOrderItem tbOrderItem = createOrderItem(item, num);
            List<TbOrderItem> list = new ArrayList<>();
            list.add(tbOrderItem);
            cart.setOrderItemList(list);
            //4.2将购物车对象添加到购物车列表
            cartList.add(cart);
        } else {
            //5.如果购物车列表中存在该商家的购物车
            // 判断购物车明细列表中是否存在该商品
            TbOrderItem tbOrderItem = searchOrderItemByItemId(cart.getOrderItemList(), itemId);
            if (tbOrderItem == null) {
                //5.1. 如果没有，新增购物车明细
                TbOrderItem orderItem = createOrderItem(item, num);
                cart.getOrderItemList().add(orderItem);
            } else {
                //5.2. 如果有，在原购物车明细上添加数量，更改金额
                tbOrderItem.setNum(tbOrderItem.getNum() + num);
                tbOrderItem.setTotalFee(new BigDecimal(tbOrderItem.getNum() * tbOrderItem.getPrice().doubleValue()));
                //如果数量操作后小于等于0，则移除
                if (tbOrderItem.getNum() <= 0) {
                    cart.getOrderItemList().remove(tbOrderItem);
                }
                //如果移除后cart的明细数量为0，则将cart移除
                if (cart.getOrderItemList().size() <= 0) {
                    cartList.remove(cart);
                }
            }
        }
        return cartList;
    }

    @Override
    public List<Cart> findCartListFromRedis(String username) {
        System.out.println("从redis中提取购物车数据....."+username);
        List<Cart>cartList = (List<Cart>) redisTemplate.boundHashOps("cartList").get(username);
        if(cartList==null){
            cartList=new ArrayList();
        }
        return cartList;

    }

    @Override
    public void saveCartListToRedis(String username, List<Cart> cartList) {
        System.out.println("向redis存入购物车数据....."+username);
        redisTemplate.boundHashOps("cartList").put(username, cartList);
    }

    /**
     * 合并购物车
     * @param cartList_cookie
     * @param cartList_redis
     * @return
     */
    @Override
    public List<Cart> mergeCartList(List<Cart> cartList_cookie, List<Cart> cartList_redis) {
       for (Cart cart : cartList_cookie) {
            boolean flag1=false;
            for (Cart cart1 : cartList_redis) {
                if(cart.getSellerId().equals(cart1.getSellerId())){
                    List<TbOrderItem> orderItemList = cart.getOrderItemList();
                    List<TbOrderItem> orderItemList1 = cart1.getOrderItemList();
                    for (TbOrderItem tbOrderItem : orderItemList) {
                        boolean flag=false;
                        for (TbOrderItem orderItem : orderItemList1) {
                            if(tbOrderItem.getItemId().longValue()==orderItem.getItemId().longValue()){
                                orderItem.setNum(orderItem.getNum()+tbOrderItem.getNum());
                                flag=true;
                                break;
                            }
                        }
                        if(!flag){
                            orderItemList1.add(tbOrderItem);
                            cart1.setOrderItemList(orderItemList1);
                        }
                    }
                    flag1=true;
                    break;
                }
            }
            if(!flag1){
                cartList_redis.add(cart);
            }
        }
        return cartList_redis;

        /*System.out.println("合并购物车");
        for(Cart cart: cartList_cookie){
            for(TbOrderItem orderItem:cart.getOrderItemList()){
                cartList_redis= addGoodsToCartList(cartList_redis,orderItem.getItemId(),orderItem.getNum());
            }
        }
        return cartList_redis;*/

    }

    private TbOrderItem searchOrderItemByItemId(List<TbOrderItem> orderItemList, Long itemId) {
        for (TbOrderItem tbOrderItem : orderItemList) {
            if ((tbOrderItem.getItemId().longValue()) == (itemId.longValue())) {
                return tbOrderItem;
            }
        }
        return null;
    }

    private TbOrderItem createOrderItem(TbItem item, Integer num) {
        if (num <= 0) {
            throw new RuntimeException("数量非法");
        }

        TbOrderItem orderItem = new TbOrderItem();
        orderItem.setGoodsId(item.getGoodsId());
        orderItem.setItemId(item.getId());
        orderItem.setNum(num);
        orderItem.setPicPath(item.getImage());
        orderItem.setPrice(item.getPrice());
        orderItem.setSellerId(item.getSellerId());
        orderItem.setTitle(item.getTitle());
        orderItem.setTotalFee(new BigDecimal(item.getPrice().doubleValue() * num));
        return orderItem;

    }

    /**
     * 判断购物车中是否有该商家的商品
     *
     * @param cartList
     * @param sellerId
     * @return
     */
    private Cart searchCartBySellerId(List<Cart> cartList, String sellerId) {
        for (Cart cart : cartList) {
            if (cart.getSellerId().equals(sellerId)) {
                return cart;
            }
        }
        return null;
    }
}
