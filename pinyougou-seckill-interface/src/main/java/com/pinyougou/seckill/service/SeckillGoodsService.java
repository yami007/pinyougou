package com.pinyougou.seckill.service;

import com.pinyougou.pojo.TbSeckillGoods;

import java.util.List;

public interface SeckillGoodsService {
    public List<TbSeckillGoods> findList();
    /**
     * 根据商品的ID 获取商品的数据
     * @param id
     * @return
     */
    public TbSeckillGoods findOne(Long id);
}
