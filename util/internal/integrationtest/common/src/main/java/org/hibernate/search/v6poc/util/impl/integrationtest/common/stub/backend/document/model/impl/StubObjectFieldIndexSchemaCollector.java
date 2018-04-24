/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.util.impl.integrationtest.common.stub.backend.document.model.impl;

import org.hibernate.search.v6poc.backend.document.model.dsl.spi.IndexSchemaNestingContext;
import org.hibernate.search.v6poc.backend.document.model.dsl.spi.IndexSchemaObjectField;
import org.hibernate.search.v6poc.backend.document.model.dsl.spi.ObjectFieldIndexSchemaCollector;
import org.hibernate.search.v6poc.util.impl.integrationtest.common.stub.backend.document.model.StubIndexSchemaNode;

class StubObjectFieldIndexSchemaCollector extends StubIndexSchemaCollector
		implements ObjectFieldIndexSchemaCollector {

	StubObjectFieldIndexSchemaCollector(StubIndexSchemaNode.Builder builder) {
		super( builder );
	}

	@Override
	public IndexSchemaObjectField withContext(IndexSchemaNestingContext context) {
		return new StubIndexSchemaObjectField( builder, context, true );
	}
}
