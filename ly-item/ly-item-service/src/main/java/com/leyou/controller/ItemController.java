package com.leyou.controller;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.pojo.Item;
import com.leyou.pojo.dto.SpecGroupDTO;
import com.leyou.pojo.dto.SpecParamDTO;
import com.leyou.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @version V1.0
 * @author: weiyuan
 * @date: 2019/10/30 20:31
 * @description:
 */
@RestController
@RequestMapping("spec")
public class ItemController {

    @Autowired
    private ItemService itemService;

    @PostMapping("item")
    public ResponseEntity<Item> saveItem(Item item) {

        Item result = itemService.saveItem(item);
        return ResponseEntity.ok(result);
    }


    //根据id查询手机的  属性组信息
    @GetMapping("/groups/of/category/")
    public ResponseEntity<List<SpecGroupDTO>> queryGroupById(@RequestParam("id") Long id) {

        return ResponseEntity
                .ok(itemService.queryGroupById(id));
    }

    //根据id查询手机的  属性组里的详细属性
    @GetMapping("/params")
    public ResponseEntity<List<SpecParamDTO>> queryGroupItemById(
            @RequestParam(value = "gid", required = false) Long gid,
            @RequestParam(value = "cid", required = false) Long cid,
            @RequestParam(value = "searching", required = false) Boolean searching
    ) {

        return ResponseEntity
                .ok(itemService.queryGroupItemById(gid, cid, searching));
    }

    /**
     * 查询规格参数组，及组内参数
     * @param id 商品分类id
     * @return 规格组及组内参数
     */
    @GetMapping("/of/category")
    public ResponseEntity<List<SpecGroupDTO>> querySpecsByCid(@RequestParam("id") Long id){
        return ResponseEntity.ok(itemService.querySpecsByCid(id));
    }
}
