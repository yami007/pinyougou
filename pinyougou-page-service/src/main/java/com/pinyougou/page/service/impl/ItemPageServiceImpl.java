package com.pinyougou.page.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.mapper.TbGoodsDescMapper;
import com.pinyougou.mapper.TbGoodsMapper;
import com.pinyougou.mapper.TbItemCatMapper;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.page.service.ItemPageService;
import com.pinyougou.pojo.*;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfig;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Service
public class ItemPageServiceImpl implements ItemPageService {

    @Autowired
    private TbGoodsMapper goodsMapper;

    @Autowired
    private TbGoodsDescMapper goodsDescMapper;

    @Autowired
    private FreeMarkerConfig freeMarkerConfig;

    @Autowired
    private TbItemCatMapper tbItemCatMapper;

    @Autowired
    private TbItemMapper itemMapper;


    @Override
    public boolean genItemHtml(Long goodsId) {
        TbGoods tbGoods = goodsMapper.selectByPrimaryKey(goodsId);
        TbGoodsDesc tbGoodsDesc = goodsDescMapper.selectByPrimaryKey(goodsId);
        return getHtmlByFreeMarker(tbGoods, tbGoodsDesc, "item.ftl");
    }

    private boolean getHtmlByFreeMarker(TbGoods tbGoods, TbGoodsDesc tbGoodsDesc, String template) {
        Configuration configuration = freeMarkerConfig.getConfiguration();
        FileWriter fileWriter = null;
        try {
            Template templateObject = configuration.getTemplate(template);
            //分类列表
            TbItemCat category1= tbItemCatMapper.selectByPrimaryKey(tbGoods.getCategory1Id());
            TbItemCat category2= tbItemCatMapper.selectByPrimaryKey(tbGoods.getCategory2Id());
            TbItemCat category3= tbItemCatMapper.selectByPrimaryKey(tbGoods.getCategory3Id());
            //4.SKU列表
            TbItemExample example=new TbItemExample();
            TbItemExample.Criteria criteria = example.createCriteria();
            criteria.andStatusEqualTo("1");//状态为有效
            criteria.andGoodsIdEqualTo(tbGoods.getId());//指定SPU ID
            example.setOrderByClause("is_default desc");//按照状态降序，保证第一个为默认
            List<TbItem> itemList = itemMapper.selectByExample(example);


            Map model = new HashMap<>();
            model.put("goods", tbGoods);
            model.put("goodsDesc", tbGoodsDesc);
            model.put("category1", category1.getName());
            model.put("category2", category2.getName());
            model.put("category3", category3.getName());
            model.put("itemList", itemList);
            fileWriter = new FileWriter(new File("F:\\freemarker\\" + tbGoods.getId() + ".html"));
            templateObject.process(model, fileWriter);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                fileWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
