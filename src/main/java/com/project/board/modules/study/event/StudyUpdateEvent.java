package com.project.board.modules.study.event;

import com.project.board.modules.study.domain.entity.Study;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

// 스터디 수정시 발생시킬 이벤트
@RequiredArgsConstructor
@Getter // 이벤트 처리시 사용
public class StudyUpdateEvent {
    private final Study study;
    private final String message;
}
