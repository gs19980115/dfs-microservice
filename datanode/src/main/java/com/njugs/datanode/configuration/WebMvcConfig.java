package com.njugs.datanode.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * 解决跨域问题
 * 网上抄的解决方案
 * 总觉得很暴力
 * 这个WebMvcConfigurerAdapter是啥子哟
 * 怎么还被杠掉了
 */
@Configuration
public class WebMvcConfig extends WebMvcConfigurerAdapter {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**").allowedHeaders("*")
                .allowedMethods("*")
                .allowedOrigins("*")
                .allowCredentials(true);
    }
}