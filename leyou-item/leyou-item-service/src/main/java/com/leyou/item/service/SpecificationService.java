package com.leyou.item.service;

import com.leyou.item.mapper.SpecGroupMapper;
import com.leyou.item.mapper.SpecParamMapper;
import com.leyou.item.pojo.SpecGroup;
import com.leyou.item.pojo.SpecParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SpecificationService {

    @Autowired
    private SpecGroupMapper groupMapper;
    @Autowired
    private SpecParamMapper paramMapper;

    /**
     * 根据商品分类id查询规格分组
     *
     * @param cid 分类id
     * @return 规格分组集合
     */
    public List<SpecGroup> findGroupsByCid(Long cid) {
        SpecGroup record = new SpecGroup();
        record.setCid(cid);
        return groupMapper.select(record);
    }

    /**
     * 根据商品规格分组id查询规格分组下的参数
     *
     * @param gid 规格分组id
     * @return 规格分组的参数集合
     */
    public List<SpecParam> findParams(Long gid, Long cid, Boolean generic, Boolean searching) {
        SpecParam record = new SpecParam();
        record.setGroupId(gid);
        record.setCid(cid);
        record.setGeneric(generic);
        record.setSearching(searching);
        return paramMapper.select(record);
    }

    /**
     * 根据商品分类id查询规格分组和规格参数
     */
    public List<SpecGroup> findGroupsWithParamByCid(Long cid) {
        List<SpecGroup> groups = findGroupsByCid(cid);
        groups.forEach(group -> group.setParams(findParams(group.getId(), null, null, null)));
        return groups;
    }
}
