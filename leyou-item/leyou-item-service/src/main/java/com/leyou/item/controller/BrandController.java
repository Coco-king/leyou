package com.leyou.item.controller;

import com.leyou.commom.pojo.PageResult;
import com.leyou.item.pojo.Brand;
import com.leyou.item.service.BrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/brand")
public class BrandController {
    @Autowired
    private BrandService brandService;

    /**
     * 根据条件分页并排序模糊查询
     *
     * @param key    模糊的条件
     * @param page   当前页
     * @param rows   每页几条数据
     * @param sortBy 根据哪一列排序
     * @param desc   排序规则
     * @return 分页bean
     */
    @GetMapping("/page")
    public ResponseEntity<PageResult<Brand>> findBrandsByPage(
            @RequestParam(value = "key", required = false) String key,
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "rows", defaultValue = "5") Integer rows,
            @RequestParam(value = "sortBy", required = false) String sortBy,
            @RequestParam(value = "desc", required = false) Boolean desc
    ) {
        PageResult<Brand> results = brandService.findBrandsByPage(key, page, rows, sortBy, desc);
        if (CollectionUtils.isEmpty(results.getItems())) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(results);
    }

    /**
     * 保存品牌
     *
     * @param brand 品牌名
     * @param cids  分类
     */
    @PostMapping
    public ResponseEntity<Void> saveBrand(Brand brand, @RequestParam("cids") List<Long> cids) {
        brandService.saveBrand(brand, cids);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 修改品牌
     *
     * @param brand 品牌名
     * @param cids  分类
     */
    @PutMapping
    public ResponseEntity<Void> updateBrand(Brand brand, @RequestParam("cids") List<Long> cids) {
        brandService.updateBrand(brand, cids);
        return ResponseEntity.noContent().build();
    }

    /**
     * 根据id删除品牌
     *
     * @param bid 品牌id
     */
    @GetMapping("/delete/{bid}")
    public ResponseEntity<Void> deleteBrand(@PathVariable("bid") Long bid) {
        brandService.deleteBrandById(bid);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 根据分类id查询出所属的品牌
     *
     * @param cid 分类id
     * @return 分类下的品牌列表
     */
    @GetMapping("/cid/{cid}")
    public ResponseEntity<List<Brand>> findBrandsByCid(@PathVariable("cid") Long cid) {
        List<Brand> brands = brandService.findBrandsByCid(cid);
        if (CollectionUtils.isEmpty(brands)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(brands);
    }

    /**
     * 根据品牌ID查询品牌
     */
    @GetMapping("/{id}")
    public ResponseEntity<Brand> findBrandById(@PathVariable("id") Long id) {
        Brand brand = brandService.findBrandById(id);
        if (brand == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(brand);
    }
}
