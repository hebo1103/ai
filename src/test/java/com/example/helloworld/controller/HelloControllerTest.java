package com.example.helloworld.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * HelloController 集成测试
 * 测试用例 TC001: 调用 GET /api/hello 接口，期望返回 200 和 "hello world"
 */
@SpringBootTest
@AutoConfigureMockMvc
class HelloControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnHelloWorld_WhenGetApiHello() throws Exception {
        // given - 无需准备数据
        
        // when & then - 调用 GET /api/hello 并验证响应
        mockMvc.perform(get("/api/hello"))
                .andExpect(status().isOk())  // 期望 HTTP 200
                .andExpect(content().string("hello world"));  // 期望响应内容为 "hello world"
    }
}
