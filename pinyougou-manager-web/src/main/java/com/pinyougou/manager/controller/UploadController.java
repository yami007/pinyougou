package com.pinyougou.manager.controller;

import com.pinyougou.common.utils.FastDFSClient;
import com.pinyougou.pojo.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class UploadController {
    //配置文件中存储的ip地址
    @Value("${IMAGE_SERVER_URL}")
    private String IMAGE_SERVER_URL;

    @RequestMapping("/upload")
    public Result upload(MultipartFile file){
        //取出文件扩展名
        String originalFilename = file.getOriginalFilename();
        String extName  = originalFilename.substring(originalFilename.lastIndexOf('.')+1);
        try {
            //创建客户端工具类，传入配置地址
            FastDFSClient fastDFSClient = new FastDFSClient("classpath:config/fdfs_client.conf");
            //传入文件的字节码数组及扩展名，执行上传，返回文件名
            String filename = fastDFSClient.uploadFile(file.getBytes(), extName);
            //拼接返回的url和ip地址，拼装成完整的url
            filename=IMAGE_SERVER_URL+filename;
            //将完整文件名返回页面
            return new Result(true,filename);
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"上传失败");
        }
    }
}
