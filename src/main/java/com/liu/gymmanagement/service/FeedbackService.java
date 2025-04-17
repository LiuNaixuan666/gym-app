package com.liu.gymmanagement.service;

import com.liu.gymmanagement.dto.FeedbackDTO;
import com.liu.gymmanagement.mapper.FeedbackMapper;
import com.liu.gymmanagement.model.Feedback;
import com.liu.gymmanagement.model.FeedbackExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class FeedbackService {

    @Autowired
    private FeedbackMapper feedbackMapper;

    public void submitFeedback(FeedbackDTO dto) {
        Feedback feedback = new Feedback();
        feedback.setUserid(dto.getUserId());
        feedback.setContent(dto.getContent());
        feedback.setCategory(dto.getCategory()); // 你可以在这里添加校验 enum 的逻辑
        feedback.setTimestamp(new Date()); // 也可以省略，由数据库自动填充

        feedbackMapper.insertSelective(feedback);
    }
    public List<Feedback> getAllFeedbacks() {
        FeedbackExample example = new FeedbackExample();
        example.setOrderByClause("Timestamp DESC"); // 按时间倒序排列

        // ✅ 改为使用带 BLOB 字段的查询方法
        return feedbackMapper.selectByExampleWithBLOBs(example);
    }

}