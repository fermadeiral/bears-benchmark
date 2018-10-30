package org.apache.olingo.jpa.processor.core.serializer;

import org.apache.olingo.commons.api.data.ContextURL;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.jpa.processor.core.query.Util;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.serializer.EntityCollectionSerializerOptions;
import org.apache.olingo.server.api.serializer.ODataSerializer;
import org.apache.olingo.server.api.serializer.SerializerException;
import org.apache.olingo.server.api.serializer.SerializerResult;
import org.apache.olingo.server.api.uri.UriHelper;
import org.apache.olingo.server.api.uri.UriInfo;

public final class JPASerializeCollection implements JPASerializer {
	private final ServiceMetadata serviceMetadata;
	private final UriInfo uriInfo;
	private final UriHelper uriHelper;
	private final ODataSerializer serializer;

	public JPASerializeCollection(final ServiceMetadata serviceMetadata, final OData odata,
			final ContentType responseFormat, final UriInfo uriInfo) throws SerializerException {
		this(serviceMetadata, odata.createSerializer(responseFormat), odata.createUriHelper(), uriInfo);
	}

	JPASerializeCollection(final ServiceMetadata serviceMetadata, final ODataSerializer serializer,
			final UriHelper uriHelper, final UriInfo uriInfo) {
		this.uriInfo = uriInfo;
		this.serializer = serializer;
		this.serviceMetadata = serviceMetadata;
		this.uriHelper = uriHelper;
	}

	@Override
	public SerializerResult serialize(final ODataRequest request, final EntityCollection result)
			throws SerializerException {

		final EdmEntitySet targetEdmEntitySet = Util.determineTargetEntitySet(uriInfo.getUriResourceParts());

		final String selectList = uriHelper.buildContextURLSelectList(targetEdmEntitySet.getEntityType(),
				uriInfo.getExpandOption(), uriInfo.getSelectOption());

		final ContextURL contextUrl = ContextURL.with()
				.entitySet(targetEdmEntitySet)
				.selectList(selectList)
				.build();

		final String id = request.getRawBaseUri() + "/" + targetEdmEntitySet.getEntityType().getName();
		final EntityCollectionSerializerOptions opts = EntityCollectionSerializerOptions.with()
				.contextURL(contextUrl)
				.id(id)
				.count(uriInfo.getCountOption())
				.select(uriInfo.getSelectOption())
				.expand(uriInfo.getExpandOption())
				.build();

		final SerializerResult serializerResult = serializer.entityCollection(this.serviceMetadata, targetEdmEntitySet
				.getEntityType(), result, opts);
		return serializerResult;

	}

}
