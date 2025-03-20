package com.liu.gymmanagement.service;

import com.liu.gymmanagement.dto.GymTimeslotDTO;
import com.liu.gymmanagement.mapper.GymTimeslotMapper;
import com.liu.gymmanagement.mapper.GymTimeslotTemplateMapper;
import com.liu.gymmanagement.model.GymTimeslot;
import com.liu.gymmanagement.model.GymTimeslotExample;
import com.liu.gymmanagement.model.GymTimeslotTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class GymTimeslotService {

    @Autowired
    private GymTimeslotMapper gymTimeslotMapper;
    @Autowired
    private GymTimeslotTemplateMapper gymTimeslotTemplateMapper;

    // 每天中午 12:00 运行，检查并补充未来 7 天的时段
    @Scheduled(cron = "0 0 12 * * ?")  // CRON表达式：每天中午12点触发
    public void generateNextWeekTimeslots() {
        LocalDate today = LocalDate.now();
        LocalDate lastDay = today.plusDays(7); // 未来 7 天

        // 获取所有时段模板
        List<GymTimeslotTemplate> templates = gymTimeslotTemplateMapper.selectAll();

        for (LocalDate date = today; !date.isAfter(lastDay); date = date.plusDays(1)) {
            for (GymTimeslotTemplate template : templates) {
                // ✅ 使用 MyBatis 逆向生成的 countByExample
                GymTimeslotExample example = new GymTimeslotExample();
                GymTimeslotExample.Criteria criteria = example.createCriteria();
                criteria.andGymidEqualTo(template.getGymid());
                criteria.andDateEqualTo(date);

                int count = gymTimeslotMapper.countByExample(example);
                if (count > 0) continue; // 如果已有时段，跳过

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

    // 修改时段容量
    public boolean updateTimeslotCapacity(int gymId, int timeslotId, int newCapacity) {
        GymTimeslotExample example = new GymTimeslotExample();
        example.createCriteria().andGymidEqualTo(gymId).andIdEqualTo(timeslotId);
        GymTimeslot timeslot = new GymTimeslot();
        timeslot.setMaxCapacity(newCapacity);
        return gymTimeslotMapper.updateByExampleSelective(timeslot, example) > 0;
    }
}
