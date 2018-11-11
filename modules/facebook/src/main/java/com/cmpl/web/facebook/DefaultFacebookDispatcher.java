package com.cmpl.web.facebook;

import com.cmpl.web.core.common.exception.BaseException;
import com.cmpl.web.core.news.entry.NewsEntryDTO;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Implementation du dispatcher pour facebook import
 *
 * @author Louis
 */
public class DefaultFacebookDispatcher implements FacebookDispatcher {

  private final FacebookImportService facebookImportService;

  private final FacebookImportTranslator facebookImportTranslator;

  public DefaultFacebookDispatcher(FacebookImportService facebookImportService,
      FacebookImportTranslator facebookImportTranslator) {
    this.facebookImportService = Objects.requireNonNull(facebookImportService);

    this.facebookImportTranslator = Objects.requireNonNull(facebookImportTranslator);

  }

  @Override
  public FacebookImportResponse createEntity(FacebookImportRequest facebookImportRequest,
      Locale locale)
      throws BaseException {

    List<NewsEntryDTO> createdEntries = facebookImportService
        .importFacebookPost(facebookImportTranslator.fromRequestToPosts(facebookImportRequest),
            locale);
    return facebookImportTranslator.fromDTOToResponse(createdEntries);
  }

}
