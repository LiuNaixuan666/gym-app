package com.liu.gymmanagement.model;

public class Gym {
    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column gym.GymID
     *
     * @mbg.generated Thu Mar 20 02:19:12 CST 2025
     */
    private Integer gymid;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column gym.Name
     *
     * @mbg.generated Thu Mar 20 02:19:12 CST 2025
     */
    private String name;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column gym.Description
     *
     * @mbg.generated Thu Mar 20 02:19:12 CST 2025
     */
    private String description;

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column gym.GymID
     *
     * @return the value of gym.GymID
     *
     * @mbg.generated Thu Mar 20 02:19:12 CST 2025
     */
    public Integer getGymid() {
        return gymid;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column gym.GymID
     *
     * @param gymid the value for gym.GymID
     *
     * @mbg.generated Thu Mar 20 02:19:12 CST 2025
     */
    public void setGymid(Integer gymid) {
        this.gymid = gymid;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column gym.Name
     *
     * @return the value of gym.Name
     *
     * @mbg.generated Thu Mar 20 02:19:12 CST 2025
     */
    public String getName() {
        return name;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column gym.Name
     *
     * @param name the value for gym.Name
     *
     * @mbg.generated Thu Mar 20 02:19:12 CST 2025
     */
    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column gym.Description
     *
     * @return the value of gym.Description
     *
     * @mbg.generated Thu Mar 20 02:19:12 CST 2025
     */
    public String getDescription() {
        return description;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column gym.Description
     *
     * @param description the value for gym.Description
     *
     * @mbg.generated Thu Mar 20 02:19:12 CST 2025
     */
    public void setDescription(String description) {
        this.description = description == null ? null : description.trim();
    }
}