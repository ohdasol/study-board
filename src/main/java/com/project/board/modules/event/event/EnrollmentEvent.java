package com.project.board.modules.event.event;

import com.project.board.modules.event.domain.entity.Enrollment;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 모임 관련 변경 사항에 대한 알림 기능 구현
 * 
 * 알림이 발생해야 되는 상황은 다음과 같음
 * 스터디 수정 추가 알림
 *  - 새 모임 추가
 *  - 모임 변경
 *  - 모임 취소
 *  
 * 모임 참가 신청
 *  - 참가 신청 수락
 *  - 참가 신청 거절
 *  
 *  
 *  모임 관련 이벤트 클래스
 *  공통적으로 참가 정보와 메시지를 가지므로 상속을 통해 간단하게 구현
 *  부모 클래스 EnrollmentEvent 작성
 *
 *  모임 참가 수락과 거절에 관련된 이벤트를 EnrollmentEvent를 상속
 *   - EnrollmentAcceptedEvent, EnrollmentRejectedEvent
 */
@Getter
@RequiredArgsConstructor
public class EnrollmentEvent {
    protected final Enrollment enrollment;
    protected final String message;
}
