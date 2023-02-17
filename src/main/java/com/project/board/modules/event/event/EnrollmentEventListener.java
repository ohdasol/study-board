package com.project.board.modules.event.event;

import com.project.board.infra.config.AppProperties;
import com.project.board.infra.mail.EmailMessage;
import com.project.board.infra.mail.EmailService;
import com.project.board.modules.account.domain.entity.Account;
import com.project.board.modules.event.domain.entity.Enrollment;
import com.project.board.modules.event.domain.entity.Event;
import com.project.board.modules.notification.domain.entity.Notification;
import com.project.board.modules.notification.domain.entity.NotificationType;
import com.project.board.modules.notification.infra.repository.NotificationRepository;
import com.project.board.modules.study.domain.entity.Study;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;

// 모임 관련 이벤트가 발생했을 때 이벤트를 처리해 줄 이벤트 리스너 구현
@Slf4j
@Async // 비동기 처리, 이 어노테이션을 사용하기 위해서는 @EnableAsync 어노테이션이 적용된 설정 클래스가 필요
@Component
@Transactional
@RequiredArgsConstructor
public class EnrollmentEventListener {

    private final NotificationRepository notificationRepository;
    private final EmailService emailService;
    private final AppProperties appProperties;
    private final TemplateEngine templateEngine;

    @EventListener
    public void handleEnrollmentEvent(EnrollmentEvent enrollmentEvent) {
        Enrollment enrollment = enrollmentEvent.getEnrollment();
        Account account = enrollment.getAccount();
        Event event = enrollment.getEvent();
        Study study = event.getStudy();
        if (account.getNotificationSetting().isStudyRegistrationResultByEmail()) {
            sendEmail(enrollmentEvent, account, event, study);
        }
        if (account.getNotificationSetting().isStudyRegistrationResultByWeb()) {
            createNotification(enrollmentEvent, account, event, study);
        }
    }

    private void sendEmail(EnrollmentEvent enrollmentEvent, Account account, Event event, Study study) {
        Context context = new Context();
        context.setVariable("nickname", account.getNickname());
        context.setVariable("link", "/study/" + study.getEncodedPath() + "/events/" + event.getId());
        context.setVariable("linkName", study.getTitle());
        context.setVariable("message", enrollmentEvent.getMessage());
        context.setVariable("host", appProperties.getHost());
        String message = templateEngine.process("mail/simple-link", context);
        EmailMessage emailMessage = EmailMessage.builder()
                .subject("[Study With Me] " + event.getTitle() + " 모임 참가 신청 결과입니다.")
                .to(account.getEmail())
                .message(message)
                .build();
        emailService.sendEmail(emailMessage);
    }

    private void createNotification(EnrollmentEvent enrollmentEvent, Account account, Event event, Study study) {
        notificationRepository.save(Notification.from(study.getTitle() + " / " + event.getTitle(),
                "/study/" + study.getEncodedPath() + "/events/" + event.getId(), false,
                LocalDateTime.now(), enrollmentEvent.getMessage(), account, NotificationType.EVENT_ENROLLMENT));
    }
}
