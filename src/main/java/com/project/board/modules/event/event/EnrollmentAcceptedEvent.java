package com.project.board.modules.event.event;

import com.project.board.modules.event.domain.entity.Enrollment;

// 모임 참가 수락 이벤트
public class EnrollmentAcceptedEvent extends EnrollmentEvent{
    public EnrollmentAcceptedEvent(Enrollment enrollment) {
        super(enrollment, "모임 참가 신청을 확인했습니다. 모임에 참석하세요.");
    }
}
