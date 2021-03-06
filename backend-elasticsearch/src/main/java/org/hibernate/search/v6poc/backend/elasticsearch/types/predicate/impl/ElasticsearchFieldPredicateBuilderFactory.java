/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.backend.elasticsearch.types.predicate.impl;

import org.hibernate.search.v6poc.backend.elasticsearch.search.predicate.impl.ElasticsearchSearchPredicateBuilder;
import org.hibernate.search.v6poc.search.predicate.spi.MatchPredicateBuilder;
import org.hibernate.search.v6poc.search.predicate.spi.RangePredicateBuilder;
import org.hibernate.search.v6poc.search.predicate.spi.SpatialWithinBoundingBoxPredicateBuilder;
import org.hibernate.search.v6poc.search.predicate.spi.SpatialWithinCirclePredicateBuilder;
import org.hibernate.search.v6poc.search.predicate.spi.SpatialWithinPolygonPredicateBuilder;

public interface ElasticsearchFieldPredicateBuilderFactory {

	MatchPredicateBuilder<ElasticsearchSearchPredicateBuilder> createMatchPredicateBuilder(String absoluteFieldPath);

	RangePredicateBuilder<ElasticsearchSearchPredicateBuilder> createRangePredicateBuilder(String absoluteFieldPath);

	SpatialWithinCirclePredicateBuilder<ElasticsearchSearchPredicateBuilder> createSpatialWithinCirclePredicateBuilder(
			String absoluteFieldPath);

	SpatialWithinPolygonPredicateBuilder<ElasticsearchSearchPredicateBuilder> createSpatialWithinPolygonPredicateBuilder(
			String absoluteFieldPath);

	SpatialWithinBoundingBoxPredicateBuilder<ElasticsearchSearchPredicateBuilder> createSpatialWithinBoundingBoxPredicateBuilder(
			String absoluteFieldPath);
}
