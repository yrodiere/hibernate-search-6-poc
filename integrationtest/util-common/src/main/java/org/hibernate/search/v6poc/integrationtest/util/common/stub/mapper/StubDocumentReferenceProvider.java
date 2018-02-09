/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.integrationtest.util.common.stub.mapper;

import org.hibernate.search.v6poc.backend.index.spi.DocumentReferenceProvider;

public class StubDocumentReferenceProvider implements DocumentReferenceProvider {

	private final String identifier;
	private final String routingKey;

	public StubDocumentReferenceProvider(String identifier) {
		this( identifier, null );
	}

	public StubDocumentReferenceProvider(String identifier, String routingKey) {
		this.identifier = identifier;
		this.routingKey = routingKey;
	}

	@Override
	public String getIdentifier() {
		return identifier;
	}

	@Override
	public String getRoutingKey() {
		return routingKey;
	}
}
