package com.leyou.client;

import com.leyou.ItemClient;
import com.leyou.common.vo.PageResult;
import com.leyou.pojo.Goods;
import com.leyou.pojo.dto.SpuDTO;
import com.leyou.repository.GoodsRepository;
import com.leyou.service.SearchService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @version V1.0
 * @author: lg9288
 * @date: 2019/11/8 19:29
 * @description:
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class LoadDataTest {
    @Autowired
    private SearchService searchService;
    @Autowired
    private GoodsRepository goodsRepository;
    @Autowired
    private ItemClient itemClient;
    @Test
    public void loadData() {
        int page = 1,rows = 100;
        while (true) {
            //1.通过fegin调用分页的查询
            PageResult<SpuDTO> result = itemClient.querySpuByPage(page, rows,null,true);
            //2.获取spuDto的集合
            List<SpuDTO> items = result.getItems();
            Integer totalPage = result.getTotalPage();
            List<Goods> goodsList = new ArrayList<>();
            for (SpuDTO spu : items) {
                Goods goods = searchService.buildGoods(spu);
                goodsList.add(goods);

            }
            //3.将数据库数据保存到索引库
            goodsRepository.saveAll(goodsList);
            page++;
            if (page >totalPage) {
                break;
            }
        }
    }
}
