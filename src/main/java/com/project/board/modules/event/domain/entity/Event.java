package com.project.board.modules.event.domain.entity;

import com.project.board.modules.account.domain.UserAccount;
import com.project.board.modules.account.domain.entity.Account;
import com.project.board.modules.event.endpoint.form.EventForm;
import com.project.board.modules.study.domain.entity.Study;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 모임(Event) 엔티티 설계
 *
 * Event는 Study, Account를 참조할 수 있는 단방향 연관관계
 * Enrollment와는 양방향 연관관계
 */
@NamedEntityGraph(
        name = "Event.withEnrollments",
        attributeNodes = @NamedAttributeNode("enrollments")
)
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@ToString
public class Event {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne // 기본 값을 사용하여 단방향 관계를 나타냄
    private Study study;

    @ManyToOne
    private Account createdBy;

    @Column(nullable = false)
    private String title;

    @Lob
    private String description;

    @Column(nullable = false)
    private LocalDateTime createdDateTime;

    @Column(nullable = false)
    private LocalDateTime endEnrollmentDateTime;

    @Column(nullable = false)
    private LocalDateTime startDateTime;

    @Column(nullable = false)
    private LocalDateTime endDateTime;

    private Integer limitOfEnrollments;

    @OneToMany(mappedBy = "event") @ToString.Exclude // 모임과 참가는 서로 양방향 연관관계를 가지고 있으므로 mappedBy를 이용하여 관계를 정의
    @OrderBy("enrolledAt") // 참석 리스트가 조회될 때 참석 날짜 기준으로 정렬, ORDER BY절과 같음
    private List<Enrollment> enrollments = new ArrayList<>(); // Collection 필드 초기화

    @Enumerated(EnumType.STRING)
    private EventType eventType;

    public static Event from(EventForm eventForm, Account account, Study study) {
        Event event = new Event();
        event.eventType = eventForm.getEventType();
        event.description = eventForm.getDescription();
        event.endDateTime = eventForm.getEndDateTime();
        event.endEnrollmentDateTime = eventForm.getEndEnrollmentDateTime();
        event.limitOfEnrollments = eventForm.getLimitOfEnrollments();
        event.startDateTime = eventForm.getStartDateTime();
        event.title = eventForm.getTitle();
        event.createdBy = account;
        event.study = study;
        event.createdDateTime = LocalDateTime.now();
        return event;
    }

    // 참석하지 않은 상태인지 확인
    public boolean isEnrollableFor(UserAccount userAccount) {
        return isNotClosed() && !isAttended(userAccount) && !isAlreadyEnrolled(userAccount);
    }

    // 참석하지 않은 상태인지 확인
    public boolean isDisenrollableFor(UserAccount userAccount) {
        return isNotClosed() && !isAttended(userAccount) && isAlreadyEnrolled(userAccount);
    }

    private boolean isNotClosed() {
        return this.endEnrollmentDateTime.isAfter(LocalDateTime.now());
    }

    // 모임 참석 완료 여부
    private boolean isAlreadyEnrolled(UserAccount userAccount) {
        Account account = userAccount.getAccount();
        for (Enrollment enrollment : this.enrollments) {
            if (enrollment.getAccount().equals(account)) {
                return true;
            }
        }
        return false;
    }

    public boolean isAttended(UserAccount userAccount) {
        Account account = userAccount.getAccount();
        for (Enrollment enrollment : this.enrollments) {
            if (enrollment.getAccount().equals(account) && enrollment.isAttended()) {
                return true;
            }
        }
        return false;
    }

    // 모임 자리 여부 확인 메서드
    public int numberOfRemainSpots() {
        int accepted = (int) this.enrollments.stream()
                .filter(Enrollment::isAccepted)
                .count();
        return this.limitOfEnrollments - accepted;
    }

    // 모임 참가 신청자 수 확인 메서드
    public Long getNumberOfAcceptedEnrollments() {
        return this.enrollments.stream()
                .filter(Enrollment::isAccepted)
                .count();
    }

    public void updateFrom(EventForm eventForm) {
        this.title = eventForm.getTitle();
        this.description = eventForm.getDescription();
        this.eventType = eventForm.getEventType();
        this.startDateTime = eventForm.getStartDateTime();
        this.endDateTime = eventForm.getEndDateTime();
        this.limitOfEnrollments = eventForm.getLimitOfEnrollments();
        this.endEnrollmentDateTime = eventForm.getEndEnrollmentDateTime();
    }

    // 모임 유형이 선착순이고 모임 정원이 참가 요청 수보다 큰지 확인
    public boolean isAbleToAcceptWaitingEnrollment() {
        return this.eventType == EventType.FCFS && this.limitOfEnrollments > this.getNumberOfAcceptedEnrollments();
    }

    // 모임에 참가 내역 추가
    public void addEnrollment(Enrollment enrollment) {
        this.enrollments.add(enrollment);
        enrollment.attach(this);
    }

    // 모임에서 참가 내역 삭제
    public void removeEnrollment(Enrollment enrollment) {
        this.enrollments.remove(enrollment);
        enrollment.detachEvent();
    }

    // 대기 중인 있는 참가 신청들이 수용 가능한지 확인하여 첫 번째 신청을 가져와 참가 신청 상태로 변경
    public void acceptNextIfAvailable() {
        if (this.isAbleToAcceptWaitingEnrollment()) {
            this.firstWaitingEnrollment().ifPresent(Enrollment::accept);
        }
    }

    // 참가 신청 중 신청 완료되지 않은 첫 번째 내역을 가져옴
    private Optional<Enrollment> firstWaitingEnrollment() {
        return this.enrollments.stream()
                .filter(e -> !e.isAccepted())
                .findFirst();
    }

    // 대기 중인 참가 신청 수용 가능 여부를 판단하여 참가 가능한 수만큼 대기 리스트의 상태를 참가 신청 완료 상태로 변경
    public void acceptWaitingList() {
        if (this.isAbleToAcceptWaitingEnrollment()) {
            List<Enrollment> waitingList = this.enrollments.stream()
                    .filter(e -> !e.isAccepted())
                    .collect(Collectors.toList());
            int numberToAccept = (int) Math.min(limitOfEnrollments - getNumberOfAcceptedEnrollments(), waitingList.size());
            waitingList.subList(0, numberToAccept).forEach(Enrollment::accept);
        }
    }

    // 관지자 확인 모임이고, 참가 신청 수락
    public void accept(Enrollment enrollment) {
        if (this.eventType == EventType.CONFIRMATIVE && this.limitOfEnrollments > this.getNumberOfAcceptedEnrollments()) {
            enrollment.accept();
        }
    }

    // 관리자 확인 모임이고, 참가 신청 거절
    public void reject(Enrollment enrollment) {
        if (this.eventType == EventType.CONFIRMATIVE) {
            enrollment.reject();
        }
    }

    // 수락 가능 확인
    public boolean isAcceptable(Enrollment enrollment) {
        return this.eventType == EventType.CONFIRMATIVE
                && this.enrollments.contains(enrollment)
                && this.limitOfEnrollments > this.getNumberOfAcceptedEnrollments()
                && !enrollment.isAttended()
                && !enrollment.isAccepted();
    }

    // 수락 거부 확인
    public boolean isRejectable(Enrollment enrollment) {
        return this.eventType == EventType.CONFIRMATIVE
                && this.enrollments.contains(enrollment)
                && !enrollment.isAttended()
                && enrollment.isAccepted();
    }
}