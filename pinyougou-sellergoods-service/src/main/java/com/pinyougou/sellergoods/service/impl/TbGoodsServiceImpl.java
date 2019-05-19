package com.pinyougou.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbGoodsMapper;
import com.pinyougou.pojo.PageResult;
import com.pinyougou.pojo.TbGoods;
import com.pinyougou.pojo.TbGoodsExample;
import com.pinyougou.sellergoods.service.TbGoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
@Service
@Transactional
public class TbGoodsServiceImpl implements TbGoodsService {
    @Autowired
    private TbGoodsMapper goodsMapper;

    /**
     * 查询全部
     */
    @Override
    public List<TbGoods> findAll() {
        return goodsMapper.selectByExample(null);
    }

    /**
     * 按分页查询
     */
    @Override
    public PageResult findPage(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        Page<TbGoods> page=   (Page<TbGoods>) goodsMapper.selectByExample(null);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 增加
     */
    @Override
    public void add(TbGoods goods) {
        goodsMapper.insert(goods);
    }


    /**
     * 修改
     */
    @Override
    public void update(TbGoods goods){
        goodsMapper.updateByPrimaryKey(goods);
    }

    /**
     * 根据ID获取实体
     * @param id
     * @return
     */
    @Override
    public TbGoods findOne(Long id){
        return goodsMapper.selectByPrimaryKey(id);
    }

    /**
     * 批量删除
     */
    @Override
    public void delete(Long[] ids) {
        for(Long id:ids){
            TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);
            tbGoods.setIsDelete(true);
            goodsMapper.updateByPrimaryKey(tbGoods);
        }
    }


    @Override
    public PageResult findPage(TbGoods goods, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);

        TbGoodsExample example=new TbGoodsExample();
        TbGoodsExample.Criteria criteria = example.createCriteria();

        if(goods!=null){
            if(goods.getSellerId()!=null && goods.getSellerId().length()>0){
                criteria.andSellerIdLike("%"+goods.getSellerId()+"%");
            }
            if(goods.getGoodsName()!=null && goods.getGoodsName().length()>0){
                criteria.andGoodsNameLike("%"+goods.getGoodsName()+"%");
            }
            if(goods.getAuditStatus()!=null && goods.getAuditStatus().length()>0){
                criteria.andAuditStatusEqualTo(goods.getAuditStatus());
            }
            if(goods.getIsDelete()!=null){
                criteria.andIsDeleteEqualTo(goods.getIsDelete());
            }
            if(goods.getIsMarketable()!=null && goods.getIsMarketable().length()>0){
                criteria.andIsMarketableLike("%"+goods.getIsMarketable()+"%");
            }
            if(goods.getCaption()!=null && goods.getCaption().length()>0){
                criteria.andCaptionLike("%"+goods.getCaption()+"%");
            }
            if(goods.getSmallPic()!=null && goods.getSmallPic().length()>0){
                criteria.andSmallPicLike("%"+goods.getSmallPic()+"%");
            }
            if(goods.getIsEnableSpec()!=null && goods.getIsEnableSpec().length()>0){
                criteria.andIsEnableSpecLike("%"+goods.getIsEnableSpec()+"%");
            }

        }

        Page<TbGoods> page= (Page<TbGoods>)goodsMapper.selectByExample(example);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 更改商品审核状态
     * @param ids
     * @param statusnum
     */
    @Override
    public void updatestatus(Long[] ids, String statusnum) {
        if(ids.length>0){
            for (Long id : ids) {
                TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);
                tbGoods.setAuditStatus(statusnum);
                goodsMapper.updateByPrimaryKey(tbGoods);
            }
        }
    }

    @Override
    public void marketable(Long[] ids) {
        for (Long id : ids) {
            TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);
            tbGoods.setIsMarketable("0");
            goodsMapper.updateByPrimaryKey(tbGoods);
        }
    }

}
