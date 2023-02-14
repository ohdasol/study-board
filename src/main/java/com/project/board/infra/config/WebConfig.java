package com.project.board.infra.config;

import com.project.board.modules.notification.infra.interceptor.NotificationInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.security.StaticResourceLocation;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
/**
 * Interceptor를 등록해 주기 위한 WebMvc 설정
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {
    private final NotificationInterceptor notificationInterceptor;

    /**
     * Interceptor에서 redirect를 제외했으므로 static location만 추가로 제외해 주면 됨
     * Interceptor를 등록하면서 excludePathPattern을 이용해 static location일 때 Interceptor가 동작하지 않게
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        List<String> staticResourcesPath = Stream.of(StaticResourceLocation.values())
                .flatMap(StaticResourceLocation::getPatterns)
                .collect(Collectors.toList());
        staticResourcesPath.add("/node_modules/**");
        registry.addInterceptor(notificationInterceptor)
                .excludePathPatterns(staticResourcesPath);
    }
}
