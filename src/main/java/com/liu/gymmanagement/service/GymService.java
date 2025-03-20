package com.liu.gymmanagement.service;

import com.liu.gymmanagement.mapper.GymMapper;
import com.liu.gymmanagement.model.Gym;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GymService {

    @Autowired
    private GymMapper gymMapper;

    public List<Gym> getAllGyms() {
        return gymMapper.selectAll();  // 使用 MyBatis 自动生成的方法查询所有健身房
    }
    // 更新健身房信息
    public boolean updateGym(Gym gym) {
        int rowsAffected = gymMapper.updateByPrimaryKeySelective(gym);
        return rowsAffected > 0;
    }
}
