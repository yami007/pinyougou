package com.pinyougou.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.order.service.Orderservice;
import com.pinyougou.pojo.Result;
import com.pinyougou.pojo.TbOrder;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/order")
public class OrderController {
    @Reference
    private Orderservice orderservice;

    @RequestMapping("/add")
    public Result add(@RequestBody TbOrder tbOrder) {
        //获取当前登录人账号
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        tbOrder.setUserId(username);
        tbOrder.setSourceType("2");//订单来源  PC
        try {
            orderservice.add(tbOrder);
            return new Result(true, "增加成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "增加失败");
        }

    }
}
