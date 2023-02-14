package com.project.board.modules.notification.application;

import com.project.board.modules.notification.domain.entity.Notification;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class NotificationService {

    // 읽지 않은 알림 확인하면 읽은 상태로 변경
    public void markAsRead(List<Notification> notifications) {
        notifications.forEach(Notification::read);
    }
}
