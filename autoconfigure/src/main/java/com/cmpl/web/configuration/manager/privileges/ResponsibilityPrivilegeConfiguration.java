package com.cmpl.web.configuration.manager.privileges;

import com.cmpl.web.core.common.user.Privilege;
import com.cmpl.web.core.common.user.SimplePrivilege;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ResponsibilityPrivilegeConfiguration {

  @Bean
  public Privilege responsabilitiesReadPrivilege() {
    return new SimplePrivilege("administration", "responsibilities", "read");
  }

  @Bean
  public Privilege responsabilitiesWritePrivilege() {
    return new SimplePrivilege("administration", "responsibilities", "write");
  }

  @Bean
  public Privilege responsabilitiesCreatePrivilege() {
    return new SimplePrivilege("administration", "responsibilities", "create");
  }

  @Bean
  public Privilege responsabilitiesDeletePrivilege() {
    return new SimplePrivilege("administration", "responsibilities", "delete");
  }

}
