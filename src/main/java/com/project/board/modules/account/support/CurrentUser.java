package com.project.board.modules.account.support;

import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 스프링 시큐리티 기능을 활용하여 현재 인증된 사용자 정보를 참조
 * 런타임시 유지
 * 파라미터에 사용할 수 있어야 함
 * 인증 정보가 없으면 null, 있으면 account 반환
 *
 * @AuthenticationPrincipal : 로그인한 사용자 정보를 받아 사용할 수 있음
 * Authentication 객체의 getPrincipal()를 가져오기 위해 사용
 *
 * @AuthenticationPrincipal은 SpEL을 사용해서 Principal 내부 정보에 접근할 수도 있음
 * expression = "#this == 'anonymousUser' ? null : account"
 * => 인증을 하지 않은 상태라면 anonymousUser라는 문자열이 리턴되고 인증이 되었다면 인증된 계정의 객체를 반환
 *    익명 인증인 경우에는 null로 설정하고, 아닌 경우에는 account 프로퍼티를 조회해서 설정
 *
 * Custom Annotation을 생성하는 방법
 * Principal은 Authentication 객체를 생성할 때 필요한 첫 번째 파라미터로 사용자의 인증 정보를 담고 있음
 *
 *  @CurrentUser : Principal 객체에 담겨 있는 UserAccount의 account를 반환
 * */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
@AuthenticationPrincipal(expression = "#this == 'anonymousUser' ? null : account")
public @interface CurrentUser {

}
