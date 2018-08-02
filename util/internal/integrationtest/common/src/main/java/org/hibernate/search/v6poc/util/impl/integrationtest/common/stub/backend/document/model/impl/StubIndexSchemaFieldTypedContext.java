/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.util.impl.integrationtest.common.stub.backend.document.model.impl;

import org.hibernate.search.v6poc.backend.document.IndexFieldAccessor;
import org.hibernate.search.v6poc.backend.document.converter.FromIndexFieldValueConverter;
import org.hibernate.search.v6poc.backend.document.model.dsl.IndexSchemaFieldTypedContext;
import org.hibernate.search.v6poc.backend.document.model.dsl.Sortable;
import org.hibernate.search.v6poc.backend.document.model.dsl.Store;
import org.hibernate.search.v6poc.backend.document.converter.ToIndexFieldValueConverter;
import org.hibernate.search.v6poc.backend.document.spi.IndexSchemaFieldDefinitionHelper;
import org.hibernate.search.v6poc.util.impl.integrationtest.common.stub.backend.document.model.StubIndexSchemaNode;

class StubIndexSchemaFieldTypedContext<F> implements IndexSchemaFieldTypedContext<F> {

	private IndexSchemaFieldDefinitionHelper<F> helper;
	private final StubIndexSchemaNode.Builder builder;
	private final boolean included;

	StubIndexSchemaFieldTypedContext(StubIndexSchemaNode.Builder builder, Class<F> inputType, boolean included) {
		this.helper = new IndexSchemaFieldDefinitionHelper<>( builder, inputType );
		this.builder = builder;
		this.included = included;
	}

	@Override
	public IndexSchemaFieldTypedContext<F> analyzer(String analyzerName) {
		builder.analyzerName( analyzerName );
		return this;
	}

	@Override
	public IndexSchemaFieldTypedContext<F> normalizer(String normalizerName) {
		builder.normalizerName( normalizerName );
		return this;
	}

	@Override
	public IndexSchemaFieldTypedContext<F> store(Store store) {
		builder.store( store );
		return this;
	}

	@Override
	public IndexSchemaFieldTypedContext<F> sortable(Sortable sortable) {
		builder.sortable( sortable );
		return this;
	}

	@Override
	public IndexFieldAccessor<F> createAccessor() {
		IndexFieldAccessor<F> accessor = helper.createAccessor();
		initializeAccessor();
		return accessor;
	}

	@Override
	public <V, U> IndexFieldAccessor<V> createAccessor(ToIndexFieldValueConverter<V, ? extends F> toIndexConverter,
			FromIndexFieldValueConverter<? super F, U> fromIndexConverter) {
		IndexFieldAccessor<V> accessor = helper.createAccessor( toIndexConverter, fromIndexConverter );
		initializeAccessor();
		return accessor;
	}

	private void initializeAccessor() {
		if ( included ) {
			helper.initialize( new StubIncludedIndexFieldAccessor<>( builder.getAbsolutePath(), builder.getRelativeName() ) );
		}
		else {
			helper.initialize( new StubExcludedIndexFieldAccessor<>( builder.getAbsolutePath(), builder.getRelativeName() ) );
		}
	}
}
