/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.util.impl.integrationtest.common.stub.backend.index.impl;

import java.util.List;

import org.hibernate.search.v6poc.engine.spi.SessionContext;
import org.hibernate.search.v6poc.util.impl.integrationtest.common.stub.backend.search.StubQueryElementCollector;
import org.hibernate.search.v6poc.util.impl.integrationtest.common.stub.backend.search.StubSearchQueryBuilder;
import org.hibernate.search.v6poc.util.impl.integrationtest.common.stub.backend.search.StubSearchWork;
import org.hibernate.search.v6poc.search.query.spi.DocumentReferenceHitCollector;
import org.hibernate.search.v6poc.search.query.spi.HitAggregator;
import org.hibernate.search.v6poc.search.query.spi.LoadingHitCollector;
import org.hibernate.search.v6poc.search.query.spi.ProjectionHitCollector;
import org.hibernate.search.v6poc.search.query.spi.SearchQueryBuilder;
import org.hibernate.search.v6poc.search.query.spi.SearchQueryFactory;

class StubSearchQueryFactory implements SearchQueryFactory<StubQueryElementCollector> {
	private final StubBackend backend;
	private final List<String> indexNames;

	StubSearchQueryFactory(StubBackend backend, List<String> indexNames) {
		this.backend = backend;
		this.indexNames = indexNames;
	}

	@Override
	public <O> SearchQueryBuilder<O, StubQueryElementCollector> asObjects(SessionContext sessionContext,
			HitAggregator<LoadingHitCollector, List<O>> hitAggregator) {
		return new StubSearchQueryBuilder<>( backend, indexNames, StubSearchWork.ResultType.OBJECTS, hitAggregator );
	}

	@Override
	public <T> SearchQueryBuilder<T, StubQueryElementCollector> asReferences(SessionContext sessionContext,
			HitAggregator<DocumentReferenceHitCollector, List<T>> hitAggregator) {
		return new StubSearchQueryBuilder<>( backend, indexNames, StubSearchWork.ResultType.REFERENCES, hitAggregator );
	}

	@Override
	public <T> SearchQueryBuilder<T, StubQueryElementCollector> asProjections(SessionContext sessionContext,
			HitAggregator<ProjectionHitCollector, List<T>> hitAggregator, String... projections) {
		return new StubSearchQueryBuilder<>( backend, indexNames, StubSearchWork.ResultType.PROJECTIONS, hitAggregator );
	}
}
