package com.cmpl.web.configuration.modules.facebook;

import com.cmpl.web.core.breadcrumb.BreadCrumb;
import com.cmpl.web.core.breadcrumb.BreadCrumbBuilder;
import com.cmpl.web.core.breadcrumb.BreadCrumbItem;
import com.cmpl.web.core.breadcrumb.BreadCrumbItemBuilder;
import com.cmpl.web.core.common.context.ContextHolder;
import com.cmpl.web.core.common.message.WebMessageSource;
import com.cmpl.web.core.common.user.Privilege;
import com.cmpl.web.core.common.user.SimplePrivilege;
import com.cmpl.web.core.factory.menu.MenuFactory;
import com.cmpl.web.core.file.FileService;
import com.cmpl.web.core.media.MediaService;
import com.cmpl.web.core.menu.BackMenuItem;
import com.cmpl.web.core.menu.BackMenuItemBuilder;
import com.cmpl.web.core.news.entry.NewsEntryService;
import com.cmpl.web.core.page.BackPage;
import com.cmpl.web.core.page.BackPageBuilder;
import com.cmpl.web.core.page.BackPagePlugin;
import com.cmpl.web.facebook.DefaultFacebookAdapter;
import com.cmpl.web.facebook.DefaultFacebookDispatcher;
import com.cmpl.web.facebook.DefaultFacebookImportService;
import com.cmpl.web.facebook.DefaultFacebookImportTranslator;
import com.cmpl.web.facebook.DefaultFacebookService;
import com.cmpl.web.facebook.DoNothingFacebookAdapter;
import com.cmpl.web.facebook.FacebookAdapter;
import com.cmpl.web.facebook.FacebookCustomApiVersionConnectionFactory;
import com.cmpl.web.facebook.FacebookDispatcher;
import com.cmpl.web.facebook.FacebookImportService;
import com.cmpl.web.facebook.FacebookImportTranslator;
import com.cmpl.web.facebook.FacebookService;
import com.cmpl.web.modules.facebook.factory.DefaultFacebookDisplayFactory;
import com.cmpl.web.modules.facebook.factory.FacebookDisplayFactory;
import com.cmpl.web.modules.social.configuration.SocialAutoConfigurerAdapter;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.plugin.core.PluginRegistry;
import org.springframework.plugin.core.config.EnablePluginRegistries;
import org.springframework.social.UserIdSource;
import org.springframework.social.config.annotation.EnableSocial;
import org.springframework.social.connect.Connection;
import org.springframework.social.connect.ConnectionFactory;
import org.springframework.social.connect.ConnectionFactoryLocator;
import org.springframework.social.connect.ConnectionRepository;
import org.springframework.social.connect.mem.InMemoryConnectionRepository;
import org.springframework.social.connect.web.ConnectController;
import org.springframework.social.connect.web.GenericConnectionStatusView;
import org.springframework.social.facebook.api.Facebook;

@Configuration
@EnableSocial
@PropertySource("classpath:/facebook/facebook.properties")
@EnablePluginRegistries({BackPagePlugin.class})
public class FacebookConfiguration {

  private static final Logger LOGGER = LoggerFactory.getLogger(FacebookConfiguration.class);

  @Value("${facebookConfigurationFile}")
  String facebookConfigurationFile;


  @Value(value = "${baseUrl}")
  String baseUrl;

  @Bean
  @ConditionalOnProperty(prefix = "import.", name = "enabled")
  public BackMenuItem facebookBackMenuItem(BackMenuItem webmastering,
    Privilege facebookImportPrivilege) {
    return BackMenuItemBuilder.create().href("/manager/facebook").label("facebook.access.label")
      .title("facebook.access.title").iconClass("fa fa-facebook").parent(webmastering).order(7)
      .privilege(facebookImportPrivilege.privilege()).build();
  }

  @Bean
  public SimplePrivilege facebookImportPrivilege() {
    return new SimplePrivilege("webmastering", "facebook", "import");
  }


  @Bean
  BackPage facebookAccess() {
    return BackPageBuilder.create().decorated(true).pageName("FACEBOOK_ACCESS")
      .templatePath("back/facebook_access").titleKey("back.access.title").build();
  }

  @Bean
  BackPage facebookImport() {
    return BackPageBuilder.create().decorated(true).pageName("FACEBOOK_IMPORT")
      .templatePath("back/facebook_import").titleKey("back.access.title").build();
  }

  @Bean
  @ConditionalOnProperty(prefix = "import.", name = "enabled")
  public BreadCrumb facebookImportBreadCrumb() {
    return BreadCrumbBuilder.create().items(facebookImportBreadCrumbItems())
      .pageName("FACEBOOK_IMPORT").build();
  }

  List<BreadCrumbItem> facebookImportBreadCrumbItems() {
    List<BreadCrumbItem> items = new ArrayList<>();
    items.add(BreadCrumbItemBuilder.create().text("back.index.label").href("/manager/").build());
    items.add(BreadCrumbItemBuilder.create().text("facebook.access.title").href("/manager/facebook")
      .build());
    return items;
  }

  @Bean
  @ConditionalOnProperty(prefix = "import.", name = "enabled")
  public BreadCrumb facebookAccessBreadCrumb() {
    return BreadCrumbBuilder.create().items(facebookAccessBreadCrumbItems())
      .pageName("FACEBOOK_ACCESS").build();
  }

  List<BreadCrumbItem> facebookAccessBreadCrumbItems() {
    List<BreadCrumbItem> items = new ArrayList<>();
    items.add(BreadCrumbItemBuilder.create().text("back.index.label").href("/manager/").build());
    items.add(BreadCrumbItemBuilder.create().text("facebook.access.label").href("/manager/facebook")
      .build());
    return items;
  }

  @Bean
  public FacebookProperties facebookProperties() {
    FacebookProperties properties = new FacebookProperties();
    JSONParser parser = new JSONParser();
    Object obj;
    try {
      obj = parser.parse(new FileReader(facebookConfigurationFile));
      JSONObject jsonObject = (JSONObject) obj;
      String appId = (String) jsonObject.get("appId");
      String appSecret = (String) jsonObject.get("appSecret");

      properties.setAppId(appId);
      properties.setAppSecret(appSecret);
    } catch (Exception e) {
      LOGGER.error("Impossible d'initialiser Facebook Connect ", e);
      properties.setAppId("mockId");
      properties.setAppSecret("mockSecret");
    }
    return properties;
  }

  @Bean
  public FacebookImportTranslator facebookImportTranslator() {
    return new DefaultFacebookImportTranslator();
  }

  @Bean
  public FacebookDisplayFactory facebookDisplayFactory(MenuFactory menuFactory,
    WebMessageSource messageSource,
    FacebookAdapter facebookAdapter, PluginRegistry<BreadCrumb, String> breadCrumbs,
    Set<Locale> availableLocales, @Qualifier(value = "backPages")
    PluginRegistry<BackPage, String> backPages) {
    return new DefaultFacebookDisplayFactory(menuFactory, messageSource, facebookAdapter,
      breadCrumbs,
      availableLocales, backPages);
  }

  @Bean
  public FacebookDispatcher facebookDispatcher(FacebookImportService facebookImportService,
    FacebookImportTranslator facebookImportTranslator) {
    return new DefaultFacebookDispatcher(facebookImportService, facebookImportTranslator);
  }

  @Bean
  @ConditionalOnProperty(prefix = "import.", name = "enabled")
  public FacebookService facebookService(ContextHolder contextHolder, Facebook facebookConnector,
    ConnectionRepository inMemoryConnectionRepository, NewsEntryService newsEntryService) {
    return new DefaultFacebookService(contextHolder, facebookConnector,
      inMemoryConnectionRepository,
      newsEntryService);
  }

  @Bean
  public FacebookImportService facebookImportService(NewsEntryService newsEntryService,
    FacebookAdapter facebookAdapter,
    MediaService mediaService, FileService fileService, WebMessageSource messageSource) {
    return new DefaultFacebookImportService(newsEntryService, facebookAdapter, mediaService,
      fileService, messageSource);
  }

  @Configuration
  @EnableSocial
  @EnableConfigurationProperties(FacebookProperties.class)
  protected static class FacebookConfigurerAdapter extends SocialAutoConfigurerAdapter {

    private final FacebookProperties facebookProperties;

    protected FacebookConfigurerAdapter(FacebookProperties facebookProperties) {
      this.facebookProperties = facebookProperties;
    }

    @Bean
    @ConditionalOnMissingBean(Facebook.class)
    @Scope(value = "request", proxyMode = ScopedProxyMode.INTERFACES)
    public Facebook facebook(ConnectionRepository inMemoryConnectionRepository) {
      Connection<Facebook> connection = inMemoryConnectionRepository
        .findPrimaryConnection(Facebook.class);
      return connection != null ? connection.getApi() : null;
    }

    @Bean(name = {"connect/facebookConnect", "connect/facebookConnected"})
    @ConditionalOnProperty(prefix = "spring.social", name = "auto-connection-views")
    public GenericConnectionStatusView facebookConnectView() {
      return new GenericConnectionStatusView("facebook", "Facebook");
    }

    @Override
    protected ConnectionFactory<?> createConnectionFactory() {
      return new FacebookCustomApiVersionConnectionFactory("2.7", facebookProperties.getAppId(),
        facebookProperties.getAppSecret());

    }

    @Override
    public UserIdSource getUserIdSource() {
      return new UserIdSource() {
        @Override
        public String getUserId() {
          return String.valueOf(UUID.randomUUID().getLeastSignificantBits());
        }
      };
    }

  }

  @Bean
  @ConditionalOnProperty(prefix = "import.", name = "enabled")
  public FacebookAdapter facebookAdapter(FacebookService facebookService,
    Facebook facebookConnector) {
    return new DefaultFacebookAdapter(facebookService, facebookConnector);
  }

  @Bean
  @ConditionalOnProperty(prefix = "import.", name = "enabled", havingValue = "false")
  public FacebookAdapter mockFacebookAdapter() {
    return new DoNothingFacebookAdapter();
  }

  @Bean
  public ConnectController connectController(ConnectionFactoryLocator connectionFactoryLocator,
    ConnectionRepository inMemoryConnectionRepository) {
    ConnectController connectController = new ConnectController(connectionFactoryLocator,
      inMemoryConnectionRepository);
    connectController.setApplicationUrl(baseUrl);
    return connectController;
  }

  @Bean
  public ConnectionRepository inMemoryConnectionRepository(
    ConnectionFactoryLocator connectionFactoryLocator) {
    return new InMemoryConnectionRepository(connectionFactoryLocator);
  }

}
