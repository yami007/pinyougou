package com.pinyougou.user.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.mapper.TbAddressMapper;
import com.pinyougou.pojo.TbAddress;
import com.pinyougou.pojo.TbAddressExample;
import com.pinyougou.user.service.AddressService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
@Service
public class AddressServiceImpl implements AddressService {
    @Autowired
    private TbAddressMapper tbAddressMapper;

    @Override
    public List<TbAddress> findListByUserId(String userId) {
        TbAddressExample tbAddressExample = new TbAddressExample();
        TbAddressExample.Criteria criteria = tbAddressExample.createCriteria();
        criteria.andUserIdEqualTo(userId);
        List<TbAddress> tbAddresses = tbAddressMapper.selectByExample(tbAddressExample);
        return tbAddresses;
    }

    @Override
    public void addAddress(TbAddress tbAddress) {
        tbAddressMapper.insert(tbAddress);
    }


}
