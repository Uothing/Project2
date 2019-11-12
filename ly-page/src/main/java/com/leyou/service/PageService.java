package com.leyou.service;

import com.leyou.ItemClient;
import com.leyou.pojo.dto.BrandDTO;
import com.leyou.pojo.dto.CategoryDTO;
import com.leyou.pojo.dto.SpecGroupDTO;
import com.leyou.pojo.dto.SpuDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @version V1.0
 * @author: weiyuan
 * @date: 2019/11/11 21:07
 * @description:
 */
@Service
public class PageService {

    @Autowired
    private ItemClient itemClient;

    public Map<String, Object> loadItemData(Long id) {
        //查询spu
        SpuDTO spu = itemClient.querySpuById(id);

        //查询分类
        List<CategoryDTO> categories = itemClient.queryCategoryByIds(spu.getCategoryIds());

        //查询品牌
        BrandDTO brand = itemClient.queryBrandById(spu.getBrandId());

        //查规格
        List<SpecGroupDTO> specs = itemClient.querySpecsByCid(spu.getCid3());

        // 封装数据
        Map<String, Object> data = new HashMap<>();
        data.put("categories", categories);
        data.put("brand", brand);
        data.put("spuName", spu.getName());
        data.put("subTitle", spu.getSubTitle());
        data.put("detail", spu.getSpuDetail());
        data.put("skus", spu.getSkus());
        data.put("specs", specs);

        return data;
    }


}
