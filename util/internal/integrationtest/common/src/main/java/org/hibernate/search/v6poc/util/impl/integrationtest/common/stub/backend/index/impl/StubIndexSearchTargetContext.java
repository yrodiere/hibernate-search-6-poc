/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.util.impl.integrationtest.common.stub.backend.index.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.hibernate.search.v6poc.search.dsl.spi.SearchTargetContext;
import org.hibernate.search.v6poc.util.impl.integrationtest.common.stub.backend.search.StubQueryElementCollector;
import org.hibernate.search.v6poc.util.impl.integrationtest.common.stub.backend.search.predicate.impl.StubSearchPredicateFactory;
import org.hibernate.search.v6poc.util.impl.integrationtest.common.stub.backend.search.sort.StubSearchSortFactory;

class StubIndexSearchTargetContext implements SearchTargetContext<StubQueryElementCollector> {
	private final StubSearchPredicateFactory predicateFactory;
	private final StubSearchSortFactory sortFactory;
	private final StubSearchQueryFactory queryFactory;

	StubIndexSearchTargetContext(StubBackend backend, List<String> indexNames) {
		this.predicateFactory = new StubSearchPredicateFactory();
		this.sortFactory = new StubSearchSortFactory();
		List<String> immutableIndexNames = Collections.unmodifiableList( new ArrayList<>( indexNames ) );
		this.queryFactory = new StubSearchQueryFactory( backend, immutableIndexNames );
	}

	@Override
	public StubSearchPredicateFactory getSearchPredicateFactory() {
		return predicateFactory;
	}

	@Override
	public StubSearchSortFactory getSearchSortFactory() {
		return sortFactory;
	}

	@Override
	public StubSearchQueryFactory getSearchQueryFactory() {
		return queryFactory;
	}

}
