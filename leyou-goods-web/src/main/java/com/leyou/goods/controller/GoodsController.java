package com.leyou.goods.controller;

import com.leyou.goods.service.GoodsHtmlService;
import com.leyou.goods.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;

@Controller
@RequestMapping
public class GoodsController {

    @Autowired
    private GoodsService goodsService;

    @Autowired
    private GoodsHtmlService goodsHtmlService;

    @GetMapping("/item/{id}.html")
    public ModelAndView toItemPage(@PathVariable("id") Long id) {
        ModelAndView mv = new ModelAndView("item");
        Map<String, Object> map = goodsService.loadData(id);
        mv.addAllObjects(map);
        goodsHtmlService.asyncExecute(id);
        return mv;
    }
}
