package com.cmpl.web.core.factory.media;

import com.cmpl.web.core.breadcrumb.BreadCrumb;
import com.cmpl.web.core.breadcrumb.BreadCrumbBuilder;
import com.cmpl.web.core.common.context.ContextHolder;
import com.cmpl.web.core.common.message.WebMessageSource;
import com.cmpl.web.core.common.resource.PageWrapper;
import com.cmpl.web.core.factory.menu.MenuFactory;
import com.cmpl.web.core.group.GroupService;
import com.cmpl.web.core.media.MediaDTO;
import com.cmpl.web.core.media.MediaDTOBuilder;
import com.cmpl.web.core.media.MediaService;
import com.cmpl.web.core.membership.MembershipService;
import com.cmpl.web.core.page.BackPage;
import com.cmpl.web.core.page.BackPageBuilder;
import java.util.ArrayList;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.plugin.core.PluginRegistry;
import org.springframework.util.CollectionUtils;
import org.springframework.web.servlet.ModelAndView;

@RunWith(MockitoJUnitRunner.class)
public class DefaultMediaManagerDisplayFactoryTest {

  @Mock
  private MediaService mediaService;

  @Mock
  private ContextHolder contextHolder;

  @Mock
  private MenuFactory menuFactory;

  @Mock
  private WebMessageSource messageSource;

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

  @Spy
  @InjectMocks
  private DefaultMediaManagerDisplayFactory displayFactory;

  @Test
  public void testComputeModelAndViewForViewAllMedias() throws Exception {

    PageWrapper<MediaDTO> wrapper = new PageWrapper<>();
    BreadCrumb breadcrumb = BreadCrumbBuilder.create().build();
    BDDMockito.doReturn(breadcrumb).when(displayFactory)
        .computeBreadCrumb(BDDMockito.any(BackPage.class));
    BDDMockito.doReturn(wrapper).when(displayFactory)
        .computePageWrapper(BDDMockito.any(Locale.class),
            BDDMockito.anyInt(), BDDMockito.anyString());
    BackPage backPage = BackPageBuilder.create().pageName("test").templatePath("somePath")
        .titleKey("some.key").decorated(true).build();
    BDDMockito.doReturn(backPage).when(displayFactory).computeBackPage(BDDMockito.anyString());

    ModelAndView result = displayFactory.computeModelAndViewForViewAllMedias(Locale.FRANCE, 0);

    Assert.assertEquals(wrapper, result.getModel().get("wrappedEntities"));

  }

  @Test
  public void testComputeEntries_Empty() throws Exception {

    BDDMockito.given(contextHolder.getElementsPerPage()).willReturn(5);
    List<MediaDTO> medias = new ArrayList<>();
    PageImpl<MediaDTO> page = new PageImpl<>(medias);
    BDDMockito.given(mediaService.getPagedEntities(BDDMockito.any(PageRequest.class)))
        .willReturn(page);

    Page<MediaDTO> result = displayFactory.computeEntries(Locale.FRANCE, 1, "");
    Assert.assertTrue(CollectionUtils.isEmpty(result.getContent()));

  }

  @Test
  public void testComputeMediasEntries_Not_Empty() throws Exception {

    BDDMockito.given(contextHolder.getElementsPerPage()).willReturn(5);
    List<MediaDTO> medias = new ArrayList<>();
    MediaDTO media = MediaDTOBuilder.create().build();
    medias.add(media);
    PageImpl<MediaDTO> page = new PageImpl<>(medias);
    BDDMockito.given(mediaService.getPagedEntities(BDDMockito.any(PageRequest.class)))
        .willReturn(page);

    Page<MediaDTO> result = displayFactory.computeEntries(Locale.FRANCE, 1, "");
    Assert.assertEquals(6, result.getTotalElements());

  }

  @Test
  public void testComputePageWrapperOfMedias() throws Exception {

    List<MediaDTO> medias = new ArrayList<>();
    MediaDTO media = MediaDTOBuilder.create().build();
    medias.add(media);
    PageImpl<MediaDTO> page = new PageImpl<>(medias);

    String pageLabel = "Page 1";

    BDDMockito.doReturn(page).when(displayFactory)
        .computeEntries(BDDMockito.any(Locale.class), BDDMockito.anyInt(),
            BDDMockito.anyString());
    BDDMockito.doReturn(pageLabel).when(displayFactory).getI18nValue(BDDMockito.anyString(),
        BDDMockito.any(Locale.class), BDDMockito.anyInt(), BDDMockito.anyInt());
    PageWrapper<MediaDTO> wrapper = displayFactory.computePageWrapper(Locale.FRANCE, 1, "");

    Assert.assertEquals(0, wrapper.getCurrentPageNumber());
    Assert.assertTrue(wrapper.isFirstPage());
    Assert.assertTrue(wrapper.isLastPage());
    Assert.assertEquals(page, wrapper.getPage());
    Assert.assertEquals(1, wrapper.getTotalPages());
    Assert.assertEquals("/manager/medias", wrapper.getPageBaseUrl());
    Assert.assertEquals(pageLabel, wrapper.getPageLabel());

  }

  @Test
  public void testComputeModelAndViewForUploadMedia() throws Exception {
    ModelAndView mediaManager = new ModelAndView();
    String test = "test";
    mediaManager.addObject("test", test);

    BreadCrumb breadcrumb = BreadCrumbBuilder.create().build();
    BDDMockito.doReturn(breadcrumb).when(displayFactory)
        .computeBreadCrumb(BDDMockito.any(BackPage.class));

    BackPage backPage = BackPageBuilder.create().pageName("test").templatePath("somePath")
        .titleKey("some.key").decorated(true).build();
    BDDMockito.doReturn(backPage).when(displayFactory).computeBackPage(BDDMockito.anyString());

    displayFactory.computeModelAndViewForUploadMedia(Locale.FRANCE);

  }
}
