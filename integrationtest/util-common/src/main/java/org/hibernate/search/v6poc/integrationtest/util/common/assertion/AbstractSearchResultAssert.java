/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.integrationtest.util.common.assertion;

import org.hibernate.search.v6poc.search.SearchResult;

import org.fest.assertions.Assertions;

public abstract class AbstractSearchResultAssert<S, T> {

	final SearchResult<T> actual;

	AbstractSearchResultAssert(SearchResult<T> actual) {
		this.actual = actual;
	}

	@SafeVarargs
	public final S hasHits(T... hits) {
		Assertions.assertThat( actual.getHits() )
				.as( "Hits of " + actual )
				.containsExactly( (Object[]) hits );
		return thisAsSelfType();
	}

	public S hasNoHits() {
		Assertions.assertThat( actual.getHits() )
				.as( "Hits of " + actual )
				.isEmpty();
		return thisAsSelfType();
	}

	public S hasHitCount(long expected) {
		Assertions.assertThat( actual.getHitCount() )
				.as( "HitCount of " + actual )
				.isEqualTo( expected );
		return thisAsSelfType();
	}

	protected final S thisAsSelfType() {
		return (S) this;
	}

}
