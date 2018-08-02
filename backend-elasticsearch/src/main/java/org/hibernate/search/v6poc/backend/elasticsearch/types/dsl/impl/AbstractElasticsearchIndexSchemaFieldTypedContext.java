/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.backend.elasticsearch.types.dsl.impl;

import org.hibernate.search.v6poc.backend.document.converter.FromIndexFieldValueConverter;
import org.hibernate.search.v6poc.backend.document.converter.ToIndexFieldValueConverter;
import org.hibernate.search.v6poc.backend.document.model.dsl.spi.IndexSchemaContext;
import org.hibernate.search.v6poc.backend.document.IndexFieldAccessor;
import org.hibernate.search.v6poc.backend.document.spi.IndexSchemaFieldDefinitionHelper;
import org.hibernate.search.v6poc.backend.elasticsearch.document.model.dsl.ElasticsearchIndexSchemaFieldTypedContext;
import org.hibernate.search.v6poc.backend.elasticsearch.document.model.impl.ElasticsearchIndexSchemaNodeCollector;
import org.hibernate.search.v6poc.backend.elasticsearch.document.model.impl.ElasticsearchIndexSchemaNodeContributor;
import org.hibernate.search.v6poc.backend.elasticsearch.document.model.impl.ElasticsearchIndexSchemaObjectNode;
import org.hibernate.search.v6poc.backend.elasticsearch.document.model.impl.esnative.PropertyMapping;

/**
 * @author Yoann Rodiere
 */
public abstract class AbstractElasticsearchIndexSchemaFieldTypedContext<F>
		implements ElasticsearchIndexSchemaFieldTypedContext<F>,
		ElasticsearchIndexSchemaNodeContributor<PropertyMapping> {

	private final IndexSchemaFieldDefinitionHelper<F> helper;

	AbstractElasticsearchIndexSchemaFieldTypedContext(IndexSchemaContext schemaContext, Class<F> fieldType) {
		this.helper = new IndexSchemaFieldDefinitionHelper<>( schemaContext, fieldType );
	}

	@Override
	public IndexFieldAccessor<F> createAccessor() {
		return helper.createAccessor();
	}

	@Override
	public <V, U> IndexFieldAccessor<V> createAccessor(ToIndexFieldValueConverter<V, ? extends F> toIndexConverter,
			FromIndexFieldValueConverter<? super F, U> fromIndexConverter) {
		return helper.createAccessor( toIndexConverter, fromIndexConverter );
	}

	@Override
	public PropertyMapping contribute(ElasticsearchIndexSchemaNodeCollector collector,
			ElasticsearchIndexSchemaObjectNode parentNode) {
		return contribute( helper, collector, parentNode );
	}

	protected abstract PropertyMapping contribute(IndexSchemaFieldDefinitionHelper<F> helper,
			ElasticsearchIndexSchemaNodeCollector collector,
			ElasticsearchIndexSchemaObjectNode parentNode);

	protected final IndexSchemaContext getSchemaContext() {
		return helper.getSchemaContext();
	}

}
