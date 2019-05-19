package com.pinyougou.seckill.task;

import com.pinyougou.common.utils.SysConstants;
import com.pinyougou.mapper.TbSeckillGoodsMapper;
import com.pinyougou.pojo.TbSeckillGoods;
import com.pinyougou.pojo.TbSeckillGoodsExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Component
public class SeckillGoosTask {
    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private TbSeckillGoodsMapper tbSeckillGoodsMapper;

    @Scheduled(cron = "0/30 * * * * ? ")
    public void pushGoods() {
        TbSeckillGoodsExample tbSeckillGoodsExample = new TbSeckillGoodsExample();
        TbSeckillGoodsExample.Criteria criteria = tbSeckillGoodsExample.createCriteria();
        //   审核过
        criteria.andStatusEqualTo("1");
        //  活动开始时间 <当前时间<活动结束时间
        Date crunttime = new Date();
        criteria.andStartTimeLessThanOrEqualTo(crunttime);//开始时间要小于当前的时间
        criteria.andEndTimeGreaterThan(crunttime);//结束时间 要大于当前时间
        //  剩余库存>0
        criteria.andStockCountGreaterThan(0);//剩余的库存一定要大于0
        //  并且排除掉redis中本来就有的商品的列表
        Set<Long> ids = redisTemplate.boundHashOps("seckillGoods").keys();
        if (ids != null && ids.size() > 0) {
            List<Long> idsgoods = new ArrayList<>();
            for (Long id : ids) {
                idsgoods.add(id);
            }
            criteria.andIdNotIn(idsgoods);
        }
        //查询
        List<TbSeckillGoods> tbSeckillGoods = tbSeckillGoodsMapper.selectByExample(tbSeckillGoodsExample);

        //存储到redis中
        for (TbSeckillGoods tbSeckillGood : tbSeckillGoods) {
            //将商品数据根据每一个商品的库存来压队列
            pushGoodsList(tbSeckillGood);
            redisTemplate.boundHashOps("seckillGoods").put(tbSeckillGood.getId(), tbSeckillGood);
        }
    }
    /**
     * 将商品数据压如队列中
     * @param goods
     */
    public void pushGoodsList(TbSeckillGoods goods){
        //向同一个队列中压入商品数据
        for (Integer i = 0; i < goods.getStockCount(); i++) {
            //库存为多少就是多少个SIZE 值就是id即可
            redisTemplate.boundListOps(SysConstants.SEC_KILL_GOODS_PREFIX+goods.getId()).leftPush(goods.getId());
        }
    }
}
