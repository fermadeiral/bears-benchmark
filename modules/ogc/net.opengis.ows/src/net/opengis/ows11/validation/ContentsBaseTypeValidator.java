/**
 *
 * $Id$
 */
package net.opengis.ows11.validation;

import org.eclipse.emf.common.util.EList;

/**
 * A sample validator interface for {@link net.opengis.ows11.ContentsBaseType}.
 * This doesn't really do anything, and it's not a real EMF artifact.
 * It was generated by the org.eclipse.emf.examples.generator.validator plug-in to illustrate how EMF's code generator can be extended.
 * This can be disabled with -vmargs -Dorg.eclipse.emf.examples.generator.validator=false.
 */
public interface ContentsBaseTypeValidator {
  boolean validate();

  boolean validateDatasetDescriptionSummary(EList value);
  boolean validateOtherSource(EList value);
}
