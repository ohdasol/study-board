package com.project.board.modules.account.domain.entity;

import com.project.board.modules.account.endpoint.controller.form.NotificationForm;
import com.project.board.modules.board.domain.entity.Board;
import com.project.board.modules.board.domain.entity.BoardComment;
import com.project.board.modules.tag.domain.entity.Tag;
import lombok.*;
import org.hibernate.Hibernate;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.*;

/**
 * access = AccessLevel.PROTECTED : 무분별한 객체 생성에 대해 체크
 * @ToString : toString() 메소드를 자동으로 생성
 * toString() : 객체가 가지고 있는 정보나 값들을 문자열로 만들어 리턴 해주는 메서드
 */
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED) @AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder @Getter @ToString
public class Account extends AuditingEntity {

    @Id @GeneratedValue // Id : DB 시퀀스 값
    @Column(name = "account_id")
    private Long id;

    @Column(unique = true) // unique 값만 추가 가능
    private String email;

    @Column(unique = true)
    private String nickname;

    private String password;

    private boolean isValid;

    private String emailToken;

    private LocalDateTime joinedAt;

    @Embedded // 해당 필드가 DB에서는 개별 매핑
    private Profile profile = new Profile();

    @Embedded
    private NotificationSetting notificationSetting = new NotificationSetting();

    private LocalDateTime emailTokenGeneratedAt; // 이메일 토큰이 발급한 시기 저장

    @ManyToMany @ToString.Exclude // @ToString이 있을 경우 순환참조하여 에러가 발생하기 때문에 추가
    private Set<Tag> tags = new HashSet<>();

    @ManyToMany @ToString.Exclude
    private Set<Zone> zones = new HashSet<>();

    @OneToMany(mappedBy = "account")
    private List<Board> boardList = new ArrayList<>();

    @OneToMany(mappedBy = "account")
    private List<BoardComment> boardCommentList = new ArrayList<>();

    public static Account with(String email, String nickname, String password) {
        Account account = new Account();
        account.email = email;
        account.nickname = nickname;
        account.password = password;
        return account;
    }

    public void generateToken() { // 토큰 발급할 때 발급 시기 업데이트
        this.emailToken = UUID.randomUUID().toString();
        this.emailTokenGeneratedAt = LocalDateTime.now();
    }

    public boolean enableToSendEmail() { // 이메일을 보낼 수 있는지 체크
        return this.emailTokenGeneratedAt.isBefore(LocalDateTime.now().minusMinutes(5)); // 5분이 지났는지 체크되게 설정
    }

    public void verified() {
        this.isValid = true; // 계정 유효함을 알 수 있게 isValid 항목을 true
        joinedAt = LocalDateTime.now();
    }

    @PostLoad
    private void init() {
        if (profile == null) {
            profile = new Profile();
        }
        if (notificationSetting == null) {
            notificationSetting = new NotificationSetting();
        }
    }

    public void updateProfile(com.project.board.modules.account.endpoint.controller.form.Profile profile) {
        if (this.profile == null) {
            this.profile = new Profile();
        }
        this.profile.bio = profile.getBio();
        this.profile.url = profile.getUrl();
        this.profile.job = profile.getJob();
        this.profile.location = profile.getLocation();
        this.profile.image = profile.getImage();
    }

    public void updatePassword(String newPassword) {
        this.password = newPassword;
    }

    public void updateNotification(NotificationForm notificationForm) {
        this.notificationSetting.studyCreatedByEmail = notificationForm.isStudyCreatedByEmail();
        this.notificationSetting.studyCreatedByWeb = notificationForm.isStudyCreatedByWeb();
        this.notificationSetting.studyUpdatedByWeb = notificationForm.isStudyUpdatedByWeb();
        this.notificationSetting.studyUpdatedByEmail = notificationForm.isStudyUpdatedByEmail();
        this.notificationSetting.studyRegistrationResultByEmail = notificationForm.isStudyRegistrationResultByEmail();
        this.notificationSetting.studyRegistrationResultByWeb = notificationForm.isStudyRegistrationResultByWeb();
    }

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    public boolean isValid(String token) {
        return this.emailToken.equals(token);
    }

    @Embeddable
    @NoArgsConstructor(access = AccessLevel.PROTECTED) @AllArgsConstructor(access = AccessLevel.PROTECTED)
    @Builder @Getter @ToString
    public static class Profile {
        private String bio;
        private String url;
        private String job;
        private String location;
        private String company;

        @Lob @Basic(fetch = FetchType.EAGER) // Lob : 대용량을 저장할 수 있는 데이터 타입
        private String image;
    }

    @Embeddable // 다른 엔티티에 귀속될 수 있음
    @NoArgsConstructor(access = AccessLevel.PROTECTED) @AllArgsConstructor(access = AccessLevel.PROTECTED)
    @Builder @Getter @ToString
    public static class NotificationSetting {
        private boolean studyCreatedByEmail = false;
        private boolean studyCreatedByWeb = true;
        private boolean studyRegistrationResultByEmail = false;
        private boolean studyRegistrationResultByWeb = true;
        private boolean studyUpdatedByEmail = false;
        private boolean studyUpdatedByWeb = true;

    }

    /**
     * 컨트롤러에서 전달된 객체와 DB에서 찾은 객체는 id가 동일하면 같은 객체로 판별
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
            return false;
        }
        Account account = (Account) o;
        return id != null && Objects.equals(id, account.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}