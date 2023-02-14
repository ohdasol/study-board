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
 * */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
@AuthenticationPrincipal(expression = "#this == 'anonymousUser' ? null : account")
public @interface CurrentUser {

}
