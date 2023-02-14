package com.project.board.modules.account.endpoint.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.board.modules.account.application.AccountService;
import com.project.board.modules.account.endpoint.controller.form.*;
import com.project.board.modules.account.endpoint.controller.validator.NicknameFormValidator;
import com.project.board.modules.account.endpoint.controller.validator.PasswordFormValidator;
import com.project.board.modules.tag.domain.entity.Tag;
import com.project.board.modules.tag.infra.repository.TagRepository;
import com.project.board.modules.account.domain.entity.Account;
import com.project.board.modules.account.domain.entity.Zone;
import com.project.board.modules.account.support.CurrentUser;
import com.project.board.modules.zone.infra.repository.ZoneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class SettingsController { // 계정 설정 관련 컨트롤러

    static final String SETTINGS_PROFILE_VIEW_NAME = "settings/profile"; // 프로필 수정 url 상수로 지정
    static final String SETTINGS_PROFILE_URL = "/" + SETTINGS_PROFILE_VIEW_NAME; // view 지정
    static final String SETTINGS_PASSWORD_VIEW_NAME = "settings/password"; // 비밀번호 url 
    static final String SETTINGS_PASSWORD_URL = "/" + SETTINGS_PASSWORD_VIEW_NAME;
    static final String SETTINGS_NOTIFICATION_VIEW_NAME = "settings/notification"; // 알림 설정 url
    static final String SETTINGS_NOTIFICATION_URL = "/" + SETTINGS_NOTIFICATION_VIEW_NAME;
    static final String SETTINGS_ACCOUNT_VIEW_NAME = "settings/account"; // 닉네임 변경 url
    static final String SETTINGS_ACCOUNT_URL = "/" + SETTINGS_ACCOUNT_VIEW_NAME;
    static final String SETTINGS_TAGS_VIEW_NAME = "settings/tags"; // 관심 주제 url
    static final String SETTINGS_TAGS_URL = "/" + SETTINGS_TAGS_VIEW_NAME; 
    static final String SETTINGS_ZONE_VIEW_NAME = "settings/zones"; // 지역 관련 url
    static final String SETTINGS_ZONE_URL = "/" + SETTINGS_ZONE_VIEW_NAME;

    private final AccountService accountService;
    private final PasswordFormValidator passwordFormValidator;
    private final NicknameFormValidator nicknameFormValidator;
    private final TagRepository tagRepository;
    private final ZoneRepository zoneRepository;
    private final ObjectMapper objectMapper; // 스프링이 자동으로 빈으로 등록해 주는 ObjectMapper를 주입

    // 비밀번호 변경 요청
    @InitBinder("passwordForm")
    public void passwordFormValidator(WebDataBinder webDataBinder) { // 비밀번호 검증을 위한 validator 추가
        webDataBinder.addValidators(passwordFormValidator);
    }

    // @InitBinder를 통해 FormValidator를 추가해 주는 부분에서 필드변수로 주입받아 사용하도록 수정
    @InitBinder("nicknameForm")
    public void nicknameFormInitBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(nicknameFormValidator);
    }

    // 프로필 수정
    @GetMapping(SETTINGS_PROFILE_URL) // 기존 문자열을 상수로 대체
    public String profileUpdateForm(@CurrentUser Account account, Model model) {
        model.addAttribute(account);
        model.addAttribute(Profile.from(account));
        return SETTINGS_PROFILE_VIEW_NAME;
    }

    /**
     * 현재 사용자의 계정 정보와 프로필 폼을 통해 전달된 정보를 받음 Profile 폼을 validation 할 때 발생하는 에러들을 Errors 객체를 통해 전달 받고 다시 뷰로 데이터를 전달하기 위한 model 객체도 주입
     * 에러가 있으면 계정정보만 추가로 전달하고 다시 해당 뷰를 보여줌
     * AccountService에게 프로필 업데이트를 위임
     * 사용자가 화면을 새로고침 하더라도 폼 데이터가 다시 발생하지 않도록 리다이렉트
     */
    @PostMapping(SETTINGS_PROFILE_URL)
    public String updateProfile(@CurrentUser Account account, @Valid Profile profile, Errors errors, Model model, RedirectAttributes attributes) { // RedirectAttributes : 리다이렉트 시 1회성 데이터를 전달할 수 있는 객체
        if (errors.hasErrors()) {
            model.addAttribute(account);
            return SETTINGS_PROFILE_VIEW_NAME;
        }
        accountService.updateProfile(account, profile); // 프로필 업데이트를 위임
        attributes.addFlashAttribute("message", "프로필을 수정하였습니다.");
        return "redirect:" + SETTINGS_PROFILE_URL;
    }

    // 비밀번호 수정 뷰로 라우팅
    @GetMapping(SETTINGS_PASSWORD_URL)
    public String passwordUpdateForm(@CurrentUser Account account, Model model) {
        model.addAttribute(account);
        model.addAttribute(new PasswordForm());
        return SETTINGS_PASSWORD_VIEW_NAME;
    }

    // 비밀번호 폼을 전달받아 해당 비밀번호로 업데이트
    @PostMapping(SETTINGS_PASSWORD_URL)
    public String updatePassword(@CurrentUser Account account, @Valid PasswordForm passwordForm, Errors errors, Model model, RedirectAttributes attributes) {
        if (errors.hasErrors()) { // 에러가 있을 경우 다시 페이지를 띄우고 그렇지 않을 경우 피드백 메시지와 함께 리다이렉트
            model.addAttribute(account);
            return SETTINGS_PASSWORD_VIEW_NAME;
        }
        accountService.updatePassword(account, passwordForm.getNewPassword());
        attributes.addFlashAttribute("message", "패스워드를 변경했습니다.");
        return "redirect:" + SETTINGS_PASSWORD_URL;
    }

    // 알림 설정 뷰로 라우팅
    @GetMapping(SETTINGS_NOTIFICATION_URL)
    public String notificationForm(@CurrentUser Account account, Model model) {
        model.addAttribute(account);
        model.addAttribute(NotificationForm.from(account));
        return SETTINGS_NOTIFICATION_VIEW_NAME;
    }

    // 알림 설정 요청 처리
    @PostMapping(SETTINGS_NOTIFICATION_URL)
    public String updateNotification(@CurrentUser Account account, @Valid NotificationForm notificationForm, Errors errors, Model model, RedirectAttributes attributes) {
        if (errors.hasErrors()) {
            model.addAttribute(account);
            return SETTINGS_NOTIFICATION_URL;
        }
        accountService.updateNotification(account, notificationForm);
        attributes.addFlashAttribute("message", "알림설정을 수정하였습니다.");
        return "redirect:" + SETTINGS_NOTIFICATION_URL;
    }

    // 닉네임 수정 뷰로 라우팅
    @GetMapping(SETTINGS_ACCOUNT_URL)
    public String nicknameForm(@CurrentUser Account account, Model model) {
        model.addAttribute(account);
        model.addAttribute(new NicknameForm(account.getNickname()));
        return SETTINGS_ACCOUNT_VIEW_NAME;
    }

    // 닉네임 폼 전달 및 업데이트
    @PostMapping(SETTINGS_ACCOUNT_URL)
    public String updateNickname(@CurrentUser Account account, @Valid NicknameForm nicknameForm, Errors errors, Model model, RedirectAttributes attributes) {
        if (errors.hasErrors()) {
            model.addAttribute(account);
            return SETTINGS_ACCOUNT_VIEW_NAME;
        }
        accountService.updateNickname(account, nicknameForm.getNickname());
        attributes.addFlashAttribute("message", "닉네임을 수정하였습니다.");
        return "redirect:" + SETTINGS_ACCOUNT_URL;
    }

    /**
     * 관심 주제 추가시 자동완성 기능을 구현
     * 기존에 있는 태그 목록에서 선택 가능
     * tagify 라이브러리의 기능을 활용
     */
    // 관심 주제 뷰로 라우팅
    @GetMapping(SETTINGS_TAGS_URL)
    public String updateTags(@CurrentUser Account account, Model model) {
        model.addAttribute(account);
        Set<Tag> tags = accountService.getTags(account); // AccountService에 태그 조회를 위임
        model.addAttribute("tags", tags.stream() // 받은 태그를 모델에 List<String> 형식으로 넘겨줌
                .map(Tag::getTitle)
                .collect(Collectors.toList()));
        List<String> allTags = tagRepository.findAll()
                .stream()
                .map(Tag::getTitle)
                .collect(Collectors.toList()); // TagRepository에서 전체 태그 목록을 가져와 리스트로 변환
        String whitelist = null;
        try {
            whitelist = objectMapper.writeValueAsString(allTags); // List를 JSON 형태로 변환
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        model.addAttribute("whitelist", whitelist); // whitelist를 model로 전달
        return SETTINGS_TAGS_VIEW_NAME;
    }

    /**
     * @ReponseStatus : HTTP 응답 상태를 매핑
     * @ReponseStatus(HttpStatus.OK) : @ReponseStatus를 이용해 정상 처리됐을 경우 응답 상태만 정의
     * @RequestBody : JSON 형식의 데이터를 요청 받음
     */
    // 관심 주제 등록
    @PostMapping(SETTINGS_TAGS_URL + "/add")
    @ResponseStatus(HttpStatus.OK)
    public void addTag(@CurrentUser Account account, @RequestBody TagForm tagForm) {
        String title = tagForm.getTagTitle(); // 태그 존재 여부 확인
        Tag tag = tagRepository.findByTitle(title)
                .orElseGet(() -> tagRepository.save(Tag.builder() 
                        .title(title)
                        .build())); // 존재하지 않을 경우 TagRepository를 이용해 저장
        accountService.addTag(account, tag); // 계정 정보에 태그를 추가해 주어야 하므로 AccountService에게 태그 추가를 위임
    }

    // 관심 주제 삭제
    @PostMapping(SETTINGS_TAGS_URL + "/remove")
    @ResponseStatus(HttpStatus.OK)
    public void removeTag(@CurrentUser Account account, @RequestBody TagForm tagForm) {
        String title = tagForm.getTagTitle();  // 태그 존재 여부 확인
        Tag tag = tagRepository.findByTitle(title)
                .orElseThrow(IllegalArgumentException::new); // 존재하지 않을 경우 예외 던짐(존재하지 않는 태그는 삭제할 수 없기 때문)
        accountService.removeTag(account, tag);
    }

    // 지역 뷰로 라우팅
    @GetMapping(SETTINGS_ZONE_URL)
    public String updateZonesForm(@CurrentUser Account account, Model model) throws JsonProcessingException {
        model.addAttribute(account);
        Set<Zone> zones = accountService.getZones(account);
        model.addAttribute("zones", zones.stream()
                .map(Zone::toString)
                .collect(Collectors.toList()));
        List<String> allZones = zoneRepository.findAll().stream()
                .map(Zone::toString)
                .collect(Collectors.toList());
        model.addAttribute("whitelist", objectMapper.writeValueAsString(allZones));
        return SETTINGS_ZONE_VIEW_NAME;
    }

    // 지역 등록
    @PostMapping(SETTINGS_ZONE_URL + "/add")
    @ResponseStatus(HttpStatus.OK)
    public void addZone(@CurrentUser Account account, @RequestBody ZoneForm zoneForm) {
        Zone zone = zoneRepository.findByCityAndProvince(zoneForm.getCityName(), zoneForm.getProvinceName())
                .orElseThrow(IllegalArgumentException::new);
        accountService.addZone(account, zone);
    }

    // 지역 삭제
    @PostMapping(SETTINGS_ZONE_URL + "/remove")
    @ResponseStatus(HttpStatus.OK)
    public void removeZone(@CurrentUser Account account, @RequestBody ZoneForm zoneForm) {
        Zone zone = zoneRepository.findByCityAndProvince(zoneForm.getCityName(), zoneForm.getProvinceName())
                .orElseThrow(IllegalArgumentException::new);
        accountService.removeZone(account, zone);
    }
}