package com.leyou.item.service;

import com.leyou.item.mapper.CategoryMapper;
import com.leyou.item.pojo.Category;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    /**
     * 根据父节点查询子节点
     *
     * @param pid 父节点的id
     * @return 子节点列表集合
     */
    public List<Category> findCategoryByPid(Long pid) {
        Category record = new Category();
        record.setParentId(pid);
        return categoryMapper.select(record);
    }

    /**
     * 根据品牌id查询商品分类
     *
     * @param bid 品牌id
     * @return 封装商品分类列表集合的响应实体对象
     */
    public List<Category> findCategoryFromCategoryBrandByBid(Long bid) {
        return categoryMapper.findCategoryFromCategoryBrandByBid(bid);
    }

    /**
     * 保存新的分类
     *
     * @param category 分类对象
     */
    @Transactional
    public void saveCategory(Category category) {
        categoryMapper.updateIsParentByParentId(category.getParentId());
        categoryMapper.insertSelective(category);
    }

    /**
     * 修改分类名称
     *
     * @param category 分类对象
     */
    @Transactional
    public void updateCategory(Category category) {
        categoryMapper.updateByPrimaryKeySelective(category);
    }

    /**
     * 删除分类，如果删除的是一个父目录那么子目录也一起删除
     *
     * @param id 分类ID
     */
    @Transactional
    public void deleteCategory(Long id) {
        Example example = new Example(Category.class);
        Example.Criteria criteria = example.createCriteria();
        if (id != null && id > 0) {
            criteria.andEqualTo("id", id).orEqualTo("parentId", id);
        }
        categoryMapper.deleteByExample(example);
    }

    /**
     * 根据分类id集合查询分类name集合
     *
     * @param ids 分类id集合
     * @return 分类name集合
     */
    public List<String> findNamesByIds(List<Long> ids) {
        List<Category> categories = categoryMapper.selectByIdList(ids);
        return categories.stream().map(Category::getName).collect(Collectors.toList());
    }
}
