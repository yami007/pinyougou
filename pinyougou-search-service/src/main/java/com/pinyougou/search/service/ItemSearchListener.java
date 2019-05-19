package com.pinyougou.search.service;

import com.alibaba.fastjson.JSON;
import com.pinyougou.pojo.TbItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.util.List;
import java.util.Map;
@Component
public class ItemSearchListener implements MessageListener {
    @Autowired
    private ItemSearchService itemSearchService;
    @Override
    public void onMessage(Message message) {
        TextMessage textMessage= (TextMessage) message;
        try {
            String text = textMessage.getText();
            List<TbItem> tbItems = JSON.parseArray(text, TbItem.class);
            for (TbItem tbItem : tbItems) {
                Map jsonObject = JSON.parseObject(tbItem.getSpec());
                tbItem.setSpecMap(jsonObject);
            }
            itemSearchService.importItem(tbItems);
            System.out.println("成功导入数据"+text);
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
