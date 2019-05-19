package com.pinyougou.seckill.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.common.utils.SysConstants;
import com.pinyougou.mapper.TbSeckillOrderMapper;
import com.pinyougou.pojo.TbSeckillGoods;
import com.pinyougou.pojo.TbSeckillOrder;
import com.pinyougou.seckill.pojo.RecordOrderInfo;
import com.pinyougou.seckill.runnable.OrderHandlerThread;
import com.pinyougou.seckill.service.SeckillOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Date;

@Service
public class seckillOrderServiceImpl implements SeckillOrderService {
    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;
    @Autowired
    private OrderHandlerThread orderHandlerThread;
    @Autowired
    private TbSeckillOrderMapper tbSeckillOrderMapper;

    @Override
    public void submitOrder(Long seckillId, String userId) {
        Long goodsId = (Long) redisTemplate.boundListOps(SysConstants.SEC_KILL_GOODS_PREFIX + seckillId).rightPop();
        if (goodsId == null) {
            //说明商品已经没有库存了
            throw new RuntimeException("商品已被抢光");
        }

        //判断是否有未支付订单
        Boolean exists = redisTemplate.boundHashOps("seckillOrder").hasKey(userId);

        if (exists) {
            throw new RuntimeException("请先支付已有的订单");
        }

        Boolean aBoolean = redisTemplate.boundHashOps(SysConstants.SEC_USER_QUEUE_FLAG_KEY).hasKey(userId);

        if (aBoolean) {
            throw new RuntimeException("正在排队");
        }

        //判断该商品的抢购用户数 是否已经达到上限  这里我们要求抢购人数不能多于商品的库存数
        Long size = redisTemplate.boundListOps(SysConstants.SEC_KILL_GOODS_PREFIX + seckillId).size();
        TbSeckillGoods seckillGoods = (TbSeckillGoods) redisTemplate.boundHashOps("seckillGoods").get(seckillId);

        if (size.intValue() >= seckillGoods.getStockCount().intValue()) {
            throw new RuntimeException("该商品抢购排队用户达到上限!");
        }

        //标识某商品有多少用户在抢购 抢购一个压入队列(也可以使用key incr来判断)
        redisTemplate.boundListOps(SysConstants.SEC_KILL_LIMIT_PREFIX + seckillId).leftPush(seckillId);

        //用户排队下单  压入队列中
        redisTemplate.boundListOps(SysConstants.SEC_KILL_GOODS_PREFIX).leftPush(new RecordOrderInfo(userId, seckillId));

        //表示用户下单
        redisTemplate.boundHashOps(SysConstants.SEC_USER_QUEUE_FLAG_KEY).put(userId, seckillId);

        //多线程调用
        //new Thread(orderHandlerThread).start();
        threadPoolTaskExecutor.execute(orderHandlerThread);
    }

    @Override
    public TbSeckillOrder getOrderByUserId(String userId) {
        return (TbSeckillOrder) redisTemplate.boundHashOps("seckillOrder").get(userId);
    }

    @Override
    public Boolean existsFlag(String userId) {
        return redisTemplate.boundHashOps(SysConstants.SEC_USER_QUEUE_FLAG_KEY).hasKey(userId);
    }

    @Override
    public void saveOrderFromRedisToDb(String userId, String transactionId) {
        //查询订单信息
        TbSeckillOrder seckillOrder = (TbSeckillOrder) redisTemplate.boundHashOps("seckillOrder").get(userId);

        if (seckillOrder != null) {
            //更新订单数据，并持久化到数据库中
            seckillOrder.setPayTime(new Date());
            seckillOrder.setStatus("1");
            seckillOrder.setTransactionId(transactionId);

            //更新到数据库中
            tbSeckillOrderMapper.updateByPrimaryKeySelective(seckillOrder);

            //移除Redis缓存中的订单数据
            redisTemplate.boundHashOps("seckillOrder").delete(userId);
        }
    }
}
