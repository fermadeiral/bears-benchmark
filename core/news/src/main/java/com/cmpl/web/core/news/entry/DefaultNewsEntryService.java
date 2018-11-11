package com.cmpl.web.core.news.entry;

import com.cmpl.web.core.common.service.DefaultBaseService;
import com.cmpl.web.core.models.NewsEntry;
import com.cmpl.web.core.news.content.NewsContentDTO;
import com.cmpl.web.core.news.content.NewsContentService;
import com.cmpl.web.core.news.image.NewsImageDTO;
import com.cmpl.web.core.news.image.NewsImageService;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

/**
 * Implementation de l'interface pour la gestion des NewsEntry
 *
 * @author Louis
 */
public class DefaultNewsEntryService extends DefaultBaseService<NewsEntryDTO, NewsEntry> implements
    NewsEntryService {

  private static final Logger LOGGER = LoggerFactory.getLogger(DefaultNewsEntryService.class);

  private final NewsEntryDAO newsEntryDAO;

  private final NewsImageService newsImageService;

  private final NewsContentService newsContentService;

  public DefaultNewsEntryService(NewsEntryDAO newsEntryDAO, NewsImageService newsImageService,
      NewsContentService newsContentService, NewsEntryMapper newsEntryMapper) {
    super(newsEntryDAO, newsEntryMapper);

    this.newsEntryDAO = newsEntryDAO;
    this.newsImageService = Objects.requireNonNull(newsImageService);
    this.newsContentService = Objects.requireNonNull(newsContentService);
  }

  @Override
  @Transactional
  public NewsEntryDTO createEntity(NewsEntryDTO dto) {

    LOGGER.info("Creation d'une nouvelle entrée de blog");

    dto.setNewsContent(createContent(dto.getNewsContent()));
    dto.setNewsImage(createImage(dto.getNewsImage()));

    return mapper.toDTO(newsEntryDAO.createEntity(mapper.toEntity(dto)));
  }

  NewsContentDTO createContent(NewsContentDTO contentToCreate) {

    if (contentToCreate != null) {
      return newsContentService.createEntity(contentToCreate);
    }
    return null;
  }

  NewsImageDTO createImage(NewsImageDTO imageToCreate) {
    if (imageToCreate != null) {
      return newsImageService.createEntity(imageToCreate);

    }
    return null;
  }

  @Override
  @Transactional
  public NewsEntryDTO updateEntity(NewsEntryDTO dto) {

    LOGGER.info("Mise à jour d'une entrée de blog d'id " + dto.getId());

    dto.setNewsContent(updateContent(dto.getNewsContent()));
    dto.setNewsImage(updateImage(dto.getNewsImage()));

    return mapper.toDTO(newsEntryDAO.updateEntity(mapper.toEntity(dto)));
  }

  NewsContentDTO updateContent(NewsContentDTO contentToUpdate) {
    if (contentToUpdate != null) {
      return dealWithContentToUpdate(contentToUpdate);
    }
    return contentToUpdate;
  }

  NewsImageDTO updateImage(NewsImageDTO imageToUpdate) {
    if (imageToUpdate != null) {
      return dealWithImageToUpdate(imageToUpdate);
    }
    return imageToUpdate;
  }

  NewsContentDTO dealWithContentToUpdate(NewsContentDTO contentToUpdate) {
    NewsContentDTO contentSaved;
    Long contentId = contentToUpdate.getId();
    if (contentId == null) {
      contentSaved = newsContentService.createEntity(contentToUpdate);
    } else {
      contentSaved = newsContentService.updateEntity(contentToUpdate);
    }
    return contentSaved;
  }

  NewsImageDTO dealWithImageToUpdate(NewsImageDTO imageToUpdate) {
    Long imageToUpdateId = imageToUpdate.getId();
    if (imageToUpdateId == null) {
      return newsImageService.createEntity(imageToUpdate);
    } else {
      return newsImageService.updateEntity(imageToUpdate);
    }

  }

  @Override
  public NewsEntryDTO getEntity(Long id) {
    LOGGER.info("Récupération de l'entrée de blog d'id " + id);
    NewsEntry entry = newsEntryDAO.getEntity(id);
    return mapper.toDTO(entry);
  }

  @Override
  public List<NewsEntryDTO> getEntities() {

    LOGGER.info("Récupération de toutes les entrées de blog");
    return mapper.toListDTO(newsEntryDAO.getEntities());
  }

  @Override
  public boolean isAlreadyImportedFromFacebook(String facebookId) {
    return !CollectionUtils.isEmpty(newsEntryDAO.findByFacebookId(facebookId));
  }

  @Override
  public Page<NewsEntryDTO> getPagedEntities(PageRequest pageRequest) {
    return super.getPagedEntities(pageRequest);
  }

}
