package com.cmpl.web.core.factory.news;

import com.cmpl.web.core.breadcrumb.BreadCrumb;
import com.cmpl.web.core.breadcrumb.BreadCrumbBuilder;
import com.cmpl.web.core.common.context.ContextHolder;
import com.cmpl.web.core.common.message.DefaultWebMessageSource;
import com.cmpl.web.core.common.resource.PageWrapper;
import com.cmpl.web.core.factory.menu.MenuFactory;
import com.cmpl.web.core.group.GroupService;
import com.cmpl.web.core.membership.MembershipService;
import com.cmpl.web.core.menu.MenuItem;
import com.cmpl.web.core.menu.MenuItemBuilder;
import com.cmpl.web.core.news.content.NewsContentDTO;
import com.cmpl.web.core.news.content.NewsContentDTOBuilder;
import com.cmpl.web.core.news.content.NewsContentRequest;
import com.cmpl.web.core.news.content.NewsContentRequestBuilder;
import com.cmpl.web.core.news.entry.NewsEntryDTO;
import com.cmpl.web.core.news.entry.NewsEntryDTOBuilder;
import com.cmpl.web.core.news.entry.NewsEntryRequest;
import com.cmpl.web.core.news.entry.NewsEntryRequestBuilder;
import com.cmpl.web.core.news.entry.NewsEntryService;
import com.cmpl.web.core.news.image.NewsImageDTO;
import com.cmpl.web.core.news.image.NewsImageDTOBuilder;
import com.cmpl.web.core.news.image.NewsImageRequest;
import com.cmpl.web.core.news.image.NewsImageRequestBuilder;
import com.cmpl.web.core.page.BackPage;
import com.cmpl.web.core.page.BackPageBuilder;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.PageImpl;
import org.springframework.plugin.core.PluginRegistry;
import org.springframework.web.servlet.ModelAndView;

@RunWith(MockitoJUnitRunner.class)
public class DefaultNewsManagerDisplayFactoryTest {

  @Mock
  private MenuFactory menuFactory;

  @Mock
  private DefaultWebMessageSource messageSource;

  @Mock
  private NewsEntryService newsEntryService;

  @Mock
  private ContextHolder contextHolder;

  @Mock
  private PluginRegistry<BreadCrumb, String> breadCrumbRegistry;

  @Mock
  private PluginRegistry<BackPage, String> backPages;

  @Mock
  private Set<Locale> availableLocales;

  @Mock
  private MembershipService membershipService;

  @Mock
  private GroupService groupService;

  @InjectMocks
  @Spy
  private DefaultNewsManagerDisplayFactory displayFactory;

  @Test
  public void testComputeModelAndViewForOneNewsEntry() throws Exception {

    String tile = "login";
    String href = "/";
    String label = "label";
    String title = "title";
    String decoratorBack = "decorator_back";
    List<MenuItem> subMenuItems = new ArrayList<MenuItem>();
    MenuItem index = MenuItemBuilder.create().href(href).label(label).title(title)
        .subMenuItems(subMenuItems).build();
    MenuItem news = MenuItemBuilder.create().href(href).label(label).title(title)
        .subMenuItems(subMenuItems).build();

    List<MenuItem> backMenu = Arrays.asList(index, news);

    String author = "author";
    LocalDateTime date = LocalDateTime.now();
    String tags = "tag;lol";
    String content = "content";
    String alt = "alt";

    NewsEntryRequest request = NewsEntryRequestBuilder.create().build();

    NewsContentDTO newsContent = NewsContentDTOBuilder.create().content(content).id(1L)
        .creationDate(date)
        .modificationDate(date).build();

    NewsImageDTO newsImage = NewsImageDTOBuilder.create().alt(alt).id(1L).creationDate(date)
        .modificationDate(date)
        .build();

    NewsEntryDTO newsEntry = NewsEntryDTOBuilder.create().author(author).tags(tags).title(title)
        .newsContent(newsContent).newsImage(newsImage).id(1L).creationDate(date)
        .modificationDate(date).build();

    BreadCrumb breadcrumb = BreadCrumbBuilder.create().build();
    BDDMockito.doReturn(breadcrumb).when(displayFactory)
        .computeBreadCrumb(BDDMockito.any(BackPage.class));

    BDDMockito.doReturn(backMenu).when(displayFactory)
        .computeBackMenuItems(BDDMockito.any(BackPage.class),
            BDDMockito.eq(Locale.FRANCE));
    BDDMockito.doReturn(request).when(displayFactory)
        .computeNewsEntryRequest(BDDMockito.any(NewsEntryDTO.class));
    BDDMockito.doReturn(newsEntry).when(newsEntryService).getEntity(BDDMockito.anyLong());
    BackPage backPage = BackPageBuilder.create().pageName("test").templatePath("somePath")
        .titleKey("some.key").decorated(true).build();
    BDDMockito.doReturn(backPage).when(displayFactory).computeBackPage(BDDMockito.anyString());

    ModelAndView result = displayFactory.computeModelAndViewForOneNewsEntry(Locale.FRANCE, "123");

    Assert.assertEquals(decoratorBack, result.getViewName());
    Assert.assertEquals(backPage.getTemplatePath(),
        result.getModel().get("content"));

    BDDMockito.verify(displayFactory, BDDMockito.times(1))
        .computeBackMenuItems(BDDMockito.any(BackPage.class),
            BDDMockito.eq(Locale.FRANCE));

    BDDMockito.verify(displayFactory, BDDMockito.times(1))
        .computeNewsEntryRequest(BDDMockito.any(NewsEntryDTO.class));

  }

  @Test
  public void testComputeModelAndViewForBackPage() throws Exception {
    String tile = "login";
    String href = "/";
    String label = "label";
    String title = "title";
    List<MenuItem> subMenuItems = new ArrayList<MenuItem>();
    MenuItem index = MenuItemBuilder.create().href(href).label(label).title(title)
        .subMenuItems(subMenuItems).build();
    MenuItem news = MenuItemBuilder.create().href(href).label(label).title(title)
        .subMenuItems(subMenuItems).build();

    List<MenuItem> backMenu = Arrays.asList(index, news);

    String author = "author";
    LocalDateTime date = LocalDateTime.now();
    String tags = "tag;lol";
    String content = "content";
    String alt = "alt";

    NewsContentDTO newsContent = NewsContentDTOBuilder.create().content(content).id(1L)
        .creationDate(date)
        .modificationDate(date).build();

    NewsImageDTO newsImage = NewsImageDTOBuilder.create().alt(alt).id(1L).creationDate(date)
        .modificationDate(date)
        .build();

    NewsEntryDTO newsEntry = NewsEntryDTOBuilder.create().author(author).tags(tags).title(title)
        .newsContent(newsContent).newsImage(newsImage).id(1L).creationDate(date)
        .modificationDate(date).build();

    PageWrapper<NewsEntryDTO> pageWrapper = new PageWrapper<>();
    pageWrapper.setPage(new PageImpl<>(Arrays.asList(newsEntry)));

    BreadCrumb breadcrumb = BreadCrumbBuilder.create().build();
    BDDMockito.doReturn(breadcrumb).when(displayFactory)
        .computeBreadCrumb(BDDMockito.any(BackPage.class));

    BDDMockito.doReturn(backMenu).when(displayFactory)
        .computeBackMenuItems(BDDMockito.any(BackPage.class),
            BDDMockito.eq(Locale.FRANCE));

    BackPage backPage = BackPageBuilder.create().pageName("test").templatePath("somePath")
        .titleKey("some.key").decorated(true).build();

    ModelAndView result = displayFactory
        .computeModelAndViewForBackPage(backPage, Locale.FRANCE);

    Assert.assertEquals("somePath", result.getModel().get("content"));

    BDDMockito.verify(displayFactory, BDDMockito.times(1))
        .computeBackMenuItems(BDDMockito.any(BackPage.class),
            BDDMockito.eq(Locale.FRANCE));

  }

  @Test
  public void testComputeModelAndViewForBackPageCreateNews() throws Exception {

    String href = "/";
    String label = "label";
    String title = "title";
    String decoratorBack = "decorator_back";
    List<MenuItem> subMenuItems = new ArrayList<MenuItem>();
    MenuItem index = MenuItemBuilder.create().href(href).label(label).title(title)
        .subMenuItems(subMenuItems).build();
    MenuItem news = MenuItemBuilder.create().href(href).label(label).title(title)
        .subMenuItems(subMenuItems).build();

    List<MenuItem> backMenu = Arrays.asList(index, news);

    BreadCrumb breadcrumb = BreadCrumbBuilder.create().build();
    BDDMockito.doReturn(breadcrumb).when(displayFactory)
        .computeBreadCrumb(BDDMockito.any(BackPage.class));

    BDDMockito.doReturn(backMenu).when(displayFactory)
        .computeBackMenuItems(BDDMockito.any(BackPage.class),
            BDDMockito.eq(Locale.FRANCE));

    BackPage backPage = BackPageBuilder.create().pageName("test").templatePath("somePath")
        .titleKey("some.key").decorated(true).build();
    BDDMockito.doReturn(backPage).when(displayFactory).computeBackPage(BDDMockito.anyString());

    ModelAndView result = displayFactory.computeModelAndViewForBackPageCreateNews(Locale.FRANCE);

    Assert.assertEquals(decoratorBack, result.getViewName());
    Assert.assertEquals(backPage.getTemplatePath(), result.getModel().get("content"));

    BDDMockito.verify(displayFactory, BDDMockito.times(1))
        .computeBackMenuItems(BDDMockito.any(BackPage.class),
            BDDMockito.eq(Locale.FRANCE));

  }

  @Test
  public void testComputeNewsContentRequest() throws Exception {
    LocalDateTime date = LocalDateTime.now();
    NewsContentDTO newsContent = NewsContentDTOBuilder.create().content("someContent")
        .id(123456789L).creationDate(date)
        .modificationDate(date).build();
    NewsEntryDTO newsEntry = NewsEntryDTOBuilder.create().newsContent(newsContent).build();

    NewsContentRequest result = displayFactory.computeNewsContentRequest(newsEntry);

    Assert.assertEquals(newsContent.getContent(), result.getContent());
    Assert.assertEquals(newsContent.getId(), result.getId());
    Assert.assertEquals(newsContent.getCreationDate(), result.getCreationDate());
    Assert.assertEquals(newsContent.getModificationDate(), result.getCreationDate());
  }

  @Test
  public void testComputeNewsImageRequest() throws Exception {
    LocalDateTime date = LocalDateTime.now();
    NewsImageDTO newsImage = NewsImageDTOBuilder.create().alt("someAlt").legend("someLegend")
        .id(123456789L)
        .creationDate(date).modificationDate(date).build();
    NewsEntryDTO newsEntry = NewsEntryDTOBuilder.create().newsImage(newsImage).build();

    NewsImageRequest result = displayFactory.computeNewsImageRequest(newsEntry);

    Assert.assertEquals(newsImage.getAlt(), result.getAlt());
    Assert.assertEquals(newsImage.getLegend(), result.getLegend());
    Assert.assertEquals(newsImage.getId(), result.getId());
    Assert.assertEquals(newsImage.getCreationDate(), result.getCreationDate());
    Assert.assertEquals(newsImage.getModificationDate(), result.getCreationDate());
  }

  @Test
  public void testComputeNewsEntryRequest() throws Exception {

    LocalDateTime creationDate = LocalDateTime.now();
    LocalDateTime modificationDate = LocalDateTime.now();
    long id = 123456789L;
    NewsEntryDTO newsEntry = NewsEntryDTOBuilder.create().tags("aTag").author("someAuthor")
        .title("someTitle").id(id)
        .creationDate(creationDate).modificationDate(modificationDate).build();

    NewsContentRequest contentRequest = NewsContentRequestBuilder.create().id(id)
        .content("someContent")
        .creationDate(creationDate).modificationDate(modificationDate).build();
    NewsImageRequest imageRequest = NewsImageRequestBuilder.create().id(id)
        .creationDate(creationDate)
        .modificationDate(modificationDate).alt("someAlt").legend("someLegend").build();

    BDDMockito.doReturn(imageRequest).when(displayFactory)
        .computeNewsImageRequest(BDDMockito.eq(newsEntry));
    BDDMockito.doReturn(contentRequest).when(displayFactory)
        .computeNewsContentRequest(BDDMockito.eq(newsEntry));
    NewsEntryRequest result = displayFactory.computeNewsEntryRequest(newsEntry);

    Assert.assertEquals(newsEntry.getTags(), result.getTags());
    Assert.assertEquals(newsEntry.getTitle(), result.getTitle());
    Assert.assertEquals(newsEntry.getAuthor(), result.getAuthor());
    Assert.assertEquals(newsEntry.getId(), result.getId());
    Assert.assertEquals(newsEntry.getCreationDate(), result.getCreationDate());
    Assert.assertEquals(newsEntry.getModificationDate(), result.getCreationDate());

    NewsContentRequest resultContentRequest = result.getContent();
    Assert.assertEquals(contentRequest, resultContentRequest);

    NewsImageRequest resultImageRequest = result.getImage();
    Assert.assertEquals(imageRequest, resultImageRequest);
  }

  @Test
  public void testComputeNewsRequestForEditForm_No_Image_Content() throws Exception {

    LocalDateTime creationDate = LocalDateTime.now();
    LocalDateTime modificationDate = LocalDateTime.now();
    long id = 123456789L;

    NewsContentDTO newsContent = NewsContentDTOBuilder.create().content("someContent").id(id)
        .creationDate(creationDate)
        .modificationDate(modificationDate).build();
    NewsEntryDTO newsEntry = NewsEntryDTOBuilder.create().newsContent(newsContent).id(id).build();

    NewsContentRequest contentRequest = NewsContentRequestBuilder.create().id(id)
        .content("someContent")
        .creationDate(creationDate).modificationDate(modificationDate).build();
    NewsImageRequest imageRequest = NewsImageRequestBuilder.create().build();
    NewsEntryRequest newsEntryRequest = NewsEntryRequestBuilder.create().id(123456789L)
        .content(contentRequest)
        .image(imageRequest).build();

    BDDMockito.doReturn(newsEntry).when(newsEntryService).getEntity(BDDMockito.anyLong());
    BDDMockito.doReturn(newsEntryRequest).when(displayFactory)
        .computeNewsEntryRequest(BDDMockito.eq(newsEntry));

    NewsEntryRequest result = displayFactory.computeNewsRequestForEditForm("123456789");

    Assert.assertEquals(newsEntryRequest, result);
    Assert.assertNull(result.getImage().getId());
    Assert.assertNotNull(result.getContent().getId());

  }

  @Test
  public void testComputeNewsRequestForEditForm_No_Image_No_Content() throws Exception {
    long id = 123456789L;

    NewsEntryDTO newsEntry = NewsEntryDTOBuilder.create().id(id).build();
    NewsContentRequest contentRequest = NewsContentRequestBuilder.create().build();
    NewsImageRequest imageRequest = NewsImageRequestBuilder.create().build();
    NewsEntryRequest newsEntryRequest = NewsEntryRequestBuilder.create().id(id)
        .content(contentRequest)
        .image(imageRequest).build();

    BDDMockito.doReturn(newsEntry).when(newsEntryService).getEntity(BDDMockito.anyLong());
    BDDMockito.doReturn(newsEntryRequest).when(displayFactory)
        .computeNewsEntryRequest(BDDMockito.eq(newsEntry));

    NewsEntryRequest result = displayFactory.computeNewsRequestForEditForm("123456789");

    Assert.assertEquals(newsEntryRequest, result);
    Assert.assertNull(result.getImage().getId());
    Assert.assertNull(result.getContent().getId());
  }

  @Test
  public void testComputeNewsRequestForEditForm_Image_No_Content() throws Exception {
    LocalDateTime creationDate = LocalDateTime.now();
    LocalDateTime modificationDate = LocalDateTime.now();
    long id = 123456789L;

    NewsImageDTO newsImage = NewsImageDTOBuilder.create().id(id).build();
    NewsEntryDTO newsEntry = NewsEntryDTOBuilder.create().newsImage(newsImage).id(id).build();

    NewsContentRequest contentRequest = NewsContentRequestBuilder.create().build();
    NewsImageRequest imageRequest = NewsImageRequestBuilder.create().id(id)
        .creationDate(creationDate)
        .modificationDate(modificationDate).alt("someAlt").legend("someLegend").build();
    NewsEntryRequest newsEntryRequest = NewsEntryRequestBuilder.create().id(id)
        .content(contentRequest)
        .image(imageRequest).build();

    BDDMockito.doReturn(newsEntry).when(newsEntryService).getEntity(BDDMockito.anyLong());
    BDDMockito.doReturn(newsEntryRequest).when(displayFactory)
        .computeNewsEntryRequest(BDDMockito.eq(newsEntry));

    NewsEntryRequest result = displayFactory.computeNewsRequestForEditForm("123456789");

    Assert.assertEquals(newsEntryRequest, result);
    Assert.assertNotNull(result.getImage().getId());
    Assert.assertNull(result.getContent().getId());
  }

  @Test
  public void testComputeNewsRequestForEditForm_Image_Content() throws Exception {
    LocalDateTime creationDate = LocalDateTime.now();
    LocalDateTime modificationDate = LocalDateTime.now();
    long id = 123456789L;

    NewsImageDTO newsImage = NewsImageDTOBuilder.create().id(id).build();
    NewsContentDTO newsContent = NewsContentDTOBuilder.create().content("someContent").id(id)
        .creationDate(creationDate)
        .modificationDate(modificationDate).build();
    NewsEntryDTO newsEntry = NewsEntryDTOBuilder.create().newsImage(newsImage)
        .newsContent(newsContent).id(id).build();

    NewsContentRequest contentRequest = NewsContentRequestBuilder.create().id(id)
        .content("someContent")
        .creationDate(creationDate).modificationDate(modificationDate).build();
    NewsImageRequest imageRequest = NewsImageRequestBuilder.create().id(id)
        .creationDate(creationDate)
        .modificationDate(modificationDate).alt("someAlt").legend("someLegend").build();
    NewsEntryRequest newsEntryRequest = NewsEntryRequestBuilder.create().id(id)
        .content(contentRequest)
        .image(imageRequest).build();

    BDDMockito.doReturn(newsEntry).when(newsEntryService).getEntity(BDDMockito.anyLong());
    BDDMockito.doReturn(newsEntryRequest).when(displayFactory)
        .computeNewsEntryRequest(BDDMockito.eq(newsEntry));

    NewsEntryRequest result = displayFactory.computeNewsRequestForEditForm("123456789");

    Assert.assertEquals(newsEntryRequest, result);
    Assert.assertNotNull(result.getImage().getId());
    Assert.assertNotNull(result.getContent().getId());
  }

}
