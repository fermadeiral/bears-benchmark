package com.cmpl.web.manager.ui.modules.facebook;

import com.cmpl.web.core.common.message.WebMessageSource;
import com.cmpl.web.core.common.notification.NotificationCenter;
import com.cmpl.web.facebook.FacebookDispatcher;
import com.cmpl.web.facebook.FacebookImportRequest;
import com.cmpl.web.facebook.FacebookImportResponse;
import com.cmpl.web.manager.ui.core.common.stereotype.ManagerController;
import com.cmpl.web.modules.facebook.factory.FacebookDisplayFactory;
import java.util.Locale;
import java.util.Objects;
import javax.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

/**
 * Controller pour la gestion de l'import des posts facebook
 *
 * @author Louis
 */
@ManagerController
public class FacebookController {

  private static final Logger LOGGER = LoggerFactory.getLogger(FacebookController.class);

  private final FacebookDisplayFactory facebookDisplayFactory;

  private final FacebookDispatcher facebookDispatcher;

  private final NotificationCenter notificationCenter;

  private final WebMessageSource messageSource;

  public FacebookController(FacebookDisplayFactory facebookDisplayFactory,
      FacebookDispatcher facebookDispatcher,
      NotificationCenter notificationCenter, WebMessageSource messageSource) {

    this.facebookDisplayFactory = Objects.requireNonNull(facebookDisplayFactory);
    this.facebookDispatcher = Objects.requireNonNull(facebookDispatcher);
    this.notificationCenter = Objects.requireNonNull(notificationCenter);
    this.messageSource = Objects.requireNonNull(messageSource);
  }

  /**
   * Mapping pour acceder a la partie facebook
   */
  @GetMapping(value = "/manager/facebook")
  @PreAuthorize("hasAuthority('webmastering:facebook:import')")
  public ModelAndView printFacebookAccess(Locale locale) {
    return facebookDisplayFactory.computeModelAndViewForFacebookAccessPage(locale);
  }

  /**
   * Mapping pour l'affichage des imports possibles
   */
  @GetMapping(value = "/manager/facebook/import")
  @PreAuthorize("hasAuthority('webmastering:facebook:import')")
  public ModelAndView printFacebookImport(Locale locale) {

    return facebookDisplayFactory.computeModelAndViewForFacebookImportPage(locale);
  }

  /**
   * Mapping pour l'import de posts
   */
  @PostMapping(value = "/manager/facebook/import")
  @ResponseBody
  @PreAuthorize("hasAuthority('webmastering:facebook:import')")
  public ResponseEntity<FacebookImportResponse> createNewsEntry(
      @Valid @RequestBody FacebookImportRequest facebookImportRequest, BindingResult bindingResult,
      Locale locale) {

    LOGGER.info("Tentative de création d'entrées de blog venant de facebook");
    if (bindingResult.hasErrors()) {
      notificationCenter.sendNotification("create.error", bindingResult, locale);
      LOGGER.error("Echec de la creation de l'entrée");
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
    try {
      FacebookImportResponse response = facebookDispatcher
          .createEntity(facebookImportRequest, locale);

      notificationCenter
          .sendNotification("success", messageSource.getMessage("create.success", locale));

      LOGGER.info("Entrées crées");
      return new ResponseEntity<>(response, HttpStatus.CREATED);
    } catch (Exception e) {
      LOGGER.info("Echec de l'import des posts facebook", e);
      notificationCenter
          .sendNotification("danger", messageSource.getMessage("create.error", locale));
      return new ResponseEntity<>(HttpStatus.CONFLICT);
    }

  }
}
