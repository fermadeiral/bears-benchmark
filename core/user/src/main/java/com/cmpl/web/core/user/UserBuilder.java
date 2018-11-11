package com.cmpl.web.core.user;

import com.cmpl.web.core.common.builder.BaseBuilder;
import com.cmpl.web.core.models.User;
import java.time.LocalDateTime;

public class UserBuilder extends BaseBuilder<User> {

  private String login;

  private String password;

  private LocalDateTime lastConnection;

  private String email;

  private String description;

  private LocalDateTime lastPasswordModification;

  private UserBuilder() {

  }

  public UserBuilder login(String login) {
    this.login = login;
    return this;
  }

  public UserBuilder password(String password) {
    this.password = password;
    return this;
  }

  public UserBuilder lastConnection(LocalDateTime lastConnection) {
    this.lastConnection = lastConnection;
    return this;
  }

  public UserBuilder lastPasswordModification(LocalDateTime lastPasswordModification) {
    this.lastPasswordModification = lastPasswordModification;
    return this;
  }

  public UserBuilder email(String email) {
    this.email = email;
    return this;
  }

  public UserBuilder description(String description) {
    this.description = description;
    return this;
  }

  @Override
  public User build() {
    User user = new User();
    user.setDescription(description);
    user.setEmail(email);
    user.setLastConnection(lastConnection);
    user.setLastPasswordModification(lastPasswordModification);
    user.setLogin(login);
    user.setPassword(password);
    user.setCreationDate(creationDate);
    user.setCreationUser(creationUser);
    user.setModificationUser(modificationUser);
    user.setId(id);
    user.setModificationDate(modificationDate);
    return user;
  }

  public static UserBuilder create() {
    return new UserBuilder();
  }

}
