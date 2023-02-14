package com.project.board.modules.account.endpoint.controller.validator;

import com.project.board.modules.account.endpoint.controller.form.SignUpForm;
import com.project.board.modules.account.infra.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component // 빈 생성
@RequiredArgsConstructor
public class SignUpFormValidator implements Validator {

    private final AccountRepository accountRepository;

    /**
     * SignUpForm 클래스일 때 검증을 수행하기 위해 타입 지정
     * */
    @Override
    public boolean supports(Class<?> clazz) {
        return clazz.isAssignableFrom(SignUpForm.class);
    }

    /**
     * target으로 해당 객체 넘겨줄 것
     * 검증할 타입으로 가져오고 Repository에서 중복 여부 체크
     * 중복일 경우 에러 반환
     */
    @Override
    public void validate(Object target, Errors errors) {
        SignUpForm signUpForm = (SignUpForm) target;
        if (accountRepository.existsByEmail(signUpForm.getEmail())) {
            errors.rejectValue("email", "invalid.email", new Object[]{signUpForm.getEmail()},
                    "이미 사용중인 이메일입니다.");
        }
        if (accountRepository.existsByNickname(signUpForm.getNickname())) {
            errors.rejectValue("nickname", "invalid.nickname", new Object[]{signUpForm.getEmail()},
                    "이미 사용중인 닉네임입니다.");
        }
    }
}
