package com.leyou.pojo.dto;

import lombok.Data;

/**
 * @version V1.0
 * @author: weiyuan
 * @date: 2019/11/5 9:44
 * @description:
 */
@Data
public class SpecParamDTO {
    private Long id;
    private Long cid;
    private Long groupId;
    private String name;
    private Boolean numeric;
    private String unit;
    private Boolean generic;
    private Boolean searching;
    private String segments;
}
