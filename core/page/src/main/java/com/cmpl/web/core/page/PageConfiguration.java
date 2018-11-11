package com.cmpl.web.core.page;

import com.cmpl.web.core.file.FileService;
import com.cmpl.web.core.models.Page;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EntityScan(basePackageClasses = Page.class)
@EnableJpaRepositories(basePackageClasses = PageRepository.class)
public class PageConfiguration {

    @Bean
    public PageDispatcher pageDispatcher(PageTranslator translator, PageService pageService) {
        return new DefaultPageDispatcher(translator, pageService);
    }

    @Bean
    public PageDAO pageDAO(PageRepository pageRepository, ApplicationEventPublisher publisher) {
        return new DefaultPageDAO(pageRepository, publisher);
    }

    @Bean
    public PageMapper pageMapper() {
        return new PageMapper();
    }

    @Bean
    public RenderingPageMapper renderingPageMapper() {
        return new RenderingPageMapper();
    }

    @Bean
    public PageService pageService(PageDAO pageDAO, PageMapper pageMapper, FileService fileService) {
        return new DefaultPageService(pageDAO, pageMapper, fileService);
    }

    @Bean
    public PageTranslator pageTranslator() {
        return new DefaultPageTranslator();
    }


    @Bean
    public RenderingPageService renderingPageService(PageDAO pageDAO, RenderingPageMapper renderingPageMapper) {
        return new DefaultRenderingPageService(pageDAO, renderingPageMapper);
    }

}
