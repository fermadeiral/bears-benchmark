package com.cmpl.web.core.page;

import com.cmpl.web.core.common.builder.BaseBuilder;

public class PageDTOBuilder extends BaseBuilder<PageDTO> {

  private String name;

  private String menuTitle;

  private String body;

  private String header;

  private String footer;

  private String meta;

  private String amp;

  private String href;

  private boolean indexed;


  private PageDTOBuilder() {

  }

  public PageDTOBuilder name(String name) {
    this.name = name;
    return this;
  }

  public PageDTOBuilder href(String href) {
    this.href = href;
    return this;
  }

  public PageDTOBuilder menuTitle(String menuTitle) {
    this.menuTitle = menuTitle;
    return this;
  }

  public PageDTOBuilder body(String body) {
    this.body = body;
    return this;
  }

  public PageDTOBuilder header(String header) {
    this.header = header;
    return this;
  }

  public PageDTOBuilder footer(String footer) {
    this.footer = footer;
    return this;
  }

  public PageDTOBuilder meta(String meta) {
    this.meta = meta;
    return this;
  }

  public PageDTOBuilder amp(String amp) {
    this.amp = amp;
    return this;
  }

  public PageDTOBuilder indexed(boolean indexed) {
    this.indexed = indexed;
    return this;
  }

  @Override
  public PageDTO build() {
    PageDTO pageDTO = new PageDTO();
    pageDTO.setBody(body);

    pageDTO.setHref(href);

    pageDTO.setFooter(footer);
    pageDTO.setMeta(meta);
    pageDTO.setHeader(header);

    pageDTO.setMenuTitle(menuTitle);

    pageDTO.setName(name);
    pageDTO.setAmp(amp);
    pageDTO.setIndexed(indexed);
    pageDTO.setCreationDate(creationDate);
    pageDTO.setCreationUser(creationUser);
    pageDTO.setModificationUser(modificationUser);
    pageDTO.setId(id);
    pageDTO.setModificationDate(modificationDate);

    return pageDTO;
  }

  public static PageDTOBuilder create() {
    return new PageDTOBuilder();
  }

}
