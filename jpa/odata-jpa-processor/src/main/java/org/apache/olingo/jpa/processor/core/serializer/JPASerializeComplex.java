package org.apache.olingo.jpa.processor.core.serializer;

import org.apache.olingo.commons.api.data.ContextURL;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.edm.EdmComplexType;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.jpa.processor.core.query.Util;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.serializer.ComplexSerializerOptions;
import org.apache.olingo.server.api.serializer.ODataSerializer;
import org.apache.olingo.server.api.serializer.SerializerException;
import org.apache.olingo.server.api.serializer.SerializerResult;
import org.apache.olingo.server.api.uri.UriHelper;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriResourceProperty;

class JPASerializeComplex implements JPASerializer {
  private final ServiceMetadata serviceMetadata;
  private final UriInfo uriInfo;
  private final UriHelper uriHelper;
  private final ODataSerializer serializer;

  JPASerializeComplex(final ServiceMetadata serviceMetadata, final ODataSerializer serializer,
      final UriHelper uriHelper,
      final UriInfo uriInfo) {
    this.uriInfo = uriInfo;
    this.serializer = serializer;
    this.serviceMetadata = serviceMetadata;
    this.uriHelper = uriHelper;
  }

  @Override
  public SerializerResult serialize(final ODataRequest request, final EntityCollection result)
      throws SerializerException {

    final EdmEntitySet targetEdmEntitySet = Util.determineTargetEntitySet(uriInfo.getUriResourceParts());

    final Property property = result.getEntities().get(0).getProperties().get(0);

    final UriResourceProperty uriProperty = Util.determineStartNavigationPath(uriInfo.getUriResourceParts());
    final EdmComplexType edmPropertyType = (EdmComplexType) uriProperty.getProperty().getType();

    final String selectList = uriHelper.buildContextURLSelectList(targetEdmEntitySet.getEntityType(),
        uriInfo.getExpandOption(), uriInfo.getSelectOption());

    final ContextURL contextUrl = ContextURL.with()
        .entitySet(targetEdmEntitySet)
        .navOrPropertyPath(Util.determineProptertyNavigationPath(uriInfo.getUriResourceParts()))
        .selectList(selectList)
        .build();
    final ComplexSerializerOptions options = ComplexSerializerOptions.with()
        .contextURL(contextUrl)
        .select(uriInfo.getSelectOption())
        .expand(uriInfo.getExpandOption())
        .build();

    final SerializerResult serializerResult = serializer.complex(serviceMetadata, edmPropertyType, property, options);
    return serializerResult;
  }
}
