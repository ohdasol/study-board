package com.project.board.modules.account.domain;

import com.project.board.modules.account.domain.entity.Account;
import lombok.Getter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.List;

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