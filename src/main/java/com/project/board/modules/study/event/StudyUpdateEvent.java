package com.project.board.modules.study.event;

import com.project.board.modules.study.domain.entity.Study;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
/**
 * 스터디 변경시 알림 기능 구현
 *
 * 스터디 공개 때 알림을 보내는 것과 마찬가지로 특정 시점에 이벤트를 발생시키는 방법으로 구현
 *
 * 알림을 전송하는 시점
 * - 스터디 소개를 업데이트 했을 때
 * - 스터디가 종료되었을 때
 * - 스터디 팀원을 모집할 때, 모집이 종료 되었을 때
 */
// 스터디 수정시 발생시킬 이벤트
@RequiredArgsConstructor
@Getter // 이벤트 처리시 사용
public class StudyUpdateEvent {
    private final Study study;
    private final String message;
}
