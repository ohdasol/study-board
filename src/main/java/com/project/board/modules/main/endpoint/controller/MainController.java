package com.project.board.modules.main.endpoint.controller;

import com.project.board.modules.account.domain.entity.Account;
import com.project.board.modules.account.infra.repository.AccountRepository;
import com.project.board.modules.account.support.CurrentUser;
import com.project.board.modules.event.infra.repository.EnrollmentRepository;
import com.project.board.modules.study.domain.entity.Study;
import com.project.board.modules.study.infra.repository.StudyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class MainController {

  private final StudyRepository studyRepository;
  private final AccountRepository accountRepository;
  private final EnrollmentRepository enrollmentRepository;

  /**
   * 스터디 검색 기능 => querydsl을 이용해 구현
   *
   * 키워드를 입력 받아 스터디를 검색
   * 키워드는 스터디 제목, 관심사, 도시 이름
   *
   * 로그인하지 않고도 검색 가능하게
   *
   * 검색으로 보여질 내용은 아래와 같음
   *
   * 검색 키워드, 결과 개수
   * 스터디 정보
   * - 이름
   * - 짧은 소개
   * - 태그
   * - 지역
   * - 멤버 수
   * - 스터디 공개 일시
   */

  /**
   * search 뷰 라우팅
   *
   * account 정보가 있을 때는 home으로 리다이렉트 되도록 하였고, home에서 표시하기 위해 필요한 정보들을 조회할 수 있는 기능 추가
   *  - 로그인 후 홈 화면 진입시 계정, 스터디, 모임 정보를 조회할 수 있게 쿼리 메서드 및 querydsl을 사용해 구현
   *
   *  @CurrentUser로 인해 현재 인증된 사용자 정보에 따라 객체가 할당, @AuthenticationPrincipal이 적용되고, 인증 여부에 따라 account를 반환해서 넘겨줌
   */
  @GetMapping("/")
  public String home(@CurrentUser Account account, Model model) {
    if (account != null) {
      Account accountLoaded = accountRepository.findAccountWithTagsAndZonesById(account.getId());
      model.addAttribute(accountLoaded);
      model.addAttribute("enrollmentList",
          enrollmentRepository.findByAccountAndAcceptedOrderByEnrolledAtDesc(accountLoaded, true));
      model.addAttribute("studyList",
          studyRepository.findByAccount(accountLoaded.getTags(), accountLoaded.getZones()));
      model.addAttribute("studyManagerOf",
          studyRepository.findFirst5ByManagersContainingAndClosedOrderByPublishedDateTimeDesc(
              account, false));
      model.addAttribute("studyMemberOf",
          studyRepository.findFirst5ByMembersContainingAndClosedOrderByPublishedDateTimeDesc(
              account, false));
      return "home";
    } // 로그인 후 홈 화면
    model.addAttribute("studyList",
        studyRepository.findFirst9ByPublishedAndClosedOrderByPublishedDateTimeDesc(true, false)); // 최근 공개된 스터디를 9개 조회하기 위해 JPA 쿼리 메서드를 사용
    return "index";
  } // 로그인 전 홈 화면

  @GetMapping("/login")
  public String login() {
    return "login";
  }

  /**
   * 페이징 적용
   * 
   * Pageable 파라미터 추가, study는 Page 타입
   * @PageableDefault를 이용하여 페이지 사이즈와 페이지, 정렬 방식 등의 기본 값 지정 가능
   * 페이지 사이즈는 9, 정렬은 공개 날짜가 오래된 순
   * 
   * 페이지 내비게이션, 정렬 조건 등 기능 추가
   */
  // 스터디 검색, 키워드로 조회한 결과를 모델에 담아 search 페이지로 이동시킴
  @GetMapping("/search/study")
  public String searchStudy(String keyword, Model model,
      @PageableDefault(size = 9, sort = "publishedDateTime", direction = Sort.Direction.ASC) Pageable pageable) {
    Page<Study> studyPage = studyRepository.findByKeyword(keyword, pageable); // 키워드 검색을 위해 호출
    model.addAttribute("studyPage", studyPage);
    model.addAttribute("keyword", keyword);
    model.addAttribute("sortProperty", pageable.getSort().toString().contains("publishedDateTime")
        ? "publishedDateTime"
        : "memberCount");
    return "search";
  }
}