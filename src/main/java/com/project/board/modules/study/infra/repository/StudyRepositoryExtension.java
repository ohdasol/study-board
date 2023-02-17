package com.project.board.modules.study.infra.repository;

import com.project.board.modules.account.domain.entity.Zone;
import com.project.board.modules.study.domain.entity.Study;
import com.project.board.modules.tag.domain.entity.Tag;

import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
/**
 * 키워드 검색하는 기능은 querydsl을 이용해 구현
 *
 * StudyRepositoryExtension 인터페이스 추가
 *
 * StudyRepository가 StudyRepositoryExtension을 상속
 */
@Transactional(readOnly = true)
public interface StudyRepositoryExtension {

  Page<Study> findByKeyword(String keyword, Pageable pageable);

  // 로그인 전 스터디 조회를 위한 메서드
  List<Study> findByAccount(Set<Tag> tags, Set<Zone> zones);
}
