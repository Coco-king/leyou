package com.leyou.item.api;

import com.leyou.item.pojo.SpecGroup;
import com.leyou.item.pojo.SpecParam;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/spec")
public interface SpecificationApi {
    /**
     * 根据商品规格分组id查询规格分组下的参数
     *
     * @param gid 规格分组id
     * @return 规格分组的参数集合
     */
    @GetMapping("/params")
    List<SpecParam> findParams(
            @RequestParam(value = "gid", required = false) Long gid,
            @RequestParam(value = "cid", required = false) Long cid,
            @RequestParam(value = "generic", required = false) Boolean generic,
            @RequestParam(value = "searching", required = false) Boolean searching
    );

    /**
     * 根据商品分类id查询规格分组和规格参数
     */
    @GetMapping("/group/param/{cid}")
    List<SpecGroup> findGroupsWithParamByCid(@PathVariable("cid") Long cid);
}
