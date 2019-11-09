package com.leyou.repository;

import com.leyou.pojo.Goods;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * @version V1.0
 * @author: weiyuan
 * @date: 2019/11/8 10:22
 * @description:
 */
public interface GoodsRepository extends ElasticsearchRepository<Goods, Long> {
}
