/**
 *
 * $Id$
 */
package net.opengis.wfs20.validation;

import java.net.URI;

import javax.xml.namespace.QName;

import org.eclipse.emf.common.util.EList;

import org.opengis.filter.Filter;

import org.opengis.filter.sort.SortBy;

/**
 * A sample validator interface for {@link net.opengis.wfs20.QueryType}.
 * This doesn't really do anything, and it's not a real EMF artifact.
 * It was generated by the org.eclipse.emf.examples.generator.validator plug-in to illustrate how EMF's code generator can be extended.
 * This can be disabled with -vmargs -Dorg.eclipse.emf.examples.generator.validator=false.
 */
public interface QueryTypeValidator {
  boolean validate();

  boolean validateFeatureVersion(String value);
  boolean validateSrsName(URI value);
  boolean validateFilter(Filter value);
  boolean validatePropertyNames(EList<QName> value);
  boolean validateSortBy(EList<SortBy> value);
}
