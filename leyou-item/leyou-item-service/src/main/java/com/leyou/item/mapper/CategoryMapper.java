package com.leyou.item.mapper;

import com.leyou.item.pojo.Category;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import tk.mybatis.mapper.additional.idlist.SelectByIdListMapper;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface CategoryMapper extends Mapper<Category>, SelectByIdListMapper<Category, Long> {
    @Update("UPDATE tb_category SET is_parent = TRUE WHERE id = #{parentId}")
    void updateIsParentByParentId(Long parentId);

    @Insert("INSERT INTO tb_category (name,parent_id,is_parent,sort) VALUES (#{name},#{parentId},#{isParent},#{sort})")
    @Options(useGeneratedKeys = true)
    void saveCategoryReturnId(Category category);

    @Select("select * from tb_category where id in (select category_id from tb_category_brand where brand_id = #{bid})")
    List<Category> findCategoryFromCategoryBrandByBid(Long bid);
}
