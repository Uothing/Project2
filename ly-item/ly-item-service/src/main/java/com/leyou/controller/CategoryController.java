package com.leyou.controller;

import com.leyou.pojo.dto.CategoryDTO;
import com.leyou.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @version V1.0
 * @author: weiyuan
 * @date: 2019/10/31 19:29
 * @description:
 */
@RestController
@RequestMapping("category")
public class CategoryController {


    @Autowired
    private CategoryService categoryService;

    @GetMapping("/of/parent")
    public ResponseEntity<List<CategoryDTO>> queryByParentId(
            @RequestParam(value = "pid", defaultValue = "0") Long pid
    ) {
        return ResponseEntity.ok(categoryService.queryListByParent(pid));
    }


    //修改品牌信息  回显--根据id查询所点击的商品分类信息  返回到前端
    @GetMapping("/of/brand")
    public ResponseEntity<List<CategoryDTO>> updateBrand(
            @RequestParam("id") Long pid
    ) {
        return ResponseEntity.ok(categoryService.updateBrand(pid));
    }

    //根据id的集合查询商品分类
    @GetMapping("list")
    public ResponseEntity<List<CategoryDTO>> queryByIds(@RequestParam("ids") List<Long> idList) {
        return ResponseEntity.ok(categoryService.queryCategoryByIds(idList));
    }
}
