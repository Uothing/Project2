package com.leyou.service;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.BeanHelper;
import com.leyou.item.entity.Category;
import com.leyou.mapper.CategoryMapper;
import com.leyou.pojo.dto.CategoryDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @version V1.0
 * @author: weiyuan
 * @date: 2019/10/31 19:35
 * @description:
 */
@Service
public class CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    public List<CategoryDTO> queryListByParent(Long pid) {

        //根据pid
        Category category = new Category();
        category.setParentId(pid);
        // 查询
        List<Category> categoryList = categoryMapper.select(category);

        // 判断是否为空
        if (CollectionUtils.isEmpty(categoryList)) {
            throw new LyException(ExceptionEnum.CATEGORY_NOT_FOUND);
        }

        // 转DTO
        return BeanHelper.copyWithCollection(categoryList, CategoryDTO.class);

    }


    //修改品牌信息--根据id查询所点击的商品分类信息  返回到前端

    public List<CategoryDTO> updateBrand(Long pid) {

        // 查询
        List<Category> categoryList = categoryMapper.updateBrand(pid);

        // 判断是否为空
        if (CollectionUtils.isEmpty(categoryList)) {
            throw new LyException(ExceptionEnum.CATEGORY_NOT_FOUND);
        }

        // 转DTO
        return BeanHelper.copyWithCollection(categoryList, CategoryDTO.class);
    }

    //根据分类id的集合查询商品分类
    public List<CategoryDTO> queryCategoryByIds(List<Long> ids){
        List<Category> list = categoryMapper.selectByIdList(ids);
        if (CollectionUtils.isEmpty(list)) {
            // 没找到，返回404
            throw new LyException(ExceptionEnum.CATEGORY_NOT_FOUND);
        }
        return BeanHelper.copyWithCollection(list, CategoryDTO.class);
    }
}
