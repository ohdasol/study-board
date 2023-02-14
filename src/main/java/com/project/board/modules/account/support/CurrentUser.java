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
 * @AuthenticationPrincipal : Authentication 객체의 getPrincipal()를 가져오기 위해 사용
 * Custom Annotation을 생성하는 방법
 * Principal은 Authentication 객체를 생성할 때 필요한 첫 번째 파라미터로 사용자의 인증 정보를 담고 있음
 * */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
@AuthenticationPrincipal(expression = "#this == 'anonymousUser' ? null : account")
public @interface CurrentUser {

}
