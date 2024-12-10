package com.capstone.safeGuard;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

@SpringBootTest
class RedisTest {

	@Autowired
	private RedisTemplate<String, Object> redisTemplate;

	@Test
	void testRedis() {
		String key = "testKey";
		String value = "Hello, Redis!";

		// 데이터 저장
		redisTemplate.opsForValue().set(key, value);

		// 데이터 조회
		String retrievedValue = (String) redisTemplate.opsForValue().get(key);
		System.out.println("Retrieved Value: " + retrievedValue);

		// 테스트 검증
		Assertions.assertEquals(value, retrievedValue);
	}
}

