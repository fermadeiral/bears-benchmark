package org.apache.olingo.jpa.processor.core.processor;

import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.ODataLibraryException;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ODataResponse;

public interface JPARequestProcessor {

  public void retrieveData(ODataRequest request, ODataResponse response, ContentType responseFormat)
      throws ODataApplicationException, ODataLibraryException;
}
