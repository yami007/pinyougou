package com.pinyougou.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.TbAddress;
import com.pinyougou.user.service.AddressService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/adress")
public class AdressController {
    @Reference
    private AddressService addressService;

    @RequestMapping("/findListByUserId")
    public List<TbAddress> findListByUserId() {
        //得到登陆人账号,判断当前是否有人登陆
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        List<TbAddress> TbadressListByUserId = addressService.findListByUserId(username);
        return TbadressListByUserId;
    }
}
