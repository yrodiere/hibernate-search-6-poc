/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.integrationtest.util.common.stub.backend.index.impl;

import org.hibernate.search.v6poc.backend.index.spi.IndexManagerBuilder;
import org.hibernate.search.v6poc.backend.spi.Backend;
import org.hibernate.search.v6poc.cfg.ConfigurationPropertySource;
import org.hibernate.search.v6poc.engine.spi.BuildContext;
import org.hibernate.search.v6poc.integrationtest.util.common.stub.backend.StubBackendBehavior;
import org.hibernate.search.v6poc.integrationtest.util.common.stub.backend.StubBackendUtils;
import org.hibernate.search.v6poc.integrationtest.util.common.stub.backend.document.impl.StubDocumentElement;

public class StubBackend implements Backend<StubDocumentElement> {

	private final String name;

	StubBackend(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return StubBackend.class.getSimpleName() + "[" + name + "]";
	}

	public StubBackendBehavior getBehavior() {
		return StubBackendBehavior.get( name );
	}

	@Override
	public String normalizeIndexName(String rawIndexName) {
		return StubBackendUtils.normalizeIndexName( rawIndexName );
	}

	@Override
	public IndexManagerBuilder<StubDocumentElement> createIndexManagerBuilder(String name, BuildContext context,
			ConfigurationPropertySource propertySource) {
		return new StubIndexManagerBuilder( this, name );
	}

	@Override
	public void close() {
		// No-op
	}
}
