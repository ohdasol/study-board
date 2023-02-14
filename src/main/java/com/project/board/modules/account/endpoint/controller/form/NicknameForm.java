package com.project.board.modules.account.endpoint.controller.form;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

// 변경할 닉네임을 전달받을 클래스
@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NicknameForm {
    @NotBlank
    @Length(min = 3, max = 20)
    @Pattern(regexp = "^[ㄱ-ㅎ가-힣a-z0-9-]{3,20}$")
    private String nickname;

    // 생성자로 닉네임을 받아 초기화
    public NicknameForm(String nickname) {
        this.nickname = nickname;
    }
}
