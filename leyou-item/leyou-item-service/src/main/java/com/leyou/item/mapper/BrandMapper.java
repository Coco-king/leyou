package com.leyou.item.mapper;

import com.leyou.item.pojo.Brand;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface BrandMapper extends Mapper<Brand> {

    /**
     * 更新tb_category_brand表中品牌和分类的对应关系，如果指定字段存在即更新，不存在则增加
     *
     * @param cid 分类id
     * @param bid 品牌id
     */
    @Insert("INSERT INTO tb_category_brand VALUES (#{cid},#{bid})")
    void saveCategoryBrand(@Param("cid") Long cid, @Param("bid") Long bid);

    /**
     * 根据品牌id删除品牌和tb_category_brand表中的相关分类信息
     *
     * @param bid 品牌id
     */
    @Delete("DELETE FROM tb_category_brand WHERE brand_id = #{bid}")
    void deleteBrandFromCategoryBrandByBid(Long bid);

    /**
     * 根据分类id连表(tb_category_brand)查询对应分类的品牌
     *
     * @param cid 分类id
     * @return 对应的品牌列表
     */
    @Select("SELECT * FROM `tb_brand` a INNER JOIN `tb_category_brand` b ON a.id = b.brand_id WHERE b.category_id = #{cid}")
    List<Brand> findBrandsByCid(Long cid);
}
