package org.apache.olingo.jpa.processor.core.filter;

import org.apache.olingo.server.api.uri.queryoption.expression.Member;

class JPALambdaAnyOperation extends JPALambdaOperation {

	public JPALambdaAnyOperation(final JPAFilterComplierAccess jpaComplier, final Member member) {
		super(jpaComplier, member);
	}

}
