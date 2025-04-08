package com.liu.gymmanagement.mapper;

import com.liu.gymmanagement.model.Accesslogs;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;

public interface AccesslogsCustomMapper {

    @Select("SELECT * FROM accesslogs WHERE UserID = #{userId} AND GymID = #{gymId} AND ReservationID = #{reservationId} ORDER BY EntryTime DESC LIMIT 1")
    @Results({
            @Result(property = "logid", column = "LogID"),
            @Result(property = "userid", column = "UserID"),
            @Result(property = "gymid", column = "GymID"),
            @Result(property = "reservationid", column = "ReservationID"),
            @Result(property = "entrytime", column = "EntryTime"),
            @Result(property = "exittime", column = "ExitTime")
    })
    Accesslogs findLatestByUserIdGymIdAndReservationId(@Param("userId") String userId,
                                                       @Param("gymId") int gymId,
                                                       @Param("reservationId") int reservationId);

    // 插入新的出入记录（新增 reservationId 字段）
    @Insert("INSERT INTO accesslogs (UserID, GymID, ReservationID, EntryTime) " +
            "VALUES (#{userid}, #{gymid}, #{reservationid}, #{entrytime})")
    void insertAccessLog(Accesslogs accesslogs);


    // 更新出场时间
    @Update("UPDATE accesslogs SET ExitTime = #{exitTime} " +
            "WHERE UserID = #{userId} AND GymID = #{gymId} AND ReservationID = #{reservationId} AND ExitTime IS NULL")
    void updateExitTime(@Param("userId") String userId,
                        @Param("gymId") int gymId,
                        @Param("reservationId") int reservationId,
                        @Param("exitTime") LocalDateTime exitTime);




    // 查询最新的入场记录
    @Select("SELECT * FROM accesslogs WHERE UserID = #{userId} AND GymID = #{gymId} ORDER BY EntryTime DESC LIMIT 1")
    @Results({
            @Result(property = "userid", column = "UserID"),
            @Result(property = "gymid", column = "GymID"),
            @Result(property = "entrytime", column = "EntryTime")
    })
    Accesslogs findLatestByUserIdAndGymId(String userId, int gymId);

//    // 插入新的出入记录
//    @Insert("INSERT INTO accesslogs (UserID, GymID, EntryTime) VALUES (#{userId}, #{gymId}, #{entryTime})")
//    @Results({
//            @Result(property = "userid", column = "UserID"),
//            @Result(property = "gymid", column = "GymID"),
//            @Result(property = "entrytime", column = "EntryTime")
//    })
//    void insertAccessLog(Accesslogs accesslogs);

//    // 更新出场时间
//    @Insert("UPDATE accesslogs SET ExitTime = #{exitTime} WHERE UserID = #{userId} AND GymID = #{gymId} AND ExitTime IS NULL")
//    @Results({
//            @Result(property = "userid", column = "UserID"),
//            @Result(property = "gymid", column = "GymID"),
//            @Result(property = "exittime", column = "ExitTime")
//    })
//    void updateExitTime(String userId, int gymId, LocalDateTime exitTime);
}
