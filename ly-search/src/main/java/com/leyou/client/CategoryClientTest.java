package com.leyou.client;

import com.leyou.ItemClient;
import com.leyou.LySearchApplication;
import com.leyou.pojo.dto.CategoryDTO;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.List;

/**
 * @version V1.0
 * @author: weiyuan
 * @date: 2019/11/8 9:31
 * @description:
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = LySearchApplication.class)
public class CategoryClientTest {

    @Autowired
    private ItemClient itemClient;

    @Test
    public void queryByIdList() {
        List<CategoryDTO> categoryDTOList = itemClient.queryCategoryByIds(Arrays.asList(1L, 2L, 3L));

        for (CategoryDTO categoryDTO : categoryDTOList) {
            System.out.println("categoryDTO = " + categoryDTO);
        }
        Assert.assertEquals(3, categoryDTOList.size());
    }
}
