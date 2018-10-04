/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package net.opengis.wcs10.validation;

import net.opengis.wcs10.MetadataTypeType;

/**
 * A sample validator interface for {@link net.opengis.wcs10.MetadataLinkType}.
 * This doesn't really do anything, and it's not a real EMF artifact.
 * It was generated by the org.eclipse.emf.examples.generator.validator plug-in to illustrate how EMF's code generator can be extended.
 * This can be disabled with -vmargs -Dorg.eclipse.emf.examples.generator.validator=false.
 */
public interface MetadataLinkTypeValidator {
    boolean validate();

    boolean validateMetadataType(MetadataTypeType value);
}
