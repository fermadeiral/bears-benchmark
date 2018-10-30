package org.apache.olingo.jpa.processor.core.serializer;

import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.serializer.FixedFormatSerializer;
import org.apache.olingo.server.api.serializer.SerializerException;
import org.apache.olingo.server.api.serializer.SerializerResult;

public final class JPASerializeCount implements JPASerializer {
	private final FixedFormatSerializer serializer;

	public JPASerializeCount(final OData odata) {
		this(odata.createFixedFormatSerializer());
	}

	JPASerializeCount(final FixedFormatSerializer serializer) {
		this.serializer = serializer;
	}

	@Override
	public SerializerResult serialize(final ODataRequest request, final EntityCollection result)
			throws SerializerException {
		return new JPAValueSerializerResult(serializer.count(result.getCount()));
	}
}
