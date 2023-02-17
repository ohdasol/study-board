package com.project.board.modules.event.endpoint;

import com.project.board.modules.account.domain.entity.Account;
import com.project.board.modules.account.support.CurrentUser;
import com.project.board.modules.event.application.EventService;
import com.project.board.modules.event.domain.entity.Enrollment;
import com.project.board.modules.event.domain.entity.Event;
import com.project.board.modules.event.endpoint.form.EventForm;
import com.project.board.modules.event.validator.EventValidator;
import com.project.board.modules.study.application.StudyService;
import com.project.board.modules.study.domain.entity.Study;
import com.project.board.modules.study.infra.repository.StudyRepository;
import com.project.board.modules.event.infra.repository.EnrollmentRepository;
import com.project.board.modules.event.infra.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/study/{path}")
@RequiredArgsConstructor
public class EventController {

    private final StudyService studyService;
    private final EventService eventService;
    private final EventRepository eventRepository;
    private final StudyRepository studyRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final EventValidator eventValidator;

    @InitBinder("eventForm")
    public void initBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(eventValidator);
    }

    // 모임 뷰 라우팅
    @GetMapping("/new-event")
    public String newEventForm(@CurrentUser Account account, @PathVariable String path, Model model) {
        Study study = studyService.getStudyToUpdateStatus(account, path);
        model.addAttribute(study);
        model.addAttribute(account);
        model.addAttribute(new EventForm());
        return "event/form";
    }

    // 모임 생성
    @PostMapping("/new-event")
    public String createNewEvent(@CurrentUser Account account, @PathVariable String path, @Valid EventForm eventForm, Errors errors, Model model) {
        Study study = studyService.getStudyToUpdateStatus(account, path);
        if (errors.hasErrors()) {
            model.addAttribute(account);
            model.addAttribute(study);
            return "event/form";
        }
        Event event = eventService.createEvent(study, eventForm, account);
        return "redirect:/study/" + study.getEncodedPath() + "/events/" + event.getId(); // 성공했을 때 event id를 이용해 리다이렉트
    }

    // 모임 조회 화면
    @GetMapping("/events/{id}")
    public String getEvent(@CurrentUser Account account, @PathVariable String path, @PathVariable("id") Event event, Model model) {
        model.addAttribute(account);
        model.addAttribute(event);
        model.addAttribute(studyRepository.findStudyWithManagersByPath(path));
        return "event/view";
    }

    // 모임 목록 조회 화면
    @GetMapping("/events")
    public String viewStudyEvents(@CurrentUser Account account, @PathVariable String path, Model model) {
        Study study = studyService.getStudy(path);
        model.addAttribute(account);
        model.addAttribute(study);
        List<Event> events = eventRepository.findByStudyOrderByStartDateTime(study);
        List<Event> newEvents = new ArrayList<>();
        List<Event> oldEvents = new ArrayList<>();
        for (Event event : events) {
            if (event.getEndDateTime().isBefore(LocalDateTime.now())) {
                oldEvents.add(event);
            } else {
                newEvents.add(event);
            }
        }
        model.addAttribute("newEvents", newEvents);
        model.addAttribute("oldEvents", oldEvents);
        return "study/events";
    }

    // 모임 수정 화면 
    @GetMapping("/events/{id}/edit")
    public String updateEventForm(@CurrentUser Account account, @PathVariable String path, @PathVariable("id") Event event, Model model) {
        model.addAttribute(studyService.getStudyToUpdate(account, path));
        model.addAttribute(account);
        model.addAttribute(event);
        model.addAttribute(EventForm.from(event));
        return "event/update-form";
    }

    /**
     * 모임 수정 처리
     *
     * 모임 수정 로직 보완
     *  - 선착순 모임 수정 시 모집 인원이 늘어나도록 수정하였으므로 대기 중인 참가 신청이 있을 때 가능한 만큼 신청을 확정 상태로 변경하는 기능 추가
     */
    @PostMapping("/events/{id}/edit")
    public String updateEventSubmit(@CurrentUser Account account, @PathVariable String path, @PathVariable("id") Event event, @Valid EventForm eventForm, Errors errors, Model model) {
        Study study = studyService.getStudyToUpdate(account, path);
        eventForm.setEventType(event.getEventType());
        eventValidator.validateUpdateForm(eventForm, event, errors);
        if (errors.hasErrors()) {
            model.addAttribute(account);
            model.addAttribute(study);
            model.addAttribute(event);
            return "event/update-form";
        }
        eventService.updateEvent(event, eventForm);
        return "redirect:/study/" + study.getEncodedPath() + "/events/" + event.getId();
    }

    // 모임 삭제
    @DeleteMapping("/events/{id}")
    public String deleteEvent(@CurrentUser Account account, @PathVariable String path, @PathVariable("id") Event event) {
        Study study = studyService.getStudyToUpdateStatus(account, path);
        eventService.deleteEvent(event);
        return "redirect:/study/" + study.getEncodedPath() + "/events";
    }

    /**
     * 기능 구현을 위해 고려해야 할 사항들은 다음과 같음
     *
     * 모임 참가 신청 및 취소시 스터디 조회
     *  - 조회하는 스터디의 경우 관리자 권한 없이 읽어올 수 있어야 하므로 데이터를 필요한 만큼만 조회
     *
     * 모임 참가 신청
     *  - 선착순 모임의 경우 참가 신청이 가능한지 여부 판별 필요
     *  - 가능할 경우 상태 변경
     *
     * 모임 탈퇴
     *  - 선착순 모임이라면 탈퇴 이후 대기중인 모임 참가 신청 중 가장 빨리 신청한 것을 확정 상태로 변경
     */
    // 선착순 - 모임 참가 신청
    @PostMapping("/events/{id}/enroll")
    public String enroll(@CurrentUser Account account, @PathVariable String path, @PathVariable("id") Event event) {
        Study study = studyService.getStudyToEnroll(path);
        eventService.enroll(event, account);
        return "redirect:/study/" + study.getEncodedPath() + "/events/" + event.getId();
    }

    // 선착순 - 모임 참가 취소
    @PostMapping("/events/{id}/leave")
    public String leave(@CurrentUser Account account, @PathVariable String path, @PathVariable("id") Event event) {
        Study study = studyService.getStudyToEnroll(path);
        eventService.leave(event, account);
        return "redirect:/study/" + study.getEncodedPath() + "/events/" + event.getId();
    }

    // 관리자 확인 - 모임 참가 신청 수락
    @GetMapping("/events/{eventId}/enrollments/{enrollmentId}/accept")
    public String acceptEnrollment(@CurrentUser Account account, @PathVariable String path, @PathVariable("eventId") Event event, @PathVariable("enrollmentId") Enrollment enrollment) {
        Study study = studyService.getStudyToUpdate(account, path);
        eventService.acceptEnrollment(event, enrollment);
        return "redirect:/study/" + study.getEncodedPath() + "/events/" + event.getId();
    }

    // 관리자 확인 - 모임 참가 신청 거부
    @GetMapping("/events/{eventId}/enrollments/{enrollmentId}/reject")
    public String rejectEnrollment(@CurrentUser Account account, @PathVariable String path, @PathVariable("eventId") Event event, @PathVariable("enrollmentId") Enrollment enrollment) {
        Study study = studyService.getStudyToUpdate(account, path);
        eventService.rejectEnrollment(event, enrollment);
        return "redirect:/study/" + study.getEncodedPath() + "/events/" + event.getId();
    }

    // 출석 체크 확인
    @GetMapping("/events/{eventId}/enrollments/{enrollmentId}/checkin")
    public String checkInEnrollment(@CurrentUser Account account, @PathVariable String path, @PathVariable("eventId") Event event, @PathVariable("enrollmentId") Enrollment enrollment) {
        Study study = studyService.getStudyToUpdate(account, path);
        eventService.checkInEnrollment(event, enrollment);
        return "redirect:/study/" + study.getEncodedPath() + "/events/" + event.getId();
    }

    // 출석 체크 취소
    @GetMapping("/events/{eventId}/enrollments/{enrollmentId}/cancel-checkin")
    public String cancelCheckInEnrollment(@CurrentUser Account account, @PathVariable String path, @PathVariable("eventId") Event event, @PathVariable("enrollmentId") Enrollment enrollment) {
        Study study = studyService.getStudyToUpdate(account, path);
        eventService.cancelCheckinEnrollment(event, enrollment);
        return "redirect:/study/" + study.getEncodedPath() + "/events/" + event.getId();
    }
}