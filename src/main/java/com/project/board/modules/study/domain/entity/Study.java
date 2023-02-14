package com.project.board.modules.study.domain.entity;

import com.project.board.modules.account.domain.UserAccount;
import com.project.board.modules.account.domain.entity.Account;
import com.project.board.modules.account.domain.entity.Zone;
import com.project.board.modules.study.endpoint.form.StudyDescriptionForm;
import com.project.board.modules.study.endpoint.form.StudyForm;
import com.project.board.modules.tag.domain.entity.Tag;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.NamedAttributeNode;
import javax.persistence.NamedEntityGraph;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.hibernate.annotations.ColumnDefault;
/**
 * 스터디 관리 도메인 설계
 *
 * 속성은 아래와 같다
 * Set<Account> managers: 관리자
 * Set<Account> members: 회원
 * Set<Tag> tags: 관심주제
 * Set<Zone> zones: 활동지역
 *
 * Study에서 Account로 @ManyToMany 단방향 관계 두 개(managers, members)
 * Study에서 Zone으로 @ManyToMany 단방향 관계
 * Study에서 Tag로 @ManyToMany 단방향 관계
 */

/**
 * @EntityGraph : 
 * JPA를 사용하여 연관관계가 있는 엔티티를 조회할 경우 N + 1 문제가 발생할 수 있는데 이를 해결하기 위해 fetch join을 사용
 * fetch join을 사용하기 위해서는 JPARepository 인터페이스가 기본적으로 제공하는 기본 메서드들에는 사용할 수 가 없어서 이때 사용할 수 있음
 *
 * N+1 문제 ?
 * 조회 쿼리를 날렸을 때 데이터들의 개수만큼 조인 쿼리가 발생하는 것(성능에 영향을 주기 때문에 방치할 수 없음)
 *
 * NamedEntityGraph : EntityGraph에 이름을 명시, Study.withAll
 * tags, zones, managers, members 4가지 attribute에 대해 Lazy(지연)로딩을 사용하지 않음
 *
 * 지연로딩 ? 지연로딩이 설정되어 있는 엔티티는 프록시 객체로 가져옴(실체 객체를 호출하면 중간에 가로채 다른 동작을 수행하는 객체로 변경)
 */
@Entity
@NamedEntityGraph(name = "Study.withAll", attributeNodes = {
    @NamedAttributeNode("tags"),
    @NamedAttributeNode("zones"),
    @NamedAttributeNode("managers"),
    @NamedAttributeNode("members")
})
@NamedEntityGraph(name = "Study.withTagsAndManagers", attributeNodes = { // 스터디 주제
    @NamedAttributeNode("tags"),
    @NamedAttributeNode("managers")
})
@NamedEntityGraph(name = "Study.withZonesAndManagers", attributeNodes = { // 스터디 활동 지역
    @NamedAttributeNode("zones"),
    @NamedAttributeNode("managers")
})
@NamedEntityGraph(name = "Study.withManagers", attributeNodes = { // 스터디 관리자
    @NamedAttributeNode("managers")
})
@NamedEntityGraph(name = "Study.withMembers", attributeNodes = { // 스터디 회원
    @NamedAttributeNode("members")
})
@NamedEntityGraph(name = "Study.withTagsAndZones", attributeNodes = { // 관심 주제와 지역 정보
    @NamedAttributeNode("tags"),
    @NamedAttributeNode("zones")
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Study {

  @Id
  @GeneratedValue
  private Long id;

  @ManyToMany
  private Set<Account> managers = new HashSet<>(); // 컬렉션 타입의 필드 변수를 빈 객체로 초기화, 관리자 변경 등을 고려해 관리자를 여러 명을 설정할 수 있게 해줌

  @ManyToMany
  private Set<Account> members = new HashSet<>();

  // 스터디 페이지 경로로 유니크 값을 가짐
  @Column(unique = true)
  private String path;

  private String title;

  private String shortDescription;

  @Lob
  @Basic(fetch = FetchType.EAGER)
  private String fullDescription;

  @Lob
  @Basic(fetch = FetchType.EAGER)
  private String image;

  @ManyToMany
  private Set<Tag> tags = new HashSet<>();

  @ManyToMany
  private Set<Zone> zones = new HashSet<>();

  private LocalDateTime publishedDateTime;

  private LocalDateTime closedDateTime;

  private LocalDateTime recruitingUpdatedDateTime;

  private boolean recruiting;

  private boolean published;

  private boolean closed;

  // 배너 사용 유무 조회
  @Accessors(fluent = true) // 메서드가 생성되는 것을 방지
  private boolean useBanner;

  @ColumnDefault(value = "0")
  private Integer memberCount = 0;

  // static 생성자
  public static Study from(StudyForm studyForm) {
    Study study = new Study();
    study.title = studyForm.getTitle();
    study.shortDescription = studyForm.getShortDescription();
    study.fullDescription = studyForm.getFullDescription();
    study.path = studyForm.getPath();
    return study;
  }

  // 관리자 계정 추가 메서드
  public void addManager(Account account) {
    managers.add(account);
  }

  // 스터디 가입 가능 여부 메서드
  public boolean isJoinable(UserAccount userAccount) {
    Account account = userAccount.getAccount();
    return this.isPublished() && this.isRecruiting() && !this.members.contains(account)
        && !this.managers.contains(account);
  }

  // 스터디 멤버 확인 여부 메서드
  public boolean isMember(UserAccount userAccount) {
    return this.members.contains(userAccount.getAccount());
  }

  // 스터디 관리자 확인 여부 메서드
  public boolean isManager(UserAccount userAccount) {
    return this.managers.contains(userAccount.getAccount());
  }

  // 스터디 수정 내용 업데이트
  public void updateDescription(StudyDescriptionForm studyDescriptionForm) {
    this.shortDescription = studyDescriptionForm.getShortDescription();
    this.fullDescription = studyDescriptionForm.getFullDescription();
  }

  public void updateImage(String image) {
    this.image = image;
  }

  public void setBanner(boolean useBanner) {
    this.useBanner = useBanner;
  }

  public void addTag(Tag tag) {
    this.tags.add(tag);
  }

  public void removeTag(Tag tag) {
    this.tags.remove(tag);
  }

  public void addZone(Zone zone) {
    this.zones.add(zone);
  }

  public void removeZone(Zone zone) {
    this.zones.remove(zone);
  }

  // 스터디 이전 상태를 검사하여 예외를 던지거나 공개된 상태로 변경
  public void publish() {
    if (this.closed || this.published) {
      throw new IllegalStateException("스터디를 이미 공개했거나 종료된 스터디 입니다.");
    }
    this.published = true;
    this.publishedDateTime = LocalDateTime.now();
  }

  // 스터디 이전 상태를 검사하여 예외를 던지거나 종료된 상태로 변경
  public void close() {
    if (!this.published || this.closed) {
      throw new IllegalStateException("스터디를 공개하지 않았거나 이미 종료한 스터디 입니다.");
    }
    this.closed = true;
    this.closedDateTime = LocalDateTime.now();
  }

  // 팀원 모집이 가능한 상태 여부 검사
  public boolean isEnableToRecruit() {
    return this.published && this.recruitingUpdatedDateTime == null
        || this.recruitingUpdatedDateTime.isBefore(LocalDateTime.now().minusHours(1));
  }

  public void updatePath(String newPath) {
    this.path = newPath;
  }

  public void updateTitle(String newTitle) {
    this.title = newTitle;
  }

  public boolean isRemovable() {
    return !this.published;
  }

  // 스터디 상태를 검사하여 예외를 던지거나 팀원 모집 상태로 변경
  public void startRecruit() {
    if (!isEnableToRecruit()) {
      throw new RuntimeException("인원 모집을 시작할 수 없습니다. 스터디를 공개하거나 한 시간 뒤 다시 시도하세요.");
    }
    this.recruiting = true;
    this.recruitingUpdatedDateTime = LocalDateTime.now();
  }

  // 스터디 상태를 검사하여 예외를 던지거나 팀원 모집 중단 상태로 변경
  public void stopRecruit() {
    if (!isEnableToRecruit()) {
      throw new RuntimeException("인원 모집을 멈출 수 없습니다. 스터디를 공개하거나 한 시간 뒤 다시 시도하세요.");
    }
    this.recruiting = false;
    this.recruitingUpdatedDateTime = LocalDateTime.now();
  }

  public void addMember(Account account) {
    this.members.add(account);
    this.memberCount++;
  }

  public void removeMember(Account account) {
    this.members.remove(account);
    this.memberCount--;
  }

  public String getEncodedPath() {
    return URLEncoder.encode(path, StandardCharsets.UTF_8);
  }

  public boolean isManagedBy(Account account) {
    return this.getManagers().contains(account);
  }
}
