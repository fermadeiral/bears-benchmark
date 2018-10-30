package org.apache.olingo.jpa.processor.core.query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;

import javax.persistence.EntityManager;
import javax.persistence.Tuple;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.AbstractQuery;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;
import javax.persistence.criteria.Subquery;

import org.apache.olingo.commons.api.edm.EdmNavigationProperty;
import org.apache.olingo.commons.api.edm.EdmProperty;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationAttribute;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAAttribute;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAAttributePath;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAOnConditionItem;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPASelector;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAStructuredType;
import org.apache.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import org.apache.olingo.jpa.processor.core.api.JPAODataSessionContextAccess;
import org.apache.olingo.jpa.processor.core.exception.ODataJPAQueryException;
import org.apache.olingo.jpa.processor.core.filter.JPAFilterComplier;
import org.apache.olingo.jpa.processor.core.filter.JPAFilterCrossComplier;
import org.apache.olingo.jpa.processor.core.filter.JPAOperationConverter;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriInfoResource;
import org.apache.olingo.server.api.uri.UriParameter;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceComplexProperty;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;
import org.apache.olingo.server.api.uri.UriResourceNavigation;
import org.apache.olingo.server.api.uri.UriResourcePartTyped;
import org.apache.olingo.server.api.uri.UriResourcePrimitiveProperty;
import org.apache.olingo.server.api.uri.queryoption.OrderByItem;
import org.apache.olingo.server.api.uri.queryoption.OrderByOption;
import org.apache.olingo.server.api.uri.queryoption.SelectOption;
import org.apache.olingo.server.api.uri.queryoption.SkipOption;
import org.apache.olingo.server.api.uri.queryoption.TopOption;
import org.apache.olingo.server.api.uri.queryoption.expression.Expression;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitException;
import org.apache.olingo.server.api.uri.queryoption.expression.Member;

public abstract class JPAAbstractEntityQuery extends JPAAbstractQuery {

	protected final UriInfoResource uriResource;
	protected final CriteriaQuery<Tuple> cq;
	protected final Root<?> root;
	protected final JPAFilterComplier filter;
	protected final JPAODataSessionContextAccess context;
	private final OData odata;
	private final Map<String, List<String>> requestHeaders;

	public JPAAbstractEntityQuery(final OData odata, final JPAODataSessionContextAccess context,
			final JPAEntityType jpaEntityType, final EntityManager em, final Map<String, List<String>> requestHeaders,
			final UriInfoResource uriResource) throws ODataApplicationException {

		super(context.getEdmProvider().getServiceDocument(), jpaEntityType, em, context.getDebugger());
		this.odata = odata;
		this.locale = determineLocale(requestHeaders);
		this.requestHeaders = requestHeaders;
		this.uriResource = uriResource;
		this.cq = cb.createTupleQuery();
		this.root = cq.from(jpaEntityType.getTypeClass());
		this.filter = new JPAFilterCrossComplier(odata, sd, em, jpaEntityType, new JPAOperationConverter(cb, context
				.getOperationConverter()), uriResource, this);
		this.context = context;
	}

	protected OData getOData() {
		return odata;
	}

	protected Map<String, List<String>> getRequestHeaders() {
		return requestHeaders;
	}

	@Override
	public AbstractQuery<?> getQuery() {
		return cq;
	}

	/**
	 * Applies the $skip and $top options of the OData request to the query. The values are defined as follows:
	 * <ul>
	 * <li> The $top system query option specifies a non-negative integer n that limits the number of items returned from
	 * a collection.
	 * <li> The $skip system query option specifies a non-negative integer n that excludes the first n items of the
	 * queried collection from the result.
	 * </ul>
	 * For details see:
	 * <a href=
	 * "http://docs.oasis-open.org/odata/odata/v4.0/errata02/os/complete/part1-protocol/odata-v4.0-errata02-os-part1-protocol-complete.html#_Toc406398306"
	 * >OData Version 4.0 Part 1 - 11.2.5.3 System Query Option $top</a>
	 *
	 * @throws ODataApplicationException
	 */
	protected void addTopSkip(final TypedQuery<Tuple> tq) throws ODataApplicationException {
		/*
		 * Where $top and $skip are used together, $skip MUST be applied before $top, regardless of the order in which they
		 * appear in the request.
		 * If no unique ordering is imposed through an $orderby query option, the service MUST impose a stable ordering
		 * across requests that include $skip.
		 *
		 * URL example: http://localhost:8080/BuPa/BuPa.svc/Organizations?$count=true&$skip=5
		 */

		final TopOption topOption = uriResource.getTopOption();
		if (topOption != null) {
			final int topNumber = topOption.getValue();
			if (topNumber >= 0) {
				tq.setMaxResults(topNumber);
			} else {
				throw new ODataJPAQueryException(ODataJPAQueryException.MessageKeys.QUERY_PREPARATION_INVALID_VALUE,
						HttpStatusCode.BAD_REQUEST, Integer.toString(topNumber), "$top");
			}
		}

		final SkipOption skipOption = uriResource.getSkipOption();
		if (skipOption != null) {
			final int skipNumber = skipOption.getValue();
			if (skipNumber >= 0) {
				tq.setFirstResult(skipNumber);
			} else {
				throw new ODataJPAQueryException(ODataJPAQueryException.MessageKeys.QUERY_PREPARATION_INVALID_VALUE,
						HttpStatusCode.BAD_REQUEST, Integer.toString(skipNumber), "$skip");
			}
		}
	}

	protected List<JPASelector> buildEntityPathList(final JPAEntityType jpaEntity) throws ODataApplicationException {

		try {
			return jpaEntity.getPathList();
		} catch (final ODataJPAModelException e) {
			throw new ODataJPAQueryException(e, HttpStatusCode.BAD_REQUEST);
		}
	}

	private List<JPASelector> buildPathValue(final JPAEntityType jpaEntity, final String select)
			throws ODataApplicationException {

		List<JPASelector> jpaPathList = new ArrayList<JPASelector>();
		String selectString;
		try {
			selectString = select.replace(Util.VALUE_RESOURCE, "");
			if (selectString.isEmpty()) {
				// Stream value
				jpaPathList.add(jpaEntity.getStreamAttributePath());
				jpaPathList.addAll(jpaEntity.getKeyPath());
			} else {
				// Property value
				selectString = selectString.substring(0, selectString.length() - 1);
				jpaPathList = buildPathList(jpaEntity, selectString);
			}
		} catch (final ODataJPAModelException e) {
			throw new ODataJPAQueryException(e, HttpStatusCode.BAD_REQUEST);
		}
		return jpaPathList;
	}

	private List<JPASelector> buildPathList(final JPAEntityType jpaEntity, final String select)
			throws ODataApplicationException {

		final String[] selectList = select.split(SELECT_ITEM_SEPERATOR); // OData separator for $select
		return buildPathList(jpaEntity, selectList);
	}

	private List<JPASelector> buildPathList(final JPAEntityType jpaEntity, final String[] selectList)
			throws ODataApplicationException {

		final List<JPASelector> jpaPathList = new ArrayList<JPASelector>();
		try {
			final List<? extends JPAAttribute> jpaKeyList = jpaEntity.getKeyAttributes();

			for (final String selectItem : selectList) {
				final JPASelector selectItemPath = jpaEntity.getPath(selectItem);
				if (selectItemPath.getLeaf().isComplex()) {
					// Complex Type
					final List<JPAAttributePath> c = jpaEntity.searchChildPath(selectItemPath);
					jpaPathList.addAll(c);
				} else {
					// Primitive Type
					jpaPathList.add(selectItemPath);
				}
				if (selectItemPath.getLeaf().isKey()) {
					jpaKeyList.remove(selectItemPath.getLeaf());
				}
			}
			Collections.sort(jpaPathList);
			for (final JPAAttribute key : jpaKeyList) {
				final JPASelector keyPath = jpaEntity.getPath(key.getExternalName());
				final int insertAt = Collections.binarySearch(jpaPathList, keyPath);
				if (insertAt < 0) {
					jpaPathList.add((insertAt * -1) - 1, keyPath);
				}
			}
		} catch (final ODataJPAModelException e) {
			throw new ODataJPAQueryException(e, HttpStatusCode.BAD_REQUEST);
		}
		return jpaPathList;
	}

	protected final List<JPASelector> buildSelectionPathList(final UriInfoResource uriResource)
			throws ODataApplicationException {
		List<JPASelector> jpaPathList = null;
		// TODO It is also possible to request all actions or functions available for each returned entity:
		// http://host/service/Products?$select=DemoService.*

		// Convert uri select options into a list of jpa attributes
		String selectionText = null;
		final List<UriResource> resources = uriResource.getUriResourceParts();

		selectionText = Util.determineProptertyNavigationPath(resources);
		// TODO Combine path selection and $select e.g. Organizations('4')/Address?$select=Country,Region
		if (selectionText == null || selectionText.isEmpty()) {
			final SelectOption select = uriResource.getSelectOption();
			if (select != null) {
				selectionText = select.getText();
			}
		}

		if (selectionText != null && selectionText.contains(Util.VALUE_RESOURCE)) {
			jpaPathList = buildPathValue(jpaEntityType, selectionText);
		} else if (selectionText != null && !selectionText.equals(SELECT_ALL) && !selectionText.isEmpty()) {
			jpaPathList = buildPathList(jpaEntityType, selectionText);
		} else {
			jpaPathList = buildEntityPathList(jpaEntityType);
		}
		// filter ignored columns here, because we may add later ignored columns to
		// select columns required to $expand
		for (int i = jpaPathList.size(); i > 0; i--) {
			final JPASelector selector = jpaPathList.get(i - 1);
			if (selector.getLeaf().ignore()) {
				jpaPathList.remove(i - 1);
			}
		}

		try {
			if (jpaEntityType.hasStream()) {
				final JPASelector mimeTypeAttribute = jpaEntityType.getContentTypeAttributePath();
				if (mimeTypeAttribute != null) {
					jpaPathList.add(mimeTypeAttribute);
				}
			}
		} catch (final ODataJPAModelException e1) {
			throw new ODataJPAQueryException(ODataJPAQueryException.MessageKeys.QUERY_PREPARATION_ERROR,
					HttpStatusCode.INTERNAL_SERVER_ERROR, e1);
		}

		// TODO select that fields only if $expand option is given
		// TODO use JPAExpandItemInfoFactory
		// Add also fields that are required for $expand (for the key building to merge
		// results from sub queries)
		final Map<JPAExpandItemWrapper, JPAAssociationPath> associationPathList = Util.determineAssoziations(sd,
				uriResource.getUriResourceParts(), uriResource.getExpandOption());
		if (!associationPathList.isEmpty()) {
			Collections.sort(jpaPathList);
			for (final Entry<JPAExpandItemWrapper, JPAAssociationPath> entry : associationPathList.entrySet()) {
				try {
					for (final JPAOnConditionItem joinItem : entry.getValue().getJoinConditions()) {
						final int insertIndex = Collections.binarySearch(jpaPathList, joinItem.getLeftPath());
						if (insertIndex < 0) {
							jpaPathList.add(/* Math.abs(insertIndex), */ joinItem.getLeftPath());
						}
					}
				} catch (final ODataJPAModelException e) {
					throw new ODataJPAQueryException(e, HttpStatusCode.BAD_REQUEST);
				}
			}
		}
		return jpaPathList;

	}

	/**
	 *
	 * @param orderByTarget
	 * @param queryRoot
	 * @return
	 * @throws ODataApplicationException
	 */
	protected Map<String, From<?, ?>> createFromClause(final List<JPAAssociationAttribute> orderByTarget)
			throws ODataApplicationException {
		final HashMap<String, From<?, ?>> joinTables = new HashMap<String, From<?, ?>>();
		// 1. Create root
		joinTables.put(jpaEntityType.getInternalName(), root);

		// 2. OrderBy navigation property
		for (final JPAAssociationAttribute orderBy : orderByTarget) {
			final Join<?, ?> join = root.join(orderBy.getInternalName(), JoinType.LEFT);
			// Take on condition from JPA metadata; no explicit on
			joinTables.put(orderBy.getInternalName(), join);
		}

		return joinTables;
	}

	/**
	 * If asc or desc is not specified, the service MUST order by the specified property in ascending order.
	 * See:
	 * <a
	 * href=
	 * "http://docs.oasis-open.org/odata/odata/v4.0/errata02/os/complete/part1-protocol/odata-v4.0-errata02-os-part1-protocol-complete.html#_Toc406398305"
	 * >OData Version 4.0 Part 1 - 11.2.5.2 System Query Option $orderby</a> <p>
	 *
	 * @throws ODataJPAModelException
	 *
	 */
	protected List<Order> createOrderByList(final Map<String, From<?, ?>> joinTables, final OrderByOption orderByOption)
			throws ODataApplicationException {
		// .../Organizations?$orderby=Address/Country --> one item, two resourcePaths
		// [...ComplexProperty,...PrimitiveProperty]
		// .../Organizations?$orderby=Roles/$count --> one item, two resourcePaths [...NavigationProperty,...Count]
		// .../Organizations?$orderby=Roles/$count desc,Address/Country asc -->two items
		//
		// SQL example to order by number of entities of the
		// SELECT t0."BusinessPartnerID" ,COUNT(t1."BusinessPartnerID")
		// FROM {oj "OLINGO"."org.apache.olingo.jpa::BusinessPartner" t0
		// LEFT OUTER JOIN "OLINGO"."org.apache.olingo.jpa::BusinessPartnerRole" t1
		// ON (t1."BusinessPartnerID" = t0."BusinessPartnerID")}
		// WHERE (t0."Type" = ?)
		// GROUP BY t0."BusinessPartnerID"
		// ORDER BY COUNT(t1."BusinessPartnerID") DESC

		// TODO Functions and orderBy: Part 1 - 11.5.3.1 Invoking a Function

		final List<Order> orders = new ArrayList<Order>();
		if (orderByOption != null) {
			for (final OrderByItem orderByItem : orderByOption.getOrders()) {
				final Expression expression = orderByItem.getExpression();
				if (expression instanceof Member) {
					final UriInfoResource resourcePath = ((Member) expression).getResourcePath();
					JPAStructuredType type = jpaEntityType;
					Path<?> p = joinTables.get(jpaEntityType.getInternalName());
					for (final UriResource uriResource : resourcePath.getUriResourceParts()) {
						if (uriResource instanceof UriResourcePrimitiveProperty) {
							final EdmProperty edmProperty = ((UriResourcePrimitiveProperty) uriResource).getProperty();
							try {
								final JPAAttribute attribute = type.getPath(edmProperty.getName()).getLeaf();
								p = p.get(attribute.getInternalName());
							} catch (final ODataJPAModelException e) {
								throw new ODataJPAQueryException(e, HttpStatusCode.BAD_REQUEST);
							}
							if (orderByItem.isDescending()) {
								orders.add(cb.desc(p));
							} else {
								orders.add(cb.asc(p));
							}
						} else if (uriResource instanceof UriResourceComplexProperty) {
							final EdmProperty edmProperty = ((UriResourceComplexProperty) uriResource).getProperty();
							try {
								final JPAAttribute attribute = type.getPath(edmProperty.getName()).getLeaf();
								p = p.get(attribute.getInternalName());
								type = attribute.getStructuredType();
							} catch (final ODataJPAModelException e) {
								throw new ODataJPAQueryException(e, HttpStatusCode.BAD_REQUEST);
							}
						} else if (uriResource instanceof UriResourceNavigation) {
							final EdmNavigationProperty edmNaviProperty = ((UriResourceNavigation) uriResource).getProperty();
							From<?, ?> join;
							try {
								join = joinTables
										.get(jpaEntityType.getAssociationPath(edmNaviProperty.getName()).getLeaf()
												.getInternalName());
							} catch (final ODataJPAModelException e) {
								throw new ODataJPAQueryException(e, HttpStatusCode.BAD_REQUEST);
							}
							if (orderByItem.isDescending()) {
								orders.add(cb.desc(cb.count(join)));
							} else {
								orders.add(cb.asc(cb.count(join)));
							}
						} // else if (uriResource instanceof UriResourceCount) {}
					}
				}
			}
		}
		return orders;
	}

	/**
	 * The value of the $select query option is a comma-separated list of <b>properties</b>, qualified action names,
	 * qualified function names, the <b>star operator (*)</b>, or the star operator prefixed with the namespace or alias
	 * of the schema in order to specify all operations defined in the schema. See:
	 * <a
	 * href=
	 * "http://docs.oasis-open.org/odata/odata/v4.0/errata02/os/complete/part1-protocol/odata-v4.0-errata02-os-part1-protocol-complete.html#_Toc406398297"
	 * >OData Version 4.0 Part 1 - 11.2.4.1 System Query Option $select</a> <p>
	 * See also:
	 * <a
	 * href=
	 * "http://docs.oasis-open.org/odata/odata/v4.0/errata02/os/complete/part2-url-conventions/odata-v4.0-errata02-os-part2-url-conventions-complete.html#_Toc406398163"
	 * >OData Version 4.0 Part 2 - 5.1.3 System Query Option $select</a>
	 *
	 * @param select
	 * @return
	 * @throws ODataApplicationException
	 */
	protected List<Selection<?>> createSelectClause(final List<JPASelector> jpaPathList)
			throws ODataApplicationException {

		final List<Selection<?>> selections = new LinkedList<Selection<?>>();

		// Build select clause
		for (final JPASelector jpaPath : jpaPathList) {
			if (JPAAssociationPath.class.isInstance(jpaPath)) {
				final JPAAssociationPath asso = ((JPAAssociationPath) jpaPath);
				LOG.log(Level.SEVERE,
						"Query includes a association (navigation join via '"
								+ asso.getSourceType().getExternalName() + "#"
								+ jpaPath.getAlias()
								+ "'), but without mapped JPA attributes usable for JOIN. That is not supported! An $expand will not work! Map the column as attribute to be usable for OData.");
				continue;
			}
			final Path<?> p = convertToCriteriaPath(jpaPath);
			if (p == null) {
				continue;
			}
			p.alias(jpaPath.getAlias());
			selections.add(p);
		}

		return selections;
	}

	protected javax.persistence.criteria.Expression<Boolean> createWhereFromKeyPredicates()
			throws ODataApplicationException {
		javax.persistence.criteria.Expression<Boolean> whereCondition = null;

		final List<UriResource> resources = uriResource.getUriResourceParts();
		UriResource resourceItem = null;
		// Given key: Organizations('1')
		if (resources != null) {
			for (int i = resources.size() - 1; i >= 0; i--) {
				resourceItem = resources.get(i);
				if (resourceItem instanceof UriResourceEntitySet || resourceItem instanceof UriResourceNavigation) {
					break;
				}
			}
			final List<UriParameter> keyPredicates = determineKeyPredicates(resourceItem);
			whereCondition = createWhereByKey(root, whereCondition, keyPredicates);
		}
		return whereCondition;
	}

	protected javax.persistence.criteria.Expression<Boolean> createWhere() throws ODataApplicationException {

		javax.persistence.criteria.Expression<Boolean> whereCondition = createWhereFromKeyPredicates();
		final javax.persistence.criteria.Expression<Boolean> existsSubQuery = buildNavigationSubQueries(root);
		whereCondition = addWhereClause(whereCondition, existsSubQuery);

		// http://docs.oasis-open.org/odata/odata/v4.0/errata02/os/complete/part1-protocol/odata-v4.0-errata02-os-part1-protocol-complete.html#_Toc406398301
		// http://docs.oasis-open.org/odata/odata/v4.0/errata02/os/complete/part2-url-conventions/odata-v4.0-errata02-os-part2-url-conventions-complete.html#_Toc406398094
		// https://tools.oasis-open.org/version-control/browse/wsvn/odata/trunk/spec/ABNF/odata-abnf-construction-rules.txt
		try {
			whereCondition = addWhereClause(whereCondition, filter.compile());
		} catch (final ExpressionVisitException e) {
			throw new ODataJPAQueryException(ODataJPAQueryException.MessageKeys.QUERY_PREPARATION_FILTER_ERROR,
					HttpStatusCode.BAD_REQUEST, e);
		}

		if (uriResource.getSearchOption() != null && uriResource.getSearchOption().getSearchExpression() != null) {
			whereCondition = addWhereClause(whereCondition,
					context.getDatabaseProcessor().createSearchWhereClause(cb, this.cq, root, jpaEntityType, uriResource
							.getSearchOption()));
		}

		return whereCondition;
	}

	protected JPAAssociationPath determineAssoziation(final UriResourcePartTyped naviStart,
			final StringBuffer associationName)
					throws ODataApplicationException {

		JPAEntityType naviStartType;
		try {
			if (naviStart instanceof UriResourceEntitySet) {
				naviStartType = sd.getEntityType(((UriResourceEntitySet) naviStart).getType());
			} else {
				naviStartType = sd.getEntityType(((UriResourceNavigation) naviStart).getProperty().getType());
			}
			return naviStartType.getAssociationPath(associationName.toString());
		} catch (final ODataJPAModelException e) {
			throw new ODataJPAQueryException(e, HttpStatusCode.BAD_REQUEST);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public Root<?> getRoot() {
		return root;
	}

	protected final Path<?> convertToCriteriaPath(final JPASelector jpaPath) {
		Path<?> p = root;
		for (final JPAAttribute jpaPathElement : jpaPath.getPathElements()) {
			if (jpaPathElement.isCollection()) {
				p = From.class.cast(p).join(jpaPathElement.getInternalName(), JoinType.LEFT);
			} else {
				p = p.get(jpaPathElement.getInternalName());
			}
		}
		return p;
	}

	boolean hasNavigation(final List<UriResource> uriResourceParts) {
		if (uriResourceParts != null) {
			for (int i = uriResourceParts.size() - 1; i >= 0; i--) {
				if (uriResourceParts.get(i) instanceof UriResourceNavigation) {
					return true;
				}
			}
		}
		return false;
	}

	private javax.persistence.criteria.Expression<Boolean> addWhereClause(
			javax.persistence.criteria.Expression<Boolean> whereCondition,
			final javax.persistence.criteria.Expression<Boolean> additioanlExpression) {

		if (additioanlExpression != null) {
			if (whereCondition == null) {
				whereCondition = additioanlExpression;
			} else {
				whereCondition = cb.and(whereCondition, additioanlExpression);
			}
		}
		return whereCondition;
	}

	/**
	 * Generate sub-queries in order to select the target of a navigation to a different entity<p>
	 * In case of multiple navigation steps a inner navigation has a dependency in both directions, to the upper and to
	 * the lower query:<p>
	 * <code>SELECT * FROM upper WHERE EXISTS( <p>
	 * SELECT ... FROM inner WHERE upper = inner<p>
	 * AND EXISTS( SELECT ... FROM lower<p>
	 * WHERE inner = lower))</code><p>
	 * This is solved by a three steps approach
	 */
	private javax.persistence.criteria.Expression<Boolean> buildNavigationSubQueries(final Root<?> root)
			throws ODataApplicationException {

		final List<UriResource> resourceParts = uriResource.getUriResourceParts();

		// No navigation
		if (!hasNavigation(resourceParts)) {
			return null;
		}

		// 1. Determine all relevant associations
		final List<JPANavigationProptertyInfo> naviPathList = Util.determineAssoziations(sd, resourceParts);
		JPAAbstractQuery parent = this;
		final List<JPANavigationQuery> queryList = new ArrayList<JPANavigationQuery>();

		// 2. Create the queries and roots
		for (final JPANavigationProptertyInfo naviInfo : naviPathList) {
			queryList.add(new JPANavigationQuery(sd, naviInfo.getUriResiource(), parent, em, naviInfo.getAssociationPath()));
			parent = queryList.get(queryList.size() - 1);
		}
		// 3. Create select statements
		Subquery<?> childQuery = null;
		for (int i = queryList.size() - 1; i >= 0; i--) {
			childQuery = queryList.get(i).getSubQueryExists(childQuery);
		}

		return cb.exists(childQuery);
	}

	@Override
	protected Locale getLocale() {
		return locale;
	}

	@Override
	JPAODataSessionContextAccess getContext() {
		return context;
	}
}
