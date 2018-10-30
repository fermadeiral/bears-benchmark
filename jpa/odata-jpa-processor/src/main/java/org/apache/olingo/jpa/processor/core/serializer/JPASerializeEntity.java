package org.apache.olingo.jpa.processor.core.serializer;

import org.apache.olingo.commons.api.data.ContextURL;
import org.apache.olingo.commons.api.data.ContextURL.Suffix;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.jpa.processor.core.query.Util;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.serializer.EntitySerializerOptions;
import org.apache.olingo.server.api.serializer.ODataSerializer;
import org.apache.olingo.server.api.serializer.SerializerException;
import org.apache.olingo.server.api.serializer.SerializerResult;
import org.apache.olingo.server.api.uri.UriHelper;
import org.apache.olingo.server.api.uri.UriInfo;

public final class JPASerializeEntity implements JPASerializer {
	private final ServiceMetadata serviceMetadata;
	private final UriInfo uriInfo;
	private final UriHelper uriHelper;
	private final ODataSerializer serializer;

	public JPASerializeEntity(final ServiceMetadata serviceMetadata, final OData odata, final ContentType responseFormat, final UriInfo uriInfo)
			throws SerializerException {
		this(serviceMetadata, odata.createSerializer(responseFormat), odata.createUriHelper(), uriInfo);
	}

	public JPASerializeEntity(final ServiceMetadata serviceMetadata, final ODataSerializer serializer,
			final UriHelper uriHelper, final UriInfo uriInfo) throws SerializerException {
		this.uriInfo = uriInfo;
		this.serializer = serializer;
		this.serviceMetadata = serviceMetadata;
		this.uriHelper = uriHelper;
	}

	@Override
	public SerializerResult serialize(final ODataRequest request, final EntityCollection result)
			throws SerializerException {

		final EdmEntitySet targetEdmEntitySet = Util.determineTargetEntitySet(uriInfo.getUriResourceParts());

		final EdmEntityType entityType = targetEdmEntitySet.getEntityType();

		final String selectList = uriHelper.buildContextURLSelectList(targetEdmEntitySet.getEntityType(),
				uriInfo.getExpandOption(), uriInfo.getSelectOption());

		final ContextURL contextUrl = ContextURL.with().entitySet(targetEdmEntitySet).suffix(Suffix.ENTITY)
				.selectList(selectList).build();

		final EntitySerializerOptions options = EntitySerializerOptions.with().contextURL(contextUrl)
				.select(uriInfo.getSelectOption()).expand(uriInfo.getExpandOption()).build();

		final SerializerResult serializerResult = serializer.entity(serviceMetadata, entityType,
				result.getEntities().get(0), options);
		return serializerResult;
	}
}
