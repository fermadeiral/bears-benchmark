package com.cmpl.web.core.user;

import com.cmpl.web.core.common.form.BaseUpdateForm;
import java.time.LocalDateTime;
import javax.validation.constraints.NotEmpty;
import org.springframework.format.annotation.DateTimeFormat;

public class UserUpdateForm extends BaseUpdateForm<UserDTO> {

  @NotEmpty(message = "empty.user.login")
  private String login;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private LocalDateTime lastConnection;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private LocalDateTime lastPasswordModification;

  @NotEmpty(message = "empty.user.email")
  private String email;

  private String description;

  public UserUpdateForm() {

  }

  public UserUpdateForm(UserDTO userDTO) {
    super(userDTO);
    this.login = userDTO.getLogin();
    this.lastConnection = userDTO.getLastConnection();
    this.email = userDTO.getEmail();
    this.description = userDTO.getDescription();
    this.lastPasswordModification = userDTO.getLastPasswordModification();
  }

  public String getLogin() {
    return login;
  }

  public void setLogin(String login) {
    this.login = login;
  }

  public LocalDateTime getLastConnection() {
    return lastConnection;
  }

  public void setLastConnection(LocalDateTime lastConnection) {
    this.lastConnection = lastConnection;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public LocalDateTime getLastPasswordModification() {
    return lastPasswordModification;
  }

  public void setLastPasswordModification(LocalDateTime lastPasswordModification) {
    this.lastPasswordModification = lastPasswordModification;
  }
}
