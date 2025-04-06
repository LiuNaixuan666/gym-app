package com.liu.gymmanagement.service;
import com.liu.gymmanagement.mapper.GymTimeslotMapper;
import com.liu.gymmanagement.mapper.GymTimeslotTemplateMapper;
import com.liu.gymmanagement.model.GymTimeslot;
import com.liu.gymmanagement.model.GymTimeslotTemplate;
import com.liu.gymmanagement.model.GymTimeslotTemplateExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class GymTimeslotTemplateService {
    @Autowired
    private GymTimeslotTemplateMapper gymTimeslotTemplateMapper;
    @Autowired
    private GymTimeslotMapper gymTimeslotMapper;


    public List<GymTimeslotTemplate> getAllTemplates() {
        GymTimeslotTemplateExample example = new GymTimeslotTemplateExample();
        return gymTimeslotTemplateMapper.selectByExample(example);
    }
    public boolean addTemplate(GymTimeslotTemplate template) {
        return gymTimeslotTemplateMapper.insert(template) > 0;
    }

    public boolean updateTemplate(int id, GymTimeslotTemplate template) {
        template.setId(id);
        return gymTimeslotTemplateMapper.updateByPrimaryKey(template) > 0;
    }

    public boolean deleteTemplate(int id) {
        return gymTimeslotTemplateMapper.deleteByPrimaryKey(id) > 0;
    }

}