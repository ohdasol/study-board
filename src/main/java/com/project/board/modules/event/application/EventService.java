package com.project.board.modules.event.application;

import com.project.board.modules.account.domain.entity.Account;
import com.project.board.modules.event.endpoint.form.EventForm;
import com.project.board.modules.study.domain.entity.Study;
import com.project.board.modules.study.event.StudyUpdateEvent;
import com.project.board.modules.event.domain.entity.Enrollment;
import com.project.board.modules.event.domain.entity.Event;
import com.project.board.modules.event.event.EnrollmentAcceptedEvent;
import com.project.board.modules.event.event.EnrollmentRejectedEvent;
import com.project.board.modules.event.infra.repository.EnrollmentRepository;
import com.project.board.modules.event.infra.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 모임 생성과 관련된 이벤트는 StudyEventListener에서 처리하도록 StudyUpdateEvent로 처리
     */
    public Event createEvent(Study study, EventForm eventForm, Account account) {
        Event event = Event.from(eventForm, account, study);
        eventPublisher.publishEvent(new StudyUpdateEvent(event.getStudy(), "'" + event.getTitle() + "' 모임이 생성되었습니다."));
        return eventRepository.save(event);
    }

    public void updateEvent(Event event, EventForm eventForm) {
        event.updateFrom(eventForm);
        event.acceptWaitingList(); // 모임 인원 수정시에도 반영될 수 있게 대기 목록에 있는 사용자들을 추가시킴
        eventPublisher.publishEvent(new StudyUpdateEvent(event.getStudy(), "'" + event.getTitle() + "' 모임 정보가 수정되었습니다."));
    }

    public void deleteEvent(Event event) {
        eventRepository.delete(event);
        eventPublisher.publishEvent(new StudyUpdateEvent(event.getStudy(), "'" + event.getTitle() + "' 모임이 취소되었습니다."));
    }

    public void enroll(Event event, Account account) {
        if (!enrollmentRepository.existsByEventAndAccount(event, account)) { // 모임에 해당 계정이 참가한 내역 여부 확인
            Enrollment enrollment = Enrollment.of(LocalDateTime.now(), event.isAbleToAcceptWaitingEnrollment(), account); // 참가 내역이 없으므로 참가 정보를 생성
            event.addEnrollment(enrollment); // 모임에 참가 정보 등록
            enrollmentRepository.save(enrollment); // 참가 정보 저장
        }
    }

    public void leave(Event event, Account account) {
        Enrollment enrollment = enrollmentRepository.findByEventAndAccount(event, account); // 참가 내역 조회
        if (!enrollment.isAttended()) {
            event.removeEnrollment(enrollment); // 모임에서 참가 내역 삭제
            enrollmentRepository.delete(enrollment); // 참가 정보 삭제
            event.acceptNextIfAvailable(); // 모임에서 다음 대기자를 참가 상태로 변경
        }
    }

    /**
     * 참가 관련 이벤트는 EnrollmentEvent를 전달하도록 함
     */
    public void acceptEnrollment(Event event, Enrollment enrollment) {
        event.accept(enrollment);
        eventPublisher.publishEvent(new EnrollmentAcceptedEvent(enrollment));
    }

    public void rejectEnrollment(Event event, Enrollment enrollment) {
        event.reject(enrollment);
        eventPublisher.publishEvent(new EnrollmentRejectedEvent(enrollment));
    }

    public void checkInEnrollment(Event event, Enrollment enrollment) {
        enrollment.attend();
    }

    public void cancelCheckinEnrollment(Event event, Enrollment enrollment) {
        enrollment.absent();
    }
}
