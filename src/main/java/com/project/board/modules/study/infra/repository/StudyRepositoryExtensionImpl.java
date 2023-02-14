package com.project.board.modules.study.infra.repository;

import com.project.board.modules.account.domain.entity.QAccount;
import com.project.board.modules.account.domain.entity.QZone;
import com.project.board.modules.account.domain.entity.Zone;
import com.project.board.modules.study.domain.entity.QStudy;
import com.project.board.modules.study.domain.entity.Study;
import com.project.board.modules.tag.domain.entity.QTag;
import com.project.board.modules.tag.domain.entity.Tag;
import com.querydsl.core.QueryResults;
import com.querydsl.jpa.JPQLQuery;

import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

// 키워드 검색 내용을 구현할 클래스
public class StudyRepositoryExtensionImpl extends QuerydslRepositorySupport implements
    StudyRepositoryExtension { // QuerydslRepositorySupport를 상속하여 엔티티 타입만 부모 객체에 전달

  // 부모 클래스의 스터디 클래스 호출
  public StudyRepositoryExtensionImpl() {
    super(Study.class);
  }

  // querydsl을 이용해 쿼리를 생성한 뒤 조회
  @Override
  public Page<Study> findByKeyword(String keyword, Pageable pageable) {
    QStudy study = QStudy.study;
    JPQLQuery<Study> query = from(study)
        .where(study.published.isTrue()
            .and(study.title.containsIgnoreCase(keyword))
            .or(study.tags.any().title.containsIgnoreCase(keyword))
            .or(study.zones.any().localNameOfCity.containsIgnoreCase(keyword)))
        .leftJoin(study.tags, QTag.tag).fetchJoin()
        .leftJoin(study.zones, QZone.zone).fetchJoin()
        .leftJoin(study.members, QAccount.account).fetchJoin()
        .distinct();
    JPQLQuery<Study> pageableQuery = getQuerydsl().applyPagination(pageable, query);
    QueryResults<Study> fetchResults = pageableQuery.fetchResults();
    return new PageImpl<>(fetchResults.getResults(), pageable, fetchResults.getTotal());
  }

  // account가 가진 tags와 zones를 이용해 study를 조회하기 위해 정의
  @Override
  public List<Study> findByAccount(Set<Tag> tags, Set<Zone> zones) {
    QStudy study = QStudy.study;
    JPQLQuery<Study> query = from(study).where(study.published.isTrue()
            .and(study.closed.isFalse())
            .and(study.tags.any().in(tags))
            .and(study.zones.any().in(zones)))
        .leftJoin(study.tags, QTag.tag).fetchJoin()
        .leftJoin(study.zones, QZone.zone).fetchJoin()
        .orderBy(study.publishedDateTime.desc())
        .distinct()
        .limit(9);
    return query.fetch();
  }
}
