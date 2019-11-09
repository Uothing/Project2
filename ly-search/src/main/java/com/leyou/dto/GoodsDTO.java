package com.leyou.dto;

import lombok.Data;

/**
 * @author
 */
@Data
public class GoodsDTO {
    private Long id; // spuId
    private String subTitle;// 卖点
    private String skus;// sku信息的json结构
}