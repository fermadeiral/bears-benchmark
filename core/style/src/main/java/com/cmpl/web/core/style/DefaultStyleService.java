package com.cmpl.web.core.style;

import com.cmpl.web.core.common.service.DefaultBaseService;
import com.cmpl.web.core.file.FileService;
import com.cmpl.web.core.media.MediaService;
import com.cmpl.web.core.models.Style;
import java.util.Objects;

public class DefaultStyleService extends DefaultBaseService<StyleDTO, Style> implements
    StyleService {

  private final StyleDAO styleDAO;

  private final MediaService mediaService;

  private final FileService fileService;

  public DefaultStyleService(StyleDAO styleDAO, StyleMapper styleMapper, MediaService mediaService,
      FileService fileService) {
    super(styleDAO, styleMapper);
    this.styleDAO = styleDAO;
    this.mediaService = Objects.requireNonNull(mediaService);
    this.fileService = Objects.requireNonNull(fileService);
  }

  @Override
  public StyleDTO getStyle() {
    return mapper.toDTO(styleDAO.getStyle());
  }

  @Override
  public StyleDTO updateEntity(StyleDTO dto) {

    String content = dto.getContent();
    fileService.saveMediaOnSystem(dto.getMedia().getName(), content.getBytes());

    return mapper.toDTO(styleDAO.updateEntity(mapper.toEntity(dto)));

  }

  @Override
  public StyleDTO createEntity(StyleDTO dto) {
    mediaService.createEntity(dto.getMedia());
    String content = dto.getContent();
    fileService.saveMediaOnSystem(dto.getMedia().getName(), content.getBytes());

    return mapper.toDTO(styleDAO.createEntity(mapper.toEntity(dto)));

  }

}
