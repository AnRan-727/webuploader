package com.liuyinlong.www.webuploader.webuploader.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/**
 * Created by IntelliJ IDEA.
 * User: AnRan
 * Url: www.liuyinlong.com
 * Date: 2020/7/28
 */
@Controller
public class ToPathController {

    /**
     * 去首页
     * @return
     */
    @RequestMapping(value = "/")
    public String toIndex(){
        return "index";
    }
    /**
     * 去单文件上传
     * @return
     */
    @RequestMapping(value = "/toPath/toOneUp")
    public String toOneUp(){
        return "/one/webupload";
    }
    /**
     * 去多文件上传
     * @return
     */
    @RequestMapping(value = "/toPath/toMultiUp")
    public String toMultiUp(){
        return "/multi/webupload";
    }








}
