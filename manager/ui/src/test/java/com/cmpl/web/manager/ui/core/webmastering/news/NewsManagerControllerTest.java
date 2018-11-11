package com.cmpl.web.manager.ui.core.webmastering.news;

import com.cmpl.web.core.common.context.ContextHolder;
import com.cmpl.web.core.common.exception.BaseException;
import com.cmpl.web.core.common.message.WebMessageSource;
import com.cmpl.web.core.common.notification.NotificationCenter;
import com.cmpl.web.core.factory.news.NewsManagerDisplayFactory;
import com.cmpl.web.core.news.entry.NewsEntryDTO;
import com.cmpl.web.core.news.entry.NewsEntryDTOBuilder;
import com.cmpl.web.core.news.entry.NewsEntryDispatcher;
import com.cmpl.web.core.news.entry.NewsEntryRequest;
import com.cmpl.web.core.news.entry.NewsEntryRequestBuilder;
import com.cmpl.web.core.news.entry.NewsEntryResponse;
import java.util.Locale;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.ModelAndView;

;

@RunWith(MockitoJUnitRunner.class)
public class NewsManagerControllerTest {

  @Mock
  private NewsManagerDisplayFactory newsManagerDisplayFactory;

  @Mock
  private NewsEntryDispatcher dispatcher;

  @Mock
  private NotificationCenter notificationCenter;

  @Mock
  private WebMessageSource messageSource;


  @Mock
  private ContextHolder contextHolder;

  @Spy
  @InjectMocks
  private NewsManagerController controller;

  @Test
  public void testDeleteNewsEntry() throws Exception {
    BindingResult bindingResult = BDDMockito.mock(BindingResult.class);
    ResponseEntity<NewsEntryResponse> result = controller.deleteNewsEntry("666", Locale.FRANCE);

    Assert.assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
  }

  @Test
  public void testGetNewsEntity_Ok() throws Exception {

    ModelAndView model = new ModelAndView("back/news/edit_news_entry");

    BDDMockito.doReturn(model).when(newsManagerDisplayFactory)
      .computeModelAndViewForOneNewsEntry(BDDMockito.eq(Locale.FRANCE), BDDMockito.eq("666"));

    ModelAndView result = controller.getNewsEntity("666", Locale.FRANCE);

    Assert.assertEquals(model, result);
  }

  @Test
  public void testUpdateNewsEntry_Ok() throws Exception {
    NewsEntryRequest request = NewsEntryRequestBuilder.create().build();

    NewsEntryResponse response = new NewsEntryResponse();
    BindingResult bindingResult = BDDMockito.mock(BindingResult.class);
    BDDMockito.given(bindingResult.hasErrors()).willReturn(false);

    BDDMockito.doReturn(response).when(dispatcher)
      .updateEntity(BDDMockito.eq(request), BDDMockito.eq("666"),
        BDDMockito.eq(Locale.FRANCE));
    ResponseEntity<NewsEntryResponse> result = controller
      .updateNewsEntry("666", request, bindingResult, Locale.FRANCE);

    Assert.assertEquals(HttpStatus.OK, result.getStatusCode());
    Assert.assertEquals(response, result.getBody());
  }

  @Test
  public void testCreateNewsEntry_Ok() throws Exception {
    NewsEntryRequest request = NewsEntryRequestBuilder.create().build();

    NewsEntryDTO entryDTO = NewsEntryDTOBuilder.create().id(1L).build();
    NewsEntryResponse response = new NewsEntryResponse();
    response.setNewsEntry(entryDTO);

    BindingResult bindingResult = BDDMockito.mock(BindingResult.class);
    BDDMockito.given(bindingResult.hasErrors()).willReturn(false);

    BDDMockito.doReturn(response).when(dispatcher)
      .createEntity(BDDMockito.eq(request), BDDMockito.eq(Locale.FRANCE));
    ResponseEntity<NewsEntryResponse> result = controller
      .createNewsEntry(request, bindingResult, Locale.FRANCE);

    Assert.assertEquals(HttpStatus.CREATED, result.getStatusCode());
    Assert.assertEquals(response, result.getBody());
  }

  @Test
  public void testCreateNewsEntry_Ko() throws BaseException {

    NewsEntryRequest request = NewsEntryRequestBuilder.create().build();

    BindingResult bindingResult = BDDMockito.mock(BindingResult.class);
    BDDMockito.given(bindingResult.hasErrors()).willReturn(false);

    BDDMockito.doThrow(new BaseException("")).when(dispatcher).createEntity(BDDMockito.eq(request),
      BDDMockito.eq(Locale.FRANCE));

    ResponseEntity<NewsEntryResponse> result = controller
      .createNewsEntry(request, bindingResult, Locale.FRANCE);

    Assert.assertEquals(HttpStatus.CONFLICT, result.getStatusCode());

  }

  @Test
  public void testPrintViewNews() throws Exception {
    ModelAndView model = new ModelAndView("back/news/view_news");

    BDDMockito.doReturn(model).when(newsManagerDisplayFactory)
      .computeModelAndViewForViewAllNews(
        BDDMockito.any(Locale.class), BDDMockito.anyInt());

    ModelAndView result = controller.printViewNews(0, Locale.FRANCE);

    Assert.assertEquals(model, result);
  }
}
