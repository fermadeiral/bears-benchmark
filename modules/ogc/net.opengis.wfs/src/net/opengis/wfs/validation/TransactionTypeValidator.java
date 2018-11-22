/**
 *
 * $Id$
 */
package net.opengis.wfs.validation;

import net.opengis.wfs.AllSomeType;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.util.FeatureMap;

/**
 * A sample validator interface for {@link net.opengis.wfs.TransactionType}.
 * This doesn't really do anything, and it's not a real EMF artifact.
 * It was generated by the org.eclipse.emf.examples.generator.validator plug-in to illustrate how EMF's code generator can be extended.
 * This can be disabled with -vmargs -Dorg.eclipse.emf.examples.generator.validator=false.
 */
public interface TransactionTypeValidator {
  boolean validate();

  boolean validateLockId(String value);
  boolean validateGroup(FeatureMap value);
  boolean validateInsert(EList value);
  boolean validateUpdate(EList value);
  boolean validateDelete(EList value);
  boolean validateNative(EList value);
  boolean validateReleaseAction(AllSomeType value);
}
