package com.project.board.modules.study.endpoint;

import com.project.board.modules.account.domain.entity.Account;
import com.project.board.modules.account.support.CurrentUser;
import com.project.board.modules.study.application.StudyService;
import com.project.board.modules.study.endpoint.form.StudyForm;
import com.project.board.modules.study.endpoint.form.validator.StudyFormValidator;
import com.project.board.modules.study.domain.entity.Study;
import com.project.board.modules.study.infra.repository.StudyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Controller
@RequiredArgsConstructor
public class StudyController {
    private final StudyService studyService;
    private final StudyFormValidator studyFormValidator;
    private final StudyRepository studyRepository;

    // 스터디 폼 검증
    @InitBinder("studyForm")
    public void studyFormInitBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(studyFormValidator);
    }

    // 스터디 생성
    @GetMapping("/new-study")
    public String newStudyForm(@CurrentUser Account account, Model model) {
        model.addAttribute(account);
        model.addAttribute(new StudyForm());
        return "study/form";
    }

    // 스터디 생성 버튼을 눌렀을 때 에러가 있을 경우 에러 전달, 없을 경우 스터디 생성 후 생성 페이지로 이동
    @PostMapping("/new-study")
    public String newStudySubmit(@CurrentUser Account account, @Valid StudyForm studyForm, Errors errors) {
        if (errors.hasErrors()) {
            return "study/form";
        }
        Study newStudy = studyService.createNewStudy(studyForm, account);
        return "redirect:/study/" + URLEncoder.encode(newStudy.getPath(), StandardCharsets.UTF_8);
    }

    /**
     * 스터디 조회 화면
     * 생성한 스터디 URL을 PathParameter로 전달받아서 StudyRepository에서 찾아 스터디 정보를 모델로 전달하고 boardList.html 페이지로 이동
     */
    @GetMapping("/study/{path}")
    public String viewStudy(@CurrentUser Account account, @PathVariable String path, Model model) {
        model.addAttribute(account);
        model.addAttribute(studyService.getStudy(path));
        return "study/view";
    }

    // 스터디 조회 화면에서 구성원 메뉴 클릭 후 관련 페이지 이동
    @GetMapping("/study/{path}/members")
    public String viewStudyMembers(@CurrentUser Account account, @PathVariable String path, Model model) {
        model.addAttribute(account);
        model.addAttribute(studyService.getStudy(path));
        return "study/members";
    }

    // 스터디 가입
    @GetMapping("/study/{path}/join")
    public String joinStudy(@CurrentUser Account account, @PathVariable String path) {
        Study study = studyRepository.findStudyWithMembersByPath(path);
        studyService.addMember(study, account);
        return "redirect:/study/" + study.getEncodedPath() + "/members";
    }

    // 스터디 탈퇴
    @GetMapping("/study/{path}/leave")
    public String leaveStudy(@CurrentUser Account account, @PathVariable String path) {
        Study study = studyRepository.findStudyWithMembersByPath(path);
        studyService.removeMember(study, account);
        return "redirect:/study/" + study.getEncodedPath() + "/members";
    }
}