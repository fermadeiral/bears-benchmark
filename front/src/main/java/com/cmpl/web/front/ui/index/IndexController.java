package com.cmpl.web.front.ui.index;

import com.cmpl.web.core.factory.DisplayFactory;
import java.util.Locale;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.ModelAndView;

/**
 * Controller pour la page d'accueil
 *
 * @author Louis
 */
@Controller
public class IndexController {

  private static final Logger LOGGER = LoggerFactory.getLogger(IndexController.class);

  private final DisplayFactory displayFactory;

  public IndexController(DisplayFactory displayFactory) {
    this.displayFactory = Objects.requireNonNull(displayFactory);

  }

  /**
   * Mapping pour la page d'accueil
   */
  @GetMapping(value = "/{websiteName}")
  public ModelAndView printIndex(@PathVariable(value = "websiteName") String websiteName,
      Locale locale) {

    LOGGER.info("Accès à la page d'index");
    return displayFactory.computeModelAndViewForWebsitePage(websiteName, "accueil", locale, 0, "");
  }

}
