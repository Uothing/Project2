package com.leyou.controller;

import com.leyou.common.vo.PageResult;
import com.leyou.item.entity.SpuDetail;
import com.leyou.pojo.dto.SkuDTO;
import com.leyou.pojo.dto.SpecParamDTO;
import com.leyou.pojo.dto.SpuDTO;
import com.leyou.pojo.dto.SpuDetailDTO;
import com.leyou.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @version V1.0
 * @author: weiyuan
 * @date: 2019/11/5 21:16
 * @description:
 */
@RestController
public class GoodsController {

    @Autowired
    private GoodsService goodsService;

    //商品查询
    @GetMapping("/spu/page")
    public ResponseEntity<PageResult<SpuDTO>> querySpuByPage(
            @RequestParam(value = "page", defaultValue = "1")Integer page,
            @RequestParam(value = "rows", defaultValue = "5")Integer rows,
            @RequestParam(value = "key", required = false)String key,
            @RequestParam(value = "saleable", required = false)Boolean saleable
    ) {
        return ResponseEntity
                .ok(goodsService.querySpuByPage(page,rows, key, saleable));
    }


    //保存商品
    @PostMapping("goods")
    public ResponseEntity<Void> saveGoods(@RequestBody SpuDTO spuDTO) {
        this.goodsService.saveGoods(spuDTO);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    //下架商品
    @PutMapping("/spu/saleable")
    public ResponseEntity<Void> unsaleGoods(
            @RequestParam("id") Long id, @RequestParam("saleable") Boolean saleable
    ) {
        this.goodsService.unsaleGoods(id, saleable);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    //编辑商品时，回显商品数据spuDetail的信息
    @GetMapping("/spu/detail")
    public ResponseEntity<SpuDetailDTO> returnGoodsDetail(@RequestParam("id") Long id) {
        return ResponseEntity.ok(goodsService.returnGoodsDetail(id));
    }


    //编辑商品时，回显商品数据sku的信息
    @GetMapping("/sku/of/spu")
    public ResponseEntity<List<SkuDTO>> returnGoodsDetailSku(@RequestParam("id") Long id) {
        return ResponseEntity.ok(goodsService.returnGoodsDetailSku(id));
    }

    //保存更新商品的数据
    @PutMapping("/goods")
    public ResponseEntity<Void> updateSaveGoods(@RequestBody SpuDTO spuDTO) {
        this.goodsService.updateSaveGoods(spuDTO);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * 根据spu的id查询spu
     * @param id
     * @return
     */
    @GetMapping("spu/{id}")
    public ResponseEntity<SpuDTO> querySpuById(@PathVariable("id") Long id){
        return ResponseEntity.ok(goodsService.querySpuById(id));
    }

    /**
     * 根据sku的ids批量查询sku
     * @param ids
     * @return
     */
    @GetMapping("/sku/list")
    public ResponseEntity<List<SkuDTO>> querySkuByIds(@RequestParam("ids") List<Long> ids) {

        return ResponseEntity.ok(goodsService.querySkuByIds(ids));
    }
}
