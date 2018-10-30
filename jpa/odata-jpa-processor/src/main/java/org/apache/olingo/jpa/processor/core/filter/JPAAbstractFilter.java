package org.apache.olingo.jpa.processor.core.filter;

import java.util.Collections;
import java.util.List;

import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPASelector;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriInfoResource;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitException;
import org.apache.olingo.server.api.uri.queryoption.expression.VisitableExpression;

public abstract class JPAAbstractFilter implements JPAFilterComplier, JPAFilterComplierAccess {
	final JPAEntityType jpaEntityType;
	final VisitableExpression expression;

	public JPAAbstractFilter(final JPAEntityType jpaEntityType, final VisitableExpression expression) {
		super();
		this.jpaEntityType = jpaEntityType;
		this.expression = expression;
	}

	public JPAAbstractFilter(final JPAEntityType jpaEntityType, final UriInfoResource uriResource) {
		super();
		this.jpaEntityType = jpaEntityType;
		if (uriResource != null && uriResource.getFilterOption() != null) {
			this.expression = uriResource.getFilterOption().getExpression();
		} else {
			this.expression = null;
		}
	}

	@Override
	public List<JPASelector> getMember() {
		final JPAMemberVisitor visitor = new JPAMemberVisitor(jpaEntityType);
		if (expression != null) {
			try {
				expression.accept(visitor);
			} catch (final ExpressionVisitException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (final ODataApplicationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return Collections.unmodifiableList(visitor.get());
		} else {
			return Collections.emptyList();
		}
	}

}