package com.codingapi.example.demo.service.impl;

import com.codingapi.example.common.db.domain.Demo;
import com.codingapi.example.demo.mapper.EDemoMapper;
import com.codingapi.example.demo.service.DemoService;
import com.codingapi.txlcn.client.bean.DTXLocal;
import com.codingapi.txlcn.commons.annotation.DTXPropagation;
import com.codingapi.txlcn.commons.annotation.TccTransaction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Description:
 * Date: 2018/12/25
 *
 * @author ujued
 */
@Service
@Slf4j
public class DemoServiceImpl implements DemoService {

    private final EDemoMapper demoMapper;

    private ConcurrentHashMap<String, Long> ids = new ConcurrentHashMap<>();

    @Value("${spring.application.name}")
    private String appName;

    @Autowired
    public DemoServiceImpl(EDemoMapper demoMapper) {
        this.demoMapper = demoMapper;
    }

    @Override
    @TccTransaction(dtxp = DTXPropagation.SUPPORTS)
    public String rpc(String value) {
        Demo demo = new Demo();
        demo.setDemoField(value);
        demo.setCreateTime(new Date());
        demo.setAppName(appName);
        demo.setGroupId(DTXLocal.getOrNew().getGroupId());
        demo.setUnitId(DTXLocal.getOrNew().getUnitId());
        demoMapper.save(demo);
        ids.put(DTXLocal.cur().getGroupId(), demo.getId());
        return "ok-e";
    }

    public void confirmRpc(String value) {
        log.info("tcc-confirm-" + DTXLocal.getOrNew().getGroupId());
        ids.remove(DTXLocal.getOrNew().getGroupId());
    }

    public void cancelRpc(String value) {
        log.info("tcc-cancel-" + DTXLocal.getOrNew().getGroupId());
        demoMapper.deleteById(ids.get(DTXLocal.getOrNew().getGroupId()));
    }
}
