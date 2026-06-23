package com.workfootprint.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.workfootprint.mapper")
public class MybatisPlusConfig {
}

