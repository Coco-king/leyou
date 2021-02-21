package com.leyou.item.controller;

import com.leyou.item.pojo.Category;
import com.leyou.item.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * 根据父节点查询子节点
     *
     * @param pid 父节点的id
     * @return 封装子节点列表集合的响应实体对象
     */
    @GetMapping("/list")
    public ResponseEntity<List<Category>> findCategoryByPid(@RequestParam(value = "pid", defaultValue = "0") Long pid) {
        if (pid == null || pid < 0) {
            // 400：参数不合法
            return ResponseEntity.badRequest().build();
        }
        List<Category> categories = categoryService.findCategoryByPid(pid);
        if (CollectionUtils.isEmpty(categories)) {
            // 404：资源服务器未找到
            return ResponseEntity.notFound().build();
        }
        // 200：查询成功
        return ResponseEntity.ok(categories);
    }

    /**
     * 根据品牌id查询商品分类
     *
     * @param bid 品牌id
     * @return 封装商品分类列表集合的响应实体对象
     */
    @GetMapping("/bid/{bid}")
    public ResponseEntity<List<Category>> findCategoryByBid(@PathVariable("bid") Long bid) {
        if (bid == null || bid < 0) {
            // 400：参数不合法
            return ResponseEntity.badRequest().build();
        }
        List<Category> categories = categoryService.findCategoryFromCategoryBrandByBid(bid);
        if (CollectionUtils.isEmpty(categories)) {
            // 404：资源服务器未找到
            return ResponseEntity.notFound().build();
        }
        // 200：查询成功
        return ResponseEntity.ok(categories);
    }

    /**
     * 保存新的分类
     *
     * @param category 分类对象
     */
    @PostMapping("/saveCategory")
    public ResponseEntity<Void> saveCategory(@RequestBody Category category) {
        categoryService.saveCategory(category);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 修改分类名称
     *
     * @param category 分类对象
     */
    @PostMapping("/updateCategory")
    public ResponseEntity<Void> updateCategory(@RequestBody Category category) {
        categoryService.updateCategory(category);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 删除分类，如果删除的是一个父目录那么子目录也一起删除
     *
     * @param id 分类ID
     */
    @PostMapping("/deleteCategory")
    public ResponseEntity<Void> deleteCategory(@RequestParam("id") Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 根据分类id集合查询分类名字
     */
    @GetMapping
    public ResponseEntity<List<String>> findNamesByIds(@RequestParam("ids") List<Long> ids) {
        List<String> names = categoryService.findNamesByIds(ids);
        if (CollectionUtils.isEmpty(names)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(names);
    }
}
