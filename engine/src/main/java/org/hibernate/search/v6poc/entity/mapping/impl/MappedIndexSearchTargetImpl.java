/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.entity.mapping.impl;

import java.util.function.Function;

import org.hibernate.search.v6poc.engine.spi.SessionContext;
import org.hibernate.search.v6poc.entity.mapping.spi.MappedIndexSearchTarget;
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

public final class MappedIndexSearchTargetImpl implements MappedIndexSearchTarget {

	private final SearchTargetContext<?> searchTargetContext;

	MappedIndexSearchTargetImpl(SearchTargetContext<?> searchTargetContext) {
		this.searchTargetContext = searchTargetContext;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "["
				+ "context=" + searchTargetContext
				+ "]";
	}

	@Override
	public SearchQueryResultDefinitionContext<DocumentReference, DocumentReference> query(SessionContext context) {
		return query( context, Function.identity(), ObjectLoader.identity() );
	}

	@Override
	public <R, O> SearchQueryResultDefinitionContext<R, O> query(SessionContext sessionContext,
			Function<DocumentReference, R> documentReferenceTransformer,
			ObjectLoader<R, O> objectLoader) {
		return new SearchQueryResultDefinitionContextImpl<>(
				searchTargetContext, sessionContext,
				documentReferenceTransformer, objectLoader
		);
	}

	@Override
	public SearchPredicateContainerContext<SearchPredicate> predicate() {
		return new SearchTargetPredicateRootContext<>( searchTargetContext.getSearchPredicateFactory() );
	}

	@Override
	public SearchSortContainerContext<SearchSort> sort() {
		return new SearchTargetSortRootContext<>( searchTargetContext.getSearchSortFactory() );
	}
}
