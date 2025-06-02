package com.liu.gymmanagement.service;

import com.liu.gymmanagement.mapper.WorkoutLogMapper;
import com.liu.gymmanagement.model.WorkoutLog;
import com.liu.gymmanagement.model.WorkoutLogExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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

    //锻炼建议
    public String generateWorkoutSuggestion(String userId) {
        // 获取7天前的日期
        LocalDate sevenDaysAgo = LocalDate.now().minusDays(7);
        Date sevenDaysAgoDate = java.sql.Date.valueOf(sevenDaysAgo);

        // 创建查询条件
        WorkoutLogExample example = new WorkoutLogExample();
        WorkoutLogExample.Criteria criteria = example.createCriteria();
        criteria.andUseridEqualTo(userId);
        criteria.andDateGreaterThanOrEqualTo(sevenDaysAgoDate);

        // 查询最近一周的记录
        List<WorkoutLog> recentLogs = workoutLogMapper.selectByExample(example);

        if (recentLogs.isEmpty()) {
            return "It seems that you haven't logged any workouts this week. Let's get moving!";
        }

        // 统计锻炼类型次数
        Map<String, Long> activityCount = recentLogs.stream()
                .collect(Collectors.groupingBy(WorkoutLog::getActivitytype, Collectors.counting()));

        // 找出出现最多的锻炼类型
        Optional<Map.Entry<String, Long>> mostFrequent = activityCount.entrySet().stream()
                .max(Map.Entry.comparingByValue());

        if (mostFrequent.isPresent() && mostFrequent.get().getValue() >= 3) {
            String activity = mostFrequent.get().getKey();
            return "You've done " + activity + " several times this week. Try mixing it up with other activities for a more balanced workout!";
        }

        return "Great job keeping active this week! Keep up the good work!";
    }
}

