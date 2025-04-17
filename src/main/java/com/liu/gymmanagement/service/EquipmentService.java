package com.liu.gymmanagement.service;

import com.liu.gymmanagement.dto.EquipmentDTO;
import com.liu.gymmanagement.mapper.EquipmentMapper;
import com.liu.gymmanagement.model.Equipment;
import com.liu.gymmanagement.model.EquipmentExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EquipmentService {

    @Autowired
    private EquipmentMapper equipmentMapper;

    // 根据 gymId 查询设备列表
    public List<EquipmentDTO> getEquipmentByGymId(Integer gymId) {
        EquipmentExample example = new EquipmentExample();
        example.createCriteria().andGymidEqualTo(gymId);

        List<Equipment> equipmentList = equipmentMapper.selectByExampleWithBLOBs(example);

        return equipmentList.stream().map(e -> {
            EquipmentDTO dto = new EquipmentDTO();
            dto.setEquipmentId(e.getEquipmentid());
            dto.setName(e.getName());
            dto.setStatus(e.getStatus());
            dto.setDescription(e.getDescription());
            return dto;
        }).collect(Collectors.toList());
    }

    // 添加设备
    public void addEquipment(Equipment equipment) {
        equipmentMapper.insertSelective(equipment);
    }

    // 修改设备
    public void updateEquipment(Equipment equipment) {
        equipmentMapper.updateByPrimaryKeySelective(equipment);
    }

    // 获取设备详情
    public Equipment getEquipmentById(Integer id) {
        return equipmentMapper.selectByPrimaryKey(id);
    }
}
