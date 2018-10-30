package org.apache.olingo.jpa.metadata.core.edm.mapper.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.geo.SRID;
import org.apache.olingo.commons.api.edm.provider.CsdlFunction;
import org.apache.olingo.commons.api.edm.provider.CsdlParameter;
import org.apache.olingo.commons.api.edm.provider.CsdlReturnType;
import org.apache.olingo.jpa.metadata.core.edm.annotation.EdmFunction;
import org.apache.olingo.jpa.metadata.core.edm.annotation.EdmFunction.ReturnType;
import org.apache.olingo.jpa.metadata.core.edm.annotation.EdmFunctionParameter;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAFunction;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAOperationParameter;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAOperationResultParameter;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAStructuredType;
import org.apache.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;

/**
 * Mapper, that is able to convert different metadata resources into a edm function metadata. It is important to know
 * that:
 * <cite>Functions MUST NOT have observable side effects and MUST return a single instance or a collection of instances
 * of any type.</cite>
 * <p>For details about Function metadata see:
 * <a href=
 * "https://docs.oasis-open.org/odata/odata/v4.0/errata02/os/complete/part3-csdl/odata-v4.0-errata02-os-part3-csdl-complete.html#_Toc406398010"
 * >OData Version 4.0 Part 3 - 12.2 Element edm:Function</a>
 * @author Oliver Grande
 *
 */

class IntermediateFunction extends IntermediateModelElement implements JPAFunction {
	private CsdlFunction edmFunction;
	private final EdmFunction jpaUserDefinedFunction;
	private final AbstractJPASchema schema;
	private final Class<?> jpaDefiningPOJO;

	IntermediateFunction(final JPAEdmNameBuilder nameBuilder, final EdmFunction jpaFunction,
			final Class<?> definingPOJO, final AbstractJPASchema schema)
					throws ODataJPAModelException {
		super(nameBuilder, JPANameBuilder.buildFunctionName(jpaFunction));
		this.setExternalName(jpaFunction.name());
		this.jpaUserDefinedFunction = jpaFunction;
		this.jpaDefiningPOJO = definingPOJO;
		this.schema = schema;
	}

	@Override
	public String getDBName() {
		return jpaUserDefinedFunction.functionName();
	}

	@Override
	public List<JPAOperationParameter> getParameter() {
		final List<JPAOperationParameter> parameterList = new ArrayList<JPAOperationParameter>();
		for (final EdmFunctionParameter jpaParameter : jpaUserDefinedFunction.parameter()) {
			parameterList.add(new IntermediatFunctionParameter(jpaParameter));
		}
		return parameterList;
	}

	@Override
	public JPAOperationResultParameter getResultParameter() {
		return new IntermediatResultFunctionParameter(jpaUserDefinedFunction.returnType());
	}

	@Override
	protected void lazyBuildEdmItem() throws ODataJPAModelException {
		if (edmFunction == null) {
			edmFunction = new CsdlFunction();
			edmFunction.setName(getExternalName());
			edmFunction.setParameters(returnNullIfEmpty(determineEdmInputParameter()));
			edmFunction.setReturnType(determineEdmResultType(jpaUserDefinedFunction.returnType()));
			edmFunction.setBound(jpaUserDefinedFunction.isBound());
			// TODO edmFunction.setComposable(isComposable)
			edmFunction.setComposable(false);
			// TODO edmFunction.setEntitySetPath(entitySetPath) for bound functions

		}
	}

	@Override
	CsdlFunction getEdmItem() throws ODataJPAModelException {
		lazyBuildEdmItem();
		return edmFunction;
	}

	String getUserDefinedFunction() {
		return jpaUserDefinedFunction.functionName();
	}

	boolean requiresFunctionImport() {
		return !isBound() && jpaUserDefinedFunction.hasFunctionImport();
	}

	@Override
	public boolean isBound() {
		return jpaUserDefinedFunction.isBound();
	}

	private List<CsdlParameter> determineEdmInputParameter() throws ODataJPAModelException {
		final List<CsdlParameter> edmInputParameterList = new ArrayList<CsdlParameter>();
		for (final EdmFunctionParameter jpaParameter : jpaUserDefinedFunction.parameter()) {

			final CsdlParameter edmInputParameter = new CsdlParameter();
			edmInputParameter.setName(jpaParameter.name());
			edmInputParameter.setType(JPATypeConvertor.convertToEdmSimpleType(jpaParameter.type()).getFullQualifiedName());

			edmInputParameter.setNullable(false);
			edmInputParameter.setCollection(jpaParameter.isCollection());
			if (jpaParameter.maxLength() >= 0) {
				edmInputParameter.setMaxLength(jpaParameter.maxLength());
			}
			if (jpaParameter.precision() >= 0) {
				edmInputParameter.setPrecision(jpaParameter.precision());
			}
			if (jpaParameter.scale() >= 0) {
				edmInputParameter.setScale(jpaParameter.scale());
			}
			if (jpaParameter.srid() != null && !jpaParameter.srid().srid().isEmpty()) {
				final SRID srid = SRID.valueOf(jpaParameter.srid().srid());
				srid.setDimension(jpaParameter.srid().dimension());
				edmInputParameter.setSrid(srid);
			}
			edmInputParameterList.add(edmInputParameter);
		}
		return edmInputParameterList;
	}

	// TODO handle multiple schemas
	private CsdlReturnType determineEdmResultType(final ReturnType returnType) throws ODataJPAModelException {
		final CsdlReturnType edmResultType = new CsdlReturnType();
		FullQualifiedName fqn;
		if (returnType.type() == Object.class) {
			final JPAStructuredType et = schema.getEntityType(jpaDefiningPOJO);
			fqn = nameBuilder.buildFQN(et.getEdmItem().getName());
			this.setIgnore(et.ignore()); // If the result type shall be ignored, ignore also a function that returns it
		} else {
			final JPAStructuredType et = schema.getEntityType(returnType.type());
			if (et != null) {
				fqn = nameBuilder.buildFQN(et.getEdmItem().getName());
				this.setIgnore(et.ignore()); // If the result type shall be ignored, ignore also a function that returns it
			} else {
				fqn = JPATypeConvertor.convertToEdmSimpleType(returnType.type()).getFullQualifiedName();
			}
		}
		edmResultType.setType(fqn);
		edmResultType.setCollection(returnType.isCollection());
		edmResultType.setNullable(returnType.isNullable());
		if (returnType.maxLength() >= 0) {
			edmResultType.setMaxLength(returnType.maxLength());
		}
		if (returnType.precision() >= 0) {
			edmResultType.setPrecision(returnType.precision());
		}
		if (returnType.scale() >= 0) {
			edmResultType.setScale(returnType.scale());
		}
		if (returnType.srid() != null && !returnType.srid().srid().isEmpty()) {
			final SRID srid = SRID.valueOf(returnType.srid().srid());
			srid.setDimension(returnType.srid().dimension());
			edmResultType.setSrid(srid);
		}
		return edmResultType;
	}

	private class IntermediatFunctionParameter implements JPAOperationParameter {
		private final EdmFunctionParameter jpaParameter;

		IntermediatFunctionParameter(final EdmFunctionParameter jpaParameter) {
			this.jpaParameter = jpaParameter;
		}

		@SuppressWarnings("unused")
		public String getDBName() {
			return jpaParameter.parameterName();
		}

		@Override
		public String getName() {
			return jpaParameter.name();
		}

		@Override
		public Class<?> getType() {
			return jpaParameter.type();
		}

		@Override
		public Integer getMaxLength() {
			return jpaParameter.maxLength();
		}

		@Override
		public Integer getPrecision() {
			return jpaParameter.precision();
		}

		@Override
		public Integer getScale() {
			return jpaParameter.scale();
		}

		@Override
		public FullQualifiedName getTypeFQN() throws ODataJPAModelException {
			return JPATypeConvertor.convertToEdmSimpleType(jpaParameter.type()).getFullQualifiedName();
		}

		@Override
		public ParameterKind getParameterKind() {
			// TODO support @Inject also for functions
			return ParameterKind.OData;
		}
	}

	private class IntermediatResultFunctionParameter implements JPAOperationResultParameter {
		private final ReturnType jpaReturnType;

		public IntermediatResultFunctionParameter(final ReturnType jpaReturnType) {
			this.jpaReturnType = jpaReturnType;
		}

		@Override
		public Class<?> getType() {
			return jpaReturnType.type();
		}

		@Override
		public Integer getMaxLength() {
			return jpaReturnType.maxLength();
		}

		@Override
		public Integer getPrecision() {
			return jpaReturnType.precision();
		}

		@Override
		public Integer getScale() {
			return jpaReturnType.scale();
		}

		@Override
		public FullQualifiedName getTypeFQN() {
			return edmFunction.getReturnType().getTypeFQN();
		}

		@Override
		public boolean isCollection() {
			return jpaReturnType.isCollection();
		}

	}
}
