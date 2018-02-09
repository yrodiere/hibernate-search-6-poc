/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.integrationtest.backend.tck;

import java.time.LocalDate;

import org.hibernate.search.v6poc.backend.document.DocumentElement;
import org.hibernate.search.v6poc.backend.document.IndexFieldAccessor;
import org.hibernate.search.v6poc.backend.document.IndexObjectFieldAccessor;
import org.hibernate.search.v6poc.backend.document.model.IndexSchemaElement;
import org.hibernate.search.v6poc.backend.document.model.ObjectFieldStorage;
import org.hibernate.search.v6poc.backend.document.model.spi.IndexSchemaObjectField;
import org.hibernate.search.v6poc.backend.index.spi.ChangesetIndexWorker;
import org.hibernate.search.v6poc.backend.index.spi.IndexManager;
import org.hibernate.search.v6poc.backend.index.spi.IndexSearchTarget;
import org.hibernate.search.v6poc.backend.spatial.GeoPoint;
import org.hibernate.search.v6poc.backend.spatial.ImmutableGeoPoint;
import org.hibernate.search.v6poc.cfg.ConfigurationPropertySource;
import org.hibernate.search.v6poc.engine.SearchMappingRepository;
import org.hibernate.search.v6poc.engine.SearchMappingRepositoryBuilder;
import org.hibernate.search.v6poc.engine.spi.SessionContext;
import org.hibernate.search.v6poc.integrationtest.backend.tck.util.TckConfiguration;
import org.hibernate.search.v6poc.integrationtest.util.common.stub.StubSessionContext;
import org.hibernate.search.v6poc.integrationtest.util.common.stub.mapper.StubDocumentReferenceProvider;
import org.hibernate.search.v6poc.integrationtest.util.common.stub.mapper.StubMapping;
import org.hibernate.search.v6poc.integrationtest.util.common.stub.mapper.StubMetadataContributor;
import org.hibernate.search.v6poc.search.DocumentReference;
import org.hibernate.search.v6poc.search.SearchPredicate;
import org.hibernate.search.v6poc.search.SearchQuery;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hibernate.search.v6poc.integrationtest.util.common.assertion.DocumentReferenceSearchResultAssert.assertThat;

/**
 * @author Yoann Rodiere
 */
public class SmokeIT {

	private static final String TYPE_NAME = "MappedType";
	private static final String RAW_INDEX_NAME = "IndexName";

	private IndexAccessors indexAccessors;
	private SearchMappingRepository mappingRepository;
	private IndexManager<?> indexManager;
	private String indexName;

	@Before
	public void setup() {
		TckConfiguration tckConfiguration = TckConfiguration.get();

		ConfigurationPropertySource propertySource = tckConfiguration.getBackendProperties().withPrefix( "backend.testedBackend" );

		SearchMappingRepositoryBuilder mappingRepositoryBuilder = SearchMappingRepository.builder( propertySource )
				.setProperty( "index.default.backend", "testedBackend" );

		StubMetadataContributor contributor = new StubMetadataContributor( mappingRepositoryBuilder );

		contributor.add(
				TYPE_NAME, RAW_INDEX_NAME,
				ctx -> this.indexAccessors = new IndexAccessors( ctx.getSchemaElement() )
		);

		mappingRepository = mappingRepositoryBuilder.build();
		StubMapping mapping = contributor.getResult();

		indexManager = mapping.getIndexManagerByTypeIdentifier( TYPE_NAME );
		indexName = mapping.getNormalizedIndexNameByTypeIdentifier( TYPE_NAME );

		initData();
	}

	@After
	public void cleanup() {
		if ( mappingRepository != null ) {
			mappingRepository.close();
		}
	}

	@Test
	public void search_match() {
		IndexSearchTarget searchTarget = indexManager.createSearchTarget().build();

		SessionContext sessionContext = new StubSessionContext();

		SearchQuery<DocumentReference> query = searchTarget.query( sessionContext )
				.asReferences()
				.predicate().match().onField( "integer" ).matching( 1 )
				.build();
		assertThat( query.execute() )
				.hasDocumentHitsAnyOrder( indexName, "1" )
				.hasHitCount( 1 );

		query = searchTarget.query( sessionContext )
				.asReferences()
				.predicate().match().onField( "string" ).matching( "text 1" )
				.build();
		assertThat( query.execute() )
				.hasDocumentHitsAnyOrder( indexName, "1" )
				.hasHitCount( 1 );

		query = searchTarget.query( sessionContext )
				.asReferences()
				.predicate().match().onField( "string_analyzed" ).matching( "text" )
				.build();
		assertThat( query.execute() )
				.hasDocumentHitsAnyOrder( indexName, "1", "2", "3" )
				.hasHitCount( 3 );

		query = searchTarget.query( sessionContext )
				.asReferences()
				.predicate().match().onField( "localDate" ).matching( LocalDate.of( 2018, 1, 1 ) )
				.build();
		assertThat( query.execute() )
				.hasDocumentHitsAnyOrder( indexName, "1" )
				.hasHitCount( 1 );

		query = searchTarget.query( sessionContext )
				.asReferences()
				.predicate().match().onField( "flattenedObject.string" ).matching( "text 1_1" )
				.build();
		assertThat( query.execute() )
				.hasDocumentHitsAnyOrder( indexName, "1" )
				.hasHitCount( 1 );
	}

	@Test
	public void search_range() {
		IndexSearchTarget searchTarget = indexManager.createSearchTarget().build();

		SessionContext sessionContext = new StubSessionContext();

		SearchQuery<DocumentReference> query = searchTarget.query( sessionContext )
				.asReferences()
				.predicate().range().onField( "integer" ).from( 2 ).to( 42 )
				.build();
		assertThat( query.execute() )
				.hasDocumentHitsAnyOrder( indexName, "2", "3" )
				.hasHitCount( 2 );

		query = searchTarget.query( sessionContext )
				.asReferences()
				.predicate().range().onField( "string" ).from( "text 2" ).to( "text 42" )
				.build();
		assertThat( query.execute() )
				.hasDocumentHitsAnyOrder( indexName, "2", "3" )
				.hasHitCount( 2 );

		query = searchTarget.query( sessionContext )
				.asReferences()
				.predicate().range().onField( "string_analyzed" ).from( "2" ).to( "42" )
				.build();
		assertThat( query.execute() )
				.hasDocumentHitsAnyOrder( indexName, "2", "3" )
				.hasHitCount( 2 );

		query = searchTarget.query( sessionContext )
				.asReferences()
				.predicate().range().onField( "localDate" )
						.from( LocalDate.of( 2018, 1, 2 ) )
						.to( LocalDate.of( 2018, 2, 23 ) )
				.build();
		assertThat( query.execute() )
				.hasDocumentHitsAnyOrder( indexName, "2" )
				.hasHitCount( 1 );

		query = searchTarget.query( sessionContext )
				.asReferences()
				.predicate().range().onField( "flattenedObject.integer" ).from( 201 ).to( 242 )
				.build();
		assertThat( query.execute() )
				.hasDocumentHitsAnyOrder( indexName, "2" )
				.hasHitCount( 1 );
	}

	@Test
	public void search_boolean() {
		IndexSearchTarget searchTarget = indexManager.createSearchTarget().build();

		SessionContext sessionContext = new StubSessionContext();

		SearchQuery<DocumentReference> query = searchTarget.query( sessionContext )
				.asReferences()
				.predicate().bool( b -> {
					b.should().match().onField( "integer" ).matching( 1 );
					b.should().match().onField( "integer" ).matching( 2 );
				} )
				.build();
		assertThat( query.execute() )
				.hasDocumentHitsAnyOrder( indexName, "1", "2" )
				.hasHitCount( 2 );

		query = searchTarget.query( sessionContext )
				.asReferences()
				.predicate().bool( b -> {
					b.must().match().onField( "string_analyzed" ).matching( "text" );
					b.filter().match().onField( "integer" ).matching( 1 );
				} )
				.build();
		assertThat( query.execute() )
				.hasDocumentHitsAnyOrder( indexName, "1" )
				.hasHitCount( 1 );

		query = searchTarget.query( sessionContext )
				.asReferences()
				.predicate().bool( b -> {
					b.must().match().onField( "string_analyzed" ).matching( "text" );
					b.mustNot().match().onField( "integer" ).matching( 2 );
				} )
				.build();
		assertThat( query.execute() )
				.hasDocumentHitsAnyOrder( indexName, "1", "3" )
				.hasHitCount( 2 );
	}

	@Test
	public void search_nested() {
		IndexSearchTarget searchTarget = indexManager.createSearchTarget().build();

		SessionContext sessionContext = new StubSessionContext();

		// Without nested storage, we expect predicates to be able to match on different objects
		SearchQuery<DocumentReference> query = searchTarget.query( sessionContext )
				.asReferences()
				.predicate().bool( b -> {
					b.must().match().onField( "flattenedObject.integer" ).matching( 101 );
					b.must().match().onField( "flattenedObject.string" ).matching( "text 1_2" );
				} )
				.build();
		assertThat( query.execute() )
				.hasDocumentHitsAnyOrder( indexName, "1" )
				.hasHitCount( 1 );

		// With nested storage, we expect direct queries to never match
		query = searchTarget.query( sessionContext )
				.asReferences()
				.predicate().match().onField( "nestedObject.integer" ).matching( 101 )
				.build();
		assertThat( query.execute() )
				.hasNoHits()
				.hasHitCount( 0 );

		// ... and predicates within nested queries to be unable to match on different objects
		query = searchTarget.query( sessionContext )
				.asReferences()
				.predicate().nested().onObjectField( "nestedObject" ).bool( b -> {
					b.must().match().onField( "nestedObject.integer" ).matching( 101 );
					b.must().match().onField( "nestedObject.string" ).matching( "text 1_2" );
				} )
				.build();
		assertThat( query.execute() )
				.hasNoHits()
				.hasHitCount( 0 );

		// ... but predicates should still be able to match on the same object
		query = searchTarget.query( sessionContext )
				.asReferences()
				.predicate().nested().onObjectField( "nestedObject" ).bool( b -> {
					b.must().match().onField( "nestedObject.integer" ).matching( 101 );
					b.must().match().onField( "nestedObject.string" ).matching( "text 1_1" );
				} )
				.build();
		assertThat( query.execute() )
				.hasDocumentHitsAnyOrder( indexName, "1" )
				.hasHitCount( 1 );
	}

	@Test
	public void search_separatePredicate() {
		IndexSearchTarget searchTarget = indexManager.createSearchTarget().build();

		SessionContext sessionContext = new StubSessionContext();

		SearchPredicate predicate = searchTarget.predicate().range().onField( "integer" ).from( 1 ).to( 2 );
		SearchQuery<DocumentReference> query = searchTarget.query( sessionContext )
				.asReferences()
				.predicate( predicate )
				.build();
		assertThat( query.execute() )
				.hasDocumentHitsAnyOrder( indexName, "1", "2" )
				.hasHitCount( 2 );

		predicate = searchTarget.predicate().match().onField( "string" ).matching( "text 1" );
		query = searchTarget.query( sessionContext )
				.asReferences()
				.predicate( predicate )
				.build();
		assertThat( query.execute() )
				.hasDocumentHitsAnyOrder( indexName, "1" )
				.hasHitCount( 1 );

		predicate = searchTarget.predicate().bool()
				.should().match().onField( "integer" ).matching( 1 )
				.should().match().onField( "integer" ).matching( 2 )
				.end();
		query = searchTarget.query( sessionContext )
				.asReferences()
				.predicate( predicate )
				.build();
		assertThat( query.execute() )
				.hasDocumentHitsAnyOrder( indexName, "1", "2" )
				.hasHitCount( 2 );
	}

	private void initData() {
		StubSessionContext sessionContext = new StubSessionContext();

		ChangesetIndexWorker<? extends DocumentElement> worker = indexManager.createWorker( sessionContext );
		worker.add( new StubDocumentReferenceProvider( "1" ), document -> {
			indexAccessors.integer.write( document, 1 );
			indexAccessors.string.write( document, "text 1" );
			indexAccessors.string_analyzed.write( document, "text 1" );
			indexAccessors.localDate.write( document, LocalDate.of( 2018, 1, 1 ) );
			indexAccessors.geoPoint.write( document, new ImmutableGeoPoint( 0, 1 ) );

			DocumentElement flattenedObject = indexAccessors.flattenedObject.self.add( document );
			indexAccessors.flattenedObject.integer.write( flattenedObject, 101 );
			indexAccessors.flattenedObject.string.write( flattenedObject, "text 1_1" );
			flattenedObject = indexAccessors.flattenedObject.self.add( document );
			indexAccessors.flattenedObject.integer.write( flattenedObject, 102 );
			indexAccessors.flattenedObject.string.write( flattenedObject, "text 1_2" );

			DocumentElement nestedObject = indexAccessors.nestedObject.self.add( document );
			indexAccessors.nestedObject.integer.write( nestedObject, 101 );
			indexAccessors.nestedObject.string.write( nestedObject, "text 1_1" );
			nestedObject = indexAccessors.nestedObject.self.add( document );
			indexAccessors.nestedObject.integer.write( nestedObject, 102 );
			indexAccessors.nestedObject.string.write( nestedObject, "text 1_2" );
		} );

		worker.add( new StubDocumentReferenceProvider( "2" ), document -> {
			indexAccessors.integer.write( document, 2 );
			indexAccessors.string.write( document, "text 2" );
			indexAccessors.string_analyzed.write( document, "text 2" );
			indexAccessors.localDate.write( document, LocalDate.of( 2018, 1, 2 ) );
			indexAccessors.geoPoint.write( document, new ImmutableGeoPoint( 0, 2 ) );

			DocumentElement flattenedObject = indexAccessors.flattenedObject.self.add( document );
			indexAccessors.flattenedObject.integer.write( flattenedObject, 201 );
			indexAccessors.flattenedObject.string.write( flattenedObject, "text 2_1" );
			flattenedObject = indexAccessors.flattenedObject.self.add( document );
			indexAccessors.flattenedObject.integer.write( flattenedObject, 202 );
			indexAccessors.flattenedObject.string.write( flattenedObject, "text 2_2" );

			DocumentElement nestedObject = indexAccessors.nestedObject.self.add( document );
			indexAccessors.nestedObject.integer.write( nestedObject, 201 );
			indexAccessors.nestedObject.string.write( nestedObject, "text 2_1" );
			nestedObject = indexAccessors.nestedObject.self.add( document );
			indexAccessors.nestedObject.integer.write( nestedObject, 202 );
			indexAccessors.nestedObject.string.write( nestedObject, "text 2_2" );
		} );

		worker.add( new StubDocumentReferenceProvider( "3" ), document -> {
			indexAccessors.integer.write( document, 3 );
			indexAccessors.string.write( document, "text 3" );
			indexAccessors.string_analyzed.write( document, "text 3" );
		} );

		// Expect the following add + delete to be simplified to a no-op by the engine
		worker.add( new StubDocumentReferenceProvider( "4" ), document -> {
			indexAccessors.integer.write( document, 4 );
			indexAccessors.string.write( document, "text 4" );
			indexAccessors.string_analyzed.write( document, "text 4" );
		} );
		worker.delete( new StubDocumentReferenceProvider( "4" ) );

		worker.add( new StubDocumentReferenceProvider( "empty" ), document -> { } );

		worker.execute().join();

		// Check that all documents are searchable
		IndexSearchTarget searchTarget = indexManager.createSearchTarget().build();
		SearchQuery<DocumentReference> query = searchTarget.query( sessionContext )
				.asReferences()
				.predicate().all().end()
				.build();
		assertThat( query.execute() )
				.hasDocumentHitsAnyOrder( indexName, "1", "2", "3", "empty" );
	}

	private static class IndexAccessors {
		final IndexFieldAccessor<Integer> integer;
		final IndexFieldAccessor<String> string;
		final IndexFieldAccessor<String> string_analyzed;
		final IndexFieldAccessor<LocalDate> localDate;
		final IndexFieldAccessor<GeoPoint> geoPoint;
		final ObjectAccessors flattenedObject;
		final ObjectAccessors nestedObject;

		IndexAccessors(IndexSchemaElement root) {
			integer = root.field( "integer" ).asInteger().createAccessor();
			string = root.field( "string" ).asString().createAccessor();
			string_analyzed = root.field( "string_analyzed" ).asString()
					.analyzer( "default" )
					.createAccessor();
			localDate = root.field( "localDate" ).asLocalDate().createAccessor();
			geoPoint = root.field( "geoPoint" ).asGeoPoint().createAccessor();
			IndexSchemaObjectField flattenedObjectField = root.objectField( "flattenedObject", ObjectFieldStorage.FLATTENED );
			flattenedObject = new ObjectAccessors( flattenedObjectField );
			IndexSchemaObjectField nestedObjectField = root.objectField( "nestedObject", ObjectFieldStorage.NESTED );
			nestedObject = new ObjectAccessors( nestedObjectField );
		}
	}

	private static class ObjectAccessors {
		final IndexObjectFieldAccessor self;
		final IndexFieldAccessor<Integer> integer;
		final IndexFieldAccessor<String> string;

		ObjectAccessors(IndexSchemaObjectField objectField) {
			self = objectField.createAccessor();
			integer = objectField.field( "integer" ).asInteger().createAccessor();
			string = objectField.field( "string" ).asString().createAccessor();
		}
	}
}
