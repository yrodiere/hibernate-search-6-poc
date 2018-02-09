/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.integrationtest.util.common.assertion;

import org.hibernate.search.v6poc.search.SearchResult;

public class SearchResultAssert<T> extends AbstractSearchResultAssert<SearchResultAssert<T>, T> {

	public static <T> SearchResultAssert<T> assertThat(SearchResult<T> searchResult) {
		return new SearchResultAssert<>( searchResult );
	}

	SearchResultAssert(SearchResult<T> searchResult) {
		super( searchResult );
	}


}
