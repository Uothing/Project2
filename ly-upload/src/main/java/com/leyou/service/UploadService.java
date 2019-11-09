package com.leyou.service;

import com.aliyun.oss.OSS;
import com.aliyun.oss.common.utils.BinaryUtil;
import com.aliyun.oss.model.MatchMode;
import com.aliyun.oss.model.PolicyConditions;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.config.OSSProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * @version V1.0
 * @author: weiyuan
 * @date: 2019/11/2 21:41
 * @description:
 */
@Service
@Slf4j
public class UploadService {


    private static final String IMAGE_DIR = "E:\\JavaTool\\nginx-1.12.2\\html\\images";
    private static final String IMAGE_URL = "http://image.leyou.com/images/";
    private static final List<String> ALLOW_IMAGE_TYPES = Arrays.asList("image/jpeg", "image/png", "image/bmp");

    public String upload(MultipartFile file) {
        // 文件校验
        String contentType = file.getContentType();
        if (!ALLOW_IMAGE_TYPES.contains(contentType)) {
            log.error("图片上传失败，文件类型有误");
            throw new LyException(ExceptionEnum.INVALID_FILE_TYPE);
        }
        // 内容校验
        try {
            BufferedImage image = ImageIO.read(file.getInputStream());
            if (image == null) {
                throw new RuntimeException();//???
            }
        } catch (IOException e) {
            log.error("图片上传失败，文件类型有误");
            throw new LyException(ExceptionEnum.INVALID_FILE_TYPE);
        }

        // 文件地址
        String filename = file.getOriginalFilename();
        // 后缀
        String extension = StringUtils.substringAfterLast(filename, ".");
        // 目标地址
        filename = UUID.randomUUID().toString() + "." +extension;
        File filePath = new File(IMAGE_DIR, filename);

        // 保存
        try {
            file.transferTo(filePath);
        } catch (IOException e) {
            log.error("文件上传失败，原因：{}", e.getMessage());
            throw new LyException(ExceptionEnum.FILE_UPLOAD_ERROR);
        }
        // 返回url
        return IMAGE_URL + filename;
    }



    @Autowired
    private OSSProperties prop;

    @Autowired
    private OSS client;

    public Map<String, Object> getSignature() {
        try {
            long expireTime = prop.getExpireTime();
            long expireEndTime = System.currentTimeMillis() + expireTime * 1000;
            Date expiration = new Date(expireEndTime);
            PolicyConditions policyConds = new PolicyConditions();
            policyConds.addConditionItem(PolicyConditions.COND_CONTENT_LENGTH_RANGE, 0, prop.getMaxFileSize());
            policyConds.addConditionItem(MatchMode.StartWith, PolicyConditions.COND_KEY, prop.getDir());

            String postPolicy = client.generatePostPolicy(expiration, policyConds);
            byte[] binaryData = postPolicy.getBytes("utf-8");
            String encodedPolicy = BinaryUtil.toBase64String(binaryData);
            String postSignature = client.calculatePostSignature(postPolicy);

            Map<String, Object> respMap = new LinkedHashMap<>();
            respMap.put("accessId", prop.getAccessKeyId());
            respMap.put("policy", encodedPolicy);
            respMap.put("signature", postSignature);
            respMap.put("dir", prop.getDir());
            respMap.put("host", prop.getHost());
            respMap.put("expire", expireEndTime);
            return respMap;
        }catch (Exception e){
            throw new LyException(ExceptionEnum.UPDATE_OPERATION_FAIL);
        }
    }
}
