/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.integrationtest.backend.tck.search.predicate;

import static org.hibernate.search.v6poc.util.impl.integrationtest.common.assertion.DocumentReferencesSearchResultAssert.assertThat;
import static org.hibernate.search.v6poc.util.impl.integrationtest.common.stub.mapper.StubMapperUtils.referenceProvider;

import org.hibernate.search.v6poc.backend.document.DocumentElement;
import org.hibernate.search.v6poc.backend.document.IndexFieldAccessor;
import org.hibernate.search.v6poc.backend.document.IndexObjectFieldAccessor;
import org.hibernate.search.v6poc.backend.document.model.dsl.IndexSchemaElement;
import org.hibernate.search.v6poc.backend.document.model.dsl.IndexSchemaObjectField;
import org.hibernate.search.v6poc.backend.document.model.dsl.ObjectFieldStorage;
import org.hibernate.search.v6poc.backend.index.spi.ChangesetIndexWorker;
import org.hibernate.search.v6poc.backend.index.spi.IndexManager;
import org.hibernate.search.v6poc.backend.index.spi.IndexSearchTarget;
import org.hibernate.search.v6poc.integrationtest.backend.tck.util.rule.SearchSetupHelper;
import org.hibernate.search.v6poc.search.DocumentReference;
import org.hibernate.search.v6poc.search.SearchPredicate;
import org.hibernate.search.v6poc.search.SearchQuery;
import org.hibernate.search.v6poc.util.impl.integrationtest.common.stub.StubSessionContext;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class NestedSearchPredicateIT {

	private static final String INDEX_NAME = "IndexName";

	private static final String DOCUMENT_1 = "nestedQueryShouldMatchId";
	private static final String DOCUMENT_2 = "nonNestedQueryShouldMatchId";

	private static final String MATCHING_STRING = "matchingWord";
	private static final String MATCHING_SECOND_LEVEL_CONDITION1_FIELD1 = "firstMatchingWord";
	private static final String MATCHING_SECOND_LEVEL_CONDITION1_FIELD2 = "firstMatchingWord";
	private static final String MATCHING_SECOND_LEVEL_CONDITION2_FIELD1 = "secondMatchingWord";
	private static final String MATCHING_SECOND_LEVEL_CONDITION2_FIELD2 = "secondMatchingWord";

	private static final String NON_MATCHING_STRING = "nonMatchingWord";
	private static final String NON_MATCHING_SECOND_LEVEL_CONDITION1_FIELD1 = "firstNonMatchingWord";
	private static final String NON_MATCHING_SECOND_LEVEL_CONDITION1_FIELD2 = "firstNonMatchingWord";
	private static final String NON_MATCHING_SECOND_LEVEL_CONDITION2_FIELD1 = "secondNonMatchingWord";
	private static final String NON_MATCHING_SECOND_LEVEL_CONDITION2_FIELD2 = "secondNonMatchingWord";

	@Rule
	public SearchSetupHelper setupHelper = new SearchSetupHelper();

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	private IndexAccessors indexAccessors;
	private IndexManager<?> indexManager;

	private StubSessionContext sessionContext = new StubSessionContext();

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

	@Test
	public void search_nestedOnTwoLevels() {
		IndexSearchTarget searchTarget = indexManager.createSearchTarget().build();

		SearchQuery<DocumentReference> query = searchTarget.query( sessionContext )
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
		assertThat( query )
				.hasReferencesHitsAnyOrder( INDEX_NAME, DOCUMENT_1 )
				.hasHitCount( 1 );
	}

	@Test
	public void search_nestedOnTwoLevels_onlySecondLevel() {
		IndexSearchTarget searchTarget = indexManager.createSearchTarget().build();

		SearchQuery<DocumentReference> query = searchTarget.query( sessionContext )
				.asReferences()
				.predicate().bool( b -> {
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
		assertThat( query )
				.hasReferencesHitsAnyOrder( INDEX_NAME, DOCUMENT_1, DOCUMENT_2 )
				.hasHitCount( 2 );
	}

	@Test
	public void search_nestedOnTwoLevels_conditionOnFirstLevel() {
		IndexSearchTarget searchTarget = indexManager.createSearchTarget().build();

		SearchQuery<DocumentReference> query = searchTarget.query( sessionContext )
				.asReferences()
				.predicate().nested().onObjectField( "nestedObject" ).bool( b -> {
					b.must().match().onField( "nestedObject.string" ).matching( MATCHING_STRING ).end();
					// This is referred to as "condition 2" in the data initialization method
					b.must().nested().onObjectField( "nestedObject.nestedObject" ).bool( b2 -> {
						b2.must().match().onField( "nestedObject.nestedObject.field1" )
								.matching( MATCHING_SECOND_LEVEL_CONDITION2_FIELD1 );
						b2.must().match().onField( "nestedObject.nestedObject.field2" )
								.matching( MATCHING_SECOND_LEVEL_CONDITION2_FIELD2 );
					} );
				} )
				.build();
		assertThat( query )
				.hasReferencesHitsAnyOrder( INDEX_NAME, DOCUMENT_2 )
				.hasHitCount( 1 );
	}

	@Test
	public void search_nestedOnTwoLevels_separatePredicates() {
		IndexSearchTarget searchTarget = indexManager.createSearchTarget().build();

		SearchPredicate predicate1 = searchTarget.predicate().nested().onObjectField( "nestedObject.nestedObject" ).bool( b2 -> {
			b2.must().match().onField( "nestedObject.nestedObject.field1" )
					.matching( MATCHING_SECOND_LEVEL_CONDITION1_FIELD1 );
			b2.must().match().onField( "nestedObject.nestedObject.field2" )
					.matching( MATCHING_SECOND_LEVEL_CONDITION1_FIELD2 );
		} );

		SearchPredicate predicate2 = searchTarget.predicate().nested().onObjectField( "nestedObject.nestedObject" ).bool( b2 -> {
			b2.must().match().onField( "nestedObject.nestedObject.field1" )
					.matching( MATCHING_SECOND_LEVEL_CONDITION2_FIELD1 );
			b2.must().match().onField( "nestedObject.nestedObject.field2" )
					.matching( MATCHING_SECOND_LEVEL_CONDITION2_FIELD2 );
		} );

		SearchQuery<DocumentReference> query = searchTarget.query( sessionContext )
				.asReferences()
				.predicate().nested().onObjectField( "nestedObject" ).bool( b -> {
					// This is referred to as "condition 1" in the data initialization method
					b.must( predicate1 );
					// This is referred to as "condition 2" in the data initialization method
					b.must( predicate2 );
				} )
				.build();
		assertThat( query )
				.hasReferencesHitsAnyOrder( INDEX_NAME, DOCUMENT_1 )
				.hasHitCount( 1 );
	}

	private void initData() {
		ChangesetIndexWorker<? extends DocumentElement> worker = indexManager.createWorker( sessionContext );
		worker.add( referenceProvider( DOCUMENT_1 ), document -> {
			ObjectAccessors accessors;
			SecondLevelObjectAccessors secondLevelAccessors;
			DocumentElement object;
			DocumentElement secondLevelObject;

			accessors = indexAccessors.nestedObject;
			secondLevelAccessors = accessors.nestedObject;

			object = accessors.self.add( document );
			secondLevelAccessors.self.addMissing( object );
			secondLevelObject = secondLevelAccessors.self.add( object );
			secondLevelAccessors.field1.write( secondLevelObject, MATCHING_SECOND_LEVEL_CONDITION2_FIELD1 );
			secondLevelAccessors.field2.write( secondLevelObject, MATCHING_SECOND_LEVEL_CONDITION2_FIELD2 );
			secondLevelAccessors.field2.write( secondLevelObject, NON_MATCHING_SECOND_LEVEL_CONDITION1_FIELD2 );

			// This object will trigger the match; others should not
			object = accessors.self.add( document );
			accessors.string.write( object, NON_MATCHING_STRING );
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
			secondLevelAccessors.self.addMissing( object );
		} );

		worker.add( referenceProvider( DOCUMENT_2 ), document -> {
			ObjectAccessors accessors = indexAccessors.nestedObject;
			DocumentElement object = accessors.self.add( document );
			SecondLevelObjectAccessors secondLevelAccessors = accessors.nestedObject;
			DocumentElement secondLevelObject = secondLevelAccessors.self.add( object );
			secondLevelAccessors.field1.write( secondLevelObject, NON_MATCHING_SECOND_LEVEL_CONDITION1_FIELD1 );

			object = accessors.self.add( document );
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
			accessors.string.write( object, MATCHING_STRING );
			secondLevelObject = secondLevelAccessors.self.add( object ); // This matches nested condition 2
			secondLevelAccessors.field1.write( secondLevelObject, MATCHING_SECOND_LEVEL_CONDITION2_FIELD1 );
			secondLevelAccessors.field2.write( secondLevelObject, MATCHING_SECOND_LEVEL_CONDITION2_FIELD2 );

			object = accessors.self.add( document );
		} );

		worker.add( referenceProvider( "neverMatching" ), document -> {
			ObjectAccessors accessors = indexAccessors.nestedObject;
			SecondLevelObjectAccessors secondLevelAccessors = accessors.nestedObject;

			DocumentElement object = accessors.self.add( document );
			DocumentElement secondLevelObject = secondLevelAccessors.self.add( object );
			secondLevelAccessors.field1.write( secondLevelObject, NON_MATCHING_SECOND_LEVEL_CONDITION1_FIELD1 );

			object = accessors.self.add( document );
			accessors.string.write( object, NON_MATCHING_STRING );
			secondLevelObject = secondLevelAccessors.self.add( object );
			secondLevelAccessors.field1.write( secondLevelObject, NON_MATCHING_SECOND_LEVEL_CONDITION1_FIELD1 );
			secondLevelAccessors.field2.write( secondLevelObject, NON_MATCHING_SECOND_LEVEL_CONDITION1_FIELD2 );
			secondLevelAccessors.self.addMissing( object );
			secondLevelObject = secondLevelAccessors.self.add( object );
			secondLevelAccessors.field1.write( secondLevelObject, NON_MATCHING_SECOND_LEVEL_CONDITION2_FIELD1 );
			secondLevelAccessors.field2.write( secondLevelObject, NON_MATCHING_SECOND_LEVEL_CONDITION2_FIELD2 );

			object = accessors.self.add( document );
			accessors.string.write( object, NON_MATCHING_STRING );
			secondLevelObject = secondLevelAccessors.self.add( object );
			secondLevelAccessors.field1.write( secondLevelObject, NON_MATCHING_SECOND_LEVEL_CONDITION1_FIELD1 );
			secondLevelAccessors.field2.write( secondLevelObject, MATCHING_SECOND_LEVEL_CONDITION1_FIELD2 );
			secondLevelObject = secondLevelAccessors.self.add( object );
			secondLevelAccessors.field1.write( secondLevelObject, MATCHING_SECOND_LEVEL_CONDITION2_FIELD1 );
			secondLevelAccessors.field2.write( secondLevelObject, MATCHING_SECOND_LEVEL_CONDITION2_FIELD2 );

			object = accessors.self.add( document );
			secondLevelObject = secondLevelAccessors.self.add( object );
			secondLevelAccessors.field1.write( secondLevelObject, MATCHING_SECOND_LEVEL_CONDITION1_FIELD1 );
			secondLevelAccessors.field2.write( secondLevelObject, NON_MATCHING_SECOND_LEVEL_CONDITION1_FIELD2 );
			secondLevelObject = secondLevelAccessors.self.add( object );
			secondLevelAccessors.field1.write( secondLevelObject, MATCHING_SECOND_LEVEL_CONDITION2_FIELD1 );
			secondLevelAccessors.field2.write( secondLevelObject, MATCHING_SECOND_LEVEL_CONDITION2_FIELD2 );

			object = accessors.self.add( document );
			secondLevelObject = secondLevelAccessors.self.add( object );
			secondLevelAccessors.field1.write( secondLevelObject, MATCHING_SECOND_LEVEL_CONDITION1_FIELD1 );
			secondLevelAccessors.field2.write( secondLevelObject, NON_MATCHING_SECOND_LEVEL_CONDITION1_FIELD2 );
		} );

		worker.add( referenceProvider( "empty" ), document -> { } );

		worker.execute().join();

		// Check that all documents are searchable
		IndexSearchTarget searchTarget = indexManager.createSearchTarget().build();
		SearchQuery<DocumentReference> query = searchTarget.query( sessionContext )
				.asReferences()
				.predicate().matchAll().end()
				.build();
		assertThat( query )
				.hasReferencesHitsAnyOrder(
						INDEX_NAME,
						DOCUMENT_1, DOCUMENT_2, "neverMatching", "empty"
				);
	}

	private static class IndexAccessors {
		final ObjectAccessors nestedObject;

		IndexAccessors(IndexSchemaElement root) {
			IndexSchemaObjectField nestedObjectField = root.objectField( "nestedObject", ObjectFieldStorage.NESTED );
			nestedObject = new ObjectAccessors( nestedObjectField );
		}
	}

	private static class ObjectAccessors {
		final IndexObjectFieldAccessor self;
		final IndexFieldAccessor<String> string;
		final SecondLevelObjectAccessors nestedObject;

		ObjectAccessors(IndexSchemaObjectField objectField) {
			self = objectField.createAccessor();
			string = objectField.field( "string" ).asString().createAccessor();
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
