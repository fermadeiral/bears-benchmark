package com.cmpl.web.front.ui.website;

import com.cmpl.web.core.factory.DisplayFactory;
import java.util.Locale;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class RenderingWebsiteController {

  private static final Logger LOGGER = LoggerFactory.getLogger(RenderingWebsiteController.class);

  private final DisplayFactory displayFactory;


  public RenderingWebsiteController(DisplayFactory displayFactory) {
    this.displayFactory = Objects.requireNonNull(displayFactory);
  }

  @GetMapping(value = "/sites/{websiteName}/pages/{pageHref}")
  public ModelAndView printWebsitePage(@PathVariable(value = "websiteName") String websiteName,
    @PathVariable(value = "pageHref") String pageHref,
    @RequestParam(name = "p", required = false) Integer pageNumber,
    @RequestParam(name = "q", required = false) String query,
    Locale locale) {

    LOGGER.info("Accès à la page {} du site {}", pageHref, websiteName);
    return displayFactory.computeModelAndViewForWebsitePage(websiteName, pageHref, locale,
      computePageNumberFromRequest(pageNumber), query);

  }

  @GetMapping(value = "/sites/{websiteName}/amp/{pageHref}")
  public ModelAndView printWebsiteAMPPage(@PathVariable(value = "websiteName") String websiteName,
    @PathVariable(value = "pageHref") String pageHref,
    @RequestParam(name = "p", required = false) Integer pageNumber,
    @RequestParam(name = "q", required = false) String query,
    Locale locale) {

    LOGGER.info("Accès à la page {} du site {}", pageHref, websiteName);
    return displayFactory.computeModelAndViewForWebsiteAMP(websiteName, pageHref, locale,
      computePageNumberFromRequest(pageNumber), query);

  }

  int computePageNumberFromRequest(Integer pageNumber) {
    if (pageNumber == null) {
      return 0;
    }
    return pageNumber.intValue();

  }

}
