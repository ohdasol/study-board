package com.project.board.infra.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
/**
 * 알림을 처리하기 위한 인프라 설정
 *
 * 알림 처리를 위해 고려해야 할 사항
 *
 * 비동기 처리(특정 코드가 끝날때 까지 코드의 실행을 멈추지 않고 다음 코드를 먼저 실행하는 것)
 *  - 애플리케이션 메인 기능에 영향을 주어선 안 됨
 *      ex) 알림 처리시 에러 발생하여 rollback이 발생하여 기존 기능에 영향을 주는 경우
 *  - 응답 시간에 영향을 주면 안 됨
 *
 * 주요 로직에 영향을 주어선 안 됨
 *  - 알림 처리 로직 분리
 */
@Configuration
@EnableAsync // 비동기 처리를 위한 기본 설정 제공
@Slf4j
public class AsyncConfig implements AsyncConfigurer { // AsyncConfigurer를 구현하여 커스텀 설정 추가
    /**
     * 스레드 풀 ?
     * 스레드를 미리 생성하고, 작업 요청이 발생할 때 마다 미리 생성된 스레드로 해당 작업을 처리하는 방식
     * 작업이 끝난 스레드는 종료되지 않고 다음 작업 요청이 들어올 때까지 대기
     *
     * 사용이유 ? 애플리케이션 성능 저하 방지
     *
     * 기본적으로 Java에서 제공되는 Executor으로 스레드 풀을 직접 지정
     *
     * CorePoolSize, MaxPoolSize, QueueCapacity 세 가지를 고려
     *
     * 처리할 태스크(이벤트)가 생겼을 때
     * 현재 일하고 있는 쓰레드 개수(active thread)가 코어 개수(core pool size)보다 작으면 남아있는 쓰레드를 사용
     * 현재 일하고 있는 쓰레드 개수가 코어 개수만큼 차있으면 큐 용량(queue capacity)이 찰때까지 큐에 쌓아둠
     * 큐 용량이 다 차면, 코어 개수를 넘어서 맥스 개수(max pool size)에 다르기 전까지 새로운 쓰레드를 만들어 처리
     * 맥스 개수를 넘기면 태스크를 처리하지 못함
     */
    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        int processors = Runtime.getRuntime().availableProcessors();
        log.info("processor count {}", processors);
        executor.setCorePoolSize(processors);
        executor.setMaxPoolSize(processors * 2);
        executor.setQueueCapacity(50);
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("AsyncExecutor-");
        executor.initialize();
        return executor;
    }
}
