/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.integrationtest.util.common.stub.backend.index.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import org.hibernate.search.v6poc.backend.index.spi.IndexSearchTarget;
import org.hibernate.search.v6poc.backend.index.spi.IndexSearchTargetBuilder;
import org.hibernate.search.v6poc.engine.spi.SessionContext;
import org.hibernate.search.v6poc.integrationtest.util.common.stub.backend.search.StubQueryElementCollector;
import org.hibernate.search.v6poc.integrationtest.util.common.stub.backend.search.predicate.impl.StubSearchPredicateFactory;
import org.hibernate.search.v6poc.integrationtest.util.common.stub.backend.search.sort.StubSearchSortFactory;
import org.hibernate.search.v6poc.search.DocumentReference;
import org.hibernate.search.v6poc.search.ObjectLoader;
import org.hibernate.search.v6poc.search.SearchPredicate;
import org.hibernate.search.v6poc.search.SearchSort;
import org.hibernate.search.v6poc.search.dsl.predicate.SearchPredicateContainerContext;
import org.hibernate.search.v6poc.search.dsl.predicate.spi.SearchTargetPredicateRootContext;
import org.hibernate.search.v6poc.search.dsl.query.SearchQueryResultDefinitionContext;
import org.hibernate.search.v6poc.search.dsl.query.spi.SearchQueryResultDefinitionContextImpl;
import org.hibernate.search.v6poc.search.dsl.sort.SearchSortContainerContext;
import org.hibernate.search.v6poc.search.dsl.sort.spi.SearchTargetSortRootContext;
import org.hibernate.search.v6poc.search.dsl.spi.SearchTargetContext;
import org.hibernate.search.v6poc.search.predicate.spi.SearchPredicateFactory;
import org.hibernate.search.v6poc.search.query.spi.SearchQueryFactory;
import org.hibernate.search.v6poc.search.sort.spi.SearchSortFactory;

class StubIndexSearchTarget implements IndexSearchTarget, SearchTargetContext<StubQueryElementCollector> {
	private final StubSearchPredicateFactory predicateFactory;
	private final StubSearchSortFactory sortFactory;
	private final StubSearchQueryFactory queryFactory;

	StubIndexSearchTarget(Builder builder) {
		this.predicateFactory = new StubSearchPredicateFactory();
		this.sortFactory = new StubSearchSortFactory();
		List<String> immutableIndexNames = Collections.unmodifiableList( new ArrayList<>( builder.indexNames ) );
		this.queryFactory = new StubSearchQueryFactory( builder.backend, immutableIndexNames );
	}

	@Override
	public <R, O> SearchQueryResultDefinitionContext<R, O> query(SessionContext context,
			Function<DocumentReference, R> documentReferenceTransformer, ObjectLoader<R, O> objectLoader) {
		return new SearchQueryResultDefinitionContextImpl<>( this, context,
				documentReferenceTransformer, objectLoader );
	}

	@Override
	public SearchPredicateContainerContext<SearchPredicate> predicate() {
		return new SearchTargetPredicateRootContext<>( predicateFactory );
	}

	@Override
	public SearchSortContainerContext<SearchSort> sort() {
		return new SearchTargetSortRootContext<>( sortFactory );
	}

	@Override
	public SearchPredicateFactory<? super StubQueryElementCollector> getSearchPredicateFactory() {
		return predicateFactory;
	}

	@Override
	public SearchSortFactory<? super StubQueryElementCollector> getSearchSortFactory() {
		return sortFactory;
	}

	@Override
	public SearchQueryFactory<StubQueryElementCollector> getSearchQueryFactory() {
		return queryFactory;
	}

	static class Builder implements IndexSearchTargetBuilder {

		private final StubBackend backend;
		private final List<String> indexNames = new ArrayList<>();

		Builder(StubBackend backend, String indexName) {
			this.backend = backend;
			this.indexNames.add( indexName );
		}

		void add(StubBackend backend, String indexName) {
			if ( !this.backend.equals( backend ) ) {
				throw new IllegalStateException( "Attempt to run a search query across two distinct backends; this is not possible." );
			}
			indexNames.add( indexName );
		}

		@Override
		public IndexSearchTarget build() {
			return new StubIndexSearchTarget( this );
		}
	}
}
