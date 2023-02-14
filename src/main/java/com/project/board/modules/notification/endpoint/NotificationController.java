package com.project.board.modules.notification.endpoint;

import com.project.board.modules.notification.application.NotificationService;
import com.project.board.modules.notification.domain.entity.Notification;
import com.project.board.modules.account.domain.entity.Account;
import com.project.board.modules.account.support.CurrentUser;
import com.project.board.modules.notification.infra.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationRepository notificationRepository;
    private final NotificationService notificationService;

    /**
     * 알림 목록을 조회하고 삭제하는 기능 구현
     *
     * 알림 버튼을 클릭했을 때 읽지 않은 알림을 보여주고 읽은 알림도 확인하거나 삭제할 수 있음
     */
    @GetMapping("/notifications")
    public String getNotifications(@CurrentUser Account account, Model model) {
        List<Notification> notifications = notificationRepository.findByAccountAndCheckedOrderByCreatedDesc(account, false);
        long numberOfChecked = notificationRepository.countByAccountAndChecked(account, true);
        putCategorizedNotifications(model, notifications, numberOfChecked, notifications.size());
        model.addAttribute("isNew", true);
        notificationService.markAsRead(notifications);
        return "notification/list";
    }

    // 읽은 알림 조회
    @GetMapping("/notifications/old")
    public String getOldNotifications(@CurrentUser Account account, Model model) {
        List<Notification> notifications = notificationRepository.findByAccountAndCheckedOrderByCreatedDesc(account, true);
        long numberOfNotChecked = notificationRepository.countByAccountAndChecked(account, false);
        putCategorizedNotifications(model, notifications, notifications.size(), numberOfNotChecked);
        model.addAttribute("isNew", false);
        return "notification/list";
    }

    @DeleteMapping("/notifications")
    public String deleteNotifications(@CurrentUser Account account) {
        notificationRepository.deleteByAccountAndChecked(account, true);
        return "redirect:/notifications";
    }

    private void putCategorizedNotifications(Model model, List<Notification> notifications, long numberOfChecked, long numberOfNotChecked) {
        ArrayList<Notification> newStudyNotifications = new ArrayList<>();
        ArrayList<Notification> eventEnrollmentNotifications = new ArrayList<>();
        ArrayList<Notification> watchingStudyNotifications = new ArrayList<>();
        for (Notification notification : notifications) {
            switch (notification.getNotificationType()) {
                case STUDY_CREATED: {
                    newStudyNotifications.add(notification);
                    break;
                }
                case EVENT_ENROLLMENT: {
                    eventEnrollmentNotifications.add(notification);
                    break;
                }
                case STUDY_UPDATED: {
                    watchingStudyNotifications.add(notification);
                    break;
                }
            }
        }
        model.addAttribute("numberOfNotChecked", numberOfNotChecked);
        model.addAttribute("numberOfChecked", numberOfChecked);
        model.addAttribute("notifications", notifications);
        model.addAttribute("newStudyNotifications", newStudyNotifications);
        model.addAttribute("eventEnrollmentNotifications", eventEnrollmentNotifications);
        model.addAttribute("watchingStudyNotifications", watchingStudyNotifications);
    }

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
}
