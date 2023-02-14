package com.project.board.modules.main.endpoint.controller;

import com.project.board.modules.account.domain.entity.Account;
import com.project.board.modules.account.support.CurrentUser;

import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * 예외 상황을 처리하여 error 페이지로 리다이렉트 시키는 핸들러 구현
 *
 * 없는 스터디 페이지 조회 시도
 * 없는 프로필 페이지 조회 시도
 * 무작위 이벤트 조회 시도
 * 허용하지 않는 요청 시도
 *  - 이미 종료된 스터디의 모임 생성 시도
 *  - 이미 종료된 모임에 참가 신청 시도
 *  - 관리자 권한이 없는 스터디 수정 시도
 *  - 기타 등등
 */

/**
 * @ControllerAdvice : 모든 @Controller에 대해 예외를 잡아 처리해주는 어노테이션
 * @ExceptionHandler : @Controller, @RestController가 적용된 Bean에서 발생하는 예외를 잡아서 하나의 메서드에서 처리해 주는 기능
 *
 */
@ControllerAdvice
@Slf4j
public class ExceptionAdvice {

  @ExceptionHandler()
  public String handleRuntimeException(@CurrentUser Account account, HttpServletRequest request,
                                       RuntimeException exception) {
    log.info(getNicknameIfExists(account) + "requested {}", request.getRequestURI());
    log.error("bad request", exception);
    return "error";
  }

  private String getNicknameIfExists(Account account) {
    return Optional.ofNullable(account)
        .map(Account::getNickname)
        .map(s -> s + " ")
        .orElse("");
  }
}
