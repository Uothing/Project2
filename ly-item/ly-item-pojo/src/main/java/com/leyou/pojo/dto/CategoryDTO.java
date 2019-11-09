package com.leyou.pojo.dto;

import lombok.Data;

/**
 * @version V1.0
 * @author: weiyuan
 * @date: 2019/10/31 19:18
 * @description:
 */
@Data
public class CategoryDTO {
    private Long id;
    private String name;
    private Long parentId;
    private Boolean isParent;
    private Integer sort;
}