package com.leyou.item.controller;

import com.leyou.item.pojo.SpecGroup;
import com.leyou.item.pojo.SpecParam;
import com.leyou.item.service.SpecificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/spec")
public class SpecificationController {

    @Autowired
    private SpecificationService specificationService;

    /**
     * 根据商品分类id查询规格分组
     *
     * @param cid 分类id
     * @return 规格分组集合
     */
    @GetMapping("/groups/{cid}")
    public ResponseEntity<List<SpecGroup>> findGroupsByCid(@PathVariable("cid") Long cid) {
        List<SpecGroup> groups = specificationService.findGroupsByCid(cid);
        if (CollectionUtils.isEmpty(groups)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(groups);
    }

    /**
     * 根据商品分类id查询规格分组和规格参数
     */
    @GetMapping("/group/param/{cid}")
    public ResponseEntity<List<SpecGroup>> findGroupsWithParamByCid(@PathVariable("cid") Long cid) {
        List<SpecGroup> groups = specificationService.findGroupsWithParamByCid(cid);
        if (CollectionUtils.isEmpty(groups)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(groups);
    }

    /**
     * 根据商品规格分组id查询规格分组下的参数
     *
     * @param gid 规格分组id
     * @return 规格分组的参数集合
     */
    @GetMapping("/params")
    public ResponseEntity<List<SpecParam>> findParams(
            @RequestParam(value = "gid", required = false) Long gid,
            @RequestParam(value = "cid", required = false) Long cid,
            @RequestParam(value = "generic", required = false) Boolean generic,
            @RequestParam(value = "searching", required = false) Boolean searching
    ) {
        List<SpecParam> groups = specificationService.findParams(gid, cid, generic, searching);
        if (CollectionUtils.isEmpty(groups)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(groups);
    }
}
