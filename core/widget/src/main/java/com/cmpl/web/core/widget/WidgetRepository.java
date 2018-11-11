package com.cmpl.web.core.widget;

import com.cmpl.web.core.common.repository.BaseRepository;
import com.cmpl.web.core.models.Widget;
import org.springframework.stereotype.Repository;

@Repository
public interface WidgetRepository extends BaseRepository<Widget> {

  Widget findByName(String widgetName);
}
