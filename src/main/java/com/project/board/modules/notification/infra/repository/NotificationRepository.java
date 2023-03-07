package com.project.board.modules.notification.infra.repository;

import com.project.board.modules.notification.domain.entity.Notification;
import com.project.board.modules.account.domain.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * Interceptor에서 알림 정보를 조회해오는 기능
     *
     * 확인하지 않은 알림의 숫자를 가져오기 위한 메서드
     */
    long countByAccountAndChecked(Account account, boolean checked);

    @Transactional
    List<Notification> findByAccountAndCheckedOrderByCreatedDesc(Account account, boolean b);

    @Transactional
    void deleteByAccountAndChecked(Account account, boolean b);
}
