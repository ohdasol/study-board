package com.project.board.modules.study.event;

import com.project.board.modules.study.domain.entity.Study;
import lombok.Getter;
/**
 * 스터디 생성시 발생시킬 이벤트
 * 알림 인프라가 잘 구축되었는지 확인하는 차원이므로 간단하게 구현
 */
@Getter
public class StudyCreatedEvent {

    private final Study study;

    public StudyCreatedEvent(Study study) {
        this.study = study;
    }
}
