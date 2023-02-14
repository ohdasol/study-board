package com.project.board.modules.account.endpoint.controller.validator;

import com.project.board.modules.account.endpoint.controller.form.NicknameForm;
import com.project.board.modules.account.domain.entity.Account;
import com.project.board.modules.account.infra.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
/**
 * @Component : 개발자가 직접 작성한 클래스를 Bean으로 만듦
 * @Bean : 개발자가 컨트롤이 불가능한 외부 라이브러리들 등록할 때 사용
 */
@Component
@RequiredArgsConstructor
public class NicknameFormValidator implements Validator {

    private final AccountRepository accountRepository;

    @Override
    public boolean supports(Class<?> clazz) {
        return NicknameForm.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        NicknameForm nicknameForm = (NicknameForm) target;
        Account account = accountRepository.findByNickname(nicknameForm.getNickname());
        if (account != null) { // 동일한 닉네임이 있는지 확인하여 에러 문구 전달
            errors.rejectValue("nickname", "wrong.value", "이미 사용중인 닉네임입니다.");
        }
    }
}
