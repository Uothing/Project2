package com.leyou.mapper;

import com.leyou.common.mapper.BaseMapper;
import com.leyou.item.entity.Sku;
import tk.mybatis.mapper.common.special.InsertListMapper;

/**
 * @author
 */
public interface SkuMapper extends BaseMapper<Sku>, InsertListMapper<Sku> {
}