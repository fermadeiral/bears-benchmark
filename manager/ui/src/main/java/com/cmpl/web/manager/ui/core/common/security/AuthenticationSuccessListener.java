package com.cmpl.web.manager.ui.core.common.security;

import com.cmpl.web.core.common.user.BackUser;
import com.cmpl.web.manager.ui.core.administration.user.CurrentUser;
import java.time.LocalDateTime;
import java.util.Objects;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.WebAuthenticationDetails;

public class AuthenticationSuccessListener implements
    ApplicationListener<AuthenticationSuccessEvent> {

  private final LoginAttemptsService loginAttemptService;

  private final RedirectStrategy redirectStrategy;

  public AuthenticationSuccessListener(LoginAttemptsService loginAttemptService) {

    this.loginAttemptService = Objects.requireNonNull(loginAttemptService);
    this.redirectStrategy = new DefaultRedirectStrategy();
  }

  public void onApplicationEvent(AuthenticationSuccessEvent e) {
    Authentication auth = e.getAuthentication();
    if (auth.getPrincipal() instanceof BackUser) {
      String identifier = ((CurrentUser) auth.getPrincipal()).getUsername();
      if (auth.getDetails() instanceof WebAuthenticationDetails) {
        WebAuthenticationDetails details = (WebAuthenticationDetails) auth.getDetails();
        loginAttemptService.successfulAttempt(identifier, details.getRemoteAddress());
      }
      LocalDateTime lastPasswordModification = ((CurrentUser) auth.getPrincipal()).getUser()
          .getLastPasswordModification();

      LocalDateTime threeMonths = LocalDateTime.now().minusMonths(3);
      if (lastPasswordModification.compareTo(threeMonths) > 0) {

      }
    }
  }
}
