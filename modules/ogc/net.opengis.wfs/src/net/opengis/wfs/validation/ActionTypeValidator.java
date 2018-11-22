/**
 *
 * $Id$
 */
package net.opengis.wfs.validation;


/**
 * A sample validator interface for {@link net.opengis.wfs.ActionType}.
 * This doesn't really do anything, and it's not a real EMF artifact.
 * It was generated by the org.eclipse.emf.examples.generator.validator plug-in to illustrate how EMF's code generator can be extended.
 * This can be disabled with -vmargs -Dorg.eclipse.emf.examples.generator.validator=false.
 */
public interface ActionTypeValidator {
  boolean validate();

  boolean validateMessage(String value);
  boolean validateCode(String value);
  boolean validateLocator(String value);
}
