package com.cmpl.web.configuration.manager.breadcrumbs;

import com.cmpl.web.core.breadcrumb.BreadCrumb;
import com.cmpl.web.core.breadcrumb.BreadCrumbBuilder;
import com.cmpl.web.core.breadcrumb.BreadCrumbItem;
import com.cmpl.web.core.breadcrumb.BreadCrumbItemBuilder;
import java.util.ArrayList;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WidgetBreadcrumbsConfiguration {

  @Bean
  public BreadCrumb widgetBreadCrumb() {
    return BreadCrumbBuilder.create().items(widgetBreadCrumbItems()).pageName("WIDGET_VIEW")
        .build();
  }

  @Bean
  public BreadCrumb widgetUpdateBreadCrumb() {
    return BreadCrumbBuilder.create().items(widgetBreadCrumbItems()).pageName("WIDGET_UPDATE")
        .build();
  }

  @Bean
  public BreadCrumb widgetCreateBreadCrumb() {
    return BreadCrumbBuilder.create().items(widgetBreadCrumbItems()).pageName("WIDGET_CREATE")
        .build();
  }

  List<BreadCrumbItem> widgetBreadCrumbItems() {
    List<BreadCrumbItem> items = new ArrayList<>();
    items.add(BreadCrumbItemBuilder.create().text("back.index.label").href("/manager/").build());
    items.add(
        BreadCrumbItemBuilder.create().text("back.widgets.title").href("/manager/widgets").build());
    return items;
  }

}
