package com.pinyougou.manager.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.pinyougou.pojo.PageResult;
import com.pinyougou.pojo.Result;
import com.pinyougou.pojo.TbGoods;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.sellergoods.service.GoodsService;
import com.pinyougou.sellergoods.service.TbGoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import java.util.List;
/**
 * controller
 * @author Administrator
 *
 */
@RestController
@RequestMapping("/goods")
public class GoodsController {

	@Reference
	private TbGoodsService tbGoodsService;
	@Reference
	private GoodsService goodsService;
//	@Reference
//	private ItemSearchService itemSearchService;

//	@Reference
//	private ItemPageService itemPageService;
	@Autowired
	private JmsTemplate jmsTemplate;
	@Autowired
	private Destination queueTextDestination;
	@Autowired
	private Destination queueSolrDeleteDestination;

	@Autowired
	private Destination pageTextDestination;
	/**
	 * 返回全部列表
	 * @return
	 */
	@RequestMapping("/findAll")
	public List<TbGoods> findAll(){			
		return tbGoodsService.findAll();
	}
	
	
	/**
	 * 返回全部列表
	 * @return
	 */
	@RequestMapping("/findPage")
	public PageResult findPage(int page, int rows){
		return tbGoodsService.findPage(page, rows);
	}
	
	/**
	 * 增加
	 * @param goods
	 * @return
	 */
	@RequestMapping("/add")
	public Result add(@RequestBody TbGoods goods){
		try {
			tbGoodsService.add(goods);
			return new Result(true, "增加成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "增加失败");
		}
	}
	
	/**
	 * 修改
	 * @param goods
	 * @return
	 */
	@RequestMapping("/update")
	public Result update(@RequestBody TbGoods goods){
		try {
			tbGoodsService.update(goods);
			return new Result(true, "修改成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "修改失败");
		}
	}	
	
	/**
	 * 获取实体
	 * @param id
	 * @return
	 */
	@RequestMapping("/findOne")
	public TbGoods findOne(Long id){
		return tbGoodsService.findOne(id);
	}
	
	/**
	 * 批量删除
	 * @param ids
	 * @return
	 */
	@RequestMapping("/delete")
	public Result delete(Long [] ids){
		try {
			tbGoodsService.delete(ids);
			//从索引库中删除
//			itemSearchService.deleteByGoodsIds(Arrays.asList(ids));
			jmsTemplate.send(queueSolrDeleteDestination, new MessageCreator() {
				@Override
				public Message createMessage(Session session) throws JMSException {
					return session.createObjectMessage(ids);
				}
			});
			return new Result(true, "删除成功"); 
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "删除失败");
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
	 * 更改商品审核状态
	 * @param ids
	 * @param statusnum
	 * @return
	 */
	@RequestMapping("/updatestatus")
	public Result updatestatus(@RequestParam("ids") Long[] ids,@RequestParam("statusnum") String statusnum){
		try {
			tbGoodsService.updatestatus(ids,statusnum);
			if(statusnum.equals("1")){
				//先根据ids查询spu
				List<TbItem> tbItems = goodsService.getItemByGoods(ids,statusnum);
				//更新索引库
				if(tbItems.size()>0){
//					itemSearchService.importItem(tbItems);
					String tbItemsStr = JSON.toJSONString(tbItems);
					jmsTemplate.send(queueTextDestination, new MessageCreator() {
						@Override
						public Message createMessage(Session session) throws JMSException {
							return session.createTextMessage(tbItemsStr);
						}
					});
				}
				//更新商品静态页面
				for (Long id : ids) {
					jmsTemplate.send(pageTextDestination, new MessageCreator() {
						@Override
						public Message createMessage(Session session) throws JMSException {
							return session.createObjectMessage(id);
						}
					});
//					itemPageService.genItemHtml(id);
				}
				return new Result(true, "审核成功");
			}else{
				return new Result(true, "驳回成功");
			}
		} catch (Exception e) {
			e.printStackTrace();
			if (statusnum.equals("1")) {
				return new Result(true, "审核失败");
			}else
				return new Result(true, "驳回失败");
		}
	}

}
