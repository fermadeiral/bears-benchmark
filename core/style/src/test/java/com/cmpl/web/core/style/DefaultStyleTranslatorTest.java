package com.cmpl.web.core.style;

import com.cmpl.web.core.media.MediaDTOBuilder;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DefaultStyleTranslatorTest {

  @Spy
  private DefaultStyleTranslator translator;

  @Test
  public void testFromUpdateFormToDTO() throws Exception {
    StyleDTO dtoOfForm = StyleDTOBuilder.create().content("someContent")
        .media(MediaDTOBuilder.create().name("someName").id(123456789l).build()).build();
    StyleUpdateForm form = new StyleUpdateForm(dtoOfForm);

    StyleDTO result = translator.fromUpdateFormToDTO(form);
    Assert.assertEquals(form.getId(), result.getId());
    Assert.assertEquals(form.getContent(), result.getContent());
    Assert.assertEquals(form.getMediaId(), result.getMedia().getId());
    Assert.assertEquals(form.getMediaName(), result.getMedia().getName());

  }

  @Test
  public void testFromDTOToResponse() throws Exception {
    StyleDTO dto = StyleDTOBuilder.create().id(123456789l).build();

    StyleResponse result = translator.fromDTOToResponse(dto);
    Assert.assertEquals(dto.getId(), result.getStyle().getId());
  }

}
