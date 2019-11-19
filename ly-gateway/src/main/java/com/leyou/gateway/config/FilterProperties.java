package com.leyou.gateway.config;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Component
@ConfigurationProperties(prefix = "ly.filter")
public class FilterProperties implements InitializingBean {
    private List<String> allowPaths;

    private Map<String,String> allowPathAndMethod = new HashMap<>();


    @Override
    public void afterPropertiesSet() throws Exception {
        for (String allowPath : allowPaths) {
            if(!allowPath.contains(":")){
                throw new RuntimeException("白名单的路径不符合规范");
            }
            String[] arr = StringUtils.split(allowPath, ":");
            allowPathAndMethod.put(arr[1], arr[0]);
        }
    }
}