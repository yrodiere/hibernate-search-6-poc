/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.search.dsl.query.spi;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import org.hibernate.search.v6poc.engine.spi.SessionContext;
import org.hibernate.search.v6poc.search.DocumentReference;
import org.hibernate.search.v6poc.search.ObjectLoader;
import org.hibernate.search.v6poc.search.ProjectionConstants;
import org.hibernate.search.v6poc.search.SearchQuery;
import org.hibernate.search.v6poc.search.dsl.query.SearchQueryResultDefinitionContext;
import org.hibernate.search.v6poc.search.dsl.query.SearchQueryWrappingDefinitionResultContext;
import org.hibernate.search.v6poc.search.dsl.query.impl.SearchQueryWrappingDefinitionResultContextImpl;
import org.hibernate.search.v6poc.search.dsl.spi.SearchTargetContext;
import org.hibernate.search.v6poc.search.query.impl.ObjectHitAggregator;
import org.hibernate.search.v6poc.search.query.impl.ProjectionHitAggregator;
import org.hibernate.search.v6poc.search.query.impl.ReferenceHitAggregator;
import org.hibernate.search.v6poc.search.query.spi.DocumentReferenceHitCollector;
import org.hibernate.search.v6poc.search.query.spi.HitAggregator;
import org.hibernate.search.v6poc.search.query.spi.LoadingHitCollector;
import org.hibernate.search.v6poc.search.query.spi.ProjectionHitCollector;
import org.hibernate.search.v6poc.search.query.spi.SearchQueryBuilder;

public final class SearchQueryResultDefinitionContextImpl<R, O, C> implements SearchQueryResultDefinitionContext<R, O> {

	private final SearchTargetContext<C> targetContext;

	private final SessionContext sessionContext;

	private final Function<DocumentReference, R> documentReferenceTransformer;

	private final ObjectLoader<R, O> objectLoader;

	public SearchQueryResultDefinitionContextImpl(SearchTargetContext<C> targetContext,
			SessionContext sessionContext,
			Function<DocumentReference, R> documentReferenceTransformer,
			ObjectLoader<R, O> objectLoader) {
		this.targetContext = targetContext;
		this.sessionContext = sessionContext;
		this.documentReferenceTransformer = documentReferenceTransformer;
		this.objectLoader = objectLoader;
	}

	@Override
	public SearchQueryWrappingDefinitionResultContext<SearchQuery<O>> asObjects() {
		HitAggregator<LoadingHitCollector, List<O>> hitAggregator =
				new ObjectHitAggregator<>( documentReferenceTransformer, objectLoader );
		SearchQueryBuilder<O, C> builder = targetContext.getSearchQueryFactory()
				.asObjects( sessionContext, hitAggregator );
		return new SearchQueryWrappingDefinitionResultContextImpl<>( targetContext, builder, Function.identity() );
	}

	@Override
	public <T> SearchQueryWrappingDefinitionResultContext<SearchQuery<T>> asReferences(Function<R, T> hitTransformer) {
		HitAggregator<DocumentReferenceHitCollector, List<T>> hitAggregator =
				new ReferenceHitAggregator<>( hitTransformer.compose( documentReferenceTransformer ) );
		SearchQueryBuilder<T, C> builder = targetContext.getSearchQueryFactory()
				.asReferences( sessionContext, hitAggregator );
		return new SearchQueryWrappingDefinitionResultContextImpl<>( targetContext, builder, Function.identity() );
	}

	@Override
	public <T> SearchQueryWrappingDefinitionResultContext<SearchQuery<T>> asProjections(
			Function<List<?>, T> hitTransformer, String... projections) {
		int expectedLoadPerHit = (int) Arrays.stream( projections )
				.filter( Predicate.isEqual( ProjectionConstants.OBJECT ) )
				.count();
		HitAggregator<ProjectionHitCollector, List<T>> hitAggregator =
				new ProjectionHitAggregator<>( documentReferenceTransformer, objectLoader, hitTransformer,
						projections.length, expectedLoadPerHit );

		SearchQueryBuilder<T, C> builder = targetContext.getSearchQueryFactory()
				.asProjections( sessionContext, hitAggregator, projections );
		return new SearchQueryWrappingDefinitionResultContextImpl<>( targetContext, builder, Function.identity() );
	}
}
