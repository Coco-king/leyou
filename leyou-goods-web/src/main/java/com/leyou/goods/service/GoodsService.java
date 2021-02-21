package com.leyou.goods.service;

import com.leyou.goods.client.BrandClient;
import com.leyou.goods.client.CategoryClient;
import com.leyou.goods.client.GoodsClient;
import com.leyou.goods.client.SpecificationClient;
import com.leyou.item.pojo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class GoodsService {

    @Autowired
    private BrandClient brandClient;

    @Autowired
    private CategoryClient categoryClient;

    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private SpecificationClient specificationClient;

    /**
     * 封装页面需要的数据
     */
    public Map<String, Object> loadData(Long spuId) {
        Map<String, Object> model = new HashMap<>();
        //查询spu
        Spu spu = goodsClient.findSpuBySpuId(spuId);
        //查询spuDetail
        SpuDetail spuDetail = goodsClient.findSpuDetailBySpuId(spuId);
        //查询skus
        List<Sku> skus = goodsClient.findSkusBySpuId(spuId);
        //查询品牌
        Brand brand = brandClient.findBrandById(spu.getBrandId());
        //查询分类
        List<Long> cids = Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3());
        List<String> names = categoryClient.findNamesByIds(cids);
        //封装categories
        List<Map<String, Object>> categories = new ArrayList<>();
        for (int i = 0; i < names.size(); i++) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", cids.get(i));
            map.put("name", names.get(i));
            categories.add(map);
        }
        //查询规格参数组
        List<SpecGroup> groups = specificationClient.findGroupsWithParamByCid(spu.getCid3());
        //封装规格参数
        List<SpecParam> params = specificationClient.findParams(null, spu.getCid3(), false, null);
        Map<Long, String> paramMap = new HashMap<>();
        params.forEach(param -> {
            paramMap.put(param.getId(), param.getName());
        });
        model.put("spu", spu);
        model.put("spuDetail", spuDetail);
        model.put("skus", skus);
        model.put("brand", brand);
        model.put("categories", categories);
        model.put("groups", groups);
        model.put("paramMap", paramMap);
        return model;
    }
}
