package org.apache.olingo.jpa.processor.core.exception;

import java.util.Enumeration;
import java.util.Locale;

import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAMessageTextBuffer;
import org.apache.olingo.server.api.ODataApplicationException;

public abstract class ODataJPAProcessException extends ODataApplicationException {

  /**
   * 
   */
  private static final long serialVersionUID = -3178033271311091314L;
  private static final String UNKNOWN_MESSAGE = "No message text found";
  private static Enumeration<Locale> locales;

  public static Enumeration<Locale> getLocales() {
    return locales;
  }

  public static void setLocales(final Enumeration<Locale> locales) {
    ODataJPAProcessException.locales = locales;
  }

  protected final String id;
  protected final ODataJPAMessageTextBuffer messageBuffer;
  protected final String[] parameter;

  public ODataJPAProcessException(final String id, final HttpStatusCode statusCode) {
    super("", statusCode.getStatusCode(), Locale.ENGLISH);
    this.id = id;
    this.messageBuffer = new ODataJPAMessageTextBuffer(getBundleName());
    this.parameter = null;
  }

  public ODataJPAProcessException(final Throwable cause, final HttpStatusCode statusCode) {
    super("", statusCode.getStatusCode(), Locale.ENGLISH, cause);
    this.id = null;
    this.messageBuffer = null;
    this.parameter = null;
  }

  public ODataJPAProcessException(final String id, final HttpStatusCode statusCode, final Throwable cause) {
    super("", statusCode.getStatusCode(), Locale.ENGLISH, cause);
    this.id = id;
    this.messageBuffer = new ODataJPAMessageTextBuffer(getBundleName());
    this.parameter = null;
  }

  public ODataJPAProcessException(final String id, final HttpStatusCode statusCode, final Throwable cause,
      final String[] params) {
    super("", statusCode.getStatusCode(), Locale.ENGLISH, cause);
    this.id = id;
    this.messageBuffer = new ODataJPAMessageTextBuffer(getBundleName());
    this.parameter = params;
  }

  public ODataJPAProcessException(final String id, final HttpStatusCode statusCode, final String[] params) {
    super("", statusCode.getStatusCode(), Locale.ENGLISH);
    this.id = id;
    this.messageBuffer = new ODataJPAMessageTextBuffer(getBundleName());
    this.parameter = params;
  }

  @Override
  public String getLocalizedMessage() {
    return getMessage();
  }

  @Override
  public String getMessage() {
    if (messageBuffer != null) {
      messageBuffer.setLocales(locales);
      return messageBuffer.getText(this, id, parameter);
    } else if (getCause() != null) {
      return getCause().getLocalizedMessage();
    } else
      return UNKNOWN_MESSAGE;
  }

  protected abstract String getBundleName();
}
