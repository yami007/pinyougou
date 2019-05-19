package com.pinyougou.search.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.pojo.TbItem;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ItemSearchServiceImpl implements ItemSearchService {
    @Autowired
    private SolrTemplate solrTemplate;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public Map search(Map searchMap) {
        Map resultMap = new HashMap();
        //主搜索
        Map map = searchList(searchMap);
        resultMap.putAll(map);
        //分组查询
        List<String> cattgoryList = searchCattgoryList(searchMap);
        resultMap.put("cattgoryList", cattgoryList);
        String category = (String) searchMap.get("category");
        if (StringUtils.isNotBlank(category)) {
            Map brandAndTemMap = getBrandAndTem(category);
            resultMap.putAll(brandAndTemMap);
        } else {
            if (cattgoryList.size()>0) {
                Map brandAndTemMap = getBrandAndTem(cattgoryList.get(0));
                resultMap.putAll(brandAndTemMap);
            }
        }
        return resultMap;
    }

    @Override
    public void importItem(List list) {
        solrTemplate.saveBeans(list);
        solrTemplate.commit();
    }

    @Override
    public void deleteByGoodsIds(List goodsIdList) {
        System.out.println("删除商品ID"+goodsIdList);
        Query query=new SimpleQuery();
        Criteria criteria=new Criteria("item_goodsid").in(goodsIdList);
        query.addCriteria(criteria);
        solrTemplate.delete(query);
        solrTemplate.commit();

    }

    private Map getBrandAndTem(String cattgory) {
        Map map = new HashMap<>();
        //根据分类名称从缓存中获取模板id
        Long temID = (Long) redisTemplate.boundHashOps("itemCat").get(cattgory);
        if (temID != null) {
            //根据模板id从缓存中获取品牌
            List<Map> brandList = (List<Map>) redisTemplate.boundHashOps("brandIdList").get(temID);
            //根据模板id从缓存中获取规格
            List<Map> specList = (List<Map>) redisTemplate.boundHashOps("specList").get(temID);
            map.put("brandList", brandList);
            map.put("specList", specList);
        }
        return map;
    }

    /**
     * 获取分类
     *
     * @param searchMap
     * @return
     */
    private List<String> searchCattgoryList(Map searchMap) {
        List<String> cattgoryList = new ArrayList<>();
        Query query = new SimpleQuery();
        //按关键字查找
        String keywords = (String) searchMap.get("keywords");
        keywords = keywords.replaceAll(" ", "");
        Criteria criteria = new Criteria("item_keywords").is(keywords);
        query.addCriteria(criteria);
        //再分组
        GroupOptions groupOptions = new GroupOptions().addGroupByField("item_category");
        query.setGroupOptions(groupOptions);
        //得到数据
        GroupPage<TbItem> tbItems = solrTemplate.queryForGroupPage(query, TbItem.class);
        //得到分组结果集
        GroupResult<TbItem> groupResult = tbItems.getGroupResult("item_category");
        Page<GroupEntry<TbItem>> groupEntries = groupResult.getGroupEntries();
        List<GroupEntry<TbItem>> content = groupEntries.getContent();
        for (GroupEntry<TbItem> entry : content) {
            cattgoryList.add(entry.getGroupValue());
        }
        return cattgoryList;
    }

    private Map searchList(Map searchMap) {
        Map map = new HashMap();
        //1.创建一个查询的对象
        HighlightQuery query = new SimpleHighlightQuery();
        //2.获取从页面传递过来的参数的值，并设置条件
        String keywords = (String) searchMap.get("keywords");//三星
        keywords = keywords.replaceAll(" ", "");
        Criteria criteria = new Criteria("item_keywords");//item_keywords:三星
        criteria.is(keywords);
        query.addCriteria(criteria);
        //1.7排序
        String sortValue = (String) searchMap.get("sort");//ASC  DESC
        String sortField = (String) searchMap.get("sortField");//排序字段
        if (sortValue != null && !sortValue.equals("")) {
            if (sortValue.equals("ASC")) {
                Sort sort = new Sort(Sort.Direction.ASC, "item_" + sortField);
                query.addSort(sort);
            }
            if (sortValue.equals("DESC")) {
                Sort sort = new Sort(Sort.Direction.DESC, "item_" + sortField);
                query.addSort(sort);
            }
        }
        //3.高亮的设置 开启高亮  设置高亮显示的域 以及 前缀和后缀

        HighlightOptions highlightOptions = new HighlightOptions();
        highlightOptions.addField("item_title");//添加高亮显示的域
        highlightOptions.setSimplePrefix("<em style=\"color:red\">");
        highlightOptions.setSimplePostfix("</em>");
        query.setHighlightOptions(highlightOptions);

        //4.过滤查询    品牌
        String brand = (String) searchMap.get("brand");
        if (StringUtils.isNotBlank(brand)) {
            FilterQuery filterquery = new SimpleFilterQuery();//item_brand:三星
            Criteria criteria1 = new Criteria("item_brand");
            criteria1.is(brand);
            filterquery.addCriteria(criteria1);
            query.addFilterQuery(filterquery);
        }

        //5.过滤查询    分类
        String category = (String) searchMap.get("category");
        if (StringUtils.isNotBlank(category)) {
            FilterQuery filterquery = new SimpleFilterQuery();//item_brand:三星
            Criteria criteria1 = new Criteria("item_category");
            criteria1.is(category);
            filterquery.addCriteria(criteria1);
            query.addFilterQuery(filterquery);
        }
        //6.过滤查询  规格
        Map<String, String> spec = (Map<String, String>) searchMap.get("spec");
        if (spec != null) {
            for (String key : spec.keySet()) {
                FilterQuery filterquery = new SimpleFilterQuery();//
                Criteria criteria1 = new Criteria("item_spec_" + key);//item_spec_网络
                criteria1.is(spec.get(key));//移动4G ====item_spec_网络:移动4G
                filterquery.addCriteria(criteria1);
                query.addFilterQuery(filterquery);
            }
        }
        //价格区间的过滤
        String price = (String) searchMap.get("price");
        if (StringUtils.isNoneBlank(price)) {
            String[] prices = price.split("-");
            FilterQuery filterquery = new SimpleFilterQuery();
            Criteria criteria1 = new Criteria("item_price");
            if (!prices[1].equals("*")) {
                criteria1.between(prices[0], prices[1], true, true);
            } else {
                criteria1.greaterThanEqual(prices[0]);
            }
            filterquery.addCriteria(criteria1);
            query.addFilterQuery(filterquery);

        }
        //执行分页查询
        Object pageNum = searchMap.get("pageNum");
        Integer pageInt = null;
        Integer pageSize = (Integer) searchMap.get("pageSize");
        if (pageNum instanceof String) {
            String pageNumStr = (String) pageNum;
            pageInt = Integer.parseInt(pageNumStr);
        } else {
            pageInt = (Integer) pageNum;
        }
        if (pageInt == null) {
            pageInt = 1;
        }
        if (pageSize == null) {
            pageSize = 40;
        }
        query.setOffset((pageInt - 1) * pageSize);//第一页
        query.setRows(pageSize);//每页显示40行

        HighlightPage<TbItem> page = solrTemplate.queryForHighlightPage(query, TbItem.class);

        //获取高亮数据

        List<HighlightEntry<TbItem>> highlighted = page.getHighlighted();
        if (highlighted != null && highlighted.size() > 0) {
            for (HighlightEntry<TbItem> tbItemHighlightEntry : highlighted) {
                if (tbItemHighlightEntry != null) {
                    TbItem entity = tbItemHighlightEntry.getEntity();//该对象就是某一个文档对应的pojo对象
                    List<HighlightEntry.Highlight> highlights = tbItemHighlightEntry.getHighlights();
                    if (highlights != null &&
                            highlights.size() > 0 &&
                            highlights.get(0) != null &&
                            highlights.get(0).getSnipplets() != null &&
                            highlights.get(0).getSnipplets().size() > 0
                            ) {
                        entity.setTitle(highlights.get(0).getSnipplets().get(0));
                    }
                }
            }
        }

        //从结果中获取总页数 总记录数 列表
        List<TbItem> content = page.getContent();
        //设置结果
        map.put("rows", content);//当前页的记录集合
        map.put("total", page.getTotalElements());//总记录数
        map.put("totalPage", page.getTotalPages());//总页数
        //返回
        return map;
    }
}
