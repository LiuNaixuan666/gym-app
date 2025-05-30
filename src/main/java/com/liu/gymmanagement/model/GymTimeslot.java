package com.liu.gymmanagement.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;

public class GymTimeslot {
    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column gym_timeslot.id
     *
     * @mbg.generated Thu Mar 20 22:59:52 CST 2025
     */
    private Integer id;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column gym_timeslot.GymID
     *
     * @mbg.generated Thu Mar 20 22:59:52 CST 2025
     */
    private Integer gymid;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column gym_timeslot.date
     *
     * @mbg.generated Thu Mar 20 22:59:52 CST 2025
     */
    private LocalDate date;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column gym_timeslot.start_time
     *
     * @mbg.generated Thu Mar 20 22:59:52 CST 2025
     */
    private LocalTime startTime;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column gym_timeslot.end_time
     *
     * @mbg.generated Thu Mar 20 22:59:52 CST 2025
     */
    private LocalTime endTime;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column gym_timeslot.max_capacity
     *
     * @mbg.generated Thu Mar 20 22:59:52 CST 2025
     */
    private Integer maxCapacity;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column gym_timeslot.current_reservations
     *
     * @mbg.generated Thu Mar 20 22:59:52 CST 2025
     */
    private Integer currentReservations;

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column gym_timeslot.id
     *
     * @return the value of gym_timeslot.id
     *
     * @mbg.generated Thu Mar 20 22:59:52 CST 2025
     */
    public Integer getId() {
        return id;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column gym_timeslot.id
     *
     * @param id the value for gym_timeslot.id
     *
     * @mbg.generated Thu Mar 20 22:59:52 CST 2025
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column gym_timeslot.GymID
     *
     * @return the value of gym_timeslot.GymID
     *
     * @mbg.generated Thu Mar 20 22:59:52 CST 2025
     */
    public Integer getGymid() {
        return gymid;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column gym_timeslot.GymID
     *
     * @param gymid the value for gym_timeslot.GymID
     *
     * @mbg.generated Thu Mar 20 22:59:52 CST 2025
     */
    public void setGymid(Integer gymid) {
        this.gymid = gymid;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column gym_timeslot.date
     *
     * @return the value of gym_timeslot.date
     *
     * @mbg.generated Thu Mar 20 22:59:52 CST 2025
     */
    public LocalDate getDate() {
        return date;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column gym_timeslot.date
     *
     * @param date the value for gym_timeslot.date
     *
     * @mbg.generated Thu Mar 20 22:59:52 CST 2025
     */
    public void setDate(LocalDate date) {
        this.date = date;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column gym_timeslot.start_time
     *
     * @return the value of gym_timeslot.start_time
     * @mbg.generated Thu Mar 20 22:59:52 CST 2025
     */
    public LocalTime getStartTime() {
        return startTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column gym_timeslot.start_time
     *
     * @param startTime the value for gym_timeslot.start_time
     *
     * @mbg.generated Thu Mar 20 22:59:52 CST 2025
     */
    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column gym_timeslot.end_time
     *
     * @return the value of gym_timeslot.end_time
     * @mbg.generated Thu Mar 20 22:59:52 CST 2025
     */
    public LocalTime getEndTime() {
        return endTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column gym_timeslot.end_time
     *
     * @param endTime the value for gym_timeslot.end_time
     *
     * @mbg.generated Thu Mar 20 22:59:52 CST 2025
     */
    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column gym_timeslot.max_capacity
     *
     * @return the value of gym_timeslot.max_capacity
     *
     * @mbg.generated Thu Mar 20 22:59:52 CST 2025
     */
    public Integer getMaxCapacity() {
        return maxCapacity;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column gym_timeslot.max_capacity
     *
     * @param maxCapacity the value for gym_timeslot.max_capacity
     *
     * @mbg.generated Thu Mar 20 22:59:52 CST 2025
     */
    public void setMaxCapacity(Integer maxCapacity) {
        this.maxCapacity = maxCapacity;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column gym_timeslot.current_reservations
     *
     * @return the value of gym_timeslot.current_reservations
     *
     * @mbg.generated Thu Mar 20 22:59:52 CST 2025
     */
    public Integer getCurrentReservations() {
        return currentReservations;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column gym_timeslot.current_reservations
     *
     * @param currentReservations the value for gym_timeslot.current_reservations
     *
     * @mbg.generated Thu Mar 20 22:59:52 CST 2025
     */
    public void setCurrentReservations(Integer currentReservations) {
        this.currentReservations = currentReservations;
    }
}