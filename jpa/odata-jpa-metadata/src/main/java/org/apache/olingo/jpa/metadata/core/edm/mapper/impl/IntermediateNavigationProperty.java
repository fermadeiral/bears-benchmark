package org.apache.olingo.jpa.metadata.core.edm.mapper.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.AssociationOverride;
import javax.persistence.CascadeType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.Attribute.PersistentAttributeType;
import javax.persistence.metamodel.SingularAttribute;

import org.apache.olingo.commons.api.edm.provider.CsdlNavigationProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlOnDelete;
import org.apache.olingo.commons.api.edm.provider.CsdlOnDeleteAction;
import org.apache.olingo.commons.api.edm.provider.CsdlReferentialConstraint;
import org.apache.olingo.jpa.metadata.core.edm.annotation.EdmIgnore;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationAttribute;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAAttributeAccessor;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPASimpleAttribute;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAStructuredType;
import org.apache.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import org.apache.olingo.jpa.metadata.core.edm.mapper.extention.IntermediateNavigationPropertyAccess;

/**
 * A navigation property describes a relation of one entity type to another entity type and allows to navigate to it.
 * IntermediateNavigationProperty represents a navigation within on service, that is source and target are described by
 * the same service document.
 * <a href=
 * "http://docs.oasis-open.org/odata/odata/v4.0/errata02/os/complete/part3-csdl/odata-v4.0-errata02-os-part3-csdl-complete.html#_Toc406397962"
 * >OData Version 4.0 Part 3 - 7 Navigation Property</a>
 * 
 * @author Oliver Grande
 *
 */
class IntermediateNavigationProperty extends IntermediateModelElement
        implements IntermediateNavigationPropertyAccess, JPAAssociationAttribute {

	private final static Logger LOG = Logger.getLogger(IntermediateNavigationProperty.class.getName());

	private final Attribute<?, ?> jpaAttribute;
	private CsdlNavigationProperty edmNaviProperty;
	private CsdlOnDelete edmOnDelete;
	private final JPAStructuredType sourceType;
	private IntermediateStructuredType targetType;
	private final IntermediateServiceDocument serviceDocument;
	private final List<IntermediateJoinColumn> joinColumns = new LinkedList<IntermediateJoinColumn>();
	private final JPAAttributeAccessor accessor;

	IntermediateNavigationProperty(final JPAEdmNameBuilder nameBuilder, final JPAStructuredType parent, final Attribute<?, ?> jpaAttribute,
	        final IntermediateServiceDocument serviceDocument) {
		super(nameBuilder, jpaAttribute.getName());
		this.jpaAttribute = jpaAttribute;
		this.serviceDocument = serviceDocument;
		this.sourceType = parent;
		buildNaviProperty();
		accessor = new FieldAttributeAccessor((Field) jpaAttribute.getJavaMember());
	}

	@Override
	public JPAAttributeAccessor getAttributeAccessor() {
		return accessor;
	}

	@Override
	public <T extends Annotation> T getAnnotation(final Class<T> annotationClass) {
		if (jpaAttribute.getJavaMember() instanceof AnnotatedElement) {
			return ((AnnotatedElement) jpaAttribute.getJavaMember()).getAnnotation(annotationClass);
		}
		return null;
	}

	@Override
	public CsdlNavigationProperty getProperty() throws ODataJPAModelException {
		return getEdmItem();
	}

	@Override
	public JPAStructuredType getStructuredType() {
		try {
			return getTargetEntity();
		} catch (final ODataJPAModelException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public JPAStructuredType getTargetEntity() throws ODataJPAModelException {
		lazyBuildEdmItem();
		return targetType;
	}

	@Override
	public Class<?> getType() {
		return jpaAttribute.getJavaType();
	}

	@Override
	public boolean isAssociation() {
		return true;
	}

	@Override
	public boolean isCollection() {
		return jpaAttribute.isCollection();
	}

	@Override
	public boolean isComplex() {
		// navigation properties are targeting always a non primitive object
		return true;
	}

	@Override
	public boolean isPrimitive() {
		// navigation properties are targeting always a non primitive object
		return false;
	}

	@Override
	public boolean isKey() {
		if (jpaAttribute instanceof SingularAttribute<?, ?>) {
			return ((SingularAttribute<?, ?>) jpaAttribute).isId();
		}
		return false;
	}

	@Override
	public boolean isSearchable() {
		return false;
	}

	@Override
	public void setOnDelete(final CsdlOnDelete onDelete) {
		edmOnDelete = onDelete;
	}

	@Override
	protected void lazyBuildEdmItem() throws ODataJPAModelException {
		if (edmNaviProperty == null) {
			targetType = serviceDocument.getStructuredType(jpaAttribute);

			String mappedBy = null;
			boolean isSourceOne = false;
			edmNaviProperty = new CsdlNavigationProperty();
			edmNaviProperty.setName(getExternalName());
			edmNaviProperty.setType(nameBuilder.buildFQN(targetType.getExternalName()));
			edmNaviProperty.setCollection(jpaAttribute.isCollection());
			// Optional --> ReleationAnnotation
			if (jpaAttribute.getJavaMember() instanceof AnnotatedElement) {
				final AnnotatedElement annotatedElement = (AnnotatedElement) jpaAttribute.getJavaMember();
				switch (jpaAttribute.getPersistentAttributeType()) {
				case ONE_TO_MANY:
					final OneToMany cardinalityOtM = annotatedElement.getAnnotation(OneToMany.class);
					if (cardinalityOtM != null) {
						mappedBy = cardinalityOtM.mappedBy();
						edmNaviProperty.setOnDelete(edmOnDelete != null ? edmOnDelete : setJPAOnDelete(cardinalityOtM.cascade()));
					}
					isSourceOne = true;
					break;
				case ONE_TO_ONE:
					final OneToOne cardinalityOtO = annotatedElement.getAnnotation(OneToOne.class);
					edmNaviProperty.setNullable(cardinalityOtO.optional());
					mappedBy = cardinalityOtO.mappedBy();
					isSourceOne = true;
					edmNaviProperty.setOnDelete(edmOnDelete != null ? edmOnDelete : setJPAOnDelete(cardinalityOtO.cascade()));
					break;
				case MANY_TO_ONE:
					final ManyToOne cardinalityMtO = annotatedElement.getAnnotation(ManyToOne.class);
					edmNaviProperty.setNullable(cardinalityMtO.optional());
					edmNaviProperty.setOnDelete(edmOnDelete != null ? edmOnDelete : setJPAOnDelete(cardinalityMtO.cascade()));
					break;
				case MANY_TO_MANY:
					// fall through
				default:
					break;
				}

				int implicitColumns = 0;
				final JoinColumns columns = annotatedElement.getAnnotation(JoinColumns.class);
				if (columns != null) {
					for (final JoinColumn column : columns.value()) {
						final IntermediateJoinColumn intermediateColumn = new IntermediateJoinColumn(column);
						final String refColumnName = intermediateColumn.getReferencedColumnName();
						final String name = intermediateColumn.getName();
						if (refColumnName == null || refColumnName.isEmpty() || name == null || name.isEmpty()) {
							implicitColumns += 1;
							if (implicitColumns > 1) {
								throw new ODataJPAModelException(ODataJPAModelException.MessageKeys.NOT_SUPPORTED_NO_IMPLICIT_COLUMNS,
								        getInternalName());
							}
							fillMissingName(isSourceOne, intermediateColumn);
						}
						joinColumns.add(intermediateColumn);
					}
				} else {
					final JoinColumn column = annotatedElement.getAnnotation(JoinColumn.class);
					if (column != null) {
						final IntermediateJoinColumn intermediateColumn = new IntermediateJoinColumn(column);
						fillMissingName(isSourceOne, intermediateColumn);
						joinColumns.add(intermediateColumn);

					} else if (mappedBy != null && !mappedBy.isEmpty()) {
						// find the join columns on opposite and fill up with informations on our
						// (source) side
						final List<IntermediateJoinColumn> oppositeJoinColumns = targetType.getJoinColumns(mappedBy);
						if (oppositeJoinColumns.isEmpty()) {
							// no explicit mapping on other side, try with 'id'
							final IntermediateJoinColumn intermediateColumn = buildDefaultIdBasedJoinColumn(isSourceOne);
							joinColumns.add(intermediateColumn);

						} else {
							IntermediateJoinColumn intermediateJoinColumn;
							for (final IntermediateJoinColumn inverseColumn : oppositeJoinColumns) {
								String columnName;
								String refernceColumnName;
								switch (jpaAttribute.getPersistentAttributeType()) {
								case ONE_TO_MANY:
									// invert direction by using the SAME
									refernceColumnName = inverseColumn.getReferencedColumnName();
									columnName = inverseColumn.getName();
									intermediateJoinColumn = new IntermediateJoinColumn(columnName, refernceColumnName);
									fillMissingName(isSourceOne, intermediateJoinColumn);
									joinColumns.add(intermediateJoinColumn);
									break;
								case ONE_TO_ONE:
									columnName = inverseColumn.getReferencedColumnName();
									refernceColumnName = inverseColumn.getName();
									intermediateJoinColumn = new IntermediateJoinColumn(columnName, refernceColumnName);
									fillMissingName(isSourceOne, intermediateJoinColumn);
									joinColumns.add(intermediateJoinColumn);
									break;
								default:
									throw new ODataJPAModelException(ODataJPAModelException.MessageKeys.INVALID_ASSOCIATION);
								}
							}
						}
					}
				}
				// Determine referential constraint
				determineReferentialConstraints(annotatedElement);
			}

			// TODO determine ContainsTarget

			if (sourceType instanceof IntermediateEntityType) {
				// Partner Attribute must not be defined at Complex Types.
				// JPA bi-directional associations are defined at both sides, e.g.
				// at the BusinessPartner and at the Roles. JPA only defines the
				// "mappedBy" at the Parent.
				if (mappedBy != null && !mappedBy.isEmpty()) {
					// edmNaviProperty.setPartner(targetType.getCorrespondingNavigationProperty(sourceType, getInternalName())
					// .getExternalName());
					edmNaviProperty.setPartner(targetType.getAssociation(mappedBy).getExternalName());
				} else {
					// no @JoinColumn and no 'mappedBy'... try alternative ways
					final IntermediateNavigationProperty partner = targetType.getCorrespondingAssociation(sourceType, getInternalName());
					if (partner != null) {
						if (partner.isMapped()) {
							edmNaviProperty.setPartner(partner.getExternalName());
						}
					} else if (isSourceOne && joinColumns.isEmpty()) {
						// define joins by 'id' column(s)
						final IntermediateJoinColumn intermediateColumn = buildDefaultIdBasedJoinColumn(isSourceOne);
						joinColumns.add(intermediateColumn);
					}
				}
			}

			if (joinColumns.isEmpty()) {
				LOG.log(Level.SEVERE, "Navigation property (" + sourceType.getInternalName() + "#" + getInternalName()
				        + ") without columns to join found, navigation to target entity is not possible!");
			}

		}

	}

	private void determineReferentialConstraints(final AnnotatedElement annotatedElement) throws ODataJPAModelException {

		final AssociationOverride overwrite = annotatedElement.getAnnotation(AssociationOverride.class);
		if (overwrite != null) {
			return;
		}

		final List<CsdlReferentialConstraint> constraints = edmNaviProperty.getReferentialConstraints();
		for (final IntermediateJoinColumn intermediateColumn : joinColumns) {

			final CsdlReferentialConstraint constraint = new CsdlReferentialConstraint();
			IntermediateModelElement sP = null;
			IntermediateModelElement tP = null;
			// TODO: navigation properties are only allowed for JPA types, so we can cast...
			// but has bad smell
			sP = ((IntermediateStructuredType) sourceType).getPropertyByDBField(intermediateColumn.getName());
			if (sP != null) {
				// do not create referential constraints (visible in $metadata) for ignored
				// attributes
				if (sP.ignore()) {
					continue;
				}
				constraint.setProperty(sP.getExternalName());
				tP = targetType.getPropertyByDBField(intermediateColumn.getReferencedColumnName());
				if (tP == null) {
					final ODataJPAModelException ex = new ODataJPAModelException(
					        ODataJPAModelException.MessageKeys.REFERENCED_PROPERTY_NOT_FOUND, getInternalName(),
					        intermediateColumn.getReferencedColumnName(), targetType.getExternalName());
					LOG.log(Level.FINER, ex.getMessage());
					// skip constraint
					continue;
				}
				// do not create referential constraints (visible in $metadata) for ignored
				// attributes
				if (tP.ignore()) {
					continue;
				}
				constraint.setReferencedProperty(tP.getExternalName());
				constraints.add(constraint);
			} else {
				// TODO: navigation properties are only allowed for JPA types, so we can cast...
				// but has bad smell
				sP = ((IntermediateStructuredType) sourceType).getPropertyByDBField(intermediateColumn.getReferencedColumnName());
				if (sP == null) {
					final ODataJPAModelException ex = new ODataJPAModelException(
					        ODataJPAModelException.MessageKeys.REFERENCED_PROPERTY_NOT_FOUND, getInternalName(),
					        intermediateColumn.getReferencedColumnName(), sourceType.getExternalName());
					LOG.log(Level.FINER, ex.getMessage());
					// skip constraint
					continue;
				}
				// do not create referential constraints (visible in $metadata) for ignored
				// attributes
				if (sP.ignore()) {
					continue;
				}
				constraint.setProperty(sP.getExternalName());
				tP = targetType.getPropertyByDBField(intermediateColumn.getName());
				if (tP == null) {
					final ODataJPAModelException ex = new ODataJPAModelException(
					        ODataJPAModelException.MessageKeys.REFERENCED_PROPERTY_NOT_FOUND, getInternalName(),
					        intermediateColumn.getName(), targetType.getExternalName());
					LOG.log(Level.FINE, ex.getMessage());
					// skip constraint
					continue;
				}
				// do not create referential constraints (visible in $metadata) for ignored
				// attributes
				if (tP.ignore()) {
					continue;
				}
				constraint.setReferencedProperty(tP.getExternalName());
				constraints.add(constraint);
			}
		}
	}

	@Override
	CsdlNavigationProperty getEdmItem() throws ODataJPAModelException {
		lazyBuildEdmItem();
		return edmNaviProperty;
	}

	PersistentAttributeType getJoinCardinality() throws ODataJPAModelException {
		return jpaAttribute.getPersistentAttributeType();
	}

	List<IntermediateJoinColumn> getJoinColumns() throws ODataJPAModelException {
		lazyBuildEdmItem();
		return joinColumns;
	}

	private boolean isMapped() {
		if (jpaAttribute.getPersistentAttributeType() == PersistentAttributeType.ONE_TO_ONE) {
			final AnnotatedElement annotatedElement = (AnnotatedElement) jpaAttribute.getJavaMember();
			final OneToOne cardinalityOtO = annotatedElement.getAnnotation(OneToOne.class);
			return cardinalityOtO.mappedBy() != null && !cardinalityOtO.mappedBy().isEmpty() ? true : false;
		}
		if (jpaAttribute.getPersistentAttributeType() == PersistentAttributeType.ONE_TO_MANY) {
			final AnnotatedElement annotatedElement = (AnnotatedElement) jpaAttribute.getJavaMember();
			final OneToMany cardinalityOtM = annotatedElement.getAnnotation(OneToMany.class);
			return cardinalityOtM.mappedBy() != null && !cardinalityOtM.mappedBy().isEmpty() ? true : false;
		}
		return false;
	}

	private void buildNaviProperty() {
		this.setExternalName(nameBuilder.buildNaviPropertyName(jpaAttribute));
		if (this.jpaAttribute.getJavaMember() instanceof AnnotatedElement) {
			final EdmIgnore jpaIgnore = ((AnnotatedElement) this.jpaAttribute.getJavaMember()).getAnnotation(EdmIgnore.class);
			if (jpaIgnore != null) {
				this.setIgnore(true);
			}
		}

		postProcessor.processNavigationProperty(this, jpaAttribute.getDeclaringType().getJavaType().getCanonicalName());
	}

	/**
	 * Calculate the a join column name based on default JPA naming stratgey:
	 * <ul>
	 * <li>the foreign key is located in the source table</li>
	 * <li>the foreign key has name with pattern: <b>&lt;relationship attribute
	 * name&gt;_&lt;target table primary key name&gt</b></li>
	 * </ul>
	 * Requirements: {@link #targetType} must be set, {@link #sourceType} must be
	 * set
	 */
	private IntermediateJoinColumn buildDefaultIdBasedJoinColumn(final boolean isSourceOne) throws ODataJPAModelException {
		final List<JPASimpleAttribute> targetKeyAttributes = targetType.getKeyAttributes();
		if (targetKeyAttributes.size() != 1) {
			throw new ODataJPAModelException(ODataJPAModelException.MessageKeys.NOT_SUPPORTED_NO_IMPLICIT_COLUMNS, this.getExternalName());
		}
		final String targetKeyName = targetKeyAttributes.get(0).getDBFieldName();
		String sourceKeyName = targetKeyName;
		if (sourceKeyName.startsWith("\"")) {
			// remove wrapping "" characters from generated name
			sourceKeyName = sourceKeyName.substring(1, sourceKeyName.length() - 1);
		}
		final IntermediateJoinColumn intermediateColumn = new IntermediateJoinColumn(Character.toString(getExternalName().charAt(0))
		        .toUpperCase(Locale.ENGLISH).concat(getExternalName().substring(1)).concat("_").concat(sourceKeyName), targetKeyName);
		return intermediateColumn;
	}

	private List<JPASimpleAttribute> determineCheckedNumberOfKeyAttributes(final JPAStructuredType theType) throws ODataJPAModelException {
		final List<JPASimpleAttribute> attributes = theType.getKeyAttributes();
		if (attributes.isEmpty()) {
			throw new ODataJPAModelException(ODataJPAModelException.MessageKeys.INVALID_ASSOCIATION);
		}
		if (attributes.size() > 1) {
			throw new ODataJPAModelException(ODataJPAModelException.MessageKeys.NOT_SUPPORTED_ATTRIBUTE_TYPE, this.getExternalName(),
			        theType.getExternalName());
		}
		return attributes;
	}

	private void fillMissingName(final boolean isSourceOne, final IntermediateJoinColumn intermediateColumn) throws ODataJPAModelException {

		final String refColumnName = intermediateColumn.getReferencedColumnName();
		final String name = intermediateColumn.getName();
		switch (jpaAttribute.getPersistentAttributeType()) {
		case MANY_TO_ONE:
			if (!isSourceOne && (refColumnName == null || refColumnName.isEmpty())) {
				final List<JPASimpleAttribute> targetKeyAttributes = determineCheckedNumberOfKeyAttributes(targetType);
				intermediateColumn.setReferencedColumnName(targetKeyAttributes.get(0).getDBFieldName());
			} else if (!isSourceOne && (name == null || name.isEmpty())) {
				final List<JPASimpleAttribute> sourceKeyAttributes = determineCheckedNumberOfKeyAttributes(sourceType);
				intermediateColumn.setName(sourceKeyAttributes.get(0).getDBFieldName());
			}
			break;
		case ONE_TO_ONE:
			if (isSourceOne && (refColumnName == null || refColumnName.isEmpty())) {
				final List<JPASimpleAttribute> targetKeyAttributes = determineCheckedNumberOfKeyAttributes(targetType);
				intermediateColumn.setReferencedColumnName(targetKeyAttributes.get(0).getDBFieldName());
			} else if (isSourceOne && (name == null || name.isEmpty())) {
				final List<JPASimpleAttribute> sourceKeyAttributes = determineCheckedNumberOfKeyAttributes(sourceType);
				intermediateColumn.setName(sourceKeyAttributes.get(0).getDBFieldName());
			}
			break;
		case MANY_TO_MANY:
			if (!isSourceOne && (refColumnName == null || refColumnName.isEmpty())) {
				final List<JPASimpleAttribute> targetKeyAttributes = determineCheckedNumberOfKeyAttributes(targetType);
				intermediateColumn.setReferencedColumnName(targetKeyAttributes.get(0).getDBFieldName());
			} else if (!isSourceOne && (name == null || name.isEmpty())) {
				final List<JPASimpleAttribute> sourceKeyAttributes = determineCheckedNumberOfKeyAttributes(sourceType);
				intermediateColumn.setReferencedColumnName(sourceKeyAttributes.get(0).getDBFieldName());
			}
			break;
		case ONE_TO_MANY:
			if (refColumnName == null || refColumnName.isEmpty()) {
				final List<JPASimpleAttribute> sourceKeyAttributes = determineCheckedNumberOfKeyAttributes(sourceType);
				intermediateColumn.setReferencedColumnName(sourceKeyAttributes.get(0).getDBFieldName());
			} else if (isSourceOne && (name == null || name.isEmpty())) {
				final List<JPASimpleAttribute> targetKeyAttributes = determineCheckedNumberOfKeyAttributes(targetType);
				intermediateColumn.setName(targetKeyAttributes.get(0).getDBFieldName());
			}
			break;
		default:
			throw new ODataJPAModelException(ODataJPAModelException.MessageKeys.INVALID_ASSOCIATION);
		}

	}

	private CsdlOnDelete setJPAOnDelete(final CascadeType[] cascades) {
		for (final CascadeType cascade : cascades) {
			if (cascade == CascadeType.REMOVE || cascade == CascadeType.ALL) {
				final CsdlOnDelete onDelete = new CsdlOnDelete();
				onDelete.setAction(CsdlOnDeleteAction.Cascade);
				return onDelete;
			}
		}
		return null;
	}
}
