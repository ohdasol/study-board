package com.project.board.modules.account.infra.repository;

import com.project.board.modules.account.domain.entity.Account;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.transaction.annotation.Transactional;

/**
 * JPA : 관계형 데이터베이스를 사용하는 방식, 자바 클래스와 DB 테이블을 매핑
 * JpaRepository : Spring Data JPA에서 제공하는 인터페이스 (JPA를 쉽게 사용하기 위한 모듈), 쿼리 실행 구현체 생성
 * */
@Transactional(readOnly = true)
public interface AccountRepository extends JpaRepository<Account, Long>,
    QuerydslPredicateExecutor<Account> {

  boolean existsByEmail(String email);

  boolean existsByNickname(String nickname);

  Account findByEmail(String email);

  Account findByNickname(String nickname);

  @EntityGraph(attributePaths = {"tags", "zones"})
  Account findAccountWithTagsAndZonesById(Long id);
}
