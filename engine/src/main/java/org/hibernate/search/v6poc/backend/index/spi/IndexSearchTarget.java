/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.backend.index.spi;

import java.util.function.Function;

import org.hibernate.search.v6poc.engine.spi.SessionContext;
import org.hibernate.search.v6poc.search.DocumentReference;
import org.hibernate.search.v6poc.search.ObjectLoader;
import org.hibernate.search.v6poc.search.SearchPredicate;
import org.hibernate.search.v6poc.search.SearchSort;
import org.hibernate.search.v6poc.search.dsl.predicate.SearchPredicateContainerContext;
import org.hibernate.search.v6poc.search.dsl.predicate.impl.SearchTargetPredicateRootContext;
import org.hibernate.search.v6poc.search.dsl.query.SearchQueryResultDefinitionContext;
import org.hibernate.search.v6poc.search.dsl.query.spi.SearchQueryResultDefinitionContextImpl;
import org.hibernate.search.v6poc.search.dsl.sort.SearchSortContainerContext;
import org.hibernate.search.v6poc.search.dsl.sort.impl.SearchTargetSortRootContext;
import org.hibernate.search.v6poc.search.dsl.spi.SearchTargetContext;

public final class IndexSearchTarget {

	private final SearchTargetContext<?> searchTargetContext;

	public IndexSearchTarget(SearchTargetContext<?> searchTargetContext) {
		this.searchTargetContext = searchTargetContext;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "["
				+ "context=" + searchTargetContext
				+ "]";
	}

	public SearchQueryResultDefinitionContext<DocumentReference, DocumentReference> query(SessionContext context) {
		return query( context, Function.identity(), ObjectLoader.identity() );
	}

	public <R, O> SearchQueryResultDefinitionContext<R, O> query(SessionContext sessionContext,
			Function<DocumentReference, R> documentReferenceTransformer,
			ObjectLoader<R, O> objectLoader) {
		return new SearchQueryResultDefinitionContextImpl<>(
				searchTargetContext, sessionContext,
				documentReferenceTransformer, objectLoader
		);
	}

	public SearchPredicateContainerContext<SearchPredicate> predicate() {
		return new SearchTargetPredicateRootContext<>( searchTargetContext.getSearchPredicateFactory() );
	}

	public SearchSortContainerContext<SearchSort> sort() {
		return new SearchTargetSortRootContext<>( searchTargetContext.getSearchSortFactory() );
	}

}
