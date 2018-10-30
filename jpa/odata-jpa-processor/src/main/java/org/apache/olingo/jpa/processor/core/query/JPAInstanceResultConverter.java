package org.apache.olingo.jpa.processor.core.query;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.data.ValueType;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPASelector;
import org.apache.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import org.apache.olingo.jpa.metadata.core.edm.mapper.impl.JPAEdmNameBuilder;
import org.apache.olingo.jpa.metadata.core.edm.mapper.impl.IntermediateServiceDocument;
import org.apache.olingo.jpa.processor.core.exception.ODataJPAQueryException;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.serializer.SerializerException;
import org.apache.olingo.server.api.uri.UriHelper;

public class JPAInstanceResultConverter {
	public static final String ACCESS_MODIFIER_GET = "get";
	public static final String ACCESS_MODIFIER_SET = "set";
	public static final String ACCESS_MODIFIER_IS = "is";
	private final static Map<String, HashMap<String, Method>> MESSAGE_BUFFER =
			new HashMap<String, HashMap<String, Method>>();

	private final List<?> jpaQueryResult;
	private final EdmEntitySet edmEntitySet;
	private final Map<String, Method> messageMap;
	private final List<JPASelector> pathList;
	private final UriHelper odataUriHelper;

	public JPAInstanceResultConverter(final UriHelper uriHelper, final IntermediateServiceDocument sd,
			final List<?> jpaQueryResult, final EdmEntitySet edmEntitySet, final Class<?> resultType)
					throws ODataJPAModelException {
		super();
		this.jpaQueryResult = jpaQueryResult;
		this.edmEntitySet = edmEntitySet;
		this.pathList = sd.getEntitySetType(edmEntitySet.getName()).getPathList();
		this.messageMap = getMethods(resultType);
		this.odataUriHelper = uriHelper;
	}

	private Map<String, Method> getMethods(final Class<?> clazz) {
		HashMap<String, Method> methods = MESSAGE_BUFFER.get(clazz.getName());
		if (methods == null) {
			methods = new HashMap<String, Method>();

			final Method[] allMethods = clazz.getMethods();
			for (final Method m : allMethods) {
				if (m.getReturnType().getName() != "void"
						&& Modifier.isPublic(m.getModifiers())) {
					methods.put(m.getName(), m);
				}
			}
			MESSAGE_BUFFER.put(clazz.getName(), methods);
		}
		return methods;
	}

	public EntityCollection getResult() throws ODataApplicationException, SerializerException, URISyntaxException {
		final EntityCollection odataEntityCollection = new EntityCollection();
		final List<Entity> odataResults = odataEntityCollection.getEntities();

		for (final Object row : jpaQueryResult) {
			final Entity odataEntity = new Entity();
			odataEntity.setType(edmEntitySet.getEntityType().getFullQualifiedName().getFullQualifiedNameAsString());
			final List<Property> properties = odataEntity.getProperties();
			for (final JPASelector path : pathList) {
				final String attributeName = path.getLeaf().getInternalName();
				final String getterName = ACCESS_MODIFIER_GET + JPAEdmNameBuilder.firstToUpper(attributeName);

				if (messageMap.get(getterName) == null) {
					throw new ODataJPAQueryException(ODataJPAQueryException.MessageKeys.QUERY_RESULT_ACCESS_NOT_FOUND,
							HttpStatusCode.INTERNAL_SERVER_ERROR, path.getAlias());
				}
				Method getMethod;
				getMethod = messageMap.get(getterName);
				try {
					properties.add(new Property(
							null,
							path.getLeaf().getExternalName(),
							ValueType.PRIMITIVE,
							getMethod.invoke(row)));
				} catch (final IllegalAccessException e) {
					throw new ODataJPAQueryException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
				} catch (final IllegalArgumentException e) {
					throw new ODataJPAQueryException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
				} catch (final InvocationTargetException e) {
					throw new ODataJPAQueryException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
				}
			}
			odataEntity.setId(new URI(odataUriHelper.buildKeyPredicate(edmEntitySet.getEntityType(), odataEntity)));
			odataResults.add(odataEntity);
		}
		return odataEntityCollection;
	}

}
