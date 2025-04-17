package com.liu.gymmanagement.service;

import com.liu.gymmanagement.dto.NotificationDTO;
import com.liu.gymmanagement.dto.NotificationRequest;
import com.liu.gymmanagement.mapper.NotificationMapper;
import com.liu.gymmanagement.model.Notification;
import com.liu.gymmanagement.model.NotificationExample;
import com.liu.gymmanagement.model.UserRole;
import com.liu.gymmanagement.repository.UserRoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    @Autowired
    private NotificationMapper notificationMapper;

    @Autowired
    private UserRoleRepository userRoleRepository;

    public List<NotificationDTO> getNotificationsForUser(String userId) {
        NotificationExample example = new NotificationExample();
        example.createCriteria().andUseridEqualTo(userId);
        example.setOrderByClause("Timestamp DESC"); // 最新的在前

        // 使用包含 BLOB 字段的查询方法
        List<Notification> notifications = notificationMapper.selectByExampleWithBLOBs(example);

        return notifications.stream().map(n -> {
            NotificationDTO dto = new NotificationDTO();
            dto.setNotificationId(n.getNotificationid());
            dto.setMessage(n.getMessage());
            dto.setType(n.getType());
            dto.setTimestamp(n.getTimestamp());
            System.out.println("Notification message: " + n.getMessage()); // 调试输出
            return dto;
        }).collect(Collectors.toList());
    }


    public void sendNotificationToAllStudents(String adminId, NotificationRequest request) {
        // 获取所有是学生的用户（RoleID=1）
        List<UserRole> studentRoles = userRoleRepository.findById_RoleID(1);

        for (UserRole studentRole : studentRoles) {
            String studentUserId = studentRole.getId().getUserID(); // 从复合主键中取出 userID

            Notification notification = new Notification();
            notification.setUserid(studentUserId);
            notification.setMessage(request.getMessage());
            notification.setType(request.getType());
            notification.setTimestamp(new Date());

            notificationMapper.insert(notification);
        }
    }
}
