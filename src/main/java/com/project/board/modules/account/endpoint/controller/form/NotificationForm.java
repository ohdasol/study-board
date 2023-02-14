package com.project.board.modules.account.endpoint.controller.form;

import com.project.board.modules.account.domain.entity.Account;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

// 알림 설정 부분에 설정한대로 객체를 반환
@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationForm {
    private boolean studyCreatedByEmail;
    private boolean studyCreatedByWeb;
    private boolean studyRegistrationResultByEmail;
    private boolean studyRegistrationResultByWeb;
    private boolean studyUpdatedByEmail;
    private boolean studyUpdatedByWeb;

    protected NotificationForm(Account account) {
        this.studyCreatedByEmail = account.getNotificationSetting().isStudyCreatedByEmail();
        this.studyCreatedByWeb = account.getNotificationSetting().isStudyCreatedByWeb();
        this.studyUpdatedByEmail = account.getNotificationSetting().isStudyUpdatedByEmail();
        this.studyUpdatedByWeb = account.getNotificationSetting().isStudyUpdatedByWeb();
        this.studyRegistrationResultByEmail = account.getNotificationSetting().isStudyRegistrationResultByEmail();
        this.studyRegistrationResultByWeb = account.getNotificationSetting().isStudyRegistrationResultByWeb();
    }

    public static NotificationForm from(Account account) {
        return new NotificationForm(account);
    }
}
