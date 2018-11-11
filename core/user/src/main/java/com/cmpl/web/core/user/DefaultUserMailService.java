package com.cmpl.web.core.user;

import com.cmpl.web.core.common.mail.MailSender;
import java.util.Locale;
import java.util.Objects;
import org.thymeleaf.context.Context;

public class DefaultUserMailService implements UserMailService {

  private final MailSender mailSender;

  public DefaultUserMailService(MailSender mailSender) {
    this.mailSender = Objects.requireNonNull(mailSender);

  }

  @Override
  public void sendAccountCreationEmail(UserDTO user, String token, Locale locale) throws Exception {

    Context context = new Context();
    context.setVariable("user", user);
    context.setVariable("token", token);

    this.mailSender
        .sendMail("mails/user-activation", context, "activation.subject", locale, user.getEmail());
  }

  @Override
  public void sendChangePasswordEmail(UserDTO user, String token, Locale locale) throws Exception {

    Context context = new Context();
    context.setVariable("user", user);
    context.setVariable("token", token);

    this.mailSender
        .sendMail("mails/user-change-password", context, "change.subject", locale, user.getEmail());
  }
}
