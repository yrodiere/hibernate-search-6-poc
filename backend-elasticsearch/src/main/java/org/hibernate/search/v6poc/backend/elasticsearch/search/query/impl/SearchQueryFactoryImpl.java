/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.backend.elasticsearch.search.query.impl;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.hibernate.search.v6poc.backend.elasticsearch.document.model.impl.ElasticsearchIndexModel;
import org.hibernate.search.v6poc.backend.elasticsearch.document.model.impl.ElasticsearchIndexSchemaFieldNode;
import org.hibernate.search.v6poc.backend.elasticsearch.impl.ElasticsearchBackend;
import org.hibernate.search.v6poc.backend.elasticsearch.logging.impl.Log;
import org.hibernate.search.v6poc.backend.elasticsearch.search.impl.ElasticsearchSearchQueryElementCollector;
import org.hibernate.search.v6poc.backend.elasticsearch.search.impl.ElasticsearchSearchTargetModel;
import org.hibernate.search.v6poc.engine.spi.SessionContext;
import org.hibernate.search.v6poc.search.ProjectionConstants;
import org.hibernate.search.v6poc.search.query.spi.DocumentReferenceHitCollector;
import org.hibernate.search.v6poc.search.query.spi.HitAggregator;
import org.hibernate.search.v6poc.search.query.spi.LoadingHitCollector;
import org.hibernate.search.v6poc.search.query.spi.ProjectionHitCollector;
import org.hibernate.search.v6poc.search.query.spi.SearchQueryBuilder;
import org.hibernate.search.v6poc.search.query.spi.SearchQueryFactory;
import org.hibernate.search.v6poc.util.spi.LoggerFactory;

class SearchQueryFactoryImpl
		implements SearchQueryFactory<ElasticsearchSearchQueryElementCollector> {

	private static final Log log = LoggerFactory.make( Log.class, MethodHandles.lookup() );

	private final ElasticsearchBackend backend;

	private final ElasticsearchSearchTargetModel searchTargetModel;

	SearchQueryFactoryImpl(ElasticsearchBackend backend, ElasticsearchSearchTargetModel searchTargetModel) {
		this.backend = backend;
		this.searchTargetModel = searchTargetModel;
	}

	@Override
	public <O> SearchQueryBuilder<O, ElasticsearchSearchQueryElementCollector> asObjects(
			SessionContext sessionContext, HitAggregator<LoadingHitCollector, List<O>> hitAggregator) {
		return createSearchQueryBuilder( sessionContext, ObjectHitExtractor.get(), hitAggregator );
	}

	@Override
	public <T> SearchQueryBuilder<T, ElasticsearchSearchQueryElementCollector> asReferences(
			SessionContext sessionContext, HitAggregator<DocumentReferenceHitCollector, List<T>> hitAggregator) {
		return createSearchQueryBuilder( sessionContext, DocumentReferenceHitExtractor.get(), hitAggregator );
	}

	@Override
	public <T> SearchQueryBuilder<T, ElasticsearchSearchQueryElementCollector> asProjections(
			SessionContext sessionContext, HitAggregator<ProjectionHitCollector, List<T>> hitAggregator,
			String... projections) {
		BitSet projectionFound = new BitSet( projections.length );

		HitExtractor<? super ProjectionHitCollector> hitExtractor;
		Set<ElasticsearchIndexModel> indexModels = searchTargetModel.getIndexModels();
		if ( indexModels.size() == 1 ) {
			ElasticsearchIndexModel indexModel = indexModels.iterator().next();
			hitExtractor = createProjectionHitExtractor( indexModel, projections, projectionFound );
		}
		else {
			// Use LinkedHashMap to ensure stable order when generating requests
			Map<String, HitExtractor<? super ProjectionHitCollector>> extractorByIndex = new LinkedHashMap<>();
			for ( ElasticsearchIndexModel indexModel : indexModels ) {
				HitExtractor<? super ProjectionHitCollector> indexHitExtractor =
						createProjectionHitExtractor( indexModel, projections, projectionFound );
				extractorByIndex.put( indexModel.getIndexName().original, indexHitExtractor );
			}
			hitExtractor = new IndexSensitiveHitExtractor<>( extractorByIndex );
		}
		if ( projectionFound.cardinality() < projections.length ) {
			projectionFound.flip( 0, projectionFound.length() );
			List<String> unknownProjections = projectionFound.stream()
					.mapToObj( i -> projections[i] )
					.collect( Collectors.toList() );
			throw log.unknownProjectionForSearch( unknownProjections, searchTargetModel.getIndexNames() );
		}

		return createSearchQueryBuilder( sessionContext, hitExtractor, hitAggregator );
	}

	private HitExtractor<? super ProjectionHitCollector> createProjectionHitExtractor(
			ElasticsearchIndexModel indexModel, String[] projections, BitSet projectionFound) {
		List<HitExtractor<? super ProjectionHitCollector>> extractors = new ArrayList<>( projections.length );
		for ( int i = 0; i < projections.length; ++i ) {
			String projection = projections[i];
			switch ( projection ) {
				case ProjectionConstants.OBJECT:
					projectionFound.set( i );
					extractors.add( ObjectHitExtractor.get() );
					break;
				case ProjectionConstants.REFERENCE:
					projectionFound.set( i );
					extractors.add( DocumentReferenceHitExtractor.get() );
					break;
				case ProjectionConstants.DOCUMENT_REFERENCE:
					projectionFound.set( i );
					extractors.add( DocumentReferenceProjectionHitExtractor.get() );
					break;
				default:
					ElasticsearchIndexSchemaFieldNode node = indexModel.getFieldNode( projection );
					if ( node != null ) {
						projectionFound.set( i );
						extractors.add( new SourceHitExtractor( projection, node.getFormatter() ) );
					}
					else {
						// Make sure that the result list will have the correct indices and size
						extractors.add( NullProjectionHitExtractor.get() );
					}
					break;
			}
		}
		return new CompositeHitExtractor<>( extractors );
	}

	private <C, T> SearchQueryBuilderImpl<C, T> createSearchQueryBuilder(
			SessionContext sessionContext, HitExtractor<? super C> hitExtractor, HitAggregator<C, List<T>> hitAggregator) {
		return new SearchQueryBuilderImpl<>(
				backend.getQueryOrchestrator(),
				backend.getWorkFactory(),
				searchTargetModel.getIndexNames(),
				sessionContext,
				hitExtractor, hitAggregator
		);
	}
}
