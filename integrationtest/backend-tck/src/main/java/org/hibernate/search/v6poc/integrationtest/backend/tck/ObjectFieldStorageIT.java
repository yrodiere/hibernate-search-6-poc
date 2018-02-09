/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.integrationtest.backend.tck;

import java.time.LocalDate;
import java.util.Arrays;

import org.hibernate.search.v6poc.backend.document.DocumentElement;
import org.hibernate.search.v6poc.backend.document.IndexFieldAccessor;
import org.hibernate.search.v6poc.backend.document.IndexObjectFieldAccessor;
import org.hibernate.search.v6poc.backend.document.model.IndexSchemaElement;
import org.hibernate.search.v6poc.backend.document.model.ObjectFieldStorage;
import org.hibernate.search.v6poc.backend.document.model.spi.IndexSchemaObjectField;
import org.hibernate.search.v6poc.backend.index.spi.ChangesetIndexWorker;
import org.hibernate.search.v6poc.backend.index.spi.IndexManager;
import org.hibernate.search.v6poc.backend.index.spi.IndexSearchTarget;
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
import org.hibernate.search.v6poc.search.SearchQuery;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hibernate.search.v6poc.integrationtest.util.common.assertion.DocumentReferenceSearchResultAssert.assertThat;

public class ObjectFieldStorageIT {

	private static final String TYPE_NAME = "MappedType";
	private static final String RAW_INDEX_NAME = "IndexName";

	private static final String EXPECTED_NESTED_MATCH_ID = "nestedQueryShouldMatchId";
	private static final String EXPECTED_NON_NESTED_MATCH_ID = "nonNestedQueryShouldMatchId";

	private static final Integer MATCHING_INTEGER = 42;
	private static final String MATCHING_STRING = "matchingWord";
	private static final String MATCHING_STRING_ANALYZED = "analyzedMatchingWord otherAnalyzedMatchingWord";
	private static final LocalDate MATCHING_LOCAL_DATE = LocalDate.of( 2018, 2, 1 );
	private static final String MATCHING_SECOND_LEVEL_CONDITION1_FIELD1 = "firstMatchingWord";
	private static final String MATCHING_SECOND_LEVEL_CONDITION1_FIELD2 = "firstMatchingWord";
	private static final String MATCHING_SECOND_LEVEL_CONDITION2_FIELD1 = "secondMatchingWord";
	private static final String MATCHING_SECOND_LEVEL_CONDITION2_FIELD2 = "secondMatchingWord";

	private static final Integer NON_MATCHING_INTEGER = 442;
	private static final String NON_MATCHING_STRING = "nonMatchingWord";
	private static final String NON_MATCHING_STRING_ANALYZED = "analyzedNonMatchingWord otherAnalyzedNonMatchingWord";
	private static final LocalDate NON_MATCHING_LOCAL_DATE = LocalDate.of( 2018, 2, 15 );
	private static final String NON_MATCHING_SECOND_LEVEL_CONDITION1_FIELD1 = "firstNonMatchingWord";
	private static final String NON_MATCHING_SECOND_LEVEL_CONDITION1_FIELD2 = "firstNonMatchingWord";
	private static final String NON_MATCHING_SECOND_LEVEL_CONDITION2_FIELD1 = "secondNonMatchingWord";
	private static final String NON_MATCHING_SECOND_LEVEL_CONDITION2_FIELD2 = "secondNonMatchingWord";

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
				.predicate().bool( b -> {
					b.must().match().onField( "flattenedObject.integer" ).matching( MATCHING_INTEGER );
					b.must().match().onField( "flattenedObject.string" ).matching( MATCHING_STRING );
					b.must().match().onField( "flattenedObject.string_analyzed" ).matching( MATCHING_STRING_ANALYZED );
					b.must().match().onField( "flattenedObject.localDate" ).matching( MATCHING_LOCAL_DATE );
				} )
				.build();
		assertThat( query.execute() )
				.hasDocumentHitsAnyOrder( indexName, EXPECTED_NON_NESTED_MATCH_ID )
				.hasHitCount( 1 );

		query = searchTarget.query( sessionContext )
				.asReferences()
				.predicate( root -> root.nested().onObjectField( "nestedObject" ).bool()
						.must().match().onField( "nestedObject.integer" ).matching( MATCHING_INTEGER )
						.must().match().onField( "nestedObject.string" ).matching( MATCHING_STRING )
						.must().match().onField( "nestedObject.string_analyzed" ).matching( MATCHING_STRING_ANALYZED )
						.must().match().onField( "nestedObject.localDate" ).matching( MATCHING_LOCAL_DATE )
				)
				.build();
		assertThat( query.execute() )
				.hasDocumentHitsAnyOrder( indexName, EXPECTED_NESTED_MATCH_ID )
				.hasHitCount( 1 );
	}

	@Test
	public void search_range() {
		IndexSearchTarget searchTarget = indexManager.createSearchTarget().build();

		SessionContext sessionContext = new StubSessionContext();

		SearchQuery<DocumentReference> query = searchTarget.query( sessionContext )
				.asReferences()
				.predicate().bool( b -> {
					b.must().range().onField( "flattenedObject.integer" )
							.from( MATCHING_INTEGER - 1 ).to( MATCHING_INTEGER + 1 );
					b.must().range().onField( "flattenedObject.string" )
							.from( MATCHING_STRING ).to( MATCHING_STRING );
					b.must().range().onField( "flattenedObject.localDate" )
							.from( MATCHING_LOCAL_DATE.minusDays( 1 ) ).to( MATCHING_LOCAL_DATE.plusDays( 1 ) );
				} )
				.build();
		assertThat( query.execute() )
				.hasDocumentHitsAnyOrder( indexName, EXPECTED_NON_NESTED_MATCH_ID )
				.hasHitCount( 1 );

		query = searchTarget.query( sessionContext )
				.asReferences()
				.predicate().nested().onObjectField( "nestedObject" ).bool( b -> {
					b.must().range().onField( "nestedObject.integer" )
							.from( MATCHING_INTEGER - 1 ).to( MATCHING_INTEGER + 1 );
					b.must().range().onField( "nestedObject.string" )
							.from( MATCHING_STRING ).to( MATCHING_STRING );
					b.must().range().onField( "nestedObject.localDate" )
							.from( MATCHING_LOCAL_DATE.minusDays( 1 ) ).to( MATCHING_LOCAL_DATE.plusDays( 1 ) );
				} )
				.build();
		assertThat( query.execute() )
				.hasDocumentHitsAnyOrder( indexName, EXPECTED_NESTED_MATCH_ID )
				.hasHitCount( 1 );
	}

	@Test
	public void search_nestedOnTwoLevels() {
		IndexSearchTarget searchTarget = indexManager.createSearchTarget().build();

		SessionContext sessionContext = new StubSessionContext();

		SearchQuery<DocumentReference> query = searchTarget.query( sessionContext )
				.asReferences()
				.predicate().bool( b -> {
					// This is referred to as "condition 1" in the data initialization method
					b.must().nested().onObjectField( "flattenedObject.nestedObject" ).bool( b2 -> {
						b2.must().match().onField( "flattenedObject.nestedObject.field1" )
								.matching( MATCHING_SECOND_LEVEL_CONDITION1_FIELD1 );
						b2.must().match().onField( "flattenedObject.nestedObject.field2" )
								.matching( MATCHING_SECOND_LEVEL_CONDITION1_FIELD2 );
					} );
					// This is referred to as "condition 2" in the data initialization method
					b.must().nested().onObjectField( "flattenedObject.nestedObject" ).bool( b2 -> {
						b2.must().match().onField( "flattenedObject.nestedObject.field1" )
								.matching( MATCHING_SECOND_LEVEL_CONDITION2_FIELD1 );
						b2.must().match().onField( "flattenedObject.nestedObject.field2" )
								.matching( MATCHING_SECOND_LEVEL_CONDITION2_FIELD1 );
					} );
				} )
				.build();
		assertThat( query.execute() )
				.hasDocumentHitsAnyOrder( indexName, EXPECTED_NON_NESTED_MATCH_ID )
				.hasHitCount( 1 );

		query = searchTarget.query( sessionContext )
				.asReferences()
				.predicate().nested().onObjectField( "nestedObject" ).bool( b -> {
					// This is referred to as "condition 1" in the data initialization method
					b.must().nested().onObjectField( "nestedObject.nestedObject" ).bool( b2 -> {
						b2.must().match().onField( "nestedObject.nestedObject.field1" )
								.matching( MATCHING_SECOND_LEVEL_CONDITION1_FIELD1 );
						b2.must().match().onField( "nestedObject.nestedObject.field2" )
								.matching( MATCHING_SECOND_LEVEL_CONDITION1_FIELD2 );
					} );
					// This is referred to as "condition 2" in the data initialization method
					b.must().nested().onObjectField( "nestedObject.nestedObject" ).bool( b2 -> {
						b2.must().match().onField( "nestedObject.nestedObject.field1" )
								.matching( MATCHING_SECOND_LEVEL_CONDITION2_FIELD1 );
						b2.must().match().onField( "nestedObject.nestedObject.field2" )
								.matching( MATCHING_SECOND_LEVEL_CONDITION2_FIELD2 );
					} );
				} )
				.build();
		assertThat( query.execute() )
				.hasDocumentHitsAnyOrder( indexName, EXPECTED_NESTED_MATCH_ID )
				.hasHitCount( 1 );
	}

	private void initData() {
		StubSessionContext sessionContext = new StubSessionContext();

		ChangesetIndexWorker<? extends DocumentElement> worker = indexManager.createWorker( sessionContext );
		worker.add( new StubDocumentReferenceProvider( EXPECTED_NESTED_MATCH_ID ), document -> {
			ObjectAccessors accessors;
			SecondLevelObjectAccessors secondLevelAccessors;
			DocumentElement object;
			DocumentElement secondLevelObject;

			// ----------------
			// Flattened object
			// Leave it empty.
			accessors = indexAccessors.flattenedObject;
			accessors.self.addMissing( document );

			// -------------
			// Nested object
			// Content specially crafted to match in nested queries.
			accessors = indexAccessors.nestedObject;
			secondLevelAccessors = accessors.nestedObject;

			object = accessors.self.add( document );
			accessors.integer.write( object, NON_MATCHING_INTEGER );
			secondLevelAccessors.self.addMissing( object );
			secondLevelObject = secondLevelAccessors.self.add( object );
			secondLevelAccessors.field1.write( secondLevelObject, MATCHING_SECOND_LEVEL_CONDITION2_FIELD1 );
			secondLevelAccessors.field2.write( secondLevelObject, MATCHING_SECOND_LEVEL_CONDITION2_FIELD2 );
			secondLevelAccessors.field2.write( secondLevelObject, NON_MATCHING_SECOND_LEVEL_CONDITION1_FIELD2 );

			// This object will trigger the match; others should not
			object = accessors.self.add( document );
			accessors.integer.write( object, MATCHING_INTEGER );
			accessors.string.write( object, MATCHING_STRING );
			accessors.string.write( object, NON_MATCHING_STRING );
			accessors.string_analyzed.write( object, MATCHING_STRING_ANALYZED );
			accessors.localDate.write( object, MATCHING_LOCAL_DATE );
			secondLevelObject = secondLevelAccessors.self.add( object );
			secondLevelAccessors.field1.write( secondLevelObject, NON_MATCHING_SECOND_LEVEL_CONDITION2_FIELD1 );
			secondLevelAccessors.field2.write( secondLevelObject, MATCHING_SECOND_LEVEL_CONDITION2_FIELD2 );
			secondLevelAccessors.field2.write( secondLevelObject, NON_MATCHING_SECOND_LEVEL_CONDITION1_FIELD2 );
			secondLevelAccessors.self.addMissing( object );
			secondLevelObject = secondLevelAccessors.self.add( object ); // This matches nested condition 1
			secondLevelAccessors.field1.write( secondLevelObject, MATCHING_SECOND_LEVEL_CONDITION1_FIELD1 );
			secondLevelAccessors.field2.write( secondLevelObject, NON_MATCHING_SECOND_LEVEL_CONDITION1_FIELD1 );
			secondLevelAccessors.field2.write( secondLevelObject, MATCHING_SECOND_LEVEL_CONDITION1_FIELD2 );
			secondLevelObject = secondLevelAccessors.self.add( object ); // This matches nested condition 2
			secondLevelAccessors.field1.write( secondLevelObject, MATCHING_SECOND_LEVEL_CONDITION2_FIELD1 );
			secondLevelAccessors.field2.write( secondLevelObject, MATCHING_SECOND_LEVEL_CONDITION2_FIELD2 );
			secondLevelAccessors.field2.write( secondLevelObject, NON_MATCHING_SECOND_LEVEL_CONDITION1_FIELD2 );

			object = accessors.self.add( document );
			accessors.localDate.write( object, NON_MATCHING_LOCAL_DATE );
			secondLevelAccessors.self.addMissing( object );
		} );

		worker.add( new StubDocumentReferenceProvider( EXPECTED_NON_NESTED_MATCH_ID ), document -> {
			/*
			 * Below, we use the same content for both the flattened object and the nested object.
			 * This is to demonstrate the practical difference of object storage:
			 * with the same content and similar conditions, the queries on the flattened object should match,
			 * but the queries on the nested object should not.
			 * In short, there is content that matches each and every condition used in tests,
			 * but this content is spread over several objects, meaning it will only match
			 * if flattened storage is used.
			 */
			for ( ObjectAccessors accessors :
					Arrays.asList( indexAccessors.flattenedObject, indexAccessors.nestedObject ) ) {
				DocumentElement object = accessors.self.add( document );
				SecondLevelObjectAccessors secondLevelAccessors = accessors.nestedObject;
				accessors.integer.write( object, NON_MATCHING_INTEGER );
				DocumentElement secondLevelObject = secondLevelAccessors.self.add( object );
				secondLevelAccessors.field1.write( secondLevelObject, NON_MATCHING_SECOND_LEVEL_CONDITION1_FIELD1 );

				object = accessors.self.add( document );
				accessors.integer.write( object, MATCHING_INTEGER );
				accessors.integer.write( object, NON_MATCHING_INTEGER );
				accessors.string.write( object, NON_MATCHING_STRING );
				secondLevelObject = secondLevelAccessors.self.add( object ); // This matches nested condition 1
				secondLevelAccessors.field1.write( secondLevelObject, MATCHING_SECOND_LEVEL_CONDITION1_FIELD1 );
				secondLevelAccessors.field2.write( secondLevelObject, MATCHING_SECOND_LEVEL_CONDITION1_FIELD2 );
				secondLevelObject = secondLevelAccessors.self.add( object );
				secondLevelAccessors.field1.write( secondLevelObject, NON_MATCHING_SECOND_LEVEL_CONDITION1_FIELD1 );
				secondLevelAccessors.field2.write( secondLevelObject, MATCHING_SECOND_LEVEL_CONDITION1_FIELD2 );

				object = accessors.self.add( document );
				accessors.string.write( object, MATCHING_STRING );
				secondLevelAccessors.self.addMissing( object );
				secondLevelObject = secondLevelAccessors.self.add( object );
				secondLevelAccessors.field1.write( secondLevelObject, MATCHING_SECOND_LEVEL_CONDITION2_FIELD1 );
				secondLevelAccessors.field2.write( secondLevelObject, NON_MATCHING_SECOND_LEVEL_CONDITION2_FIELD2 );

				object = accessors.self.add( document );
				accessors.string_analyzed.write( object, MATCHING_STRING_ANALYZED );
				secondLevelObject = secondLevelAccessors.self.add( object ); // This matches nested condition 2
				secondLevelAccessors.field1.write( secondLevelObject, MATCHING_SECOND_LEVEL_CONDITION2_FIELD1 );
				secondLevelAccessors.field2.write( secondLevelObject, MATCHING_SECOND_LEVEL_CONDITION2_FIELD2 );

				object = accessors.self.add( document );
				accessors.localDate.write( object, MATCHING_LOCAL_DATE );
				accessors.string_analyzed.write( object, NON_MATCHING_STRING_ANALYZED );
			}
		} );

		worker.add( new StubDocumentReferenceProvider( "neverMatching" ), document -> {
			/*
			 * This should not match, be it on the nested or the flattened object.
			 * For first-level nesting tests, it's because of the integer field.
			 * For second-level nesting tests, it's because there is no nested object matching condition 1.
			 */
			for ( ObjectAccessors accessors :
					Arrays.asList( indexAccessors.flattenedObject, indexAccessors.nestedObject ) ) {
				SecondLevelObjectAccessors secondLevelAccessors = accessors.nestedObject;

				DocumentElement object = accessors.self.add( document );
				accessors.integer.write( object, NON_MATCHING_INTEGER );
				DocumentElement secondLevelObject = secondLevelAccessors.self.add( object );
				secondLevelAccessors.field1.write( secondLevelObject, NON_MATCHING_SECOND_LEVEL_CONDITION1_FIELD1 );

				object = accessors.self.add( document );
				accessors.integer.write( object, NON_MATCHING_INTEGER );
				accessors.string.write( object, NON_MATCHING_STRING );
				secondLevelObject = secondLevelAccessors.self.add( object );
				secondLevelAccessors.field1.write( secondLevelObject, NON_MATCHING_SECOND_LEVEL_CONDITION1_FIELD1 );
				secondLevelAccessors.field2.write( secondLevelObject, NON_MATCHING_SECOND_LEVEL_CONDITION1_FIELD2 );
				secondLevelAccessors.self.addMissing( object );
				secondLevelObject = secondLevelAccessors.self.add( object );
				secondLevelAccessors.field1.write( secondLevelObject, NON_MATCHING_SECOND_LEVEL_CONDITION2_FIELD1 );
				secondLevelAccessors.field2.write( secondLevelObject, NON_MATCHING_SECOND_LEVEL_CONDITION2_FIELD2 );

				object = accessors.self.add( document );
				accessors.string.write( object, MATCHING_STRING );
				secondLevelObject = secondLevelAccessors.self.add( object );
				secondLevelAccessors.field1.write( secondLevelObject, NON_MATCHING_SECOND_LEVEL_CONDITION1_FIELD1 );
				secondLevelAccessors.field2.write( secondLevelObject, MATCHING_SECOND_LEVEL_CONDITION1_FIELD2 );
				secondLevelObject = secondLevelAccessors.self.add( object );
				secondLevelAccessors.field1.write( secondLevelObject, MATCHING_SECOND_LEVEL_CONDITION2_FIELD1 );
				secondLevelAccessors.field2.write( secondLevelObject, MATCHING_SECOND_LEVEL_CONDITION2_FIELD2 );

				object = accessors.self.add( document );
				accessors.string_analyzed.write( object, MATCHING_STRING_ANALYZED );
				secondLevelObject = secondLevelAccessors.self.add( object );
				secondLevelAccessors.field1.write( secondLevelObject, MATCHING_SECOND_LEVEL_CONDITION1_FIELD1 );
				secondLevelAccessors.field2.write( secondLevelObject, NON_MATCHING_SECOND_LEVEL_CONDITION1_FIELD2 );
				secondLevelObject = secondLevelAccessors.self.add( object );
				secondLevelAccessors.field1.write( secondLevelObject, MATCHING_SECOND_LEVEL_CONDITION2_FIELD1 );
				secondLevelAccessors.field2.write( secondLevelObject, MATCHING_SECOND_LEVEL_CONDITION2_FIELD2 );

				object = accessors.self.add( document );
				accessors.localDate.write( object, MATCHING_LOCAL_DATE );
				accessors.string_analyzed.write( object, NON_MATCHING_STRING_ANALYZED );
				secondLevelObject = secondLevelAccessors.self.add( object );
				secondLevelAccessors.field1.write( secondLevelObject, MATCHING_SECOND_LEVEL_CONDITION1_FIELD1 );
				secondLevelAccessors.field2.write( secondLevelObject, NON_MATCHING_SECOND_LEVEL_CONDITION1_FIELD2 );
			}
		} );

		worker.add( new StubDocumentReferenceProvider( "empty" ), document -> { } );

		worker.execute().join();

		// Check that all documents are searchable
		IndexSearchTarget searchTarget = indexManager.createSearchTarget().build();
		SearchQuery<DocumentReference> query = searchTarget.query( sessionContext )
				.asReferences()
				.predicate().all().end()
				.build();
		assertThat( query.execute() )
				.hasDocumentHitsAnyOrder(
						indexName,
						EXPECTED_NESTED_MATCH_ID, EXPECTED_NON_NESTED_MATCH_ID, "neverMatching", "empty"
				);
	}

	private static class IndexAccessors {
		final ObjectAccessors flattenedObject;
		final ObjectAccessors nestedObject;

		IndexAccessors(IndexSchemaElement root) {
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
		final IndexFieldAccessor<String> string_analyzed;
		final IndexFieldAccessor<LocalDate> localDate;
		final SecondLevelObjectAccessors nestedObject;

		ObjectAccessors(IndexSchemaObjectField objectField) {
			self = objectField.createAccessor();
			integer = objectField.field( "integer" ).asInteger().createAccessor();
			string = objectField.field( "string" ).asString().createAccessor();
			string_analyzed = objectField.field( "string_analyzed" ).asString()
					.analyzer( "default" ).createAccessor();
			localDate = objectField.field( "localDate" ).asLocalDate().createAccessor();
			IndexSchemaObjectField nestedObjectField = objectField.objectField(
					"nestedObject",
					ObjectFieldStorage.NESTED
			);
			nestedObject = new SecondLevelObjectAccessors( nestedObjectField );
		}
	}

	private static class SecondLevelObjectAccessors {
		final IndexObjectFieldAccessor self;
		final IndexFieldAccessor<String> field1;
		final IndexFieldAccessor<String> field2;

		SecondLevelObjectAccessors(IndexSchemaObjectField objectField) {
			self = objectField.createAccessor();
			field1 = objectField.field( "field1" ).asString().createAccessor();
			field2 = objectField.field( "field2" ).asString().createAccessor();
		}
	}

}
