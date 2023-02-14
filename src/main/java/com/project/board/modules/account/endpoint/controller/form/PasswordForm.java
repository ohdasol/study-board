package com.project.board.modules.account.endpoint.controller.form;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

// 변경할 비밀번호를 전달받을 클래스
@Data
@NoArgsConstructor
public class PasswordForm {
    @Length(min = 8, max = 50)
    private String newPassword; // 새로운 비밀번호
    @Length(min = 8, max = 50)
    private String newPasswordConfirm; // 비밀번호 확인
}
