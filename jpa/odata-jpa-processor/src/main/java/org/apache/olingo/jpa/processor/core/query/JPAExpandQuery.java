package org.apache.olingo.jpa.processor.core.query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import javax.persistence.EntityManager;
import javax.persistence.Tuple;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Subquery;

import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationAttribute;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAAttribute;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAOnConditionItem;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPASelector;
import org.apache.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import org.apache.olingo.jpa.processor.core.api.JPAODataSessionContextAccess;
import org.apache.olingo.jpa.processor.core.exception.ODataJPAQueryException;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriInfoResource;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitException;

/**
 * A query to retrieve the expand entities.<p> According to
 * <a href=
 * "http://docs.oasis-open.org/odata/odata/v4.0/errata02/os/complete/part2-url-conventions/odata-v4.0-errata02-os-part2-url-conventions-complete.html#_Toc406398162"
 * >OData Version 4.0 Part 2 - 5.1.2 System Query Option $expand</a> the following query options are allowed:
 * <ul>
 * <li>expandCountOption = <b>filter</b>/ search<p>
 * <li>expandRefOption = expandCountOption/ <b>orderby</b> / <b>skip</b> / <b>top</b> / inlinecount
 * <li>expandOption = expandRefOption/ <b>select</b>/ <b>expand</b> / levels <p>
 * </ul>
 * As of now only the bold once are supported
 * <p>
 * @author Oliver Grande
 *
 */
class JPAExpandQuery extends JPAAbstractEntityQuery {
	private final JPAAssociationPath assoziation;
	private final JPAExpandItemInfo item;

	public JPAExpandQuery(final OData odata, final JPAODataSessionContextAccess context, final EntityManager em,
			final UriInfoResource uriInfo, final JPAAssociationPath assoziation, final JPAEntityType entityType,
			final Map<String, List<String>> requestHeaders) throws ODataApplicationException {
		super(odata, context, entityType, em, requestHeaders, uriInfo);
		this.assoziation = assoziation;
		this.item = null;
	}

	public JPAExpandQuery(final OData odata, final JPAODataSessionContextAccess context, final EntityManager em,
			final JPAExpandItemInfo item, final Map<String, List<String>> requestHeaders) throws ODataApplicationException {

		super(odata, context, item.getEntityType(), em, requestHeaders, item.getUriInfo());
		this.assoziation = item.getExpandAssociation();
		this.item = item;
	}

	/**
	 * Process a expand query, which contains a $skip and/or a $top option.<p>
	 * This is a tricky problem, as it can not be done easily with SQL. It could be that a database offers special
	 * solutions.
	 * There is an worth reading blog regards this topic:
	 * <a href="http://www.xaprb.com/blog/2006/12/07/how-to-select-the-firstleastmax-row-per-group-in-sql/">How to select
	 * the first/least/max row per group in SQL</a>
	 * @return query result
	 * @throws ODataApplicationException
	 */
	public JPAQueryResult execute() throws ODataApplicationException {
		long skip = 0;
		long top = Long.MAX_VALUE;
		final TypedQuery<Tuple> tupleQuery = createTupleQuery();
		// Simplest solution for the problem. Read all and throw away, what is not requested
		final List<Tuple> intermediateResult = tupleQuery.getResultList();
		if (uriResource.getSkipOption() != null) {
			skip = uriResource.getSkipOption().getValue();
		}
		if (uriResource.getTopOption() != null) {
			top = uriResource.getTopOption().getValue();
		}

		final Map<String, List<Tuple>> result = convertResult(intermediateResult, assoziation, skip, top);
		return new JPAQueryResult(result, count(), jpaEntityType);
	}

	private TypedQuery<Tuple> createTupleQuery() throws ODataApplicationException {
		// TODO merge with implementation in JPAExpandQuery#execute()

		// FIXME: $expand must handle entities using @EntityCollection
		final List<JPASelector> selectionPath = buildSelectionPathList(this.uriResource);
		final Map<String, From<?, ?>> joinTables = createFromClause(new ArrayList<JPAAssociationAttribute>());

		cq.multiselect(createSelectClause(selectionPath));
		cq.where(createWhere());

		final List<Order> orderBy = createOrderByJoinCondition(assoziation);
		orderBy.addAll(createOrderByList(joinTables, uriResource.getOrderByOption()));
		cq.orderBy(orderBy);
		// TODO group by also at $expand
		final TypedQuery<Tuple> query = em.createQuery(cq);

		return query;
	}

	private Long count() {
		// TODO Count and Expand -> Olingo
		return null;
	}

	Map<String, List<Tuple>> convertResult(final List<Tuple> intermediateResult, final JPAAssociationPath a,
			final long skip, final long top)
					throws ODataApplicationException {
		String joinKey = "";
		long skiped = 0;
		long taken = 0;

		List<Tuple> subResult = null;
		final Map<String, List<Tuple>> convertedResult = new HashMap<String, List<Tuple>>();
		for (final Tuple row : intermediateResult) {
			String actuallKey;
			try {
				actuallKey = buildConcatenatedKey(row, a.getJoinConditions());
			} catch (final ODataJPAModelException e) {
				throw new ODataJPAQueryException(e, HttpStatusCode.BAD_REQUEST);
			} catch (final IllegalArgumentException e) {
				LOG.log(Level.SEVERE,
						"Problem converting database result for entity type " + item.getEntityType().getInternalName(),
						e);
				throw new ODataJPAQueryException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
			}

			if (!actuallKey.equals(joinKey)) {
				subResult = new ArrayList<Tuple>();
				convertedResult.put(actuallKey, subResult);
				joinKey = actuallKey;
				skiped = taken = 0;
			}
			if (skiped >= skip && taken < top) {
				taken += 1;
				subResult.add(row);
			} else {
				skiped += 1;
			}
		}
		return convertedResult;
	}

	private String buildConcatenatedKey(final Tuple row, final List<JPAOnConditionItem> joinColumns) {
		final StringBuffer buffer = new StringBuffer();
		for (final JPAOnConditionItem item : joinColumns) {
			buffer.append(JPASelector.PATH_SEPERATOR);
			buffer.append(row.get(item.getRightPath().getAlias()));
		}
		buffer.deleteCharAt(0);
		return buffer.toString();
	}

	private List<Order> createOrderByJoinCondition(final JPAAssociationPath a) throws ODataApplicationException {
		final List<Order> orders = new ArrayList<Order>();

		try {
			Path<?> path;
			for (final JPAOnConditionItem j : a.getJoinConditions()) {
				path = null;
				for (final JPAAttribute attr : j.getRightPath().getPathElements()) {
					if (path == null) {
						path = root.get(attr.getInternalName());
					} else {
						path = path.get(attr.getInternalName());
					}
				}
				orders.add(cb.asc(path));
				// orders.add(cb.asc(root.get(j.getRightPath().getLeaf().getInternalName())));
			}
		} catch (final ODataJPAModelException e) {
			throw new ODataJPAQueryException(e, HttpStatusCode.BAD_REQUEST);
		}
		return orders;
	}

	@Override
	protected Expression<Boolean> createWhere() throws ODataApplicationException {

		Expression<Boolean> whereCondition = null;
		try {
			whereCondition = filter.compile();
		} catch (final ExpressionVisitException e) {
			throw new ODataJPAQueryException(ODataJPAQueryException.MessageKeys.QUERY_PREPARATION_FILTER_ERROR,
					HttpStatusCode.BAD_REQUEST, e);
		}

		if (whereCondition == null) {
			whereCondition = cb.exists(buildSubQueries());// parentQuery.asSubQuery(this, assoziation));
		} else {
			whereCondition = cb.and(whereCondition, cb.exists(buildSubQueries()));
		}

		return whereCondition;
	}

	private Subquery<?> buildSubQueries() throws ODataApplicationException {
		Subquery<?> childQuery = null;

		final List<UriResource> resourceParts = uriResource.getUriResourceParts();

		// 1. Determine all relevant associations
		final List<JPANavigationProptertyInfo> expandPathList = Util.determineAssoziations(sd, resourceParts);
		expandPathList.addAll(item.getHops());

		// 2. Create the queries and roots
		JPAAbstractQuery parent = this;
		final List<JPANavigationQuery> queryList = new ArrayList<JPANavigationQuery>();

		for (final JPANavigationProptertyInfo naviInfo : expandPathList) {
			queryList.add(new JPANavigationQuery(sd, naviInfo.getUriResiource(), parent, em, naviInfo.getAssociationPath()));
			parent = queryList.get(queryList.size() - 1);
		}
		// 3. Create select statements
		for (int i = queryList.size() - 1; i >= 0; i--) {
			childQuery = queryList.get(i).getSubQueryExists(childQuery);
		}
		return childQuery;
	}
}
