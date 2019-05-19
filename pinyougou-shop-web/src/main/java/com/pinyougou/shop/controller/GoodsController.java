package com.pinyougou.shop.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.PageResult;
import com.pinyougou.pojo.Result;
import com.pinyougou.pojo.TbGoods;
import com.pinyougou.pojo.pojoGroup.Goods;
import com.pinyougou.sellergoods.service.GoodsService;
import com.pinyougou.sellergoods.service.TbGoodsService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/goods")
public class GoodsController {
    @Reference
    private GoodsService goodsService;

    @Reference
    private TbGoodsService tbGoodsService;
    @RequestMapping("/add")
    public Result add(@RequestBody Goods goods){
        //获取登录的商户
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        goods.getTbGoods().setSellerId(name);
        try {
            goodsService.add(goods);
            return new Result(true,"添加成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"添加失败");
        }
    }
    @RequestMapping("/update")
    public Result update(@RequestBody Goods goods){
        try {
            goodsService.update(goods);
            return new Result(true,"修改成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"修改失败");
        }
    }
    /**
     * 查询+分页
     * @param
     * @param page
     * @param rows
     * @return
     */
    @RequestMapping("/search")
    public PageResult search(@RequestBody TbGoods goods, int page, int rows  ){
        return tbGoodsService.findPage(goods, page, rows);
    }

    /**
     * 按id查询
     * @param id
     * @return
     */
    @RequestMapping("/findOne")
    public Goods findOne(Long id){
        return goodsService.findOne(id);
    }

}
