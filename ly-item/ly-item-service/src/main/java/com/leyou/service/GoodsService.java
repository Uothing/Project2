package com.leyou.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.BeanHelper;
import com.leyou.common.vo.PageResult;
import com.leyou.item.entity.Sku;
import com.leyou.item.entity.Spu;
import com.leyou.item.entity.SpuDetail;
import com.leyou.mapper.*;
import com.leyou.pojo.dto.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.List;
import java.util.stream.Collectors;


import static com.leyou.common.constants.MQConstants.RoutingKey.*;
import static com.leyou.common.constants.MQConstants.Exchange.*;

/**
 * @version V1.0
 * @author: weiyuan
 * @date: 2019/11/5 21:18
 * @description:
 */
@Service
public class GoodsService {

    @Autowired
    private SpuDetailMapper spuDetailMapper;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private BrandService brandService;


    @Autowired
    private SpuMapper spuMapper;

    @Autowired
    private SkuMapper skuMapper;


    //商品查询
    public PageResult<SpuDTO> querySpuByPage(
            Integer page, Integer rows, String key, Boolean saleable
        ) {
        //分页
        PageHelper.startPage(page, rows);
        //过滤 --> 为下面的真正查询做准备
        Example example = new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();
        if(StringUtils.isNoneBlank(key)) {
            criteria.andLike("name", "%" + key + "%");
        }

        //上下架
        if (saleable != null) {
            criteria.andEqualTo("saleable", saleable);
        }

        //默认时间排序
        example.setOrderByClause("update_time desc");

        // 3 查询结果
        List<Spu> list = spuMapper.selectByExample(example);
        if(CollectionUtils.isEmpty(list)){
            throw new LyException(ExceptionEnum.GOODS_NOT_FOUND);
        }
        // 4 封装分页结果
        PageInfo<Spu> info = new PageInfo<>(list);

        //DTO转换
        List<SpuDTO> spuDTOS = BeanHelper.copyWithCollection(list, SpuDTO.class);

        //处理品牌的分类名称和品牌名称
        handleCategoryAndBrand(spuDTOS);

        return new PageResult<>(info.getTotal(),info.getPages(), spuDTOS);
    }
    //处理品牌的分类名称和品牌名称
    private void handleCategoryAndBrand(List<SpuDTO> spuDTOS) {

        for (SpuDTO spu : spuDTOS) {
            // 查询分类  ??????
            String categoryName = categoryService.queryCategoryByIds(spu.getCategoryIds())
                    .stream()
                    .map(CategoryDTO::getName).collect(Collectors.joining("/"));
            spu.setCategoryName(categoryName);
            // 查询品牌
            BrandDTO brand = brandService.queryById(spu.getBrandId());
            spu.setBrandName(brand.getName());
        }
    }


    //保存商品   还要对SpuDetail、Sku、Stock进行保存
    @Transactional
    public void saveGoods(SpuDTO spuDTO) {

        //取出spu
        Spu spu = BeanHelper.copyProperties(spuDTO, Spu.class);
        //默认下架
        spu.setSaleable(false);

        int count = spuMapper.insertSelective(spu);
        if (count != 1) {
            throw new LyException(ExceptionEnum.UPDATE_OPERATION_FAIL);
        }

        //保存spuDetail
        SpuDetailDTO spuDetailDTO = spuDTO.getSpuDetail();
        //转换
        SpuDetail spuDetail = BeanHelper.copyProperties(spuDetailDTO, SpuDetail.class);
        spuDetail.setSpuId(spu.getId());

        count = spuDetailMapper.insertSelective(spuDetail);
        if (count != 1) {
            throw new LyException(ExceptionEnum.UPDATE_OPERATION_FAIL);
        }

        //sku
        List<SkuDTO> skuDTOS = spuDTO.getSkus();
        List<Sku> skuList = BeanHelper.copyWithCollection(skuDTOS, Sku.class);
        for (Sku sku : skuList) {
            sku.setSpuId(spu.getId());
            // 下架状态
            sku.setEnable(false);
        }

        count = skuMapper.insertList(skuList);
        if(count != skuList.size()){
            throw new LyException(ExceptionEnum.INSERT_OPERATION_FAIL);
        }
    }


    @Autowired
    private AmqpTemplate amqpTemplate;

    //上下架商品
    public void unsaleGoods(Long id, Boolean saleable) {

        Spu spu = new Spu();
        spu.setId(id);
        spu.setSaleable(saleable);

        //先修改tb_spu中的saleable

        int count = spuMapper.updateByPrimaryKeySelective(spu);
        if (count != 1) {
            throw new LyException(ExceptionEnum.UPDATE_OPERATION_FAIL);
        }


        //再修改tb_sku中的enable
        // 要更新的数据
        Sku sku = new Sku();
        sku.setEnable(saleable);
        // 更新的条件
        Example example = new Example(Sku.class);
        example.createCriteria().andEqualTo("spuId", id);
        count = skuMapper.updateByExampleSelective(sku, example);

        int size = skuMapper.selectCountByExample(example);
        if (count != size) {
            throw new LyException(ExceptionEnum.UPDATE_OPERATION_FAIL);
        }

        //发送mq
        String key = saleable ? ITEM_UP_KEY :  ITEM_DOWN_KEY;
        amqpTemplate.convertAndSend(ITEM_EXCHANGE_NAME, key, id);
    }


    //编辑商品时，回显商品数据spu的信息
    public SpuDetailDTO returnGoodsDetail(Long id) {

        SpuDetail spuDetail = spuDetailMapper.selectByPrimaryKey(id);

        if (spuDetail == null) {
            throw new LyException(ExceptionEnum.GOODS_NOT_FOUND);
        }

        return BeanHelper.copyProperties(spuDetail, SpuDetailDTO.class);
    }

    //编辑商品时，回显商品数据sku的信息
    public List<SkuDTO> returnGoodsDetailSku(Long id) {

        Sku sku = new Sku();
        sku.setSpuId(id);
        List<Sku> skuList = skuMapper.select(sku);

        return BeanHelper.copyWithCollection(skuList, SkuDTO.class);
    }

    // 保存更新商品的数据
    @Transactional
    public void updateSaveGoods(SpuDTO spuDTO) {

        Long spuDTOId = spuDTO.getId();

        if (spuDTOId == null) {
            throw new LyException(ExceptionEnum.INVALID_PARAM_ERROR);
        }

        // 删除sku
        Sku sku = new Sku();
        sku.setSpuId(spuDTOId);
        // 删除前先查看数量
        int size = skuMapper.selectCount(sku);
        if (size > 0) {
            int deletecount = skuMapper.delete(sku);
            if(deletecount != size){
                throw new LyException(ExceptionEnum.UPDATE_OPERATION_FAIL);
            }
        }

        //更新spu
        Spu spu = BeanHelper.copyProperties(spuDTO, Spu.class);
        spu.setSaleable(null);
        int count = spuMapper.updateByPrimaryKeySelective(spu);
        if (count != 1) {
            throw new LyException(ExceptionEnum.UPDATE_OPERATION_FAIL);
        }

        //更新spuDetail
        SpuDetail spuDetail = BeanHelper.copyProperties(spuDTO.getSpuDetail(), SpuDetail.class);
        spuDetail.setSpuId(spuDTOId);

        count = spuDetailMapper.updateByPrimaryKeySelective(spuDetail);
        if (count != 1) {
            throw new LyException(ExceptionEnum.UPDATE_OPERATION_FAIL);
        }

        //更新sku
        List<SkuDTO> skuDTOList = spuDTO.getSkus();
        List<Sku> skuList = BeanHelper.copyWithCollection(skuDTOList, Sku.class);
        for (Sku sku1 : skuList) {
            sku1.setSpuId(spuDTOId);
            // 默认下架
            sku1.setEnable(false);
        }
        count = skuMapper.insertList(skuList);
        if(count != skuList.size()){
            throw new LyException(ExceptionEnum.INSERT_OPERATION_FAIL);
        }

    }

    public SpuDTO querySpuById(Long id) {
        // 查询spu
        Spu spu = spuMapper.selectByPrimaryKey(id);
        SpuDTO spuDTO = BeanHelper.copyProperties(spu, SpuDTO.class);
        // 查询spuDetail
        spuDTO.setSpuDetail(returnGoodsDetail(id));
        // 查询sku
        spuDTO.setSkus(returnGoodsDetailSku(id));
        return spuDTO;
    }
}
