package com.liu.gymmanagement.mapper;

import com.liu.gymmanagement.model.EquipmentStatus;
import com.liu.gymmanagement.model.EquipmentStatusExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface EquipmentStatusMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table equipmentstatus
     *
     * @mbg.generated Thu Mar 20 02:19:12 CST 2025
     */
    long countByExample(EquipmentStatusExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table equipmentstatus
     *
     * @mbg.generated Thu Mar 20 02:19:12 CST 2025
     */
    int deleteByExample(EquipmentStatusExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table equipmentstatus
     *
     * @mbg.generated Thu Mar 20 02:19:12 CST 2025
     */
    int deleteByPrimaryKey(Integer statusid);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table equipmentstatus
     *
     * @mbg.generated Thu Mar 20 02:19:12 CST 2025
     */
    int insert(EquipmentStatus record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table equipmentstatus
     *
     * @mbg.generated Thu Mar 20 02:19:12 CST 2025
     */
    int insertSelective(EquipmentStatus record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table equipmentstatus
     *
     * @mbg.generated Thu Mar 20 02:19:12 CST 2025
     */
    List<EquipmentStatus> selectByExample(EquipmentStatusExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table equipmentstatus
     *
     * @mbg.generated Thu Mar 20 02:19:12 CST 2025
     */
    EquipmentStatus selectByPrimaryKey(Integer statusid);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table equipmentstatus
     *
     * @mbg.generated Thu Mar 20 02:19:12 CST 2025
     */
    int updateByExampleSelective(@Param("record") EquipmentStatus record, @Param("example") EquipmentStatusExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table equipmentstatus
     *
     * @mbg.generated Thu Mar 20 02:19:12 CST 2025
     */
    int updateByExample(@Param("record") EquipmentStatus record, @Param("example") EquipmentStatusExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table equipmentstatus
     *
     * @mbg.generated Thu Mar 20 02:19:12 CST 2025
     */
    int updateByPrimaryKeySelective(EquipmentStatus record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table equipmentstatus
     *
     * @mbg.generated Thu Mar 20 02:19:12 CST 2025
     */
    int updateByPrimaryKey(EquipmentStatus record);
}