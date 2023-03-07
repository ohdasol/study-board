package com.project.board.modules.account.endpoint.controller;

import com.project.board.modules.account.application.AccountService;
import com.project.board.modules.account.endpoint.controller.form.SignUpForm;
import com.project.board.modules.account.endpoint.controller.validator.SignUpFormValidator;
import com.project.board.modules.account.domain.entity.Account;
import com.project.board.modules.account.infra.repository.AccountRepository;
import com.project.board.modules.account.support.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;

@Controller
@RequiredArgsConstructor // 생성자 주입
public class AccountController {

    private final AccountService accountService;
    private final SignUpFormValidator signUpFormValidator;
    private final AccountRepository accountRepository;

    /**
     * @InitBinder : @Valid 어노테이션으로 검증이 필요한 객체를 가져오기 전에 수행할 메서드 지정
     * WebDataBinder : Validator를 이용헤 바인딩하여 객체 검증
     * */
    @InitBinder("signUpForm")
    public void initBinder(WebDataBinder webDataBinder) {

        webDataBinder.addValidators(signUpFormValidator);
    }

    // 회원가입 뷰 라우팅
    @GetMapping("/sign-up")
    public String signUpForm(Model model) {
        model.addAttribute(new SignUpForm());
        return "account/sign-up";
    }

    /**
     * @Valid : 타입에 대한 검증
     * @ModelAttribute : 사용자가 전달하는 값을 객체로 매핑
     * Errors : HTML에 해당 에러 전달
     */
    // 회원가입 처리
    @PostMapping("/sign-up")
    public String signUpSubmit(@Valid @ModelAttribute SignUpForm signUpForm, Errors errors) {
        if (errors.hasErrors()) {
            return "account/sign-up";
        }
        Account account = accountService.signUp(signUpForm);
        accountService.login(account);
        return "redirect:/";
    }

    /**
     * 회원 가입시 전송한 이메일을 확인하여 회원을 인증
     * 회원 가입시 다시 서버로 요청할 수 있게 토큰을 포함한 링크를 전송하고 사용자가 해당 링크를 클릭했을 때 토큰이 일치하면 가입 완료 처리
     */
    @GetMapping("/check-email-token")
    public String verifyEmail(String token, String email, Model model) {
        Account account = accountService.findAccountByEmail(email);
        if (account == null) { // 계정 정보가 없으면 모델 객체에 에러 전달
            model.addAttribute("error", "wrong.email");
            return "account/email-verification";
        }
        if (!token.equals(account.getEmailToken())) { // 계정 정보가 있지만 기존에 발급한 token과 일치하지 않는 경우 에러 전달
            model.addAttribute("error", "wrong.token");
            return "account/email-verification";
        }
        // token과 email 모두 있으면 인증 완료 처리
        accountService.verify(account);
        model.addAttribute("numberOfUsers", accountRepository.count()); // 인증에 성공한 데이터를 모델 객체에 전달
        model.addAttribute("nickname", account.getNickname());
        return "account/email-verification";
    }

    // 내비게이션바 경고창 클릭할 때 이동, email 정보와 함께 리다이렉트
    @GetMapping("/check-email")
    public String checkMail(@CurrentUser Account account, Model model) {
        model.addAttribute("email", account.getEmail());
        return "account/check-email";
    }

    // 이메일 재전송
    @GetMapping("/resend-email")
    public String resendEmail(@CurrentUser Account account, Model model) {
        if (!account.enableToSendEmail()) {
            model.addAttribute("error", "인증 이메일은 5분에 한 번만 전송할 수 있습니다.");
            model.addAttribute("email", account.getEmail());
            return "account/check-email";
        }
        accountService.sendVerificationEmail(account);
        return "redirect:/";
    }

    // 프로필 뷰 페이지 라우팅
    @GetMapping("/profile/{nickname}")
    public String viewProfile(@PathVariable String nickname, Model model, @CurrentUser Account account) {
        Account accountByNickname = accountService.getAccountBy(nickname);
        model.addAttribute(accountByNickname); // 객체 타입 전달
        model.addAttribute("isOwner", accountByNickname.equals(account)); // 전달된 객체와 DB에서 조회한 객체가 같으면 인증된 사용자
        return "account/profile";
    }

    // 이메일 로그인 뷰 페이지 라우팅
    @GetMapping("/email-login")
    public String emailLoginForm() {
        return "account/email-login";
    }

    // 이메일에 해당하는 계정 찾기, 계정이 존재하는 경우 로그인 가능한 링크를 이메일로 전송
    @PostMapping("/email-login")
    public String sendLinkForEmailLogin(String email, Model model, RedirectAttributes attributes) { // 이메일 폼을 통해 받은 정보로 계정을 찾아 메일을 전송하고 리다이렉트
        Account account = accountRepository.findByEmail(email);
        if (account == null) {
            model.addAttribute("error", "유효한 이메일 주소가 아닙니다.");
            return "account/email-login";
        }
        if (!account.enableToSendEmail()) {
            model.addAttribute("error", "너무 잦은 요청입니다. 5분 뒤에 다시 시도하세요.");
            return "account/email-login";
        }
        accountService.sendLoginLink(account);
        attributes.addFlashAttribute("message", "로그인 가능한 링크를 이메일로 전송하였습니다.");
        return "redirect:/email-login";
    }

    // 토큰과 이메일을 확인하고 해당 계정으로 로그인
    @GetMapping("/login-by-email")
    public String loginByEmail(String token, String email, Model model) { // 링크를 통해 전달한 토큰과 이메일정보를 가지고 토큰의 유효성을 판단하고 유효한 경우 로그인을 수행해 인증정보 업데이트 후 페이지 이동
        Account account = accountRepository.findByEmail(email);
        if (account == null || !account.isValid(token)) { // 토큰이나 이메일이 유효하지 않을 경우 에러
            model.addAttribute("error", "로그인할 수 없습니다.");
            return "account/logged-in-by-email";
        }
        accountService.login(account);
        return "account/logged-in-by-email";
    }
}