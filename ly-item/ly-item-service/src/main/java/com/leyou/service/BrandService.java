package com.leyou.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.BeanHelper;
import com.leyou.common.vo.PageResult;
import com.leyou.item.entity.Brand;
import com.leyou.item.entity.Category;
import com.leyou.mapper.BrandMapper;
import com.leyou.pojo.dto.BrandDTO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestParam;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

/**
 * @version V1.0
 * @author: weiyuan
 * @date: 2019/11/1 19:52
 * @description:
 */
@Service
public class BrandService {

    @Autowired
    private BrandMapper brandMapper;

    public PageResult<BrandDTO> queryBrandByPage(
            Integer page, Integer rows, String key, String sortBy, Boolean desc) {
        //分页
        PageHelper.startPage(page, rows);
        //过滤 --> 为下面的真正查询做准备
        Example example = new Example(Brand.class);

        if(StringUtils.isNoneBlank(key)) {
            example.createCriteria().orLike("name", "%" + key + "%")
                    .orEqualTo("letter", key.toUpperCase());
        }
        // 排序
        if(StringUtils.isNoneBlank(sortBy)) {
            String orderByClause = sortBy + (desc ? " DESC" : " ASC");
            example.setOrderByClause(orderByClause);// id desc
        }

        // 查询
        List<Brand> brands = brandMapper.selectByExample(example);

        // 判断是否为空
        if(CollectionUtils.isEmpty(brands)){
            throw new LyException(ExceptionEnum.BRAND_NOT_FOUND);
        }

        // 解析分页结果
        PageInfo<Brand> info = new PageInfo<>(brands);

        // 转为BrandDTO
        List<BrandDTO> list = BeanHelper.copyWithCollection(brands, BrandDTO.class);

        // 返回
        return new PageResult<BrandDTO>(info.getTotal(), list);
    }


    @Transactional
    public void saveBrand(BrandDTO brandDTO, List<Long> ids) {
        Brand brand = BeanHelper.copyProperties(brandDTO, Brand.class);
        brand.setId(null);
        //新增
        int count = brandMapper.insertSelective(brand);
        if (count != 1) {
            throw new LyException(ExceptionEnum.INSERT_OPERATION_FAIL);
        }
        //维护
        count = brandMapper.insertCategoryBrand(brand.getId(), ids);
        if (count != ids.size()) {
            throw new LyException(ExceptionEnum.INSERT_OPERATION_FAIL);
        }
    }

    //修改品牌信息
    @Transactional
    public void updateBrand(BrandDTO brandDTO, List<Long> ids) {
        Brand brand = BeanHelper.copyProperties(brandDTO, Brand.class);
        //修改
        int count = brandMapper.updateByPrimaryKeySelective(brand);
        if (count != 1) {
            throw new LyException(ExceptionEnum.UPDATE_OPERATION_FAIL);
        }

        //先删除中间表
        count = brandMapper.deleteCategoryAndBrand(brand.getId());
        if (count != 1) {
            throw new LyException(ExceptionEnum.UPDATE_OPERATION_FAIL);
        }
        //维护
        count = brandMapper.insertCategoryBrand(brand.getId(), ids);
        if (count != ids.size()) {
            throw new LyException(ExceptionEnum.UPDATE_OPERATION_FAIL);
        }
    }

    //根据品牌id查询品牌
    public BrandDTO queryById(Long id) {
        Brand brand = brandMapper.selectByPrimaryKey(id);
        if (brand == null) {
            throw new LyException(ExceptionEnum.BRAND_NOT_FOUND);
        }
        return BeanHelper.copyProperties(brand, BrandDTO.class);
    }

    //新增商品，根据所选商品分类 自动回显所属品牌信息
    public List<BrandDTO> queryBrandById(Long id) {

//        Category category = new Category();
//        category.setId(id);

        //查询
        List<Brand> brandList = brandMapper.queryBrandById(id);

        if (CollectionUtils.isEmpty(brandList)) {
            throw new LyException(ExceptionEnum.BRAND_NOT_FOUND);
        }

        return BeanHelper.copyWithCollection(brandList, BrandDTO.class);
    }

    public List<BrandDTO> queryByIds(List<Long> ids) {
        List<Brand> list = brandMapper.selectByIdList(ids);
        // 判断是否为空
        if(CollectionUtils.isEmpty(list)){
            throw new LyException(ExceptionEnum.BRAND_NOT_FOUND);
        }
        return BeanHelper.copyWithCollection(list, BrandDTO.class);
    }
}
