package com.pinyougou.seckill.service;

import com.pinyougou.pojo.TbSeckillOrder;

public interface SeckillOrderService {
    /**
     * 秒杀下单
     * @param seckillId 秒杀商品的ID
     * @param userId 下单的用户ID
     */
    public void submitOrder(Long seckillId,String userId);
    /**
     * 根据用户名获取用户下的未支付的订单
     * @param userId
     * @return
     */
    TbSeckillOrder getOrderByUserId(String userId);
    /**
     * 判断某一个用户是否在排队中
     * @param userId
     * @return
     */
    Boolean existsFlag(String userId);
    /**
     * 更新该用户的 订单从redis到数据库中
     * @param userId  用户ID
     * @param transactionId  微信支付成功后返回的支付流水号
     */
    void saveOrderFromRedisToDb(String userId,String transactionId);
}
