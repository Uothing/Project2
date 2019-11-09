package com.leyou.controller;

import com.leyou.service.UploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * @version V1.0
 * @author: weiyuan
 * @date: 2019/11/2 21:39
 * @description:
 */
@RestController
public class UploadController {

    @Autowired
    private UploadService uploadService;

    //图片上传

    @PostMapping("/image")
    public ResponseEntity<String> upImage(@RequestParam("file") MultipartFile file) {

        return ResponseEntity.ok(uploadService.upload(file));
    }

    //图片上传  阿里云

    @GetMapping("signature")
    public ResponseEntity<Map<String,Object>> getAliSignature() {

        return ResponseEntity.ok(uploadService.getSignature());
    }
}
