package com.cmpl.web.facebook;

import com.cmpl.web.core.news.entry.NewsEntryDTO;
import com.cmpl.web.core.news.entry.NewsEntryDTOBuilder;
import java.util.Arrays;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DefaultFacebookImportTranslatorTest {

  @InjectMocks
  @Spy
  private DefaultFacebookImportTranslator translator;

  @Test
  public void testFromRequestToPosts() throws Exception {
    FacebookImportRequest request = new FacebookImportRequest();

    FacebookImportPost post = new FacebookImportPost();

    request.setPostsToImport(Arrays.asList(post));

    List<FacebookImportPost> result = translator.fromRequestToPosts(request);

    Assert.assertEquals(request.getPostsToImport(), result);
  }

  @Test
  public void testFromDTOToResponse() throws Exception {

    NewsEntryDTO dto = NewsEntryDTOBuilder.create().id(123456789L).build();
    List<NewsEntryDTO> dtos = Arrays.asList(dto);

    FacebookImportResponse result = translator.fromDTOToResponse(dtos);

    Assert.assertEquals(dtos, result.getCreatedNewsEntries());

  }

}
