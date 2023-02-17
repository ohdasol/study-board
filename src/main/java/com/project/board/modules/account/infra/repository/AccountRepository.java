package com.project.board.modules.account.infra.repository;

import com.project.board.modules.account.domain.entity.Account;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.transaction.annotation.Transactional;

/**
 * JPA : 관계형 데이터베이스를 사용하는 방식, 자바 클래스와 DB 테이블을 매핑
 * JpaRepository : Spring Data JPA에서 제공하는 인터페이스 (JPA를 쉽게 사용하기 위한 모듈), 쿼리 실행 구현체 생성
 *
 *
 * Querydsl?
 * SQL, JPQL을 코드로 작성할 수 있도록 해주는 빌더 오픈소스 프레임워크 (JPQL : 엔티티 객체를 대상으로 쿼리 작성), 자바 코드 기반으로 쿼리 작성
 *
 * QueryDSL은 컴파일 단계에서 엔티티 기반으로 QClass 생성
 * QClass 생성 방법 : Gradle -> Task -> other -> compileQuerydsl 실행
 * QClass 생성 위치 : build -> generated -> querydsl
 *
 * QuerydslPredicateExecutor : QueryDSL을 편하게 사용할 수 있게 Spring Data JPA가 제공하는 인터페이스
 * */
@Transactional(readOnly = true)
public interface AccountRepository extends JpaRepository<Account, Long>,
    QuerydslPredicateExecutor<Account> {

  boolean existsByEmail(String email);

  boolean existsByNickname(String nickname);

  Account findByEmail(String email);

  Account findByNickname(String nickname);

  // 로그인 후 홈 화면 진입
  @EntityGraph(attributePaths = {"tags", "zones"})
  Account findAccountWithTagsAndZonesById(Long id);
}
