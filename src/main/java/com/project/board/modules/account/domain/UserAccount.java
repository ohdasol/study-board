package com.project.board.modules.account.domain;

import com.project.board.modules.account.domain.entity.Account;
import lombok.Getter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.List;

/**
 * Spring Security 동작 원리
 *
 * Security는 "/login" 주소로 요청이 오면 가로채 로그인 진행
 * 로그인 진행이 완료되면 Security Session을 만들고 SecurityContextHolder(시큐리티 메모리 세션 저장소)에 저장
 * Session에 저장될 수 있는 Object는 Authentication 타입
 * Authentication(인증) 타입 안에  User 객체를 저장
 * User는 UserDetails 타입과 같음
 *
 * UserDetails : 사용자의 정보를 담는 인터페이스
 */
public class UserAccount extends User {

    /**
     * Principal에서 Account를 가져오기 위해선 중간 adaptor 역할을 하는 객체가 필요
     * @CurrentUser 어노테이션 account 반환했기 때문에 변수 이름을 반드시 동일
     *  User 객체를 생성하기 위해선 Account에서 객체 username, password, authorities를 가져옴
     */
    @Getter
    private final Account account;

    public UserAccount(Account account) {
        super(account.getNickname(), account.getPassword(), List.of(new SimpleGrantedAuthority("ROLE_USER")));
        this.account = account;
    }
}