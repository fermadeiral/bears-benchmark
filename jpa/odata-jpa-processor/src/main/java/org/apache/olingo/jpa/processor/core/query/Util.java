package org.apache.olingo.jpa.processor.core.query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.olingo.commons.api.edm.EdmBindingTarget;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.edm.EdmType;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPASelector;
import org.apache.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import org.apache.olingo.jpa.metadata.core.edm.mapper.impl.IntermediateServiceDocument;
import org.apache.olingo.jpa.processor.core.exception.ODataJPAUtilException;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceAction;
import org.apache.olingo.server.api.uri.UriResourceComplexProperty;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;
import org.apache.olingo.server.api.uri.UriResourceFunction;
import org.apache.olingo.server.api.uri.UriResourceKind;
import org.apache.olingo.server.api.uri.UriResourceLambdaVariable;
import org.apache.olingo.server.api.uri.UriResourceNavigation;
import org.apache.olingo.server.api.uri.UriResourcePartTyped;
import org.apache.olingo.server.api.uri.UriResourceProperty;
import org.apache.olingo.server.api.uri.UriResourceValue;
import org.apache.olingo.server.api.uri.queryoption.ExpandItem;
import org.apache.olingo.server.api.uri.queryoption.ExpandOption;

public class Util {

	public static final String VALUE_RESOURCE = "$VALUE";

	public static EdmEntitySet determineTargetEntitySet(final List<UriResource> resources) {
		EdmEntitySet targetEdmEntitySet = null;
		StringBuffer naviPropertyName = new StringBuffer();

		for (final UriResource resourceItem : resources) {
			switch (resourceItem.getKind()) {
			case entitySet:
				targetEdmEntitySet = ((UriResourceEntitySet) resourceItem).getEntitySet();
				break;
			case complexProperty:
				naviPropertyName.append(((UriResourceComplexProperty) resourceItem).getProperty().getName());
				naviPropertyName.append(JPASelector.PATH_SEPERATOR);
				break;
			case navigationProperty:
				naviPropertyName.append(((UriResourceNavigation) resourceItem).getProperty().getName());
				final EdmBindingTarget edmBindingTarget = targetEdmEntitySet.getRelatedBindingTarget(naviPropertyName
						.toString());
				if (edmBindingTarget instanceof EdmEntitySet) {
					targetEdmEntitySet = (EdmEntitySet) edmBindingTarget;
				}
				naviPropertyName = new StringBuffer();
				break;
			case function:
				// bound functions have an entry of type 'entitySet' in resources-path, so we
				// can ignore other settings
				// unbound functions will have a function import targeting an optional entity
				// set
				final UriResourceFunction uriResourceFunction = (UriResourceFunction) resourceItem;
				if (uriResourceFunction.getFunction() != null && !uriResourceFunction.getFunction().isBound()
						&& uriResourceFunction.getFunctionImport() != null) {
					targetEdmEntitySet = uriResourceFunction.getFunctionImport().getReturnedEntitySet();
				}
				break;
			case action:
				// bound actions have an entry of type 'entitySet' in resources-path, so we
				// can ignore other settings
				// unbound actions will have a action import targeting an optional entity set
				final UriResourceAction uriResourceAction = (UriResourceAction) resourceItem;
				if (uriResourceAction.getAction() != null && !uriResourceAction.getAction().isBound()
						&& uriResourceAction.getActionImport() != null) {
					targetEdmEntitySet = uriResourceAction.getActionImport().getReturnedEntitySet();
				}
				break;
			default:
				// do nothing
				break;
			}
		}
		return targetEdmEntitySet;
	}

	/**
	 * Finds an entity type from a navigation property
	 */
	public static EdmEntityType determineTargetEntityType(final List<UriResource> resources) {
		EdmEntityType targetEdmEntity = null;

		for (final UriResource resourceItem : resources) {
			if (resourceItem.getKind() == UriResourceKind.navigationProperty) {
				// first try the simple way like in the example
				targetEdmEntity = (EdmEntityType) ((UriResourceNavigation) resourceItem).getType();
			}
		}
		return targetEdmEntity;
	}

	/**
	 * Finds an entity type with which a navigation may starts. Can be used e.g. for filter:
	 * AdministrativeDivisions?$filter=Parent/CodeID eq 'NUTS1' returns AdministrativeDivision;
	 * AdministrativeDivisions(...)/Parent?$filter=Parent/CodeID eq 'NUTS1' returns "Parent"
	 */
	public static EdmEntityType determineStartEntityType(final List<UriResource> resources) {
		EdmEntityType targetEdmEntity = null;

		for (final UriResource resourceItem : resources) {
			if (resourceItem.getKind() == UriResourceKind.navigationProperty) {
				// first try the simple way like in the example
				targetEdmEntity = (EdmEntityType) ((UriResourceNavigation) resourceItem).getType();
			}
			if (resourceItem.getKind() == UriResourceKind.entitySet) {
				// first try the simple way like in the example
				targetEdmEntity = ((UriResourceEntitySet) resourceItem).getEntityType();
			}
		}
		return targetEdmEntity;
	}

	/**
	 * Used for Serializer
	 */
	public static UriResourceProperty determineStartNavigationPath(final List<UriResource> resources) {
		UriResourceProperty property = null;
		if (resources != null) {
			for (int i = resources.size() - 1; i >= 0; i--) {
				final UriResource resourceItem = resources.get(i);
				if (resourceItem instanceof UriResourceEntitySet || resourceItem instanceof UriResourceNavigation) {
					break;
				}
				property = (UriResourceProperty) resourceItem;
			}
		}
		return property;
	}

	public static String determineProptertyNavigationPath(final List<UriResource> resources) {
		final StringBuffer pathName = new StringBuffer();
		if (resources != null) {
			for (int i = resources.size() - 1; i >= 0; i--) {
				final UriResource resourceItem = resources.get(i);
				if (resourceItem instanceof UriResourceEntitySet || resourceItem instanceof UriResourceNavigation
						|| resourceItem instanceof UriResourceLambdaVariable) {
					break;
				}
				if (resourceItem instanceof UriResourceValue) {
					pathName.insert(0, VALUE_RESOURCE);
					pathName.insert(0, JPASelector.PATH_SEPERATOR);
				} else if (resourceItem instanceof UriResourceProperty) {
					final UriResourceProperty property = (UriResourceProperty) resourceItem;
					pathName.insert(0, property.getProperty().getName());
					pathName.insert(0, JPASelector.PATH_SEPERATOR);
				}
			}
			if (pathName.length() > 0) {
				pathName.deleteCharAt(0);
			}
		}
		return pathName.toString();
	}

	public static JPAAssociationPath determineAssoziation(final IntermediateServiceDocument sd, final EdmType naviStart,
			final StringBuffer associationName) throws ODataApplicationException {
		JPAEntityType naviStartType;

		try {
			naviStartType = sd.getEntityType(naviStart);
			return naviStartType.getAssociationPath(associationName.toString());
		} catch (final ODataJPAModelException e) {
			throw new ODataJPAUtilException(ODataJPAUtilException.MessageKeys.UNKNOWN_NAVI_PROPERTY,
					HttpStatusCode.BAD_REQUEST);
		}
	}

	public static Map<JPAExpandItemWrapper, JPAAssociationPath> determineAssoziations(final IntermediateServiceDocument sd,
			final List<UriResource> startResourceList, final ExpandOption expandOption) throws ODataApplicationException {

		final Map<JPAExpandItemWrapper, JPAAssociationPath> pathList =
				new HashMap<JPAExpandItemWrapper, JPAAssociationPath>();
		final StringBuffer associationNamePrefix = new StringBuffer();

		UriResource startResourceItem = null;
		if (startResourceList != null && expandOption != null) {
			// Example1 : /Organizations('3')/AdministrativeInformation?$expand=Created/User
			// Example2 : /Organizations('3')/AdministrativeInformation?$expand=*
			// Association name needs AdministrativeInformation as prefix
			for (int i = startResourceList.size() - 1; i >= 0; i--) {
				startResourceItem = startResourceList.get(i);
				if (startResourceItem instanceof UriResourceEntitySet || startResourceItem instanceof UriResourceNavigation) {
					break;
				}
				associationNamePrefix.insert(0, JPAAssociationPath.PATH_SEPERATOR);
				associationNamePrefix.insert(0, ((UriResourceProperty) startResourceItem).getProperty().getName());
			}
			// Example1 : ?$expand=Created/User (Property/NavigationProperty)
			// Example2 : ?$expand=Parent/CodeID (NavigationProperty/Property)
			// Example3 : ?$expand=Parent,Children (NavigationProperty, NavigationProperty)
			// Example4 : ?$expand=*
			// Example4 : ?$expand=*/$ref,Parent
			StringBuffer associationName;
			for (final ExpandItem item : expandOption.getExpandItems()) {
				if (item.isStar()) {
					final EdmEntitySet edmEntitySet = determineTargetEntitySet(startResourceList);
					try {
						final JPAEntityType jpaEntityType = sd.getEntitySetType(edmEntitySet.getName());
						final List<JPAAssociationPath> associationPaths = jpaEntityType.getAssociationPathList();
						for (final JPAAssociationPath path : associationPaths) {
							pathList.put(new JPAExpandItemWrapper(item, (JPAEntityType) path.getTargetType()), path);
						}
					} catch (final ODataJPAModelException e) {
						throw new ODataJPAUtilException(ODataJPAUtilException.MessageKeys.UNKNOWN_ENTITY_TYPE,
								HttpStatusCode.BAD_REQUEST);
					}
				} else {
					final List<UriResource> targetResourceList = item.getResourcePath().getUriResourceParts();
					associationName = new StringBuffer();
					associationName.append(associationNamePrefix);
					UriResource targetResourceItem = null;
					for (int i = 0; i < targetResourceList.size(); i++) {
						targetResourceItem = targetResourceList.get(i);
						if (targetResourceItem.getKind() != UriResourceKind.navigationProperty) {
							// if (i < targetResourceList.size() - 1) {
							associationName.append(((UriResourceProperty) targetResourceItem).getProperty().getName());
							associationName.append(JPAAssociationPath.PATH_SEPERATOR);
						} else {
							associationName.append(((UriResourceNavigation) targetResourceItem).getProperty().getName());
							break;
						}
					}
					pathList.put(new JPAExpandItemWrapper(sd, item), Util.determineAssoziation(sd,
							((UriResourcePartTyped) startResourceItem).getType(),
							associationName));
				}
			}
		}
		return pathList;
	}

	public static List<JPANavigationProptertyInfo> determineAssoziations(final IntermediateServiceDocument sd,
			final List<UriResource> resourceParts) throws ODataApplicationException {

		final List<JPANavigationProptertyInfo> pathList = new ArrayList<JPANavigationProptertyInfo>();

		StringBuffer associationName = null;
		UriResourceNavigation navigation = null;
		if (resourceParts != null && hasNavigation(resourceParts)) {
			for (int i = resourceParts.size() - 1; i >= 0; i--) {
				if (resourceParts.get(i) instanceof UriResourceNavigation && navigation == null) {
					navigation = (UriResourceNavigation) resourceParts.get(i);
					associationName = new StringBuffer();
					associationName.insert(0, navigation.getProperty().getName());
				} else {
					if (resourceParts.get(i) instanceof UriResourceComplexProperty) {
						associationName.insert(0, JPASelector.PATH_SEPERATOR);
						associationName.insert(0, ((UriResourceComplexProperty) resourceParts.get(i)).getProperty().getName());
					}
					if (resourceParts.get(i) instanceof UriResourceNavigation
							|| resourceParts.get(i) instanceof UriResourceEntitySet) {
						pathList.add(new JPANavigationProptertyInfo((UriResourcePartTyped) resourceParts.get(i),
								determineAssoziationPath(sd, ((UriResourcePartTyped) resourceParts.get(i)), associationName)));
						if (resourceParts.get(i) instanceof UriResourceNavigation) {
							navigation = (UriResourceNavigation) resourceParts.get(i);
							associationName = new StringBuffer();
							associationName.insert(0, navigation.getProperty().getName());
						}
					}
				}
			}
		}
		return pathList;
	}

	public static boolean hasNavigation(final List<UriResource> uriResourceParts) {
		if (uriResourceParts != null) {
			for (int i = uriResourceParts.size() - 1; i >= 0; i--) {
				if (uriResourceParts.get(i) instanceof UriResourceNavigation) {
					return true;
				}
			}
		}
		return false;
	}

	public static JPAAssociationPath determineAssoziationPath(final IntermediateServiceDocument sd,
			final UriResourcePartTyped naviStart, final StringBuffer associationName) throws ODataApplicationException {

		JPAEntityType naviStartType;
		try {
			if (naviStart instanceof UriResourceEntitySet) {
				naviStartType = sd.getEntityType(((UriResourceEntitySet) naviStart).getType());
			} else {
				naviStartType = sd.getEntityType(((UriResourceNavigation) naviStart).getProperty().getType());
			}
			return naviStartType.getAssociationPath(associationName.toString());
		} catch (final ODataJPAModelException e) {
			throw new ODataJPAUtilException(ODataJPAUtilException.MessageKeys.UNKNOWN_NAVI_PROPERTY,
					HttpStatusCode.BAD_REQUEST);
		}
	}
}
