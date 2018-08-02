/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.integrationtest.backend.elasticsearch.search;

import static org.hibernate.search.v6poc.util.impl.integrationtest.common.assertion.DocumentReferencesSearchResultAssert.assertThat;
import static org.hibernate.search.v6poc.util.impl.integrationtest.common.stub.mapper.StubMapperUtils.referenceProvider;

import java.util.function.Consumer;

import org.hibernate.search.v6poc.backend.document.DocumentElement;
import org.hibernate.search.v6poc.backend.document.IndexFieldAccessor;
import org.hibernate.search.v6poc.backend.document.model.dsl.IndexSchemaElement;
import org.hibernate.search.v6poc.backend.document.model.dsl.Sortable;
import org.hibernate.search.v6poc.backend.index.spi.ChangesetIndexWorker;
import org.hibernate.search.v6poc.entity.mapping.spi.MappedIndexManager;
import org.hibernate.search.v6poc.entity.mapping.spi.MappedIndexSearchTarget;
import org.hibernate.search.v6poc.engine.spi.SessionContext;
import org.hibernate.search.v6poc.integrationtest.backend.tck.util.rule.SearchSetupHelper;
import org.hibernate.search.v6poc.search.DocumentReference;
import org.hibernate.search.v6poc.search.SearchQuery;
import org.hibernate.search.v6poc.search.dsl.sort.SearchSortContainerContext;
import org.hibernate.search.v6poc.spatial.GeoPoint;
import org.hibernate.search.v6poc.spatial.ImmutableGeoPoint;
import org.hibernate.search.v6poc.util.impl.integrationtest.common.assertion.DocumentReferencesSearchResultAssert;
import org.hibernate.search.v6poc.util.impl.integrationtest.common.stub.StubSessionContext;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class ElasticsearchSearchSortIT {

	private static final String INDEX_NAME = "IndexName";

	private static final String FIRST_ID = "1";
	private static final String SECOND_ID = "2";
	private static final String THIRD_ID = "3";
	private static final String EMPTY_ID = "empty";

	@Rule
	public SearchSetupHelper setupHelper = new SearchSetupHelper();

	private IndexAccessors indexAccessors;
	private MappedIndexManager<?> indexManager;
	private SessionContext sessionContext = new StubSessionContext();

	@Before
	public void setup() {
		setupHelper.withDefaultConfiguration()
				.withIndex(
						"MappedType", INDEX_NAME,
						ctx -> this.indexAccessors = new IndexAccessors( ctx.getSchemaElement() ),
						indexManager -> this.indexManager = indexManager
				)
				.setup();

		initData();
	}

	private SearchQuery<DocumentReference> simpleQuery(Consumer<? super SearchSortContainerContext<?>> sortContributor) {
		MappedIndexSearchTarget searchTarget = indexManager.createSearchTarget().build();
		return searchTarget.query( sessionContext )
				.asReferences()
				.predicate().matchAll().end()
				.sort( sortContributor )
				.build();
	}

	@Test
	public void byDistanceDesc() {
		SearchQuery<DocumentReference> query = simpleQuery( b -> b.byDistance( "geoPoint", new ImmutableGeoPoint( 45.757864, 4.834496 ) ).desc() );

		DocumentReferencesSearchResultAssert.assertThat( query )
				.hasReferencesHitsExactOrder( INDEX_NAME, EMPTY_ID, FIRST_ID, THIRD_ID, SECOND_ID );

		query = simpleQuery( b -> b.byDistance( "geoPoint", 45.757864, 4.834496 ).desc() );

		DocumentReferencesSearchResultAssert.assertThat( query )
				.hasReferencesHitsExactOrder( INDEX_NAME, EMPTY_ID, FIRST_ID, THIRD_ID, SECOND_ID );
	}

	private void initData() {
		ChangesetIndexWorker<? extends DocumentElement> worker = indexManager.createWorker( sessionContext );
		worker.add( referenceProvider( FIRST_ID ), document -> {
			indexAccessors.geoPoint.write( document, new ImmutableGeoPoint( 45.7705687,4.835233 ) );
		} );
		worker.add( referenceProvider( SECOND_ID ), document -> {
			indexAccessors.geoPoint.write( document, new ImmutableGeoPoint( 45.7541719, 4.8386221 ) );
		} );
		worker.add( referenceProvider( THIRD_ID ), document -> {
			indexAccessors.geoPoint.write( document, new ImmutableGeoPoint( 45.7530374, 4.8510299 ) );
		} );
		worker.add( referenceProvider( EMPTY_ID ), document -> { } );

		worker.execute().join();

		// Check that all documents are searchable
		MappedIndexSearchTarget searchTarget = indexManager.createSearchTarget().build();
		SearchQuery<DocumentReference> query = searchTarget.query( sessionContext )
				.asReferences()
				.predicate().matchAll().end()
				.build();
		assertThat( query ).hasReferencesHitsAnyOrder( INDEX_NAME, FIRST_ID, SECOND_ID, THIRD_ID, EMPTY_ID );
	}

	private static class IndexAccessors {
		final IndexFieldAccessor<GeoPoint> geoPoint;

		IndexAccessors(IndexSchemaElement root) {
			geoPoint = root.field( "geoPoint" ).asGeoPoint().sortable( Sortable.YES ).createAccessor();
		}
	}
}
