package com.project.board.modules.study.infra.repository;

import com.project.board.modules.account.domain.entity.Account;
import com.project.board.modules.study.domain.entity.Study;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface StudyRepository extends JpaRepository<Study, Long>, StudyRepositoryExtension {

  boolean existsByPath(String path);

  // @NamedEntityGraph에 정의한 이름 작성
  @EntityGraph(value = "Study.withAll", type = EntityGraph.EntityGraphType.LOAD)
  Study findByPath(String path);

  @EntityGraph(value = "Study.withTagsAndManagers", type = EntityGraph.EntityGraphType.FETCH)
  Study findStudyWithTagsByPath(String path);

  @EntityGraph(value = "Study.withZonesAndManagers", type = EntityGraph.EntityGraphType.FETCH)
  Study findStudyWithZonesByPath(String path);

  @EntityGraph(value = "Study.withManagers", type = EntityGraph.EntityGraphType.FETCH)
  Study findStudyWithManagersByPath(String path);

  @EntityGraph(value = "Study.withMembers", type = EntityGraph.EntityGraphType.FETCH)
  Study findStudyWithMembersByPath(String path);

  /**
   * EventController에서 Study를 조회하기 위한 메서드
   *
   * 모임 조회시에는 관심사, 지역, 관리자, 회원 등을 가져올 필요가 없으므로 EntityGraph 불필요
   */
  Optional<Study> findStudyOnlyByPath(String path);

  @EntityGraph(value = "Study.withTagsAndZones", type = EntityGraph.EntityGraphType.FETCH)
  Study findStudyWithTagsAndZonesById(Long id);

  /**
   * 그동안은 EntityGraph 사용을 위해 Entity에 @NamedEntityGraph를 지정하고 Repository에서 @EntityGraph 참조했지만
   * 이번엔 Repository에서 한 번에 처리하는 방법으로 추가해 볼 것
   *
   * 먼저 Study가 업데이트 될 때 알림을 보내야 할 대상은 관리자와 멤버, fetch join으로 같이 조회해야 할 대상이 됨
   * 아래와 같이 구현
   *
   * @EntityGraph의 attribute로 attributePaths를 바로 지정 가능 이 때 type은 기본이 FETCH 이므로 생략 가능
   */
  @EntityGraph(attributePaths = {"managers", "members"})
  Study findStudyWithManagersAndMembersById(Long id);

  // 로그인 전 스터디 조회
  @EntityGraph(attributePaths = {"tags", "zones"})
  List<Study> findFirst9ByPublishedAndClosedOrderByPublishedDateTimeDesc(boolean published,
      boolean closed);
  List<Study> findFirst5ByManagersContainingAndClosedOrderByPublishedDateTimeDesc(Account account,
                                                                                  boolean closed);
  List<Study> findFirst5ByMembersContainingAndClosedOrderByPublishedDateTimeDesc(Account account,
      boolean closed);
}