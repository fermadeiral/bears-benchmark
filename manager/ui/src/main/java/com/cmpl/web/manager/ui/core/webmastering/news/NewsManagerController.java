package com.cmpl.web.manager.ui.core.webmastering.news;

import com.cmpl.web.core.common.context.ContextHolder;
import com.cmpl.web.core.common.message.WebMessageSource;
import com.cmpl.web.core.common.notification.NotificationCenter;
import com.cmpl.web.core.factory.news.NewsManagerDisplayFactory;
import com.cmpl.web.core.news.content.NewsContentRequest;
import com.cmpl.web.core.news.entry.NewsEntryDTO;
import com.cmpl.web.core.news.entry.NewsEntryDispatcher;
import com.cmpl.web.core.news.entry.NewsEntryRequest;
import com.cmpl.web.core.news.entry.NewsEntryResponse;
import com.cmpl.web.core.news.image.NewsImageRequest;
import com.cmpl.web.manager.ui.core.common.stereotype.ManagerController;
import java.util.Locale;
import java.util.Objects;
import javax.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

/**
 * Controller pour la gestion des NewsEntry dans le back office
 *
 * @author Louis
 */
@ManagerController
@RequestMapping(value = "/manager/news")
public class NewsManagerController {

  private static final Logger LOGGER = LoggerFactory.getLogger(NewsManagerController.class);

  private final NewsManagerDisplayFactory newsManagerDisplayFactory;

  private final NewsEntryDispatcher dispatcher;

  private final NotificationCenter notificationCenter;

  private final WebMessageSource messageSource;

  private final ContextHolder contextHolder;


  public NewsManagerController(NewsManagerDisplayFactory newsManagerDisplayFactory,
    NewsEntryDispatcher dispatcher,
    NotificationCenter notificationCenter, WebMessageSource webMessageSource,
    ContextHolder contextHolder) {

    this.newsManagerDisplayFactory = Objects.requireNonNull(newsManagerDisplayFactory);
    this.dispatcher = Objects.requireNonNull(dispatcher);
    this.notificationCenter = Objects.requireNonNull(notificationCenter);
    this.contextHolder = Objects.requireNonNull(contextHolder);
    this.messageSource = Objects.requireNonNull(webMessageSource);

  }

  /**
   * Mapping pour la page d'affichage de toute les NewsEntry
   */
  @GetMapping
  @PreAuthorize("hasAuthority('webmastering:news:read')")
  public ModelAndView printViewNews(@RequestParam(name = "p", required = false) Integer pageNumber,
    Locale locale) {

    int pageNumberToUse = computePageNumberFromRequest(pageNumber);
    return newsManagerDisplayFactory.computeModelAndViewForViewAllNews(locale, pageNumberToUse);
  }

  @GetMapping(value = "/search")
  @PreAuthorize("hasAuthority('webmastering:news:read')")
  public ModelAndView printSearchNews(
    @RequestParam(name = "p", required = false) Integer pageNumber,
    @RequestParam(name = "q") String query, Locale locale) {

    int pageNumberToUse = computePageNumberFromRequest(pageNumber);
    return newsManagerDisplayFactory
      .computeModelAndViewForAllEntitiesTab(locale, pageNumberToUse, query);
  }

  /**
   * Mapping pour la creation d'une NewsEntry
   */
  @GetMapping(value = "/_create")
  @PreAuthorize("hasAuthority('webmastering:news:create')")
  public ModelAndView printCreateNews(Locale locale) {

    return newsManagerDisplayFactory.computeModelAndViewForBackPageCreateNews(locale);
  }

  /**
   * Mapping pour la creation d'une NewsEntry
   */
  @PostMapping(produces = "application/json")
  @ResponseBody
  @PreAuthorize("hasAuthority('webmastering:news:create')")
  public ResponseEntity<NewsEntryResponse> createNewsEntry(
    @Valid @RequestBody NewsEntryRequest newsEntryRequest,
    BindingResult bindingResult, Locale locale) {

    LOGGER.info("Tentative de création d'une entrée de blog");
    if (bindingResult.hasErrors()) {
      notificationCenter.sendNotification("create.error", bindingResult, locale);
      LOGGER.error("Echec de la creation de l'entrée");
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
    try {
      NewsEntryResponse response = dispatcher.createEntity(newsEntryRequest, locale);

      LOGGER.info("Entrée crée, id " + response.getNewsEntry().getId());

      notificationCenter
        .sendNotification("success", messageSource.getMessage("create.success", locale));

      return new ResponseEntity<>(response, HttpStatus.CREATED);
    } catch (Exception e) {
      LOGGER.error("Echec de la creation de l'entrée", e);
      notificationCenter
        .sendNotification("danger", messageSource.getMessage("create.error", locale));
      return new ResponseEntity<>(HttpStatus.CONFLICT);
    }

  }

  /**
   * Mapping pour la modification d'une NewsEntry
   */
  @PutMapping(value = "/{newsEntryId}", produces = "application/json")
  @ResponseBody
  @PreAuthorize("hasAuthority('webmastering:news:write')")
  public ResponseEntity<NewsEntryResponse> updateNewsEntry(
    @Valid @PathVariable(value = "newsEntryId") String newsEntryId,
    @RequestBody NewsEntryRequest newsEntryRequest,
    BindingResult bindingResult, Locale locale) {

    LOGGER.info("Tentative de mise à jour d'une entrée de blog d'id " + newsEntryId);
    if (bindingResult.hasErrors()) {
      notificationCenter.sendNotification("update.error", bindingResult, locale);
      LOGGER.error("Echec de la modification de l'entrée");
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
    try {
      NewsEntryResponse response = dispatcher.updateEntity(newsEntryRequest, newsEntryId, locale);

      notificationCenter
        .sendNotification("success", messageSource.getMessage("update.success", locale));
      LOGGER.info("Entrée modifiée, id " + newsEntryId);

      return new ResponseEntity<>(response, HttpStatus.OK);
    } catch (Exception e) {
      LOGGER.error("Echec de la mise à jour de l'entrée d'id " + newsEntryId, e);
      notificationCenter
        .sendNotification("danger", messageSource.getMessage("update.error", locale));
      return new ResponseEntity<>(HttpStatus.CONFLICT);
    }

  }

  /**
   * Mapping pour la suppression d'une NewsEntry
   */
  @DeleteMapping(value = "/{newsEntryId}", produces = "application/json")
  @ResponseBody
  @PreAuthorize("hasAuthority('webmastering:news:delete')")
  public ResponseEntity<NewsEntryResponse> deleteNewsEntry(
    @PathVariable(value = "newsEntryId") String newsEntryId,
    Locale locale) {
    LOGGER.info("Tentative de suppression d'une entrée d'id " + newsEntryId);
    NewsEntryResponse response = dispatcher.deleteEntity(newsEntryId, locale);
    notificationCenter
      .sendNotification("success", messageSource.getMessage("delete.success", locale));
    LOGGER.info("NewsEntry " + newsEntryId + " supprimée");
    return new ResponseEntity<>(response, HttpStatus.NO_CONTENT);
  }

  /**
   * Mapping pour la recuperation d'une NewsEntry
   */
  @GetMapping(value = "/{newsEntryId}")
  @PreAuthorize("hasAuthority('webmastering:news:read')")
  public ModelAndView getNewsEntity(@PathVariable(value = "newsEntryId") String newsEntryId,
    Locale locale) {

    LOGGER.info("Récupération de l'entrée d'id " + newsEntryId);
    return newsManagerDisplayFactory.computeModelAndViewForOneNewsEntry(locale, newsEntryId);
  }

  int computePageNumberFromRequest(Integer pageNumber) {
    if (pageNumber == null) {
      return 0;
    }
    return pageNumber.intValue();

  }

  @PostMapping(value = "/{newsEntryId}/media", consumes = "multipart/form-data")
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize("hasAuthority('webmastering:media:create')")
  public void uploadNewsImage(@RequestParam("media") MultipartFile uploadedMedia,
    @PathVariable(value = "newsEntryId") String newsEntryId, Locale locale) {
    if (uploadedMedia.isEmpty()) {
      notificationCenter
        .sendNotification("danger", messageSource.getMessage("update.error", locale));
      return;
    }
    try {
      dispatcher.saveNewsMedia(newsEntryId, uploadedMedia);
      notificationCenter
        .sendNotification("success", messageSource.getMessage("create.success", locale));
    } catch (Exception e) {
      notificationCenter
        .sendNotification("danger", messageSource.getMessage("update.error", locale));
      LOGGER.error("Cannot save multipart file !", e);
    }
  }

  @GetMapping(value = "/{newsEntryId}/_main")
  @PreAuthorize("hasAuthority('webmastering:news:read')")
  public ModelAndView printUpdateNewsMain(@PathVariable(value = "newsEntryId") String newsEntryId,
    Locale locale) {

    return newsManagerDisplayFactory.computeModelAndViewForUpdateNewsMain(newsEntryId, locale);
  }

  @GetMapping(value = "/{newsEntryId}/_content")
  @PreAuthorize("hasAuthority('webmastering:news:read')")
  public ModelAndView printUpdateNewsContent(
    @PathVariable(value = "newsEntryId") String newsEntryId, Locale locale) {

    return newsManagerDisplayFactory.computeModelAndViewForUpdateNewsContent(newsEntryId, locale);
  }

  @PutMapping(value = "/{newsEntryId}/content")
  @PreAuthorize("hasAuthority('webmastering:news:write')")
  public ResponseEntity<NewsEntryResponse> handleUpdateContent(
    @PathVariable(value = "newsEntryId") String newsEntryId,
    @Valid @RequestBody NewsContentRequest request, BindingResult bindingResult, Locale locale) {
    LOGGER.info("Tentative de modification du contenu d'une entrée d'id " + newsEntryId);
    if (bindingResult.hasErrors()) {
      notificationCenter.sendNotification("update.error", bindingResult, locale);
      LOGGER.error("Echec de la modification de l'entrée");
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
    try {
      NewsEntryResponse response = dispatcher.updateContent(request, newsEntryId, locale);

      notificationCenter
        .sendNotification("success", messageSource.getMessage("update.success", locale));
      LOGGER.info("Entrée modifiée, id " + newsEntryId);

      return new ResponseEntity<>(response, HttpStatus.OK);
    } catch (Exception e) {
      LOGGER.error("Echec de la mise à jour de l'entrée d'id " + newsEntryId, e);
      notificationCenter
        .sendNotification("danger", messageSource.getMessage("update.error", locale));
      return new ResponseEntity<>(HttpStatus.CONFLICT);
    }

  }

  @GetMapping(value = "/{newsEntryId}/_image")
  @PreAuthorize("hasAuthority('webmastering:news:read')")
  public ModelAndView printUpdateNewsImage(@PathVariable(value = "newsEntryId") String newsEntryId,
    Locale locale) {
    return newsManagerDisplayFactory.computeModelAndViewForUpdateNewsImage(newsEntryId, locale);
  }

  @PutMapping(value = "/{newsEntryId}/_image")
  @PreAuthorize("hasAuthority('webmastering:news:write')")
  public ResponseEntity<NewsEntryResponse> handleUpdateImage(
    @PathVariable(value = "newsEntryId") String newsEntryId,
    @Valid @RequestBody NewsImageRequest request, BindingResult bindingResult, Locale locale) {
    LOGGER.info("Tentative de modification de l'image d'une entrée d'id " + newsEntryId);
    if (bindingResult.hasErrors()) {
      notificationCenter.sendNotification("update.error", bindingResult, locale);
      LOGGER.error("Echec de la modification de l'entrée");
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
    try {
      NewsEntryResponse response = dispatcher.updateImage(request, newsEntryId, locale);

      notificationCenter
        .sendNotification("success", messageSource.getMessage("update.success", locale));
      LOGGER.info("Entrée modifiée, id " + newsEntryId);

      return new ResponseEntity<>(response, HttpStatus.OK);
    } catch (Exception e) {
      LOGGER.error("Echec de la mise à jour de l'entrée d'id " + newsEntryId, e);
      notificationCenter
        .sendNotification("danger", messageSource.getMessage("update.error", locale));
      return new ResponseEntity<>(HttpStatus.CONFLICT);
    }

  }

  @GetMapping(value = "/{newsEntryId}/_memberships")
  @PreAuthorize("hasAuthority('webmastering:news:read')")
  public ModelAndView printUpdateNewsMembership(
    @PathVariable(value = "newsEntryId") String newsEntryId) {
    return newsManagerDisplayFactory.computeModelAndViewForMembership(newsEntryId);
  }


  @GetMapping(value = "/ajaxSearch")
  @PreAuthorize("hasAuthority('webmastering:news:read')")
  @ResponseBody
  public ResponseEntity<Page<NewsEntryDTO>> searchNewsForSelect(
    @RequestParam(name = "page", required = false) Integer pageNumber,
    @RequestParam(name = "q", required = false) String query) {
    if (pageNumber == null) {
      pageNumber = 0;
    }
    if (!StringUtils.hasText(query)) {
      query = "";
    }
    PageRequest pageRequest = PageRequest.of(pageNumber, contextHolder.getElementsPerPage(),
      Sort.by(Direction.ASC, "name"));
    return new ResponseEntity<>(dispatcher.searchEntities(pageRequest, query), HttpStatus.OK);
  }

}
