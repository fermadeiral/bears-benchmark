package com.cmpl.web.configuration.manager.privileges;

import com.cmpl.web.core.common.user.Privilege;
import com.cmpl.web.core.common.user.SimplePrivilege;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NewsPrivilegeConfiguration {

  @Bean
  public Privilege newsReadPrivilege() {
    return new SimplePrivilege("webmastering", "news", "read");
  }

  @Bean
  public Privilege newsWritePrivilege() {
    return new SimplePrivilege("webmastering", "news", "write");
  }

  @Bean
  public Privilege newsCreatePrivilege() {
    return new SimplePrivilege("webmastering", "news", "create");
  }

  @Bean
  public Privilege newsDeletePrivilege() {
    return new SimplePrivilege("webmastering", "news", "delete");
  }

}
