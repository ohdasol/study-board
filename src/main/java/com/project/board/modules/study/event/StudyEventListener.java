package com.project.board.modules.study.event;

import com.project.board.infra.config.AppProperties;
import com.project.board.infra.mail.EmailMessage;
import com.project.board.infra.mail.EmailService;
import com.project.board.modules.account.domain.entity.Account;
import com.project.board.modules.account.infra.predicates.AccountPredicates;
import com.project.board.modules.account.infra.repository.AccountRepository;
import com.project.board.modules.notification.domain.entity.Notification;
import com.project.board.modules.notification.domain.entity.NotificationType;
import com.project.board.modules.notification.infra.repository.NotificationRepository;
import com.project.board.modules.study.domain.entity.Study;
import com.project.board.modules.study.infra.repository.StudyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
/**
 * 스터디 생성 후 공개할 때 알림을 전송
 *
 * 알림 받을 대상 : 스터디 주제와 지역에 매칭되는 사용자
 * 알림 제목 : 스터디 이름
 * 알림 메시지 : 스터디 짧은 소개
 */
@Slf4j
@Async
@Transactional
@Component
@RequiredArgsConstructor
public class StudyEventListener {

    private final StudyRepository studyRepository;
    private final AccountRepository accountRepository;
    private final NotificationRepository notificationRepository;
    private final EmailService emailService;
    private final TemplateEngine templateEngine;
    private final AppProperties appProperties;

    /**
     * Iterable
     * - 반복할 수 있는 데이터 구조, 인터페이스에서 Iterator를 반환하는 iterator()가 메소드로 선언
     * Iterator
     * - Collection에 저장된 요소를 읽어오는 것을 표준화한 인터페이스로 데이터를 하나씩 읽어올 때 사용
     * - 계층구조 List, Set, Queue를 구현하는 클래스들은 iterator 메서드를 가지고 있음
     *
     * Collection
     *  - 자바에서 제공하는 자료구조들의 인터페이스로 List, ArrayList, Stack, Quque, LinkedList 등이 이를 상속받고 있음,  Iterable이 Collection의 상위 인터페이스
     */
    @EventListener // 이벤트 리스너 명시
    public void handleStudyCreatedEvent(StudyCreatedEvent studyCreatedEvent) { // EventPublisher를 통해 이벤트가 발생될 때 전달한 파라미터가 StudyCreatedEvent일 때 해당 메서드가 호출
        Study study = studyRepository.findStudyWithTagsAndZonesById(studyCreatedEvent.getStudy().getId()); // 관심사와 지역 정보를 추가로 조회
        Iterable<Account> accounts = accountRepository.findAll(AccountPredicates.findByTagsAndZones(study.getTags(), study.getZones())); // 관심사와 지역정보에 해당하는 모든 계정 찾음, querydsl 기능 사용
        for (Account account : accounts) { // 계정을 순차적으로 탐색하면서 메일 알림 설정을 한 계정에는 메일을 전송하고, 웹 알림 설정을 한 계정은 웹 알림을 저장
            Account.NotificationSetting notificationSetting = account.getNotificationSetting();
            if (notificationSetting.isStudyCreatedByEmail()) {
                sendEmail(study, account, "새로운 스터디가 오픈하였습니다.", "[Study With Me] " + study.getTitle() + " 스터디가 오픈하였습니다.");
            }
            if (notificationSetting.isStudyCreatedByWeb()) {
                saveNotification(study, account, NotificationType.STUDY_CREATED, study.getShortDescription());
            }
        }
    }

    // 스터디 수정에 대한 이벤트 처리
    @EventListener
    public void handleStudyUpdateEvent(StudyUpdateEvent studyUpdateEvent) {
        Study study = studyRepository.findStudyWithManagersAndMembersById(studyUpdateEvent.getStudy().getId());
        Set<Account> accounts = new HashSet<>();
        accounts.addAll(study.getManagers());
        accounts.addAll(study.getMembers());
        accounts.forEach(account -> {
            if (account.getNotificationSetting().isStudyUpdatedByEmail()) {
                sendEmail(study, account, studyUpdateEvent.getMessage(), "[Study With Me] " + study.getTitle() + " 스터디에 새소식이 있습니다.");
            }
            if (account.getNotificationSetting().isStudyUpdatedByWeb()) {
                saveNotification(study, account, NotificationType.STUDY_UPDATED, studyUpdateEvent.getMessage());
            }
        });
    }

    private void sendEmail(Study study, Account account, String contextMessage, String emailSubject) {
        Context context = new Context();
        context.setVariable("link", "/study/" + study.getEncodedPath());
        context.setVariable("nickname", account.getNickname());
        context.setVariable("linkName", study.getTitle());
        context.setVariable("message", contextMessage);
        context.setVariable("host", appProperties.getHost());
        String message = templateEngine.process("mail/simple-link", context);
        emailService.sendEmail(EmailMessage.builder()
                .to(account.getEmail())
                .subject(emailSubject)
                .message(message)
                .build());
    }

    private void saveNotification(Study study, Account account, NotificationType notificationType, String message) {
        notificationRepository.save(Notification.from(study.getTitle(), "/study/" + study.getEncodedPath(),
                false, LocalDateTime.now(), message, account, notificationType));
    }
}