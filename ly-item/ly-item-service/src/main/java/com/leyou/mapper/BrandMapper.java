package com.leyou.mapper;

import com.leyou.common.mapper.BaseMapper;
import com.leyou.item.entity.Brand;
import com.leyou.pojo.dto.BrandDTO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * @version V1.0
 * @author: weiyuan
 * @date: 2019/11/1 17:03
 * @description:
 */
public interface BrandMapper extends BaseMapper<Brand> {

    //中间表
    int insertCategoryBrand(@Param("bid") Long bid, @Param("ids") List<Long> ids);

    //修改数据时，先删除旧的中间表
    @Delete("DELETE from tb_category_brand WHERE brand_id = #{bid}")
    int deleteCategoryAndBrand(@Param("bid") Long bid);


    //新增商品，根据所选商品分类 自动回显所属品牌信息
    @Select("SELECT * FROM tb_brand tb \n" +
            "INNER JOIN tb_category_brand tcb\n" +
            "ON tcb.brand_id = tb.id\n" +
            "WHERE tcb.category_id = #{id}")
    List<Brand> queryBrandById(@Param("id") Long id);
}
