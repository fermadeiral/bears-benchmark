package com.cmpl.web.facebook;

import com.cmpl.web.core.common.exception.BaseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.social.facebook.api.Facebook;
import org.springframework.social.facebook.api.MediaOperations;

public class DefaultFacebookAdapter implements FacebookAdapter {

  private static final Logger LOGGER = LoggerFactory.getLogger(DefaultFacebookAdapter.class);

  private final FacebookService facebookService;

  private final Facebook facebookConnector;

  public DefaultFacebookAdapter(FacebookService facebookService, Facebook facebookConnector) {
    this.facebookService = Objects.requireNonNull(facebookService);
    this.facebookConnector = Objects.requireNonNull(facebookConnector);
  }

  @Override
  public boolean isAlreadyConnected() {
    try {
      facebookService.getRecentFeed();
    } catch (BaseException e) {
      LOGGER.debug("Utilisateur facebook non connecté", e);
      return false;
    }
    return true;
  }

  @Override
  public List<ImportablePost> getRecentFeed() {
    try {
      return facebookService.getRecentFeed();
    } catch (BaseException e) {
      LOGGER.error("Impossible de récupérer le feed récent de l'utilisateur", e);
    }
    return new ArrayList<>();
  }

  @Override
  public MediaOperations getMediaOperations() {
    return facebookConnector.mediaOperations();
  }

}
