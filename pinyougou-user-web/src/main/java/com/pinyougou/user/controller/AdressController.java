package com.pinyougou.user.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.Result;
import com.pinyougou.pojo.TbAddress;
import com.pinyougou.user.service.AddressService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
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

    @RequestMapping("/addAddress")
    public Result addAddress(@RequestBody TbAddress tbAddress){
        //得到登陆人账号,判断当前是否有人登陆
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        tbAddress.setUserId(username);
        try {
            addressService.addAddress(tbAddress);
            return new Result(true,"新增地址成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"新增地址失败");
        }
    }
}
