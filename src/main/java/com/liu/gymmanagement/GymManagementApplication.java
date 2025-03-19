package com.liu.gymmanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication
@EnableJpaRepositories("com.liu.gymmanagement.repository")
@EntityScan("com.liu.gymmanagement.model")  // 确保 Spring 扫描到实体类
public class GymManagementApplication {

	public static void main(String[] args) {

		SpringApplication.run(GymManagementApplication.class, args);
	}

}
