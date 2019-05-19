package com.pinyougou.seckill.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.Result;
import com.pinyougou.pojo.TbSeckillOrder;
import com.pinyougou.seckill.service.SeckillOrderService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/seckillOrder")
@RestController
public class SeckillOrderController {
    @Reference
    private SeckillOrderService seckillOrderService;

    @RequestMapping("/submitOrder")
    public Result submitOrder(Long seckillId) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        if ("anonymousUser".equals(userId)) {//如果未登录
            return new Result(false, "用户未登录");
        }
        try {
            seckillOrderService.submitOrder(seckillId, userId);
            return new Result(true, "提交成功");
        } catch (RuntimeException e) {
            e.printStackTrace();
            return new Result(false, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "提交失败");
        }
    }

    @RequestMapping("/queryStatus")
    public Result queryStatus() {
        //判断是否为匿名登录
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        if ("anonymousUser".equals(userId)) {
            //如果未登录
            //401  用户未登录
            return new Result(false, "401");
        } else {
            //获取未支付订单
            TbSeckillOrder order = seckillOrderService.getOrderByUserId(userId);

            //如果有那么就是有订单 说明创建订单成功跳转到支付页面
            if (order != null) {
                return new Result(true, "订单创建成功,请支付");
            } else {

                Boolean aBoolean = seckillOrderService.existsFlag(userId);

                if (aBoolean) {
                    //402 在排队中
                    return new Result(false, "402");
                } else {
                    //404 下单失败
                    return new Result(false, "404");
                }

            }
        }
    }


}
