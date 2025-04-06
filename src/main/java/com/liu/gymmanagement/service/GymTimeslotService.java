package com.liu.gymmanagement.service;

import com.liu.gymmanagement.dto.GymTimeslotDTO;
import com.liu.gymmanagement.mapper.GymTimeslotMapper;
import com.liu.gymmanagement.mapper.GymTimeslotTemplateMapper;
import com.liu.gymmanagement.model.GymTimeslot;
import com.liu.gymmanagement.model.GymTimeslotExample;
import com.liu.gymmanagement.model.GymTimeslotTemplate;
import com.liu.gymmanagement.model.GymTimeslotTemplateExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
@EnableScheduling
@Service
public class GymTimeslotService {

    @Autowired
    private GymTimeslotMapper gymTimeslotMapper;
    @Autowired
    private GymTimeslotTemplateMapper gymTimeslotTemplateMapper;

    // 每天中午 12:00 运行，检查并补充未来 7 天的时段
    @Scheduled(cron = "0 7 7 * * ?")  // CRON表达式：每天中午12点触发
    public void generateNextWeekTimeslots() {
        System.out.println("【定时任务】正在生成未来 7 天的时段...");

        LocalDate today = LocalDate.now();
        LocalDate lastDay = today.plusDays(7); // 未来 7 天

        // 获取所有时段模板
        GymTimeslotTemplateExample example = new GymTimeslotTemplateExample();
        List<GymTimeslotTemplate> templates = gymTimeslotTemplateMapper.selectByExample(example);
        System.out.println("【DEBUG】模板时段数量：" + templates.size());

        for (LocalDate date = today; !date.isAfter(lastDay); date = date.plusDays(1)) {
            for (GymTimeslotTemplate template : templates) {
                // 生成每个时段模板的多个时段
                GymTimeslotExample example2 = new GymTimeslotExample();
                GymTimeslotExample.Criteria criteria = example2.createCriteria();
                criteria.andGymidEqualTo(template.getGymid());
                criteria.andDateEqualTo(java.sql.Date.valueOf(date)); // 解决类型不匹配问题

                // 获取现有时段的列表，检查是否有重叠
                List<GymTimeslot> existingTimeslots = gymTimeslotMapper.selectByExample(example2);

                boolean isOverlap = false;
                for (GymTimeslot existingTimeslot : existingTimeslots) {
                    // 检查当前时段与已有时段是否重叠
                    if (isTimeSlotOverlap(existingTimeslot, template)) {
                        isOverlap = true;
                        break;
                    }
                }

                // 如果没有重叠，插入新时段
                if (!isOverlap) {
                    GymTimeslot timeslot = new GymTimeslot();
                    timeslot.setGymid(template.getGymid());
                    timeslot.setDate(date);
                    timeslot.setStartTime(template.getStartTime());
                    timeslot.setEndTime(template.getEndTime());
                    timeslot.setMaxCapacity(template.getMaxCapacity());
                    timeslot.setCurrentReservations(0);

                    // 插入 gym_timeslot 表
                    gymTimeslotMapper.insert(timeslot);
                }
//                else {
//                    System.out.println("时段 " + template.getStartTime() + " - " + template.getEndTime() + " 与已有时段重叠，跳过插入。");
//                }
            }

            System.out.println("成功补充未来 7 天的可预约时段");
        }
    }
    // 获取健身房的所有时段
    public List<GymTimeslotDTO> getTimeslotsForGym(int gymId) {
        GymTimeslotExample example = new GymTimeslotExample();
        example.createCriteria().andGymidEqualTo(gymId);
        List<GymTimeslot> timeslots = gymTimeslotMapper.selectByExample(example);

        // 转换为 DTO
        List<GymTimeslotDTO> dtos = new ArrayList<>();
        for (GymTimeslot timeslot : timeslots) {
            GymTimeslotDTO dto = new GymTimeslotDTO();
            dto.setTimeslotId(timeslot.getId());
            dto.setStartTime(timeslot.getStartTime());
            dto.setEndTime(timeslot.getEndTime());
            dto.setMaxCapacity(timeslot.getMaxCapacity());
            dtos.add(dto);
        }
        return dtos;
    }

    // GymTimeslotServiceImpl.java
    public List<GymTimeslot> getTimeslotsForGym(int gymId, String dateStr) {
        try {
            // 将前端传来的字符串转为Date
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date date = sdf.parse(dateStr);

            GymTimeslotExample example = new GymTimeslotExample();
            example.createCriteria()
                    .andGymidEqualTo(gymId)
                    .andDateEqualTo(date); // 这里使用转换后的Date对象

            return gymTimeslotMapper.selectByExample(example);
        } catch (ParseException e) {
            throw new IllegalArgumentException("日期格式不正确，请使用yyyy-MM-dd格式");
        }
    }

    // 修改时段容量
    public boolean updateTimeslotCapacity(int gymId, int timeslotId, int newCapacity) {
        GymTimeslotExample example = new GymTimeslotExample();
        example.createCriteria().andGymidEqualTo(gymId).andIdEqualTo(timeslotId);
        GymTimeslot timeslot = new GymTimeslot();
        timeslot.setMaxCapacity(newCapacity);
        return gymTimeslotMapper.updateByExampleSelective(timeslot, example) > 0;
    }

    public List<GymTimeslot> getFutureTimeslots() {
        LocalDate today = LocalDate.now();
        LocalDate lastDay = today.plusDays(7);
        LocalTime now = LocalTime.now();

        // 将 LocalDate 转换为 java.util.Date
        Date startDate = Date.from(today.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date endDate = Date.from(lastDay.atStartOfDay(ZoneId.systemDefault()).toInstant());


        // 创建查询条件对象
        GymTimeslotExample example = new GymTimeslotExample();
        GymTimeslotExample.Criteria criteria = example.createCriteria();

        // 设置查询条件：查询未来一周的预约时段
        criteria.andDateBetween(startDate, endDate); // 查询日期范围为今天到未来7天的时段

        // 执行查询，返回符合条件的时段列表
        List<GymTimeslot> timeslots = gymTimeslotMapper.selectByExample(example);

        // 打印查询结果，检查 startTime 是否为 null
        //timeslots.forEach(slot -> System.out.println("Timeslot: " + slot.getStartTime()));

        // 过滤当前时间之前的时段
        return timeslots.stream()
                .filter(slot -> slot.getDate().isAfter(today) || slot.getStartTime().isAfter(now))
                .collect(Collectors.toList());
    }


//    public List<GymTimeslot> getFutureTimeslots() {
//        LocalDate today = LocalDate.now();
//        LocalDate lastDay = today.plusDays(7);
//        LocalTime now = LocalTime.now();
//
//        List<GymTimeslot> timeslots = gymTimeslotMapper.selectFutureTimeslots(today, lastDay);
//
//        // 打印查询结果，检查 startTime 是否为 null
//        timeslots.forEach(slot -> System.out.println("Timeslot: " + slot.getStartTime()));
//
//        // 过滤当前时间之前的时段
//        return timeslots.stream()
//                .filter(slot -> slot.getDate().isAfter(today) || slot.getStartTime().isAfter(now))
//                .collect(Collectors.toList());
//    }



    // 检查两个时段是否重叠
    private boolean isTimeSlotOverlap(GymTimeslot existingTimeslot, GymTimeslotTemplate template) {
        LocalTime existingStart = existingTimeslot.getStartTime();
        LocalTime existingEnd = existingTimeslot.getEndTime();
        LocalTime newStart = template.getStartTime();
        LocalTime newEnd = template.getEndTime();

        // 时段重叠的条件：新时段的开始时间小于已有时段的结束时间且新时段的结束时间大于已有时段的开始时间
        return newStart.isBefore(existingEnd) && newEnd.isAfter(existingStart);
    }
//
//    public List<GymTimeslot> getAvailableTimeslots() {
//        LocalDate today = LocalDate.now();
//        LocalDate lastDay = today.plusDays(7);
//        LocalTime now = LocalTime.now();
//
//        List<GymTimeslot> timeslots = gymTimeslotMapper.selectFutureTimeslots(today, lastDay);
//
//        // 过滤当前时间之前的时段
//        return timeslots.stream()
//                .filter(slot -> slot.getDate().isAfter(today) || slot.getStartTime().isAfter(now))
//                .collect(Collectors.toList());
//    }

}
