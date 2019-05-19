package com.pinyougou.sellergoods.service;

import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.pojoGroup.Goods;

import java.util.List;

public interface GoodsService {
    public void add(Goods goods);

    Goods findOne(Long id);

    public void update(Goods goods);

    List<TbItem> getItemByGoods(Long[] ids,String status);
}
