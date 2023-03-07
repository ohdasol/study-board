package com.project.board.modules.account.endpoint.controller.form;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

// 회원가입 정보를 전달할 폼 클래스
@Data
public class SignUpForm {
    @NotBlank // @Valid가 동작하려면 검증할 대상
    @Length(min = 3, max = 20) // 문자열 길이 검사
    @Pattern(regexp = "^[ㄱ-ㅎ가-힣a-z0-9_-]{3,20}$") // 문자열 패턴 검사, 한글, 영어, 숫자, 언더스코어, 하이픈 포함 정규표현식
    private String nickname;

    @Email // 이메일 포맷 확인
    @NotBlank
    private String email;

    @NotBlank
    @Length(min = 8, max = 50)
    private String password;
}
