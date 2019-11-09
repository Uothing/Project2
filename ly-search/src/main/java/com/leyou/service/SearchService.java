package com.leyou.service;

import com.leyou.ItemClient;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.BeanHelper;
import com.leyou.common.utils.JsonUtils;
import com.leyou.common.vo.PageResult;
import com.leyou.dto.GoodsDTO;
import com.leyou.dto.SearchRequest;
import com.leyou.pojo.Goods;
import com.leyou.pojo.dto.*;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilterBuilder;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author 虎哥
 */
@Service
public class SearchService {

    @Autowired
    private ItemClient itemClient;

    public Goods buildGoods(SpuDTO spu){
        Long spuId = spu.getId();

        // 1.搜索字段，包含标题、分类、品牌
        // 1.1.查询分类
        String categoryNames = spu.getCategoryName();
        if(StringUtils.isBlank(categoryNames)) {
            categoryNames = itemClient.queryCategoryByIds(spu.getCategoryIds())
                    .stream()
                    .map(CategoryDTO::getName)
                    .collect(Collectors.joining());
        }
        // 1.2.查询品牌
        String brandName = spu.getBrandName();
        if(StringUtils.isBlank(brandName)) {
            brandName = itemClient.queryBrandById(spu.getBrandId()).getName();
        }
        // 1.3.拼接
        String all = spu.getName() + categoryNames + brandName;

        // 2.spu下的sku的集合
        // 2.1.查询sku
        List<SkuDTO> skuList = spu.getSkus();
        if(CollectionUtils.isEmpty(skuList)) {
            skuList = itemClient.returnGoodsDetailSku(spuId);
        }
        // 2.2.处理sku的字段
        List<Map<String,Object>> skus = new ArrayList<>();
        // 2.3.准备价格的集合
        Set<Long> price = new TreeSet<>();

        for (SkuDTO sku : skuList) {
            // 处理sku的字段
            Map<String,Object> map = new HashMap<>();
            map.put("id", sku.getId());
            map.put("image", StringUtils.substringBefore(sku.getImages(), ","));
            map.put("price", sku.getPrice());
            map.put("title", sku.getTitle());
            skus.add(map);
            // 处理价格
            price.add(sku.getPrice());
        }

        // 3.sku的价格的集合
        /*Set<Long> price = skuList
                .stream()
                .map(SkuDTO::getPrice)
                .collect(Collectors.toSet());*/

        // 4.规格参数，key：规格参数名称，value：规格参数值
        Map<String, Object> specs = new HashMap<>();
        // 4.1.查询规格参数名称，作为key。查询当前分类下，可以搜索的规格参数
        List<SpecParamDTO> params = itemClient.queryGroupItemById(null, spu.getCid3(), true);

        // 4.2.查询spuDetail，获取规格参数的值
        SpuDetailDTO detail = spu.getSpuDetail();
        if(detail == null){
            detail = itemClient.returnGoodsDetail(spuId);
        }
        // 4.2.1.取出通用规格参数值
        String json = detail.getGenericSpec();
        Map<Long, Object> genericSpec = JsonUtils.toMap(json, Long.class, Object.class);
        // 4.2.2.取出特有规格参数的值
        json = detail.getSpecialSpec();
        Map<Long, Object> specialSpec = JsonUtils.toMap(json, Long.class, Object.class);

        // 4.3.把key和value配对，存入specs中
        for (SpecParamDTO param : params) {
            // 取出规格参数名称，作为map的key
            String key = param.getName();
            // 取出其中的值
            Object value = null;
            // 判断是否是通用规格
            if(param.getGeneric()) {
                value = genericSpec.get(param.getId());
            }else{
                value = specialSpec.get(param.getId());
            }
            // 判断值是否为数值类型
            if(param.getNumeric()) {
                // 判断value值在哪个区间，直接把区间作为值存入elasticsearch
                value = chooseSegment(value, param);
            }
            // 放入map
            specs.put(key, value);
        }

        // 5.创建goods对象，封装数据
        Goods goods = new Goods();
        goods.setSubTitle(spu.getSubTitle());
        goods.setId(spuId);
        goods.setCategoryId(spu.getCid3());
        goods.setBrandId(spu.getBrandId());
        // 搜索字段，包含标题、分类、品牌。
        goods.setAll(all);
        goods.setCreateTime(spu.getCreateTime().getTime());
        // spu下的sku的集合
        goods.setSkus(JsonUtils.toString(skus));
        // sku的价格的集合
        goods.setPrice(price);
        // 规格参数，key：规格参数名称，value：规格参数值
        goods.setSpecs(specs);
        return goods;
    }

    private String chooseSegment(Object value, SpecParamDTO p) {
        if (value == null || StringUtils.isBlank(value.toString())) {
            return "其它";
        }
        double val = parseDouble(value.toString());
        String result = "其它";
        // 保存数值段
        for (String segment : p.getSegments().split(",")) {
            String[] segs = segment.split("-");
            // 获取数值范围
            double begin = parseDouble(segs[0]);
            double end = Double.MAX_VALUE;
            if (segs.length == 2) {
                end = parseDouble(segs[1]);
            }
            // 判断是否在范围内
            if (val >= begin && val < end) {
                if (segs.length == 1) {
                    result = segs[0] + p.getUnit() + "以上";
                } else if (begin == 0) {
                    result = segs[1] + p.getUnit() + "以下";
                } else {
                    result = segment + p.getUnit();
                }
                break;
            }
        }
        return result;
    }

    private double parseDouble(String str) {
        try {
            return Double.parseDouble(str);
        } catch (Exception e) {
            return 0;
        }
    }

    @Autowired
    private ElasticsearchTemplate esTemplate;

    public PageResult<GoodsDTO> search(SearchRequest request) {
        // 0.健壮性判断
        String key = request.getKey();
        if (StringUtils.isBlank(key)) {
            throw new LyException(ExceptionEnum.INVALID_PARAM_ERROR);
        }
        // 1.创建原生搜索构建器
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        // 2.组织条件
        // 2.0.source过滤，控制字段数量
        queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{"id", "subTitle", "skus"}, null));
        // 2.1.搜索条件
        queryBuilder.withQuery(QueryBuilders.matchQuery("all", key).operator(Operator.AND));
        // 2.2.分页条件
        int page = request.getPage() - 1;
        int size = request.getSize();
        queryBuilder.withPageable(PageRequest.of(page, size));

        // 3.搜索结果
        AggregatedPage<Goods> result = esTemplate.queryForPage(queryBuilder.build(), Goods.class);

        // 4.解析结果
        // 4.1.解析分页数据
        long total = result.getTotalElements();
        int totalPage = result.getTotalPages();
        List<Goods> list = result.getContent();
        // 4.2.转换DTO
        List<GoodsDTO> dtoList = BeanHelper.copyWithCollection(list, GoodsDTO.class);

        // 5.封装并返回
        return new PageResult<>(total, totalPage, dtoList);
    }


    //查询过滤项
    public Map<String, List<?>> queryFilters(SearchRequest request) {

        // 1.创建原生搜索构建器
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        // 2.组织条件
        // 2.0.source过滤，控制字段数量
        queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{""}, new String[0]));

        String key = request.getKey();
        if (StringUtils.isBlank(key)) {
            throw new LyException(ExceptionEnum.RESOURCE_NOT_FOUND);
        }

        // 2.1.搜索条件
        queryBuilder.withQuery(QueryBuilders.matchQuery("all", request.getKey()));
        //分页信息，减少数据
        queryBuilder.withPageable(PageRequest.of(0, 1));
        //聚合
        queryBuilder.addAggregation(AggregationBuilders.terms("categoryAgg").field("categoryId"));
        queryBuilder.addAggregation(AggregationBuilders.terms("brandAgg").field("brandId"));

        //发起请求
        AggregatedPage<Goods> goods = esTemplate.queryForPage(queryBuilder.build(), Goods.class);


        Map<String, List<?>> filters = new LinkedHashMap<>();
        //解析聚合
        Aggregations aggregations = goods.getAggregations();
        //获取分类的聚合结果
        Terms categoryAgg = aggregations.get("categoryAgg");
        /*List<? extends Terms.Bucket> buckets = categoryAgg.getBuckets();
        for (Terms.Bucket bucket : buckets) {
            Number number = bucket.getKeyAsNumber();
            long value = number.longValue();

        }*/

        List<Long> idList = categoryAgg.getBuckets()
                .stream()
                .map(Terms.Bucket::getKeyAsNumber)
                .map(Number::longValue)
                .collect(Collectors.toList());
        List<CategoryDTO> categoryDTOS = itemClient.queryCategoryByIds(idList);
        filters.put("分类", categoryDTOS);
        //获取品牌的聚合结果
        Terms brandAgg = aggregations.get("brandAgg");
        /*List<? extends Terms.Bucket> buckets = categoryAgg.getBuckets();
        for (Terms.Bucket bucket : buckets) {
            Number number = bucket.getKeyAsNumber();
            long value = number.longValue();

        }*/

        List<Long> bidList = categoryAgg.getBuckets()
                .stream()
                .map(Terms.Bucket::getKeyAsNumber)
                .map(Number::longValue)
                .collect(Collectors.toList());
        List<BrandDTO> brandDTOList = itemClient.queryBrandByIds(bidList);
        filters.put("品牌", brandDTOList);
        return filters;
    }
}