/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.util.impl.integrationtest.common.stub.backend.index.impl;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.search.v6poc.backend.index.spi.IndexSearchTarget;
import org.hibernate.search.v6poc.backend.index.spi.IndexSearchTargetBuilder;

class StubIndexSearchTargetBuilder implements IndexSearchTargetBuilder {

	private final StubBackend backend;
	private final List<String> indexNames = new ArrayList<>();

	StubIndexSearchTargetBuilder(StubBackend backend, String indexName) {
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
	public IndexSearchTarget build() {
		return new IndexSearchTarget(
				new StubIndexSearchTargetContext( backend, indexNames )
		);
	}
}
