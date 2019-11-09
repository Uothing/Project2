package com.leyou.service;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.BeanHelper;
import com.leyou.item.entity.SpecGroup;
import com.leyou.item.entity.SpecParam;
import com.leyou.mapper.SpecGroupMapper;
import com.leyou.mapper.SpecParamMapper;
import com.leyou.pojo.Item;
import com.leyou.pojo.dto.SpecGroupDTO;
import com.leyou.pojo.dto.SpecParamDTO;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Random;

/**
 * @version V1.0
 * @author: weiyuan
 * @date: 2019/10/30 20:28
 * @description:
 */
@Service
public class ItemService {


    @Autowired
    private SpecGroupMapper specGroupMapper;

    @Autowired
    private SpecParamMapper specParamMapper;

    public Item saveItem(Item item) {

        /*// 如果价格为空，则抛出异常，返回400状态码，请求参数有误
        if(item.getPrice() == null){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
        Item result = itemService.saveItem(item);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);*/


        /*if(item.getPrice() == null){
            throw new RuntimeException("价格不能为空");
        }
        Item result = itemService.saveItem(item);
        return ResponseEntity.ok(result);*/

        if(item.getPrice() == null){
            throw new LyException(ExceptionEnum.PRICE_NOT_FOUND);
        }
        //int a = 1/0;
        if(StringUtils.isBlank(item.getName())){
            throw new LyException(ExceptionEnum.INVALID_USERNAME_PASSWORD);
        }


        int id = new Random().nextInt(100);
        item.setId(id);
        return item;
    }

    //根据id查询手机的  属性组信息
    public List<SpecGroupDTO> queryGroupById(Long id) {

        //赋值id
        SpecGroup specGroup = new SpecGroup();
        specGroup.setCid(id);

        //查询
        List<SpecGroup> specGroupList = specGroupMapper.select(specGroup);

        if (CollectionUtils.isEmpty(specGroupList)) {
            throw new LyException(ExceptionEnum.SPEC_NOT_FOUND);
        }

        //返回DTO
        return BeanHelper.copyWithCollection(specGroupList, SpecGroupDTO.class);
    }


    //根据id查询手机的  属性组信息
    public List<SpecParamDTO> queryGroupItemById(Long gid, Long cid, Boolean searching) {

        //赋值id
        SpecParam specParam = new SpecParam();
        specParam.setGroupId(gid);
        specParam.setCid(cid);
        specParam.setSearching(searching);
        //查询
        List<SpecParam> specParamList = specParamMapper.select(specParam);

        if (CollectionUtils.isEmpty(specParamList)) {
            throw new LyException(ExceptionEnum.SPEC_NOT_FOUND);
        }

        //返回DTO
        return BeanHelper.copyWithCollection(specParamList, SpecParamDTO.class);
    }
}
