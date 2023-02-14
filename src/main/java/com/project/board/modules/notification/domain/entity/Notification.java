package com.project.board.modules.notification.domain.entity;

import com.project.board.modules.account.domain.entity.Account;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;
/**
 * 알림 도메인 설계
 *
 * Notification은 Account와 단방향 ManyToOne 관계
 *
 * 알림 속성
 *
 * 제목
 * 링크
 * 짧은 메시지
 * 확인 여부
 * 사용자
 * 시간
 * 알림 타입 (NotificationType) - 새 스터디, 참여 중인 스터디, 모임 참가 신청 결과
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification {
    @Id @GeneratedValue
    private Long id;

    private String title;

    private String link;

    private String message;

    private boolean checked;

    @ManyToOne
    private Account account;

    private LocalDateTime created;

    @Enumerated(EnumType.STRING)
    private NotificationType notificationType;

    // 알림 내역을 저장할 때 static 생성자를 이용해 엔티티 생성
    public static Notification from(String title, String link, boolean checked, LocalDateTime created, String message, Account account, NotificationType notificationType) {
        Notification notification = new Notification();
        notification.title = title;
        notification.link = link;
        notification.checked = checked;
        notification.created = created;
        notification.message = message;
        notification.account = account;
        notification.notificationType = notificationType;
        return notification;
    }

    // 읽지 않은 알림 리스트를 받아 엔티티에서 읽은 상태로 변경하도록 위임
    public void read() {
        this.checked = true;
    }
}
