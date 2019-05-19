package com.pinyougou.seckill.runnable;

import com.pinyougou.common.utils.IdWorker;
import com.pinyougou.common.utils.SysConstants;
import com.pinyougou.mapper.TbSeckillGoodsMapper;
import com.pinyougou.pojo.TbSeckillGoods;
import com.pinyougou.pojo.TbSeckillOrder;
import com.pinyougou.seckill.pojo.RecordOrderInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@Scope("prototype")//为了保证线程安全 可能会用到一些共享变量，所以需要多例
public class OrderHandlerThread implements Runnable {
    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private TbSeckillGoodsMapper seckillGoodsMapper;

    @Autowired
    private IdWorker idWorker;

    @Override
    public void run() {

        //从队列中取出
        RecordOrderInfo info = (RecordOrderInfo) redisTemplate.boundListOps(SysConstants.SEC_KILL_USER_ORDER_LIST).rightPop();



        if(info!=null) {

            //移除用户在排队下单标识
            redisTemplate.boundHashOps(SysConstants.SEC_USER_QUEUE_FLAG_KEY).delete(info.getUserId());



            //从队列中弹出 某一个商品的抢单人数
            redisTemplate.boundListOps(SysConstants.SEC_KILL_LIMIT_PREFIX+info.getUserId()).rightPop();



            //从nosql数据库中获取商品
            TbSeckillGoods killgoods = (TbSeckillGoods) redisTemplate.boundHashOps("seckillGoods").get(info.getId());
            //将这个商品的库存减少
            killgoods.setStockCount(killgoods.getStockCount() - 1);//减少
            redisTemplate.boundHashOps("seckillGoods").put(info.getId(), killgoods);

            if (killgoods.getStockCount() <= 0) {//如果已经被秒光
                seckillGoodsMapper.updateByPrimaryKey(killgoods);//同步到数据库
                redisTemplate.boundHashOps("seckillGoods").delete(info.getId());//将redis中的该商品清除掉
            }
            //创建订单
            long orderId = idWorker.nextId();
            TbSeckillOrder seckillOrder = new TbSeckillOrder();
            seckillOrder.setId(orderId);//设置订单的ID 这个就是out_trade_no
            seckillOrder.setCreateTime(new Date());//创建时间
            seckillOrder.setMoney(killgoods.getCostPrice());//秒杀价格  价格
            seckillOrder.setSeckillId(info.getId());//秒杀商品的ID
            seckillOrder.setSellerId(killgoods.getSellerId());
            seckillOrder.setUserId(info.getUserId());//设置用户ID
            seckillOrder.setStatus("0");//状态 未支付
            //将构建的订单保存到redis中
            redisTemplate.boundHashOps("seckillOrder").put(info.getUserId(), seckillOrder);
        }
    }

}
