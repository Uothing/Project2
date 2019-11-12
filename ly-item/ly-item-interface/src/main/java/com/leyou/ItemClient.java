package com.leyou;

import com.leyou.common.vo.PageResult;
import com.leyou.pojo.dto.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @version V1.0
 * @author: weiyuan
 * @date: 2019/11/8 9:17
 * @description:
 */
@FeignClient("item-service")
public interface ItemClient {
    /**
     * 根据id查询品牌
     * @param id
     * @return
     */
    @GetMapping("/brand/{id}")
    BrandDTO queryBrandById(@PathVariable("id") Long id);

    /**
     * 根据id的集合查询商品分类
     * @param idList 商品分类的id集合
     * @return 分类集合
     */
    @GetMapping("/category/list")
    List<CategoryDTO> queryCategoryByIds(@RequestParam("ids") List<Long> idList);

    /**
     * 分页查询spu
     * @param page 当前页
     * @param rows 每页大小
     * @param saleable 上架商品或下降商品
     * @param key 关键字
     * @return 当前页商品数据
     */
    @GetMapping("/spu/page")
    PageResult<SpuDTO> querySpuByPage(
            @RequestParam(value = "page", defaultValue = "1")Integer page,
            @RequestParam(value = "rows", defaultValue = "5")Integer rows,
            @RequestParam(value = "key", required = false)String key,
            @RequestParam(value = "saleable", required = false)Boolean saleable);

    /**
     * 根据spuID查询spuDetail
     * @param id spuID
     * @return SpuDetail
     */
    @GetMapping("/spu/detail")
    SpuDetailDTO returnGoodsDetail(@RequestParam("id") Long id);

    /**
     * 根据spuID查询sku
     * @param id spuID
     * @return sku的集合
     */
    @GetMapping("sku/of/spu")
    List<SkuDTO> returnGoodsDetailSku(@RequestParam("id") Long id);

    /**
     * 查询规格参数
     * @param gid 组id
     * @param cid 分类id
     * @param searching 是否用于搜索
     * @return 规格组集合
     */
    @GetMapping("/spec/params")
    List<SpecParamDTO> queryGroupItemById(
            @RequestParam(value = "gid", required = false) Long gid,
            @RequestParam(value = "cid", required = false) Long cid,
            @RequestParam(value = "searching", required = false) Boolean searching
    );

    /**
     * 根据品牌id批量查询品牌
     * @param idList 品牌id的集合
     * @return 品牌的集合
     */
    @GetMapping("/brand/list")
    List<BrandDTO> queryBrandByIds(@RequestParam("ids") List<Long> idList);

    /**
     * 根据spu的id查询spu
     * @param id
     * @return
     */
    @GetMapping("spu/{id}")
    SpuDTO querySpuById(@PathVariable("id") Long id);

    /**
     * 查询规格参数组，及组内参数
     * @param id 商品分类id
     * @return 规格组及组内参数
     */
    @GetMapping("/spec/of/category")
    List<SpecGroupDTO> querySpecsByCid(@RequestParam("id") Long id);
}
