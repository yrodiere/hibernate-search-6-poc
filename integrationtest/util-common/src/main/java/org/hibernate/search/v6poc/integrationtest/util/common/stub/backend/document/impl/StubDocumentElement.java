/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.integrationtest.util.common.stub.backend.document.impl;

import org.hibernate.search.v6poc.backend.document.DocumentElement;
import org.hibernate.search.v6poc.integrationtest.util.common.stub.backend.document.StubDocumentNode;

public class StubDocumentElement implements DocumentElement {

	private final StubDocumentNode.Builder builder;

	public StubDocumentElement(StubDocumentNode.Builder builder) {
		this.builder = builder;
	}

	public void putValue(String relativeName, Object value) {
		builder.field( relativeName, value );
	}

	public StubDocumentElement putChild(String relativeName) {
		StubDocumentNode.Builder childBuilder = StubDocumentNode.object();
		builder.child( relativeName, childBuilder );
		return new StubDocumentElement( childBuilder );
	}

	public void putMissingChild(String relativeName) {
		builder.missingObjectField( relativeName );
	}
}
