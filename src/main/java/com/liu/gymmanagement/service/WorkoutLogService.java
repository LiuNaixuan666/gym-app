package com.liu.gymmanagement.service;

import com.liu.gymmanagement.mapper.WorkoutLogMapper;
import com.liu.gymmanagement.model.WorkoutLog;
import com.liu.gymmanagement.model.WorkoutLogExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WorkoutLogService {

    @Autowired
    private WorkoutLogMapper workoutLogMapper;

    // 添加锻炼记录
    public void addWorkout(WorkoutLog log) {
        workoutLogMapper.insertSelective(log); // 插入非空字段
    }

    // 根据用户ID查询锻炼记录
    public List<WorkoutLog> getWorkoutsByUserId(String userId) {
        WorkoutLogExample example = new WorkoutLogExample();
        example.createCriteria().andUseridEqualTo(userId);
        example.setOrderByClause("Date DESC");
        return workoutLogMapper.selectByExampleWithBLOBs(example);
    }

    // 删除某条记录（可选）
    public void deleteWorkoutLog(Integer workoutLogId) {
        workoutLogMapper.deleteByPrimaryKey(workoutLogId);
    }

    // 更新锻炼记录（可选）
    public void updateWorkout(WorkoutLog log) {
        workoutLogMapper.updateByPrimaryKeySelective(log);
    }
}

