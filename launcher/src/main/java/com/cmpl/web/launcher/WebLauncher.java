package com.cmpl.web.launcher;

import com.cmpl.web.configuration.EnableCMPLWeb;
import com.cmpl.web.core.carousel.CarouselRepository;
import com.cmpl.web.core.carousel.item.CarouselItemRepository;
import com.cmpl.web.core.design.DesignRepository;
import com.cmpl.web.core.media.MediaRepository;
import com.cmpl.web.core.models.Privilege;
import com.cmpl.web.core.models.Responsibility;
import com.cmpl.web.core.models.Role;
import com.cmpl.web.core.models.User;
import com.cmpl.web.core.news.content.NewsContentRepository;
import com.cmpl.web.core.news.entry.NewsEntryRepository;
import com.cmpl.web.core.page.PageRepository;
import com.cmpl.web.core.responsibility.ResponsibilityBuilder;
import com.cmpl.web.core.responsibility.ResponsibilityRepository;
import com.cmpl.web.core.role.RoleBuilder;
import com.cmpl.web.core.role.RoleRepository;
import com.cmpl.web.core.role.privilege.PrivilegeBuilder;
import com.cmpl.web.core.role.privilege.PrivilegeRepository;
import com.cmpl.web.core.sitemap.SitemapRepository;
import com.cmpl.web.core.style.StyleRepository;
import com.cmpl.web.core.user.UserBuilder;
import com.cmpl.web.core.user.UserRepository;
import com.cmpl.web.core.website.WebsiteRepository;
import com.cmpl.web.core.widget.WidgetRepository;
import com.cmpl.web.core.widget.page.WidgetPageRepository;
import java.time.LocalDateTime;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.plugin.core.PluginRegistry;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Main du projet, lance une application springboot
 *
 * @author Louis
 */
@SpringBootApplication
@EnableCMPLWeb
public class WebLauncher {

  /**
   * Main du projet, lance un SpringApplication
   */
  public static void main(String[] args) {
    SpringApplication.run(WebLauncher.class, args);
  }

  @Bean
  @Profile("dev")
  public CommandLineRunner init(final NewsEntryRepository newsEntryRepository,
    final NewsContentRepository newsContentRepository, final PageRepository pageRepository,
    final CarouselRepository carouselRepository,
    final CarouselItemRepository carouselItemRepository, final MediaRepository mediaRepository,
    final WidgetRepository widgetRepository, final WidgetPageRepository widgetPageRepository,
    final UserRepository userRepository, final RoleRepository roleRepository,
    final PrivilegeRepository privilegeRepository,
    final ResponsibilityRepository responsibilityRepository,
    final PasswordEncoder passwordEncoder,
    final PluginRegistry<com.cmpl.web.core.common.user.Privilege, String> privileges,
    final WebsiteRepository websiteRepository, SitemapRepository sitemapRepository,
    StyleRepository styleRepository,
    DesignRepository designRepository) {
    return (args) -> {

      NewsFactory.createNewsEntries(newsEntryRepository, newsContentRepository);

      PageFactory
        .createPages(pageRepository, carouselRepository, carouselItemRepository,
          mediaRepository, widgetRepository, widgetPageRepository);

      WebsiteFactory
        .createWebsite(websiteRepository, pageRepository, sitemapRepository, styleRepository,
          designRepository);

      User system = UserBuilder.create().login("system").email("lperrod@cardiweb.com")
        .description("system")
        .lastConnection(LocalDateTime.now())
        .lastPasswordModification(LocalDateTime.now().minusMonths(1))
        .password(passwordEncoder.encode("system")).build();
      system = userRepository.save(system);
      Role admin = RoleBuilder.create().description("admin").name("admin").build();
      final Role createdAdmin = roleRepository.save(admin);

      Responsibility associationSystemAdmin = ResponsibilityBuilder.create()
        .roleId(admin.getId())
        .userId(system.getId()).build();
      responsibilityRepository.save(associationSystemAdmin);

      privileges.getPlugins().forEach(privilege -> {
        Privilege privilegeToCreate = PrivilegeBuilder.create()
          .roleId(createdAdmin.getId())
          .content(privilege.privilege()).build();
        privilegeRepository.save(privilegeToCreate);
      });

    };
  }

}
