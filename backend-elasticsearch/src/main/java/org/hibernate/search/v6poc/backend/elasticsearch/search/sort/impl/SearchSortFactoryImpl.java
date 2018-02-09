/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.backend.elasticsearch.search.sort.impl;

import java.lang.invoke.MethodHandles;

import org.hibernate.search.v6poc.backend.elasticsearch.logging.impl.Log;
import org.hibernate.search.v6poc.backend.elasticsearch.search.impl.ElasticsearchSearchQueryElementCollector;
import org.hibernate.search.v6poc.backend.elasticsearch.search.impl.ElasticsearchSearchTargetModel;
import org.hibernate.search.v6poc.search.SearchSort;
import org.hibernate.search.v6poc.search.sort.spi.FieldSortBuilder;
import org.hibernate.search.v6poc.search.sort.spi.ScoreSortBuilder;
import org.hibernate.search.v6poc.search.sort.spi.SearchSortContributor;
import org.hibernate.search.v6poc.util.spi.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

/**
 * @author Yoann Rodiere
 */
// TODO have one version of the factory per dialect, if necessary
public class SearchSortFactoryImpl implements ElasticsearchSearchSortFactory {

	private static final Log log = LoggerFactory.make( Log.class, MethodHandles.lookup() );

	private static final Gson GSON = new GsonBuilder().create();

	private final ElasticsearchSearchTargetModel searchTargetModel;

	public SearchSortFactoryImpl(ElasticsearchSearchTargetModel searchTargetModel) {
		this.searchTargetModel = searchTargetModel;
	}

	@Override
	public SearchSort toSearchSort(SearchSortContributor<ElasticsearchSearchSortCollector> contributor) {
		ElasticsearchSearchQueryElementCollector collector = new ElasticsearchSearchQueryElementCollector();
		contributor.contribute( collector );
		return new ElasticsearchSearchSort( collector.toJsonSort() );
	}

	@Override
	public SearchSortContributor<ElasticsearchSearchSortCollector> toContributor(SearchSort predicate) {
		if ( !( predicate instanceof ElasticsearchSearchSort ) ) {
			throw log.cannotMixElasticsearchSearchSortWithOtherSorts( predicate );
		}
		return (ElasticsearchSearchSort) predicate;
	}

	@Override
	public ScoreSortBuilder<ElasticsearchSearchSortCollector> score() {
		return new ScoreSortBuilderImpl();
	}

	@Override
	public FieldSortBuilder<ElasticsearchSearchSortCollector> field(String absoluteFieldPath) {
		return new FieldSortBuilderImpl( absoluteFieldPath, searchTargetModel::getFieldFormatter );
	}

	@Override
	public SearchSortContributor<ElasticsearchSearchSortCollector> indexOrder() {
		return IndexOrderSortContributor.INSTANCE;
	}

	@Override
	public SearchSortContributor<ElasticsearchSearchSortCollector> fromJsonString(String jsonString) {
		return new UserProvidedJsonSortContributor( GSON.fromJson( jsonString, JsonObject.class ) );
	}
}
