package org.apache.olingo.jpa.processor.core.api;

import javax.persistence.EntityManager;

import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.jpa.processor.core.exception.ODataJPAProcessorException;
import org.apache.olingo.jpa.processor.core.processor.JPAProcessorFactory;
import org.apache.olingo.jpa.processor.core.processor.JPARequestProcessor;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.ODataLibraryException;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ODataResponse;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.processor.ComplexProcessor;
import org.apache.olingo.server.api.processor.PrimitiveValueProcessor;
import org.apache.olingo.server.api.uri.UriInfo;

public class JPAODataRequestProcessor implements PrimitiveValueProcessor,
ComplexProcessor/* , CountEntityCollectionProcessor, EntityProcessor, MediaEntityProcessor */ {
	private final EntityManager em;
	private final JPAODataSessionContextAccess context;
	private JPAProcessorFactory factory;

	public JPAODataRequestProcessor(final JPAODataSessionContextAccess context, final EntityManager em) {
		super();
		this.em = em;
		this.context = context;
	}

	@Override
	public void init(final OData odata, final ServiceMetadata serviceMetadata) {
		this.factory = new JPAProcessorFactory(odata, serviceMetadata, context);
	}

	@Deprecated
	public void countEntityCollection(final ODataRequest request, final ODataResponse response, final UriInfo uriInfo)
			throws ODataApplicationException, ODataLibraryException {
		final JPARequestProcessor p = factory.createProcessor(em, uriInfo, ContentType.TEXT_PLAIN);
		p.retrieveData(request, response, ContentType.TEXT_PLAIN);
	}

	@Deprecated
	public void createEntity(final ODataRequest request, final ODataResponse response, final UriInfo uriInfo,
			final ContentType requestFormat, final ContentType responseFormat) throws ODataApplicationException,
	ODataLibraryException {

		throw new ODataJPAProcessorException(ODataJPAProcessorException.MessageKeys.NOT_SUPPORTED_CREATE,
				HttpStatusCode.NOT_IMPLEMENTED);
	}

	@Deprecated
	public void createMediaEntity(final ODataRequest request, final ODataResponse response, final UriInfo uriInfo,
			final ContentType requestFormat, final ContentType responseFormat) throws ODataApplicationException,
	ODataLibraryException {

		throw new ODataJPAProcessorException(ODataJPAProcessorException.MessageKeys.NOT_SUPPORTED_CREATE,
				HttpStatusCode.NOT_IMPLEMENTED);
	}

	@Override
	public void deleteComplex(final ODataRequest request, final ODataResponse response, final UriInfo uriInfo)
			throws ODataApplicationException, ODataLibraryException {

		throw new ODataJPAProcessorException(ODataJPAProcessorException.MessageKeys.NOT_SUPPORTED_DELETE,
				HttpStatusCode.NOT_IMPLEMENTED);
	}

	@Deprecated
	public void deleteEntity(final ODataRequest request, final ODataResponse response, final UriInfo uriInfo)
			throws ODataApplicationException, ODataLibraryException {

		throw new ODataJPAProcessorException(ODataJPAProcessorException.MessageKeys.NOT_SUPPORTED_DELETE,
				HttpStatusCode.NOT_IMPLEMENTED);
	}

	@Override
	public void deletePrimitive(final ODataRequest request, final ODataResponse response, final UriInfo uriInfo)
			throws ODataApplicationException, ODataLibraryException {

		throw new ODataJPAProcessorException(ODataJPAProcessorException.MessageKeys.NOT_SUPPORTED_DELETE,
				HttpStatusCode.NOT_IMPLEMENTED);
	}

	@Override
	public void deletePrimitiveValue(final ODataRequest request, final ODataResponse response, final UriInfo uriInfo)
			throws ODataApplicationException, ODataLibraryException {

		throw new ODataJPAProcessorException(ODataJPAProcessorException.MessageKeys.NOT_SUPPORTED_DELETE,
				HttpStatusCode.NOT_IMPLEMENTED);
	}

	@Deprecated
	public void deleteMediaEntity(final ODataRequest request, final ODataResponse response, final UriInfo uriInfo)
			throws ODataApplicationException, ODataLibraryException {

		throw new ODataJPAProcessorException(ODataJPAProcessorException.MessageKeys.NOT_SUPPORTED_DELETE,
				HttpStatusCode.NOT_IMPLEMENTED);
	}

	@Override
	public void readComplex(final ODataRequest request, final ODataResponse response, final UriInfo uriInfo,
			final ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {

		final JPARequestProcessor p = factory.createProcessor(em, uriInfo, responseFormat);
		p.retrieveData(request, response, responseFormat);
	}

	@Deprecated
	public void readEntity(final ODataRequest request, final ODataResponse response, final UriInfo uriInfo,
			final ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {

		final JPARequestProcessor p = factory.createProcessor(em, uriInfo, responseFormat);
		p.retrieveData(request, response, responseFormat);
	}

	@Deprecated
	public void readEntityCollection(final ODataRequest request, final ODataResponse response, final UriInfo uriInfo,
			final ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {

		final JPARequestProcessor p = factory.createProcessor(em, uriInfo, responseFormat);
		p.retrieveData(request, response, responseFormat);
	}

	@Override
	public void readPrimitive(final ODataRequest request, final ODataResponse response, final UriInfo uriInfo,
			final ContentType responseFormat)
					throws ODataApplicationException, ODataLibraryException {

		final JPARequestProcessor p = factory.createProcessor(em, uriInfo, responseFormat);
		p.retrieveData(request, response, responseFormat);
	}

	@Override
	public void readPrimitiveValue(final ODataRequest request, final ODataResponse response, final UriInfo uriInfo,
			final ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {

		final JPARequestProcessor p = factory.createProcessor(em, uriInfo, responseFormat);
		p.retrieveData(request, response, responseFormat);
	}

	@Deprecated
	public void readMediaEntity(final ODataRequest request, final ODataResponse response, final UriInfo uriInfo,
			final ContentType responseFormat)
					throws ODataApplicationException, ODataLibraryException {

		final JPARequestProcessor p = factory.createProcessor(em, uriInfo, responseFormat);
		p.retrieveData(request, response, responseFormat);
	}

	@Override
	public void updateComplex(final ODataRequest request, final ODataResponse response, final UriInfo uriInfo,
			final ContentType requestFormat, final ContentType responseFormat) throws ODataApplicationException,
	ODataLibraryException {

		throw new ODataJPAProcessorException(ODataJPAProcessorException.MessageKeys.NOT_SUPPORTED_UPDATE,
				HttpStatusCode.NOT_IMPLEMENTED);
	}

	@Deprecated
	public void updateEntity(final ODataRequest request, final ODataResponse response, final UriInfo uriInfo,
			final ContentType requestFormat, final ContentType responseFormat) throws ODataApplicationException,
	ODataLibraryException {

		throw new ODataJPAProcessorException(ODataJPAProcessorException.MessageKeys.NOT_SUPPORTED_UPDATE,
				HttpStatusCode.NOT_IMPLEMENTED);
	}

	@Override
	public void updatePrimitive(final ODataRequest request, final ODataResponse response, final UriInfo uriInfo,
			final ContentType requestFormat, final ContentType responseFormat) throws ODataApplicationException,
	ODataLibraryException {

		throw new ODataJPAProcessorException(ODataJPAProcessorException.MessageKeys.NOT_SUPPORTED_UPDATE,
				HttpStatusCode.NOT_IMPLEMENTED);
	}

	@Override
	public void updatePrimitiveValue(final ODataRequest request, final ODataResponse response, final UriInfo uriInfo,
			final ContentType requestFormat, final ContentType responseFormat) throws ODataApplicationException,
	ODataLibraryException {

		throw new ODataJPAProcessorException(ODataJPAProcessorException.MessageKeys.NOT_SUPPORTED_UPDATE,
				HttpStatusCode.NOT_IMPLEMENTED);
	}

	@Deprecated
	public void updateMediaEntity(final ODataRequest request, final ODataResponse response, final UriInfo uriInfo,
			final ContentType requestFormat, final ContentType responseFormat) throws ODataApplicationException,
	ODataLibraryException {

		throw new ODataJPAProcessorException(ODataJPAProcessorException.MessageKeys.NOT_SUPPORTED_UPDATE,
				HttpStatusCode.NOT_IMPLEMENTED);
	}

}
