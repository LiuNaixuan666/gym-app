package com.liu.gymmanagement.service;

import com.liu.gymmanagement.dto.CapacityLogDTO;
import com.liu.gymmanagement.mapper.GymMapper;
import com.liu.gymmanagement.model.Gym;
import com.liu.gymmanagement.mapper.CapacityLogMapper;
import com.liu.gymmanagement.model.CapacityLog;
import com.liu.gymmanagement.model.GymExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GymService {

    @Autowired
    private GymMapper gymMapper;

    @Autowired
    private CapacityLogMapper capacityLogsMapper;

    public List<Gym> getAllGyms() {
        return gymMapper.selectByExampleWithBLOBs(new GymExample());  // 查询所有
    }
    // 更新健身房信息
    public boolean updateGym(Gym gym) {
        int rowsAffected = gymMapper.updateByPrimaryKeySelective(gym);
        return rowsAffected > 0;
    }

    public List<CapacityLogDTO> getAllGymRealTimeCapacity() {
        return capacityLogsMapper.getAllGymCurrentCapacityWithNames();
    }

    // 添加健身房
    public boolean addGym(Gym gym) {
        int rowsInserted = gymMapper.insertSelective(gym);
        return rowsInserted > 0;
    }
}

// 获取所有健身房的实时容量
//    public List<CapacityLog> getAllGymRealTimeCapacity() {
//        return capacityLogsMapper.getAllGymCurrentCapacity();
//    }