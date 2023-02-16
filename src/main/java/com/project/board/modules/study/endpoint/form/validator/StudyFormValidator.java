package com.project.board.modules.study.endpoint.form.validator;

import com.project.board.modules.study.endpoint.form.StudyForm;
import com.project.board.modules.study.infra.repository.StudyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

// StudyForm에서 검증할 수 없는 부분을 추가로 검증하기 위해 validator 생성
@Component
@RequiredArgsConstructor
public class StudyFormValidator implements Validator {
    private final StudyRepository studyRepository;

    @Override
    public boolean supports(Class<?> clazz) {
        return StudyForm.class.isAssignableFrom(clazz);
    }

    // 기존 경로와 중복 여부 확인
    @Override
    public void validate(Object target, Errors errors) {
        StudyForm studyForm = (StudyForm) target;
        if (studyRepository.existsByPath(studyForm.getPath())) {
            errors.rejectValue("path", "wrong.path", "이미 사용중인 스터디 경로입니다.");
        }
    }
}
