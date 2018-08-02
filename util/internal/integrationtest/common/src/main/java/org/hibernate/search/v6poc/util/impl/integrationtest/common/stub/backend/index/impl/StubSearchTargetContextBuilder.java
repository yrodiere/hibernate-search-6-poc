/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.util.impl.integrationtest.common.stub.backend.index.impl;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.search.v6poc.backend.index.spi.SearchTargetContextBuilder;
import org.hibernate.search.v6poc.entity.mapping.spi.MappedIndexSearchTarget;
import org.hibernate.search.v6poc.entity.mapping.spi.MappedIndexSearchTargetBuilder;
import org.hibernate.search.v6poc.search.dsl.spi.SearchTargetContext;

class StubSearchTargetContextBuilder implements SearchTargetContextBuilder {

	private final StubBackend backend;
	private final List<String> indexNames = new ArrayList<>();

	StubSearchTargetContextBuilder(StubBackend backend, String indexName) {
		this.backend = backend;
		this.indexNames.add( indexName );
	}

	void add(StubBackend backend, String indexName) {
		if ( !this.backend.equals( backend ) ) {
			throw new IllegalStateException( "Attempt to run a search query across two distinct backends; this is not possible." );
		}
		indexNames.add( indexName );
	}

	@Override
	public SearchTargetContext<?> build() {
		return new StubIndexSearchTargetContext( backend, indexNames );
	}
}
