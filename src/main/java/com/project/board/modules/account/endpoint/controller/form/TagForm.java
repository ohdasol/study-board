package com.project.board.modules.account.endpoint.controller.form;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// Tag 정보를 주고 받을 폼 클래스
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TagForm {
    private String tagTitle;
}
