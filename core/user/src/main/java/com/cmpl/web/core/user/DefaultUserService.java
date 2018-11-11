package com.cmpl.web.core.user;

import com.cmpl.web.core.common.service.DefaultBaseService;
import com.cmpl.web.core.common.user.ActionToken;
import com.cmpl.web.core.common.user.ActionTokenService;
import com.cmpl.web.core.models.User;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

public class DefaultUserService extends DefaultBaseService<UserDTO, User> implements UserService {

  private static final Logger LOGGER = LoggerFactory.getLogger(DefaultUserService.class);

  private static final String CREATION_DATE_PARAMETER = "creationDate";

  private final UserDAO userDAO;

  private final ActionTokenService tokenService;

  private final UserMailService userMailService;

  public DefaultUserService(ActionTokenService tokenService, UserMailService userMailService,
      UserDAO userDAO,
      UserMapper userMapper) {
    super(userDAO, userMapper);
    this.tokenService = tokenService;
    this.userMailService = userMailService;
    this.userDAO = userDAO;
  }

  @Override
  public UserDTO updateEntity(UserDTO dto) {
    UserDTO updatedUser = super.updateEntity(dto);
    return updatedUser;
  }

  @Override
  public UserDTO findByLogin(String login) {
    User user = userDAO.findByLogin(login);
    if (user == null) {
      return null;
    }
    return mapper.toDTO(user);
  }

  @Override
  public UserDTO findByEmail(String email) {
    User user = userDAO.findByEmail(email);
    if (user == null) {
      return null;
    }
    return mapper.toDTO(user);
  }

  @Override
  public Page<UserDTO> getPagedEntities(PageRequest pageRequest) {
    return super.getPagedEntities(pageRequest);
  }

  @Override
  public void askPasswordChange(long userId, Locale locale) throws Exception {
    UserDTO user = getEntity(userId);
    userMailService.sendChangePasswordEmail(user, generatePasswordResetToken(user), locale);
  }

  @Override
  public UserDTO createUser(UserDTO dto, Locale locale) {
    UserDTO createdUser = createEntity(dto);
    try {
      userMailService
          .sendAccountCreationEmail(createdUser, generateActivationToken(createdUser), locale);
    } catch (Exception e) {
      LOGGER.error("Impossible d'envoyer le mail d'activation de compte", e);
    }
    return createdUser;
  }

  @Override
  public UserDTO getEntity(Long userId) {
    UserDTO fetchedUser = super.getEntity(userId);

    return fetchedUser;
  }

  @Override
  @Transactional
  public UserDTO createEntity(UserDTO dto) {
    UserDTO createdUser = super.createEntity(dto);

    return createdUser;

  }

  @Override
  public UserDTO updateLastConnection(Long userId, LocalDateTime connectionDateTime) {
    User result = userDAO.getEntity(userId);
    if (result == null) {
      return null;
    }
    UserDTO user = mapper.toDTO(result);
    user.setLastConnection(connectionDateTime);
    user = mapper.toDTO(userDAO.updateEntity(mapper.toEntity(user)));

    return user;

  }

  String generateActivationToken(UserDTO user) {
    ActionToken actionToken = new ActionToken();
    actionToken.setAction(UserService.USER_ACTIVATION);
    actionToken.setUserId(user.getId());
    actionToken.setExpirationDate(Instant.now().plus(3, ChronoUnit.DAYS));

    Map<String, String> additionalParameters = new HashMap<>();
    additionalParameters.put(CREATION_DATE_PARAMETER, Long.toString(Instant.now().toEpochMilli()));
    actionToken.setAdditionalParameters(additionalParameters);

    return tokenService.generateToken(actionToken);
  }

  @Override
  public String generatePasswordResetToken(UserDTO user) {
    ActionToken actionToken = new ActionToken();
    actionToken.setAction(UserService.USER_RESET_PASSWORD);
    actionToken.setUserId(user.getId());
    actionToken.setExpirationDate(Instant.now().plus(3, ChronoUnit.HOURS));

    Map<String, String> additionalParameters = new HashMap<>();
    additionalParameters.put(CREATION_DATE_PARAMETER, Long.toString(Instant.now().toEpochMilli()));
    actionToken.setAdditionalParameters(additionalParameters);

    return tokenService.generateToken(actionToken);
  }
}
