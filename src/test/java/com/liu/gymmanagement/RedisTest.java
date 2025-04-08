//package com.liu.gymmanagement;
//
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.data.redis.core.StringRedisTemplate;
//
//import java.time.Duration;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//@SpringBootTest
//public class RedisTest {
//
//    @Autowired
//    private StringRedisTemplate redisTemplate;
//
//    @Test
//    public void testRedisSetAndGet() {
//        // 设置值到 Redis，60 秒过期
//        redisTemplate.opsForValue().set("test:key", "hello redis", Duration.ofSeconds(60));
//
//        // 获取值
//        String value = redisTemplate.opsForValue().get("test:key");
//
//        // 打印输出
//        System.out.println("Redis value: " + value);
//
//        // 断言验证值是否正确
//        assertEquals("hello redis", value);
//    }
//}
