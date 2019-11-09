package com.leyou.mapper;

import com.leyou.common.mapper.BaseMapper;
import com.leyou.item.entity.Category;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * @version V1.0
 * @author: weiyuan
 * @date: 2019/10/31 19:41
 * @description:
 */
public interface CategoryMapper extends BaseMapper<Category> {

    @Select("SELECT tc.id, tc.`name`, tc.parent_id, tc.is_parent, tc.sort \n" +
            "FROM tb_category_brand tcb INNER JOIN tb_category tc\n" +
            "ON tcb.category_id = tc.id\n" +
            "WHERE tcb.brand_id = #{pid};")
    List<Category> updateBrand(@Param("pid") Long pid);
}
