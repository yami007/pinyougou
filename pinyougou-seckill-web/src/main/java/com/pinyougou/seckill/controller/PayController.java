package com.pinyougou.seckill.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pay.service.WeixinPayService;
import com.pinyougou.pojo.Result;
import com.pinyougou.pojo.TbSeckillOrder;
import com.pinyougou.seckill.service.SeckillOrderService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/pay")
public class PayController {

    @Reference
    private WeixinPayService weixinPayService;

    @Reference
    private SeckillOrderService seckillOrderService;

    public PayController() {
    }

    @RequestMapping("/createNative")
    public Map createNative() {
        //商户（pinyougou）自己生成一个支付的订单号
        //String out_trade_no=new  IdWorker(1,1).nextId()+"";
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        TbSeckillOrder orderByUserId = seckillOrderService.getOrderByUserId(userId);
        //要付款的金额
        //String total_fee="1";//一分钱
        Map resultMap = null;
        if (orderByUserId != null) {
            long fen = (long) (orderByUserId.getMoney().doubleValue() * 100);//金额（分）
            resultMap = weixinPayService.createNative(orderByUserId.getId() + "", fen + "");
        }
        if (resultMap != null) {
            return resultMap;
        } else {
            return new HashMap();
        }
    }

    @RequestMapping("/queryStatus")
    public Result queryStatus(String out_trade_no) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        Map map = weixinPayService.queryPayStatus(out_trade_no);
        //支付失败  401
        Result result = new Result(false, "401");
        String trade_state = (String) map.get("trade_state");

        if ("SUCCESS".equals(map.get("trade_state"))) {
            result = new Result(true, "支付成功");
            //如果支付成功需要更新订单的状态

            seckillOrderService.saveOrderFromRedisToDb(userId, (String) map.get("transaction_id"));

            return new Result(true, "支付成功");
        }

        if (trade_state != null && "NOTPAY".equals(trade_state)) {
            return new Result(false, "尽快支付");
        }
        if (trade_state != null && "PAYERROR".equals(trade_state)) {
            return new Result(false, "支付失败");
        }

        return result;
    }
}