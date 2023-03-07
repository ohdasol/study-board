package com.project.board.modules.event.domain.entity;

import com.project.board.modules.account.domain.entity.Account;

import java.time.LocalDateTime;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedAttributeNode;
import javax.persistence.NamedEntityGraph;
import javax.persistence.NamedSubgraph;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 참가(Enrollment, 모임 참가자) 엔티티 설계
 * 
 * 
 * Enrollment는 Study와는 관계를 가질 필요가 없음
 * Account와는 단방향 연관관계
 * Event와는 양방향 연관관계
 */
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@ToString
@NamedEntityGraph(
    name = "Enrollment.withEventAndStudy",
    attributeNodes = {
        @NamedAttributeNode(value = "event", subgraph = "study") // event를 통해 Study를 참조하기 위해 사용
    },
    subgraphs = @NamedSubgraph(name = "study", attributeNodes = @NamedAttributeNode("study"))
)

public class Enrollment {

  @Id
  @GeneratedValue
  private Long id;

  @ManyToOne
  private Event event;

  @ManyToOne
  private Account account;

  private LocalDateTime enrolledAt;

  private boolean accepted;

  private boolean attended;

  // static 생성자
  public static Enrollment of(LocalDateTime enrolledAt, boolean isAbleToAcceptWaitingEnrollment,
      Account account) {
    Enrollment enrollment = new Enrollment();
    enrollment.enrolledAt = enrolledAt;
    enrollment.accepted = isAbleToAcceptWaitingEnrollment;
    enrollment.account = account;
    return enrollment;
  }

  // 모임 참가 수락 여부
  public void accept() {
    this.accepted = true;
  }

  public void reject() {
    this.accepted = false;
  }

  public void attach(Event event) {
    this.event = event;
  }

  public void detachEvent() {
    this.event = null;
  }

  public void attend() {
    this.attended = true;
  }

  public void absent() {
    this.attended = false;
  }
}
