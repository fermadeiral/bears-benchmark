package com.cmpl.web.core.news.entry;

import com.cmpl.web.core.file.FileService;
import com.cmpl.web.core.media.MediaDTO;
import com.cmpl.web.core.media.MediaDTOBuilder;
import com.cmpl.web.core.media.MediaService;
import com.cmpl.web.core.news.content.NewsContentRequest;
import com.cmpl.web.core.news.image.NewsImageRequest;
import java.io.IOException;
import java.util.Locale;
import java.util.Objects;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.multipart.MultipartFile;

/**
 * Implementation du dispatcher pour les NewsEntry
 *
 * @author Louis
 */
public class DefaultNewsEntryDispatcher implements NewsEntryDispatcher {

  private final NewsEntryTranslator translator;

  private final NewsEntryService newsEntryService;

  private final FileService fileService;

  private final MediaService mediaService;

  private static final String MEDIA_CONTROLLER_PATH = "/public/medias/";

  public DefaultNewsEntryDispatcher(NewsEntryTranslator translator,
    NewsEntryService newsEntryService,
    FileService fileService, MediaService mediaService) {

    this.translator = Objects.requireNonNull(translator);
    this.newsEntryService = Objects.requireNonNull(newsEntryService);
    this.fileService = Objects.requireNonNull(fileService);
    this.mediaService = Objects.requireNonNull(mediaService);

  }

  @Override
  public NewsEntryResponse createEntity(NewsEntryRequest newsEntryRequest, Locale locale) {

    NewsEntryDTO entityToCreate = translator.fromRequestToDTO(newsEntryRequest);
    NewsEntryDTO entityCreated = newsEntryService.createEntity(entityToCreate);

    return translator.fromDTOToResponse(entityCreated);
  }

  @Override
  public NewsEntryResponse updateEntity(NewsEntryRequest newsEntryRequest, String newsEntryId,
    Locale locale) {

    newsEntryRequest.setId(Long.parseLong(newsEntryId));
    NewsEntryDTO entityToUpdate = translator.fromRequestToDTO(newsEntryRequest);

    NewsEntryDTO entityUpdated = newsEntryService.updateEntity(entityToUpdate);

    return translator.fromDTOToResponse(entityUpdated);

  }

  @Override
  public NewsEntryResponse updateContent(NewsContentRequest newsContentRequest, String newsEntryId,
    Locale locale) {

    NewsEntryDTO newsEntryDTO = newsEntryService.getEntity(Long.parseLong(newsEntryId));
    newsEntryDTO.setNewsContent(translator.fromRequestToDTO(newsContentRequest));
    NewsEntryDTO updatedNewsEntry = newsEntryService.updateEntity(newsEntryDTO);

    return translator.fromDTOToResponse(updatedNewsEntry);
  }

  @Override
  public NewsEntryResponse updateImage(NewsImageRequest newsImageRequest, String newsEntryId,
    Locale locale) {
    NewsEntryDTO newsEntryDTO = newsEntryService.getEntity(Long.parseLong(newsEntryId));
    newsEntryDTO.setNewsImage(translator.fromRequestToDTO(newsImageRequest));
    NewsEntryDTO updatedNewsEntry = newsEntryService.updateEntity(newsEntryDTO);

    return translator.fromDTOToResponse(updatedNewsEntry);
  }

  @Override
  public NewsEntryResponse deleteEntity(String newsEntryId, Locale locale) {

    newsEntryService.deleteEntity(Long.parseLong(newsEntryId));
    return NewsEntryResponseBuilder.create().build();
  }

  @Override
  public void saveNewsMedia(String newsEntryId, MultipartFile uploadedMedia) throws IOException {

    String extension = uploadedMedia.getOriginalFilename().split("\\.")[1];
    if (extension != null) {
      extension = extension.toLowerCase();
    }
    String fileName = "image_" + newsEntryId + "." + extension;

    fileService.saveMediaOnSystem(fileName, uploadedMedia.getBytes());

    MediaDTO mediaToSave = mediaService.findByName(fileName);
    MediaDTO mediaSaved;
    if (mediaToSave == null) {
      mediaToSave = MediaDTOBuilder.create().name(fileName)
        .contentType(uploadedMedia.getContentType())
        .extension(extension).size(uploadedMedia.getSize()).src(MEDIA_CONTROLLER_PATH + fileName)
        .build();
      mediaSaved = mediaService.createEntity(mediaToSave);
    } else {
      mediaToSave.setContentType(uploadedMedia.getContentType());
      mediaToSave.setExtension(extension);
      mediaToSave.setSize(uploadedMedia.getSize());
      mediaSaved = mediaService.updateEntity(mediaToSave);

    }

    NewsEntryDTO newsEntryToUpdate = newsEntryService.getEntity(Long.parseLong(newsEntryId));
    newsEntryToUpdate.getNewsImage().setMedia(mediaSaved);
    newsEntryService.updateEntity(newsEntryToUpdate);

  }

  @Override
  public Page<NewsEntryDTO> searchEntities(PageRequest pageRequest, String query) {
    return newsEntryService.searchEntities(pageRequest, query);
  }

}
