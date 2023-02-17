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

  /**
   * querydsl에서 발생하는 n+1 문제 해결
   *
   * left (outer) join만 추가하게 되면 첫 번째(left) 테이블에 연관 관계가 있는 모든 데이터를 가져오고(연관 관계가 없으면 null로), 중복 row가 발생함
   * 이를 해결하기 위해 추가로 fetchJoin와 distinct 필요
   
   * fetchJoin : join 관계 데이터를 한 번에 가져오기 때문에 쿼리의 수가 줄어듦
   * distinct : 중복된 row를 제거
   *
   *
   * querydsl에 페이징(Paging) 적용
   *
   * getQuerydsl()을 이용해 QuerydslRepositorySupport가 제공하는 기능을 사용할 수 있는데 페이징을 적용하기 위해 applyPagination을 호출
   * fetchResults를 이용해 조회한 결과를 얻을 수 있음
   * 반환해야 할 타입이 Page이므로 구현체인 PageImpl을 이용해 반환
   * 결과 데이터, pageable, 전체 데이터 수를 생성자로 전달해 줌
   */
  // querydsl을 이용해 쿼리를 생성한 뒤 조회
  @Override
  public Page<Study> findByKeyword(String keyword, Pageable pageable) {
    QStudy study = QStudy.study;
    JPQLQuery<Study> query = from(study)
        .where(study.published.isTrue()
            .and(study.title.containsIgnoreCase(keyword))
            .or(study.tags.any().title.containsIgnoreCase(keyword))
            .or(study.zones.any().localNameOfCity.containsIgnoreCase(keyword)))
        .leftJoin(study.tags, QTag.tag).fetchJoin() // study 기준으로 tag를 join
        .leftJoin(study.zones, QZone.zone).fetchJoin()
        .leftJoin(study.members, QAccount.account).fetchJoin()
        .distinct();
    JPQLQuery<Study> pageableQuery = getQuerydsl().applyPagination(pageable, query);
    QueryResults<Study> fetchResults = pageableQuery.fetchResults();
    return new PageImpl<>(fetchResults.getResults(), pageable, fetchResults.getTotal());
  }

  // 로그인 전 스터디 조회, account가 가진 tags와 zones를 이용해 study를 조회하기 위해 정의
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
