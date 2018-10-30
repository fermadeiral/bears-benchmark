package org.apache.olingo.jpa.metadata.core.edm.mapper.api;

import org.apache.olingo.commons.api.edm.FullQualifiedName;

public interface JPAOperationResultParameter {

  public Integer getMaxLength();

  public Integer getPrecision();

  public Integer getScale();

  public Class<?> getType();

  public FullQualifiedName getTypeFQN();

  public boolean isCollection();

}
