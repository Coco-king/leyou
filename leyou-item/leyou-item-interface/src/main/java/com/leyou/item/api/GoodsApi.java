package com.leyou.item.api;

import com.leyou.commom.pojo.PageResult;
import com.leyou.item.bo.SpuBo;
import com.leyou.item.pojo.Sku;
import com.leyou.item.pojo.Spu;
import com.leyou.item.pojo.SpuDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@RequestMapping
public interface GoodsApi {

    /**
     * 分页查询spu
     *
     * @param key      查询条件
     * @param saleable 是否上架
     * @param page     当前页
     * @param rows     每页条数
     * @return 分页结果集
     */
    @GetMapping("/spu/page")
    PageResult<SpuBo> findSpuByPage(
            @RequestParam(value = "key", required = false) String key,
            @RequestParam(value = "saleable", required = false) Boolean saleable,
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "rows", defaultValue = "5") Integer rows
    );

    /**
     * 根据SpuId查询SpuDetail
     */
    @GetMapping("/spu/detail/{spuId}")
    SpuDetail findSpuDetailBySpuId(@PathVariable("spuId") Long spuId);

    /**
     * 根据spu的id查询到sku
     */
    @GetMapping("/sku/list")
    List<Sku> findSkusBySpuId(@RequestParam("id") Long spuId);

    /**
     * 根据spu的id查询到spu
     */
    @GetMapping("/{id}")
    Spu findSpuBySpuId(@PathVariable("id") Long spuId);

    /**
     * 根据sku的id查询到sku
     */
    @GetMapping("/sku/{skuId}")
    Sku findSkuBySkuId(@PathVariable("skuId") Long skuId);
}
