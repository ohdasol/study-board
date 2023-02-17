package com.project.board.modules.event.event;

import com.project.board.modules.event.domain.entity.Enrollment;

// 모임 참가 거절 이벤트
public class EnrollmentRejectedEvent extends EnrollmentEvent{
    public EnrollmentRejectedEvent(Enrollment enrollment) {
        super(enrollment, "모임 참가 신청이 거절되었습니다.");
    }
}
