package com.dingtalk.h5app.quickstart;

import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @Description springboot单元测试基类
 * @Author: spp
 * @Date: 2022/2/10
 * @Version 1.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,classes = {Application.class})
@TestPropertySource("classpath:application.properties")
public class BaseSpringBootTest {
}