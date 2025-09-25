package cn.lethekk.userservice.service;

import com.alibaba.cloud.nacos.annotation.NacosConfigListener;
import org.springframework.stereotype.Service;

/**
 * @Author Lethekk
 * @Date 2025/9/26 0:08
 */
@Service
public class MyRateConfigService {

    @NacosConfigListener(dataId = "routeconfig",group = "config")
    public void rate(String rateConfig) {
        System.out.println("receiveRateConfig:"+rateConfig);
    }

}