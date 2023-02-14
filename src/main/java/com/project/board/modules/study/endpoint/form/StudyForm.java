package com.project.board.modules.study.endpoint.form;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

// 스터디 정보 주고 받을 폼 클래스
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudyForm {

    // StudyService에서 스터디 경로의 유효성을 확인하기 위한 부분에서 사용했던 정규표현식 패턴을 상수로 변경해 외부에서 접근할 수 있게 변경
    public static final String VALID_PATH_PATTERN = "^[ㄱ-ㅎ가-힣a-z0-9_-]{2,20}$";

    @NotBlank
    @Length(min = 2, max = 20)
    @Pattern(regexp = VALID_PATH_PATTERN)
    private String path;

    @NotBlank
    @Length(max = 50)
    private String title;

    @NotBlank
    @Length(max = 100)
    private String shortDescription;

    @NotBlank
    private String fullDescription;
}
