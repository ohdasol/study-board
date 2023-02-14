package com.project.board.modules.notification.infra.interceptor;

import com.project.board.modules.notification.infra.repository.NotificationRepository;
import com.project.board.modules.account.domain.UserAccount;
import com.project.board.modules.account.domain.entity.Account;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;
/**
 * 읽지 않은 알림이 있는 경우 내비게이션 바의 알림 아이콘을 변경하는 기능 구현
 *
 * 내비게이션 바는 모든 화면에 적용되기 때문에 모든 API에서 Notification이 있는지 체크하는 로직을 추가하는 것은 비효율적
 * 따라서 HandlerInterceptor로 읽지 않은 메시지가 있는지 확인하여 Model에 담아주는 방법 사용
 * Interceptor 적용 범위가 중요, 리다이렉트 요청과 static 리소스 요청에는 적용되지 않게 해야 됨
 */
@Component
@RequiredArgsConstructor
public class NotificationInterceptor implements HandlerInterceptor {

    private final NotificationRepository notificationRepository;

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (modelAndView != null && !isRedirectView(modelAndView) && authentication != null && isTypeOfUserAccount(authentication)) { // 리다이렉트가 아니고 인증 정보가 존재하고 UserAccount 타입일 경우
            Account account = ((UserAccount) authentication.getPrincipal()).getAccount(); // Account 정보 추출
            long count = notificationRepository.countByAccountAndChecked(account, false); // 알림 정보를 조회
            modelAndView.addObject("hasNotification", count > 0); // 모델로 전달
        }
    }

    private boolean isRedirectView(ModelAndView modelAndView) {
        Optional<ModelAndView> optionalModelAndView = Optional.ofNullable(modelAndView);
        return startsWithRedirect(optionalModelAndView) || isTypeOfRedirectView(optionalModelAndView);
    }

    private Boolean startsWithRedirect(Optional<ModelAndView> optionalModelAndView) {
        return optionalModelAndView.map(ModelAndView::getViewName)
                .map(viewName -> viewName.startsWith("redirect:"))
                .orElse(false);
    }

    private Boolean isTypeOfRedirectView(Optional<ModelAndView> optionalModelAndView) {
        return optionalModelAndView.map(ModelAndView::getView)
                .map(v -> v instanceof RedirectView)
                .orElse(false);
    }

    private boolean isTypeOfUserAccount(Authentication authentication) {
        return authentication.getPrincipal() instanceof
                UserAccount;
    }
}
