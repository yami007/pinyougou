package com.pinyougou.user.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.mapper.TbUserMapper;
import com.pinyougou.pojo.TbUser;
import com.pinyougou.user.service.UserService;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private TbUserMapper tbUserMapper;
    @Override
    public void add(TbUser user) {
        user.setCreated(new Date());//创建日期
        user.setUpdated(new Date());//修改日期
        String password = DigestUtils.md5Hex(user.getPassword());//对密码加密
        user.setPassword(password);
        user.setStatus("1");
        tbUserMapper.insert(user);
    }
}
