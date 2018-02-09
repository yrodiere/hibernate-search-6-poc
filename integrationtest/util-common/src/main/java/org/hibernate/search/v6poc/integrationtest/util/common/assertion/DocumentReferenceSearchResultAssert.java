/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.integrationtest.util.common.assertion;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.hibernate.search.v6poc.search.DocumentReference;
import org.hibernate.search.v6poc.search.SearchResult;

import org.fest.assertions.Assertions;

public class DocumentReferenceSearchResultAssert<T extends DocumentReference>
		extends AbstractSearchResultAssert<DocumentReferenceSearchResultAssert<T>, T> {

	public static <T extends DocumentReference> DocumentReferenceSearchResultAssert<T> assertThat(
			SearchResult<T> searchResult) {
		return new DocumentReferenceSearchResultAssert<>( searchResult );
	}

	private DocumentReferenceSearchResultAssert(SearchResult<T> searchResult) {
		super( searchResult );
	}

	public DocumentHitsContext documentHits() {
		return new DocumentHitsContext();
	}

	public DocumentReferenceSearchResultAssert<T> hasDocumentHitsExactOrder(String indexName, String... ids) {
		DocumentHitsContext ctx = documentHits();
		for ( String id : ids ) {
			ctx = ctx.doc( indexName, id );
		}
		return ctx.matchesAnyOrder();
	}

	public DocumentReferenceSearchResultAssert<T> hasDocumentHitsAnyOrder(String indexName, String... ids) {
		DocumentHitsContext ctx = documentHits();
		for ( String id : ids ) {
			ctx = ctx.doc( indexName, id );
		}
		return ctx.matchesAnyOrder();
	}

	public class DocumentHitsContext {

		private final List<DocumentReferenceImpl> expectedHits = new ArrayList<>();

		private DocumentHitsContext() {
		}

		public DocumentHitsContext doc(String indexName, String id) {
			expectedHits.add( new DocumentReferenceImpl( indexName, id ) );
			return this;
		}

		public DocumentReferenceSearchResultAssert<T> matchesExactOrder() {
			Assertions.assertThat( getNormalizedHits() )
					.as( "Hits of " + actual )
					.containsExactly( expectedHits.toArray() );
			return DocumentReferenceSearchResultAssert.this;
		}

		public DocumentReferenceSearchResultAssert<T> matchesAnyOrder() {
			Assertions.assertThat( getNormalizedHits() )
					.as( "Hits of " + actual )
					.containsOnly( expectedHits.toArray() );
			return DocumentReferenceSearchResultAssert.this;
		}

		private List<DocumentReferenceImpl> getNormalizedHits() {
			return actual.getHits().stream()
					.map( docRef -> new DocumentReferenceImpl( docRef.getIndexName(), docRef.getId() ) )
					.collect( Collectors.toList() );
		}

	}

	private static class DocumentReferenceImpl implements DocumentReference {
		private final String indexName;
		private final String id;

		DocumentReferenceImpl(String indexName, String id) {
			this.indexName = indexName;
			this.id = id;
		}

		@Override
		public String getIndexName() {
			return indexName;
		}

		@Override
		public String getId() {
			return id;
		}

		@Override
		public boolean equals(Object obj) {
			if ( obj == null || !DocumentReferenceImpl.class.equals( obj.getClass() ) ) {
				return false;
			}
			DocumentReferenceImpl other = (DocumentReferenceImpl) obj;
			return Objects.equals( indexName, other.indexName )
					&& Objects.equals( id, other.id );
		}

		@Override
		public int hashCode() {
			return Objects.hash( indexName, id );
		}

		@Override
		public String toString() {
			return "DocRef:" + indexName + "/" + id;
		}
	}
}
