package com.cmpl.web.configuration.modules.backup;

import com.cmpl.web.backup.writer.DataManipulator;
import com.cmpl.web.core.carousel.CarouselRepository;
import com.cmpl.web.core.carousel.item.CarouselItemRepository;
import com.cmpl.web.core.design.DesignRepository;
import com.cmpl.web.core.group.GroupRepository;
import com.cmpl.web.core.media.MediaRepository;
import com.cmpl.web.core.membership.MembershipRepository;
import com.cmpl.web.core.models.BOGroup;
import com.cmpl.web.core.models.Carousel;
import com.cmpl.web.core.models.CarouselItem;
import com.cmpl.web.core.models.Design;
import com.cmpl.web.core.models.Media;
import com.cmpl.web.core.models.Membership;
import com.cmpl.web.core.models.NewsContent;
import com.cmpl.web.core.models.NewsEntry;
import com.cmpl.web.core.models.NewsImage;
import com.cmpl.web.core.models.Page;
import com.cmpl.web.core.models.Privilege;
import com.cmpl.web.core.models.Responsibility;
import com.cmpl.web.core.models.Role;
import com.cmpl.web.core.models.Sitemap;
import com.cmpl.web.core.models.Style;
import com.cmpl.web.core.models.User;
import com.cmpl.web.core.models.Website;
import com.cmpl.web.core.models.Widget;
import com.cmpl.web.core.models.WidgetPage;
import com.cmpl.web.core.news.content.NewsContentRepository;
import com.cmpl.web.core.news.entry.NewsEntryRepository;
import com.cmpl.web.core.news.image.NewsImageRepository;
import com.cmpl.web.core.page.PageRepository;
import com.cmpl.web.core.responsibility.ResponsibilityRepository;
import com.cmpl.web.core.role.RoleRepository;
import com.cmpl.web.core.role.privilege.PrivilegeRepository;
import com.cmpl.web.core.sitemap.SitemapRepository;
import com.cmpl.web.core.style.StyleRepository;
import com.cmpl.web.core.user.UserRepository;
import com.cmpl.web.core.website.WebsiteRepository;
import com.cmpl.web.core.widget.WidgetRepository;
import com.cmpl.web.core.widget.page.WidgetPageRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@PropertySource("classpath:/backup/backup.properties")
@EnableJpaRepositories(basePackageClasses = {StyleRepository.class,
    PageRepository.class,
    MediaRepository.class, CarouselRepository.class, CarouselItemRepository.class,
    NewsEntryRepository.class,
    NewsContentRepository.class, NewsImageRepository.class, WidgetRepository.class,
    WidgetPageRepository.class,
    UserRepository.class, RoleRepository.class, ResponsibilityRepository.class,
    PrivilegeRepository.class,
    WebsiteRepository.class, DesignRepository.class, SitemapRepository.class, GroupRepository.class,
    MembershipRepository.class})
public class BackupConfiguration {


  @Bean
  public DataManipulator<Style> styleDataManipulator(StyleRepository styleRepository) {
    return new DataManipulator<>(styleRepository);
  }

  @Bean
  public DataManipulator<Page> pageDataManipulator(PageRepository pageRepository) {
    return new DataManipulator<>(pageRepository);
  }

  @Bean
  public DataManipulator<Media> mediaDataManipulator(MediaRepository mediaRepository) {
    return new DataManipulator<>(mediaRepository);
  }

  @Bean
  public DataManipulator<Carousel> carouselDataManipulator(CarouselRepository carouselRepository) {
    return new DataManipulator<>(carouselRepository);
  }

  @Bean
  public DataManipulator<CarouselItem> carouselItemDataManipulator(
      CarouselItemRepository carouselItemRepository) {
    return new DataManipulator<>(carouselItemRepository);
  }

  @Bean
  public DataManipulator<NewsEntry> newsEntryDataManipulator(
      NewsEntryRepository newsEntryRepository) {
    return new DataManipulator<>(newsEntryRepository);
  }

  @Bean
  public DataManipulator<NewsImage> newsImageDataManipulator(
      NewsImageRepository newsImageRepository) {
    return new DataManipulator<>(newsImageRepository);
  }

  @Bean
  public DataManipulator<NewsContent> newsContentDataManipulator(
      NewsContentRepository newsContentRepository) {
    return new DataManipulator<>(newsContentRepository);
  }

  @Bean
  public DataManipulator<Widget> widgetDataManipulator(WidgetRepository widgetRepository) {
    return new DataManipulator<>(widgetRepository);
  }

  @Bean
  public DataManipulator<WidgetPage> widgetPageDataManipulator(
      WidgetPageRepository widgetPageRepository) {
    return new DataManipulator<>(widgetPageRepository);
  }

  @Bean
  public DataManipulator<User> userDataManipulator(UserRepository userRepository) {
    return new DataManipulator<>(userRepository);
  }

  @Bean
  public DataManipulator<Role> roleDataManipulator(RoleRepository roleRepository) {
    return new DataManipulator<>(roleRepository);
  }

  @Bean
  public DataManipulator<Responsibility> associationUserRoleDataManipulator(
      ResponsibilityRepository responsibilityRepository) {
    return new DataManipulator<>(responsibilityRepository);
  }

  @Bean
  public DataManipulator<Privilege> privilegeDataManipulator(
      PrivilegeRepository privilegeRepository) {
    return new DataManipulator<>(privilegeRepository);
  }

  @Bean
  public DataManipulator<Website> websiteDataManipulator(WebsiteRepository websiteRepository) {
    return new DataManipulator<>(websiteRepository);
  }

  @Bean
  public DataManipulator<BOGroup> groupDataManipulator(GroupRepository groupRepository) {
    return new DataManipulator<>(groupRepository);
  }

  @Bean
  public DataManipulator<Design> designDataManipulator(DesignRepository designRepository) {
    return new DataManipulator<>(designRepository);
  }

  @Bean
  public DataManipulator<Membership> membershipDataManipulator(
      MembershipRepository membershipRepository) {
    return new DataManipulator<>(membershipRepository);
  }

  @Bean
  public DataManipulator<Sitemap> sitemapDataManipulator(SitemapRepository sitemapRepository) {
    return new DataManipulator<>(sitemapRepository);
  }

}
