package com.cmpl.web.core.sitemap;

import com.cmpl.web.core.models.Design;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EntityScan(basePackageClasses = Design.class)
@EnableJpaRepositories(basePackageClasses = SitemapRepository.class)
public class SitemapConfiguration {

  @Bean
  public SitemapDAO sitemapDAO(SitemapRepository sitemapRepository,
      ApplicationEventPublisher publisher) {
    return new DefaultSitemapDAO(sitemapRepository, publisher);
  }

  @Bean
  public SitemapMapper sitemapMapper() {
    return new SitemapMapper();
  }

  @Bean
  public SitemapService sitemapService(SitemapDAO sitemapDAO, SitemapMapper sitemapMapper) {
    return new DefaultSitemapService(sitemapDAO, sitemapMapper);
  }

  @Bean
  public SitemapTranslator sitemapTranslator() {
    return new DefaultSitemapTranslator();
  }

  @Bean
  public SitemapDispatcher sitemapDispatcher(SitemapService sitemapService,
      SitemapTranslator sitemapTranslator) {
    return new DefaultSitemapDispatcher(sitemapService, sitemapTranslator);
  }

}
