package com.pinyougou.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.pinyougou.mapper.*;
import com.pinyougou.pojo.*;
import com.pinyougou.pojo.pojoGroup.Goods;
import com.pinyougou.sellergoods.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class GoodsServiceImpl implements GoodsService {
    @Autowired
    private TbGoodsMapper tbGoodsMapper;
    @Autowired
    private TbGoodsDescMapper tbGoodsDescMapper;
    @Autowired
    private TbItemMapper tbItemMapper;
    @Autowired
    private TbBrandMapper tbBrandMapper;
    @Autowired
    private TbItemCatMapper tbItemCatMapper;
    @Autowired
    private TbSellerMapper tbSellerMapper;

    @Override
    public void add(Goods goods) {
        //插入SPU商品信息
        TbGoods tbGoods = goods.getTbGoods();
        tbGoods.setAuditStatus(TbGoods.getNOT_CHECk());
        tbGoodsMapper.insertSelective(tbGoods);
        // 插入商品扩展信息
        TbGoodsDesc tbGoodsDesc = goods.getTbGoodsDesc();
        tbGoodsDesc.setGoodsId(tbGoods.getId());
        tbGoodsDescMapper.insertSelective(tbGoodsDesc);
        // 判断是否自定义规则，
        String isEnableSpec = tbGoods.getIsEnableSpec();
        if("1".equals(isEnableSpec)){
        // 如自定义，则循环插入对应SKU商品信息
            List<TbItem> tbItemList = goods.getTbItemList();
            for (TbItem tbItem : tbItemList) {
                //通过spu的商品名称和sku的商品规格生成sku的标题
                String title = tbGoods.getGoodsName();
                String spec = tbItem.getSpec();
                Map<String,Object> parseObject = JSON.parseObject(spec);
                for (String key : parseObject.keySet()) {
                    String str = (String) parseObject.get(key);
                    title+=" "+str;
                }
                tbItem.setTitle(title);
                //设置商品图片
                String itemImages = tbGoodsDesc.getItemImages();
                List<Map> itemImageList = JSON.parseArray(itemImages, Map.class);
                if(itemImageList.size()>0) {
                    Map map = itemImageList.get(0);
                    tbItem.setImage((String) map.get("url"));
                }
                //设置叶子类目
                tbItem.setCategoryid(tbGoods.getCategory3Id());
                //设置商品状态
                tbItem.setStatus(TbItem.getNORMAL());
                //创建时间
                tbItem.setCreateTime(new Date());
                // 更新时间
                tbItem.setUpdateTime(new Date());
                //spu编号
                tbItem.setGoodsId(tbGoods.getId());
                // 商家编号
                tbItem.setSellerId(tbGoods.getSellerId());
                //品牌名称
                TbBrand brand = tbBrandMapper.selectByPrimaryKey(tbGoods.getBrandId());
                tbItem.setBrand(brand.getName());
                //分类名称
                TbItemCat itemCat = tbItemCatMapper.selectByPrimaryKey(tbGoods.getCategory3Id());
                tbItem.setCategory(itemCat.getName());
                //商家名称
                TbSeller seller = tbSellerMapper.selectByPrimaryKey(tbGoods.getSellerId());
                tbItem.setSeller(seller.getNickName());

                //插入商品数据
                tbItemMapper.insert(tbItem);
            }
        }else{
            TbItem tbItem = new TbItem();
            //通过spu的商品名称和sku的商品规格生成sku的标题
            String title = tbGoods.getGoodsName();
            tbItem.setTitle(title);
            //设置商品价格
            tbItem.setPrice(tbGoods.getPrice());
            //设置商品图片
            String itemImages = tbGoodsDesc.getItemImages();
            List<Map> itemImageList = JSON.parseArray(itemImages, Map.class);
            if(itemImageList.size()>0) {
                Map map = itemImageList.get(0);
                tbItem.setImage((String) map.get("url"));
            }
            //设置叶子类目
            tbItem.setCategoryid(tbGoods.getCategory3Id());
            //设置商品状态
            tbItem.setStatus(TbItem.getNORMAL());
            //设置是否默认
            tbItem.setIsDefault("1");//是否默认
            //库存数量
            tbItem.setNum(99999);
            //规格
            tbItem.setSpec("{}");
            //创建时间
            tbItem.setCreateTime(new Date());
            // 更新时间
            tbItem.setUpdateTime(new Date());
            //spu编号
            tbItem.setGoodsId(tbGoods.getId());
            // 商家编号
            tbItem.setSellerId(tbGoods.getSellerId());
            //品牌名称
            TbBrand brand = tbBrandMapper.selectByPrimaryKey(tbGoods.getBrandId());
            tbItem.setBrand(brand.getName());
            //分类名称
            TbItemCat itemCat = tbItemCatMapper.selectByPrimaryKey(tbGoods.getCategory3Id());
            tbItem.setCategory(itemCat.getName());
            //商家名称
            TbSeller seller = tbSellerMapper.selectByPrimaryKey(tbGoods.getSellerId());
            tbItem.setSeller(seller.getNickName());
            //插入商品数据
            tbItemMapper.insert(tbItem);
        }

    }

    @Override
    public Goods findOne(Long id) {
        Goods goods = new Goods();
        //查询商品spu
        TbGoods tbGoods = tbGoodsMapper.selectByPrimaryKey(id);
        goods.setTbGoods(tbGoods);
        //查询商品详情
        TbGoodsDescExample example = new TbGoodsDescExample();
        TbGoodsDescExample.Criteria criteria = example.createCriteria();
        criteria.andGoodsIdEqualTo(id);
        List<TbGoodsDesc> tbGoodsDescs = tbGoodsDescMapper.selectByExample(example);
        if(tbGoodsDescs.size()>0){
            goods.setTbGoodsDesc(tbGoodsDescs.get(0));
        }
        //查询sku
        TbItemExample tbItemExample =new TbItemExample();
        TbItemExample.Criteria criteria1 = tbItemExample.createCriteria();
        criteria1.andGoodsIdEqualTo(id);
        List<TbItem> tbItems = tbItemMapper.selectByExample(tbItemExample);
        goods.setTbItemList(tbItems);
        return goods;
    }

    @Override
    public void update(Goods goods) {
        //插入SPU商品信息
        TbGoods tbGoods = goods.getTbGoods();
        tbGoods.setAuditStatus(TbGoods.getNOT_CHECk());
        tbGoodsMapper.updateByPrimaryKey(tbGoods);
        // 插入商品扩展信息
        TbGoodsDesc tbGoodsDesc = goods.getTbGoodsDesc();
        tbGoodsDesc.setGoodsId(tbGoods.getId());
        tbGoodsDescMapper.updateByPrimaryKey(tbGoodsDesc);
        //先删除全部的sku
        TbItemExample tbItemExample =new TbItemExample();
        TbItemExample.Criteria criteria = tbItemExample.createCriteria();
        criteria.andGoodsIdEqualTo(tbGoods.getId());
        tbItemMapper.deleteByExample(tbItemExample);
        // 判断是否自定义规则
        String isEnableSpec = tbGoods.getIsEnableSpec();
        if("1".equals(isEnableSpec)){
            // 如自定义，则循环插入对应SKU商品信息
            List<TbItem> tbItemList = goods.getTbItemList();
            for (TbItem tbItem : tbItemList) {
                //通过spu的商品名称和sku的商品规格生成sku的标题
                String title = tbGoods.getGoodsName();
                String spec = tbItem.getSpec();
                Map<String,Object> parseObject = JSON.parseObject(spec);
                for (String key : parseObject.keySet()) {
                    String str = (String) parseObject.get(key);
                    title+=" "+str;
                }
                tbItem.setTitle(title);
                //设置商品图片
                String itemImages = tbGoodsDesc.getItemImages();
                List<Map> itemImageList = JSON.parseArray(itemImages, Map.class);
                if(itemImageList.size()>0) {
                    Map map = itemImageList.get(0);
                    tbItem.setImage((String) map.get("url"));
                }
                //设置叶子类目
                tbItem.setCategoryid(tbGoods.getCategory3Id());
                //设置商品状态
                tbItem.setStatus(TbItem.getNORMAL());
                //创建时间
                tbItem.setCreateTime(new Date());
                // 更新时间
                tbItem.setUpdateTime(new Date());
                //spu编号
                tbItem.setGoodsId(tbGoods.getId());
                // 商家编号
                tbItem.setSellerId(tbGoods.getSellerId());
                //品牌名称
                TbBrand brand = tbBrandMapper.selectByPrimaryKey(tbGoods.getBrandId());
                tbItem.setBrand(brand.getName());
                //分类名称
                TbItemCat itemCat = tbItemCatMapper.selectByPrimaryKey(tbGoods.getCategory3Id());
                tbItem.setCategory(itemCat.getName());
                //商家名称
                TbSeller seller = tbSellerMapper.selectByPrimaryKey(tbGoods.getSellerId());
                tbItem.setSeller(seller.getNickName());

                //插入商品数据
                tbItemMapper.insert(tbItem);
            }
        }else{
            TbItem tbItem = new TbItem();
            //通过spu的商品名称和sku的商品规格生成sku的标题
            String title = tbGoods.getGoodsName();
            tbItem.setTitle(title);
            //设置商品价格
            tbItem.setPrice(tbGoods.getPrice());
            //设置商品图片
            String itemImages = tbGoodsDesc.getItemImages();
            List<Map> itemImageList = JSON.parseArray(itemImages, Map.class);
            if(itemImageList.size()>0) {
                Map map = itemImageList.get(0);
                tbItem.setImage((String) map.get("url"));
            }
            //设置叶子类目
            tbItem.setCategoryid(tbGoods.getCategory3Id());
            //设置商品状态
            tbItem.setStatus(TbItem.getNORMAL());
            //设置是否默认
            tbItem.setIsDefault("1");//是否默认
            //库存数量
            tbItem.setNum(99999);
            //规格
            tbItem.setSpec("{}");
            //创建时间
            tbItem.setCreateTime(new Date());
            // 更新时间
            tbItem.setUpdateTime(new Date());
            //spu编号
            tbItem.setGoodsId(tbGoods.getId());
            // 商家编号
            tbItem.setSellerId(tbGoods.getSellerId());
            //品牌名称
            TbBrand brand = tbBrandMapper.selectByPrimaryKey(tbGoods.getBrandId());
            tbItem.setBrand(brand.getName());
            //分类名称
            TbItemCat itemCat = tbItemCatMapper.selectByPrimaryKey(tbGoods.getCategory3Id());
            tbItem.setCategory(itemCat.getName());
            //商家名称
            TbSeller seller = tbSellerMapper.selectByPrimaryKey(tbGoods.getSellerId());
            tbItem.setSeller(seller.getNickName());
            //插入商品数据
            tbItemMapper.insert(tbItem);
        }
    }

    @Override
    public List<TbItem> getItemByGoods(Long[] ids,String status) {
        TbItemExample tbItemExample = new TbItemExample();
        TbItemExample.Criteria criteria = tbItemExample.createCriteria();
        criteria.andGoodsIdIn(Arrays.asList(ids));
        criteria.andStatusEqualTo(status);
        List<TbItem> tbItems = tbItemMapper.selectByExample(tbItemExample);
        return tbItems;
    }
}
