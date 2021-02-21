package com.leyou.elasticsearch.test;

import com.leyou.commom.pojo.PageResult;
import com.leyou.item.bo.SpuBo;
import com.leyou.search.client.GoodsClient;
import com.leyou.search.pojo.Goods;
import com.leyou.search.repository.GoodsRepository;
import com.leyou.search.service.SearchService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@SpringBootTest
@RunWith(SpringRunner.class)
public class ElasticsearchTest {

    @Autowired
    private ElasticsearchTemplate template;

    @Autowired
    private GoodsRepository goodsRepository;

    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private SearchService searchService;

    @Test
    public void testAdd() {
        template.createIndex(Goods.class);
        template.putMapping(Goods.class);

        int page = 1;
        int rows = 100;
        while (rows == 100) {
            //分页查询spu
            PageResult<SpuBo> pageResult = goodsClient.findSpuByPage(null, null, page, rows);
            List<SpuBo> spuBos = pageResult.getItems();
            //把spu转为goods
            List<Goods> goodsList = spuBos.stream().map(spuBo -> {
                try {
                    return searchService.buildGoods(spuBo);
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }).collect(Collectors.toList());
            //执行保存
            goodsRepository.saveAll(goodsList);
            page++;
            rows = spuBos.size();
        }
    }
}
