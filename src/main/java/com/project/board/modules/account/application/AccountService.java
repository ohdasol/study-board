package com.project.board.modules.account.application;

import com.project.board.infra.config.AppProperties;
import com.project.board.infra.mail.EmailMessage;
import com.project.board.infra.mail.EmailService;
import com.project.board.modules.tag.domain.entity.Tag;
import com.project.board.modules.account.domain.UserAccount;
import com.project.board.modules.account.domain.entity.Account;
import com.project.board.modules.account.domain.entity.Zone;
import com.project.board.modules.account.endpoint.controller.form.NotificationForm;
import com.project.board.modules.account.endpoint.controller.form.Profile;
import com.project.board.modules.account.endpoint.controller.form.SignUpForm;
import com.project.board.modules.account.infra.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional // 해당 어노테이션이 적용되는 클래스, 메서드를 하나의 트랜잭션으로 묶어주는 역할
@Slf4j
public class AccountService implements UserDetailsService {

    private final AccountRepository accountRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final TemplateEngine templateEngine;
    private final AppProperties appProperties;

    public Account signUp(SignUpForm signUpForm) {
        Account newAccount = saveNewAccount(signUpForm);
        sendVerificationEmail(newAccount);
        return newAccount;
    }

    private Account saveNewAccount(SignUpForm signUpForm) {
        Account account = Account.with(signUpForm.getEmail(), signUpForm.getNickname(), passwordEncoder.encode(signUpForm.getPassword()));
        account.generateToken();
        return accountRepository.save(account);
    }

    // 회원 가입시 이메일 전
    public void sendVerificationEmail(Account newAccount) {
        Context context = new Context();
        context.setVariable("link", String.format("/check-email-token?token=%s&email=%s", newAccount.getEmailToken(),
                newAccount.getEmail()));
        context.setVariable("nickname", newAccount.getNickname());
        context.setVariable("linkName", "이메일 인증하기");
        context.setVariable("message", "Study With Me 가입 인증을 위해 링크를 클릭하세요.");
        context.setVariable("host", appProperties.getHost());
        String message = templateEngine.process("mail/simple-link", context);
        emailService.sendEmail(EmailMessage.builder()
                .to(newAccount.getEmail())
                .subject("Study With Me 회원 가입 인증")
                .message(message)
                .build());
    }

    public Account findAccountByEmail(String email) {
        return accountRepository.findByEmail(email);
    }

    /**
     * SecurityContextHolder.getContext()로 SecurityContext를 추출, 전역에서 호출할 수 있고 하나의 Context 객체가 반환
     * setAuthentication을 이용해 인증 토큰을 전달할 수 있는데 이 때 전달해야할 토큰이 UsernamePasswordAuthenticationToken
     * UsernamePasswordAuthenticationToken의 생성자로 nickname, password, Role을 각각 전달
     * Role은 인가(권한) 개념, 현재 USER 레벨(사용자)로 정의
     */
    public void login(Account account) {
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(new UserAccount(account),
                account.getPassword(), Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(token); // AuthenticationManager를 쓰는 방법이 정석적인 방ㅇ법
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Account account = Optional.ofNullable(accountRepository.findByEmail(username))
                .orElse(accountRepository.findByNickname(username));
        if (account == null) {
            throw new UsernameNotFoundException(username);
        }
        return new UserAccount(account);
    }

    public void verify(Account account) {
        account.verified();
        login(account); 
    }

    public void updateProfile(Account account, Profile profile) {
        account.updateProfile(profile);
        accountRepository.save(account);
    }

    public void updatePassword(Account account, String newPassword) {
        account.updatePassword(passwordEncoder.encode(newPassword));
        accountRepository.save(account);
    }

    public void updateNotification(Account account, NotificationForm notificationForm) {
        account.updateNotification(notificationForm);
        accountRepository.save(account);
    }

    public void updateNickname(Account account, String nickname) {
        account.updateNickname(nickname);
        accountRepository.save(account);
        login(account); // 로그인을 다시 호출해 인증 정보를 갱신하여 변경된 닉네임 표시
    }

    // 이메일로 로그인
    public void sendLoginLink(Account account) {
        Context context = new Context();
        context.setVariable("link", "/login-by-email?token=" + account.getEmailToken() + "&email=" + account.getEmail());
        context.setVariable("nickname", account.getNickname());
        context.setVariable("linkName", "Study With Me 로그인하기");
        context.setVariable("message", "로그인 하려면 아래 링크를 클릭하세요.");
        context.setVariable("host", appProperties.getHost());
        String message = templateEngine.process("mail/simple-link", context);
        account.generateToken();
        emailService.sendEmail(EmailMessage.builder()
                .to(account.getEmail())
                .subject("[Study With Me] 로그인 링크")
                .message(message)
                .build());
    }

    public void addTag(Account account, Tag tag) { // 계정 정보를 먼저 찾고 계정이 존재하면 태그 추가
        accountRepository.findById(account.getId())
                .ifPresent(a -> a.getTags().add(tag));
    }

    public Set<Tag> getTags(Account account) { // 계정 정보를 찾고 계정 정보가 존재하지 않으면 예외를 던지고, 존재하면 계정이 가진 태그 반환
        return accountRepository.findById(account.getId()).orElseThrow().getTags();
    }

    public void removeTag(Account account, Tag tag) { // 계정 정보를 찾고 계정 정보가 존재하면 그 계정이 가지는 태그 정보를 가져와 전달한 태그 삭제
        accountRepository.findById(account.getId())
                .map(Account::getTags)
                .ifPresent(tags -> tags.remove(tag));
    }

    // 지역 정보
    public Set<Zone> getZones(Account account) {
        return accountRepository.findById(account.getId())
                .orElseThrow()
                .getZones();
    }

    public void addZone(Account account, Zone zone) {
        accountRepository.findById(account.getId())
                .ifPresent(a -> a.getZones().add(zone));
    }

    public void removeZone(Account account, Zone zone) {
        accountRepository.findById(account.getId())
                .ifPresent(a -> a.getZones().remove(zone));
    }

    public Account getAccountBy(String nickname) {
        return Optional.ofNullable(accountRepository.findByNickname(nickname))
                .orElseThrow(() -> new IllegalArgumentException(nickname + "에 해당하는 사용자가 없습니다."));
    }
}
