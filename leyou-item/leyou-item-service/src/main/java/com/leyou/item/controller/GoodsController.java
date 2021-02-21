package com.leyou.item.controller;

import com.leyou.commom.pojo.PageResult;
import com.leyou.item.bo.SpuBo;
import com.leyou.item.pojo.Sku;
import com.leyou.item.pojo.Spu;
import com.leyou.item.pojo.SpuDetail;
import com.leyou.item.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping
public class GoodsController {

    @Autowired
    private GoodsService goodsService;

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
    public ResponseEntity<PageResult<SpuBo>> findSpuByPage(
            @RequestParam(value = "key", required = false) String key,
            @RequestParam(value = "saleable", required = false) Boolean saleable,
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "rows", defaultValue = "5") Integer rows
    ) {
        PageResult<SpuBo> result = goodsService.findSpuByPage(key, saleable, page, rows);
        if (CollectionUtils.isEmpty(result.getItems())) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(result);
    }

    /**
     * 保存商品
     *
     * @param spuBo 商品spu
     */
    @PostMapping("/goods")
    public ResponseEntity<Void> saveGoods(@RequestBody SpuBo spuBo) {
        goodsService.saveGoods(spuBo);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 修改商品
     *
     * @param spuBo 商品spu
     */
    @PutMapping("/goods")
    public ResponseEntity<Void> updateGoods(@RequestBody SpuBo spuBo) {
        goodsService.updateGoods(spuBo);
        return ResponseEntity.noContent().build();
    }

    /**
     * 根据SpuId查询SpuDetail
     */
    @GetMapping("/spu/detail/{spuId}")
    public ResponseEntity<SpuDetail> findSpuDetailBySpuId(@PathVariable("spuId") Long spuId) {
        SpuDetail spuDetail = goodsService.findSpuDetailBySpuId(spuId);
        if (spuDetail == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(spuDetail);
    }

    /**
     * 根据spu的id查询到sku
     */
    @GetMapping("/sku/list")
    public ResponseEntity<List<Sku>> findSkusBySpuId(@RequestParam("id") Long spuId) {
        List<Sku> skus = goodsService.findSkusBySpuId(spuId);
        if (CollectionUtils.isEmpty(skus)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(skus);
    }

    /**
     * 根据spu的id查询到spu
     */
    @GetMapping("/{id}")
    public ResponseEntity<Spu> findSpuBySpuId(@PathVariable("id") Long spuId) {
        Spu spu = goodsService.findSpuBySpuId(spuId);
        if (spu == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(spu);
    }

    /**
     * 根据spuId删除spu
     */
    @PostMapping("/spu/delete/{spuId}")
    public ResponseEntity<Void> deleteSpuBySpuId(@PathVariable("spuId") Long spuId) {
        goodsService.deleteSpuBySpuId(spuId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 根据spuId更改spu上下架
     */
    @PostMapping("/spu/change")
    public ResponseEntity<Void> updateSpuBySpuId(@RequestParam("spuId") Long spuId, @RequestParam("saleable") Boolean saleable) {
        goodsService.updateSpuBySpuId(spuId, saleable);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 根据sku的id查询到sku
     */
    @GetMapping("/sku/{skuId}")
    public ResponseEntity<Sku> findSkuBySkuId(@PathVariable("skuId") Long skuId) {
        Sku sku = goodsService.findSkuBySkuId(skuId);
        if (sku == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(sku);
    }
}
