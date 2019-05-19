package com.pinyougou.user.service;

import com.pinyougou.pojo.TbAddress;

import java.util.List;

public interface AddressService {
    public List<TbAddress> findListByUserId(String userId );

    public void addAddress(TbAddress tbAddress);
}
