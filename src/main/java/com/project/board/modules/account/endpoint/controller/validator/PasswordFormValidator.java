package com.project.board.modules.account.endpoint.controller.validator;

import com.project.board.modules.account.endpoint.controller.form.PasswordForm;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

// 비밀번호 검증 클래스
@Component
public class PasswordFormValidator implements Validator {
    @Override
    public boolean supports(Class<?> clazz) {
        return PasswordForm.class.isAssignableFrom(clazz);
    }

    /**
     * 위에서 PasswordForm 타입에 할당할 수 있는 타입만 받도록 하였기 때문에 target 객체는 PasswordForm으로 캐스팅 할 수 있음
     * 그 이후 새로운 비밀번호와 비밀번호 확인이 동일한지 체크하여 동일하지 않을 경우 에러 객체에 에러 문구를 전달
     */
    @Override
    public void validate(Object target, Errors errors) {
        PasswordForm passwordForm = (PasswordForm) target;
        if (!passwordForm.getNewPassword().equals(passwordForm.getNewPasswordConfirm())) {
            errors.rejectValue("newPassword", "wrong.value", "입력한 새 패스워드가 일치하지 않습니다.");
        }
    }
}
