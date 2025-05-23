package com.travelapp.backend.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")  // 모든 경로에 대해
                .allowedOrigins("http://localhost:3000")  // 프론트엔드 주소
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")  // 허용할 HTTP 메소드
                .allowedHeaders("*")  // 모든 헤더 허용
                .allowCredentials(true)  // 인증 정보 허용
                .maxAge(3600);  // 1시간 동안 pre-flight 요청 캐싱
    }
    
    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        ObjectMapper objectMapper = Jackson2ObjectMapperBuilder.json()
                .modules(new JavaTimeModule())
                .serializers(new LocalTimeSerializer(TIME_FORMATTER))
                .deserializers(new LocalTimeDeserializer(TIME_FORMATTER))
                .build();
                
        converters.add(new MappingJackson2HttpMessageConverter(objectMapper));
    }
} 