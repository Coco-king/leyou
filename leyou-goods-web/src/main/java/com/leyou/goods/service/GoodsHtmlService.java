package com.leyou.goods.service;

import com.leyou.goods.utils.ThreadUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

@Service
public class GoodsHtmlService {

    @Autowired
    private TemplateEngine engine;

    @Autowired
    private GoodsService goodsService;

    /**
     * 生成静态页面
     */
    public void createHtml(Long spuId) {
        //初始化运行上下文
        Context context = new Context();
        //把数据模型添加到运行上下文
        context.setVariables(goodsService.loadData(spuId));
        //把静态文件生成到服务器
        try (PrintWriter writer = new PrintWriter("D:\\Program\\nginx-1.14.0\\html\\item\\" + spuId + ".html")) {
            engine.process("item", context, writer);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * 使用新线程创建静态html
     */
    public void asyncExecute(Long spuId) {
        ThreadUtils.execute(() -> createHtml(spuId));
    }

    /**
     * 删除一个静态文件
     */
    public void deleteHtml(Long id) {
        File file = new File("D:\\Program\\nginx-1.14.0\\html\\item\\" + id + ".html");
        file.deleteOnExit();
    }
}
