/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.backend.elasticsearch.types.predicate.impl;

import java.lang.invoke.MethodHandles;

import org.hibernate.search.v6poc.backend.elasticsearch.logging.impl.Log;
import org.hibernate.search.v6poc.backend.elasticsearch.search.predicate.impl.ElasticsearchSearchPredicateBuilder;
import org.hibernate.search.v6poc.backend.elasticsearch.search.predicate.impl.MatchPredicateBuilderImpl;
import org.hibernate.search.v6poc.backend.elasticsearch.search.predicate.impl.RangePredicateBuilderImpl;
import org.hibernate.search.v6poc.backend.elasticsearch.types.codec.impl.ElasticsearchFieldCodec;
import org.hibernate.search.v6poc.logging.spi.EventContexts;
import org.hibernate.search.v6poc.search.predicate.spi.MatchPredicateBuilder;
import org.hibernate.search.v6poc.search.predicate.spi.RangePredicateBuilder;
import org.hibernate.search.v6poc.search.predicate.spi.SpatialWithinBoundingBoxPredicateBuilder;
import org.hibernate.search.v6poc.search.predicate.spi.SpatialWithinCirclePredicateBuilder;
import org.hibernate.search.v6poc.search.predicate.spi.SpatialWithinPolygonPredicateBuilder;
import org.hibernate.search.v6poc.util.impl.common.LoggerFactory;

public class StandardFieldPredicateBuilderFactory implements ElasticsearchFieldPredicateBuilderFactory {

	private static final Log log = LoggerFactory.make( Log.class, MethodHandles.lookup() );

	private final ElasticsearchFieldCodec codec;

	public StandardFieldPredicateBuilderFactory(ElasticsearchFieldCodec codec) {
		this.codec = codec;
	}

	@Override
	public MatchPredicateBuilder<ElasticsearchSearchPredicateBuilder> createMatchPredicateBuilder(
			String absoluteFieldPath) {
		return new MatchPredicateBuilderImpl( absoluteFieldPath, codec );
	}

	@Override
	public RangePredicateBuilder<ElasticsearchSearchPredicateBuilder> createRangePredicateBuilder(
			String absoluteFieldPath) {
		return new RangePredicateBuilderImpl( absoluteFieldPath, codec );
	}

	@Override
	public SpatialWithinCirclePredicateBuilder<ElasticsearchSearchPredicateBuilder> createSpatialWithinCirclePredicateBuilder(
			String absoluteFieldPath) {
		throw log.spatialPredicatesNotSupportedByFieldType(
				EventContexts.fromIndexFieldAbsolutePath( absoluteFieldPath )
		);
	}

	@Override
	public SpatialWithinPolygonPredicateBuilder<ElasticsearchSearchPredicateBuilder> createSpatialWithinPolygonPredicateBuilder(
			String absoluteFieldPath) {
		throw log.spatialPredicatesNotSupportedByFieldType(
				EventContexts.fromIndexFieldAbsolutePath( absoluteFieldPath )
		);
	}

	@Override
	public SpatialWithinBoundingBoxPredicateBuilder<ElasticsearchSearchPredicateBuilder> createSpatialWithinBoundingBoxPredicateBuilder(
			String absoluteFieldPath) {
		throw log.spatialPredicatesNotSupportedByFieldType(
				EventContexts.fromIndexFieldAbsolutePath( absoluteFieldPath )
		);
	}
}
