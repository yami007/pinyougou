package com.pinyougou.order.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.common.utils.IdWorker;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.mapper.TbOrderItemMapper;
import com.pinyougou.mapper.TbOrderMapper;
import com.pinyougou.mapper.TbPayLogMapper;
import com.pinyougou.order.service.Orderservice;
import com.pinyougou.pojo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class OrderserviceImpl implements Orderservice {
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private IdWorker idWorker;

    @Autowired
    private TbItemMapper tbItemMapper;

    @Autowired
    private TbOrderMapper tbOrderMapper;

    @Autowired
    private TbOrderItemMapper tbOrderItemMapper;

    @Autowired
    private TbPayLogMapper payLogMapper;

    @Override
    public void add(TbOrder order) {
        //得到购物车数据
        List<Cart> cartList = (List<Cart>)
                redisTemplate.boundHashOps("cartList").get(order.getUserId());
        List<String> orderIdList = new ArrayList();//订单ID列表
        double total_money = 0;//总金额 （元）
        for (Cart cart : cartList) {
            long orderId = idWorker.nextId();
            System.out.println("sellerId:" + cart.getSellerId());
            TbOrder tborder = new TbOrder();//新创建订单对象
            tborder.setOrderId(orderId);//订单ID
            tborder.setUserId(order.getUserId());//用户名
            tborder.setPaymentType(order.getPaymentType());//支付类型
            tborder.setStatus("1");//状态：未付款
            tborder.setCreateTime(new Date());//订单创建日期
            tborder.setUpdateTime(new Date());//订单更新日期
            tborder.setReceiverAreaName(order.getReceiverAreaName());//地址
            tborder.setReceiverMobile(order.getReceiverMobile());//手机号
            tborder.setReceiver(order.getReceiver());//收货人
            tborder.setSourceType(order.getSourceType());//订单来源
            tborder.setSellerId(cart.getSellerId());//商家ID
            //循环购物车明细
            double money = 0;
            for (TbOrderItem orderItem : cart.getOrderItemList()) {
                orderItem.setId(idWorker.nextId());
                orderItem.setOrderId(orderId);//订单ID
                orderItem.setSellerId(cart.getSellerId());
                TbItem item = tbItemMapper.selectByPrimaryKey(orderItem.getItemId());//商品
                orderItem.setGoodsId(item.getGoodsId());//设置商品的SPU的ID

                money += orderItem.getTotalFee().doubleValue();//金额累加
                tbOrderItemMapper.insert(orderItem);
            }
            tborder.setPayment(new BigDecimal(money));
            tbOrderMapper.insert(tborder);
            orderIdList.add(orderId + "");//添加到订单列表
            total_money += money;//累加到总金额

        }
        if ("1".equals(order.getPaymentType())) {//如果是微信支付
            TbPayLog payLog = new TbPayLog();
            String outTradeNo = idWorker.nextId() + "";//支付订单号
            payLog.setOutTradeNo(outTradeNo);//支付订单号
            payLog.setCreateTime(new Date());//创建时间
            //订单号列表，逗号分隔
            String ids = orderIdList.toString().replace("[", "").replace("]", "").replace("", "");
            payLog.setOrderList(ids);//订单号列表，逗号分隔
            payLog.setPayType("1");//支付类型
            payLog.setTotalFee((long) (total_money * 100));//总金额(分)
            payLog.setTradeState("0");//支付状态
            payLog.setUserId(order.getUserId());//用户ID
            payLogMapper.insert(payLog);//插入到支付日志表
            redisTemplate.boundHashOps("payLog").put(order.getUserId(), payLog);//放入缓存
        }

        redisTemplate.boundHashOps("cartList").delete(order.getUserId());
    }

    @Override
    public TbPayLog searchPayLogFromRedis(String userId) {
        return (TbPayLog) redisTemplate.boundHashOps("payLog").get(userId);
    }

    @Override
    public void updateOrderStatus(String out_trade_no, String transaction_id) {
        //1.修改支付日志状态
        TbPayLog payLog = payLogMapper.selectByPrimaryKey(out_trade_no);
        payLog.setPayTime(new Date());
        payLog.setTradeState("1");//已支付
        payLog.setTransactionId(transaction_id);//交易号
        payLogMapper.updateByPrimaryKey(payLog);
        //2.修改订单状态
        String orderList = payLog.getOrderList();//获取订单号列表
        String[] orderIds = orderList.split(",");//获取订单号数组

        for (String orderId : orderIds) {
            TbOrder order = tbOrderMapper.selectByPrimaryKey(Long.parseLong(orderId));
            if (order != null) {
                order.setStatus("2");//已付款
                tbOrderMapper.updateByPrimaryKey(order);
            }
        }
        //清除redis缓存数据
        redisTemplate.boundHashOps("payLog").delete(payLog.getUserId());

    }

}
