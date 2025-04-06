package com.liu.gymmanagement.service;

import com.liu.gymmanagement.mapper.CapacityLogMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class CapacityResetService {

    @Autowired
    private CapacityLogMapper capacityLogsMapper;

    // 每天早上6点执行
    @Scheduled(cron = "0 0 6 * * ?")
    public void resetCapacityCount() {
        capacityLogsMapper.resetCurrentCount();
        System.out.println("每天6点，所有健身房的当前人数已重置！");
    }
}
