/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.integrationtest.util.common.stub.backend.index.impl;

import org.hibernate.search.v6poc.backend.document.model.spi.IndexSchemaCollector;
import org.hibernate.search.v6poc.backend.index.spi.IndexManager;
import org.hibernate.search.v6poc.backend.index.spi.IndexManagerBuilder;
import org.hibernate.search.v6poc.integrationtest.util.common.stub.backend.document.impl.StubDocumentElement;
import org.hibernate.search.v6poc.integrationtest.util.common.stub.backend.document.model.impl.StubRootIndexSchemaCollector;

public class StubIndexManagerBuilder implements IndexManagerBuilder<StubDocumentElement> {

	private final StubBackend backend;
	private final String name;
	private final StubRootIndexSchemaCollector schemaCollector = new StubRootIndexSchemaCollector();

	public StubIndexManagerBuilder(StubBackend backend, String name) {
		this.backend = backend;
		this.name = name;
	}

	@Override
	public IndexSchemaCollector getSchemaCollector() {
		return schemaCollector;
	}

	@Override
	public IndexManager<StubDocumentElement> build() {
		return new StubIndexManager( backend, name, schemaCollector.build() );
	}
}
