/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.backend.elasticsearch.search.sort.impl;

import org.hibernate.search.v6poc.backend.elasticsearch.gson.impl.JsonAccessor;
import org.hibernate.search.v6poc.backend.elasticsearch.gson.impl.JsonObjectAccessor;
import org.hibernate.search.v6poc.backend.elasticsearch.types.codec.impl.GeoPointFieldCodec;
import org.hibernate.search.v6poc.backend.spatial.GeoPoint;
import org.hibernate.search.v6poc.search.sort.spi.DistanceSortBuilder;

import com.google.gson.JsonObject;

class DistanceSortBuilderImpl extends AbstractSearchSortBuilder
		implements DistanceSortBuilder<ElasticsearchSearchSortCollector> {

	private static final JsonObjectAccessor GEO_DISTANCE = JsonAccessor.root().property( "_geo_distance" ).asObject();

	private final String absoluteFieldPath;
	private final GeoPoint location;

	DistanceSortBuilderImpl(String absoluteFieldPath, GeoPoint location) {
		this.absoluteFieldPath = absoluteFieldPath;
		this.location = location;
	}

	@Override
	public void contribute(ElasticsearchSearchSortCollector collector) {
		getInnerObject().add( absoluteFieldPath, GeoPointFieldCodec.INSTANCE.encode( location ) );

		JsonObject outerObject = new JsonObject();
		GEO_DISTANCE.add( outerObject, getInnerObject() );
		collector.collectSort( outerObject );
	}
}
