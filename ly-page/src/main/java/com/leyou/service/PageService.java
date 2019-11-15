package com.leyou.service;

import com.leyou.ItemClient;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.pojo.dto.BrandDTO;
import com.leyou.pojo.dto.CategoryDTO;
import com.leyou.pojo.dto.SpecGroupDTO;
import com.leyou.pojo.dto.SpuDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @version V1.0
 * @author: weiyuan
 * @date: 2019/11/11 21:07
 * @description:
 */
@Service
public class PageService {

    @Autowired
    private ItemClient itemClient;

    public Map<String, Object> loadItemData(Long id) {
        //查询spu
        SpuDTO spu = itemClient.querySpuById(id);

        //查询分类
        List<CategoryDTO> categories = itemClient.queryCategoryByIds(spu.getCategoryIds());

        //查询品牌
        BrandDTO brand = itemClient.queryBrandById(spu.getBrandId());

        //查规格
        List<SpecGroupDTO> specs = itemClient.querySpecsByCid(spu.getCid3());

        // 封装数据
        Map<String, Object> data = new HashMap<>();
        data.put("categories", categories);
        data.put("brand", brand);
        data.put("spuName", spu.getName());
        data.put("subTitle", spu.getSubTitle());
        data.put("detail", spu.getSpuDetail());
        data.put("skus", spu.getSkus());
        data.put("specs", specs);

        return data;
    }


    @Autowired
    private TemplateEngine templateEngine;

    private static final String HTML_DIR = "E:\\JavaTool\\nginx-1.12.2\\html\\item";

    //创建本地静态页面
    public void creatHtml(Long id) {
        //上下文
        Context context = new Context();
        context.setVariables(loadItemData(id));

        //输出流
        File file = new File(HTML_DIR, id + ".html");

        try (Writer writer = new PrintWriter(file, "UTF-8")) {
            templateEngine.process("item", context, writer);
        } catch (Exception e) {
            throw new LyException(500, e);
        }
    }

    //删除本地静态页面
    public void deleteItemHtml(Long id) {
        File file = new File(HTML_DIR, id + ".html");
        if (file.exists()) {
            boolean result = file.delete();
            if (!result) {
                throw new RuntimeException("删除失败");
            }
        }
    }
}
