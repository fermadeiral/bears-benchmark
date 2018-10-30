package org.apache.olingo.jpa.processor.core.exception;

import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAMessageKey;

/*
 * Copied from org.apache.olingo.odata2.jpa.processor.api.exception.ODataJPAModelException
 * See also org.apache.olingo.odata2.jpa.processor.core.exception.ODataJPAMessageServiceDefault
 */
public class ODataJPADBAdaptorException extends ODataJPAProcessException {
	/**
	 *
	 */
	private static final long serialVersionUID = -7188499882306858747L;

	public static enum MessageKeys implements ODataJPAMessageKey {
		PARAMETER_MISSING,
		NOT_SUPPORTED_SEARCH,
		PARAMETER_CONVERSION_ERROR,
		WRONG_NO_KEY_PROP;

		@Override
		public String getKey() {
			return name();
		}

	}

	private static final String BUNDEL_NAME = "exceptions-processor-i18n";

	public ODataJPADBAdaptorException(final Throwable e, final HttpStatusCode statusCode) {
		super(e, statusCode);
	}

	public ODataJPADBAdaptorException(final MessageKeys messageKey, final HttpStatusCode statusCode,
			final Throwable cause, final String... params) {
		super(messageKey.getKey(), statusCode, cause, params);
	}

	public ODataJPADBAdaptorException(final MessageKeys messageKey, final HttpStatusCode statusCode) {
		super(messageKey.getKey(), statusCode);
	}

	public ODataJPADBAdaptorException(final MessageKeys messageKey, final HttpStatusCode statusCode,
			final String... params) {
		super(messageKey.getKey(), statusCode, params);
	}

	public ODataJPADBAdaptorException(final MessageKeys messageKey, final HttpStatusCode statusCode, final Throwable e) {
		super(messageKey.getKey(), statusCode, e);
	}

	@Override
	protected String getBundleName() {
		return BUNDEL_NAME;
	}

}
