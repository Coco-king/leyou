package com.leyou.item.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.commom.pojo.PageResult;
import com.leyou.item.mapper.BrandMapper;
import com.leyou.item.pojo.Brand;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service
public class BrandService {
    @Autowired
    private BrandMapper brandMapper;

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
    public PageResult<Brand> findBrandsByPage(String key, Integer page, Integer rows, String sortBy, Boolean desc) {
        // 初始化example对象
        Example example = new Example(Brand.class);
        Example.Criteria criteria = example.createCriteria();
        // 根据名字或首字母模糊查询
        if (StringUtils.isNotBlank(key))
            criteria.andLike("name", "%" + key + "%").orEqualTo("letter", key);
        // 分页
        if (rows > 0)
            PageHelper.startPage(page, rows);
        // 排序
        if (StringUtils.isNotBlank(sortBy))
            example.setOrderByClause(sortBy + " " + (desc ? "desc" : "asc"));
        // 执行查询
        List<Brand> brands = brandMapper.selectByExample(example);
        // 封装为PageInfo
        PageInfo<Brand> pageInfo = new PageInfo<>(brands);
        // 返回分页结果集
        return new PageResult<>(pageInfo.getTotal(), pageInfo.getList());
    }

    /**
     * 保存品牌
     *
     * @param brand 品牌名
     * @param cids  分类
     */
    @Transactional
    public void saveBrand(Brand brand, List<Long> cids) {
        //不用判断异常，如果出错，会直接服务器异常
        brandMapper.insertSelective(brand);
        cids.forEach(cid -> {
            brandMapper.saveCategoryBrand(cid, brand.getId());
        });
    }

    /**
     * 根据id删除品牌
     *
     * @param bid 品牌id
     */
    @Transactional
    public void deleteBrandById(Long bid) {
        //删除品牌
        brandMapper.deleteByPrimaryKey(bid);
        //删除品牌和分类关联表
        brandMapper.deleteBrandFromCategoryBrandByBid(bid);
    }

    /**
     * 根据分类id查询出所属的品牌
     *
     * @param cid 分类id
     * @return 分类下的品牌列表
     */
    public List<Brand> findBrandsByCid(Long cid) {
        return brandMapper.findBrandsByCid(cid);
    }

    /**
     * 修改品牌
     *
     * @param brand 品牌名
     * @param cids  分类
     */
    @Transactional
    public void updateBrand(Brand brand, List<Long> cids) {
        deleteBrandById(brand.getId());
        saveBrand(brand, cids);
    }

    /**
     * 根据品牌ID查询品牌
     */
    public Brand findBrandById(Long id) {
        return brandMapper.selectByPrimaryKey(id);
    }
}
