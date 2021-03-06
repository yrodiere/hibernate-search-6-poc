/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.backend.elasticsearch.document.model.dsl.impl;

import org.hibernate.search.v6poc.backend.document.IndexObjectFieldAccessor;
import org.hibernate.search.v6poc.backend.document.model.dsl.ObjectFieldStorage;
import org.hibernate.search.v6poc.backend.document.model.dsl.spi.IndexSchemaObjectFieldNodeBuilder;
import org.hibernate.search.v6poc.backend.document.spi.DeferredInitializationIndexObjectFieldAccessor;
import org.hibernate.search.v6poc.backend.elasticsearch.document.impl.ElasticsearchIndexObjectFieldAccessor;
import org.hibernate.search.v6poc.backend.elasticsearch.document.model.impl.ElasticsearchIndexSchemaNodeCollector;
import org.hibernate.search.v6poc.backend.elasticsearch.document.model.impl.ElasticsearchIndexSchemaNodeContributor;
import org.hibernate.search.v6poc.backend.elasticsearch.document.model.impl.ElasticsearchIndexSchemaObjectNode;
import org.hibernate.search.v6poc.backend.elasticsearch.document.model.impl.esnative.DataType;
import org.hibernate.search.v6poc.backend.elasticsearch.document.model.impl.esnative.DynamicType;
import org.hibernate.search.v6poc.backend.elasticsearch.document.model.impl.esnative.PropertyMapping;
import org.hibernate.search.v6poc.backend.elasticsearch.gson.impl.JsonAccessor;
import org.hibernate.search.v6poc.backend.elasticsearch.gson.impl.JsonObjectAccessor;
import org.hibernate.search.v6poc.backend.elasticsearch.util.impl.ElasticsearchFields;
import org.hibernate.search.v6poc.util.EventContext;
import org.hibernate.search.v6poc.logging.spi.EventContexts;

class ElasticsearchIndexSchemaObjectFieldNodeBuilder extends AbstractElasticsearchIndexSchemaObjectNodeBuilder
		implements IndexSchemaObjectFieldNodeBuilder, ElasticsearchIndexSchemaNodeContributor<PropertyMapping> {

	private final AbstractElasticsearchIndexSchemaObjectNodeBuilder parent;
	private final String absoluteFieldPath;
	private final String relativeFieldName;
	private final ObjectFieldStorage storage;

	private final DeferredInitializationIndexObjectFieldAccessor accessor =
			new DeferredInitializationIndexObjectFieldAccessor();

	ElasticsearchIndexSchemaObjectFieldNodeBuilder(AbstractElasticsearchIndexSchemaObjectNodeBuilder parent,
			String relativeFieldName, ObjectFieldStorage storage) {
		this.parent = parent;
		String parentAbsolutePath = parent.getAbsolutePath();
		this.absoluteFieldPath = parentAbsolutePath == null ? relativeFieldName
				: ElasticsearchFields.compose( parentAbsolutePath, relativeFieldName );
		this.relativeFieldName = relativeFieldName;
		this.storage = storage;
	}

	@Override
	public EventContext getEventContext() {
		return getRootNodeBuilder().getIndexEventContext()
				.append( EventContexts.fromIndexFieldAbsolutePath( absoluteFieldPath ) );
	}

	@Override
	public IndexObjectFieldAccessor getAccessor() {
		return accessor;
	}

	@Override
	public PropertyMapping contribute(
			ElasticsearchIndexSchemaNodeCollector collector,
			ElasticsearchIndexSchemaObjectNode parentNode) {
		ElasticsearchIndexSchemaObjectNode node =
				new ElasticsearchIndexSchemaObjectNode( parentNode, absoluteFieldPath, storage );
		collector.collect( absoluteFieldPath, node );

		JsonObjectAccessor jsonAccessor = JsonAccessor.root().property( relativeFieldName ).asObject();

		accessor.initialize( new ElasticsearchIndexObjectFieldAccessor( jsonAccessor, node ) );

		PropertyMapping mapping = new PropertyMapping();
		DataType dataType = DataType.OBJECT;
		switch ( storage ) {
			case DEFAULT:
				break;
			case FLATTENED:
				dataType = DataType.OBJECT;
				break;
			case NESTED:
				dataType = DataType.NESTED;
				break;
		}
		mapping.setType( dataType );

		// TODO allow to configure this, both at index level (configuration properties) and at field level (ElasticsearchExtension)
		mapping.setDynamic( DynamicType.STRICT );

		contributeChildren( mapping, node, collector );

		return mapping;
	}

	@Override
	ElasticsearchIndexSchemaRootNodeBuilder getRootNodeBuilder() {
		return parent.getRootNodeBuilder();
	}

	@Override
	String getAbsolutePath() {
		return absoluteFieldPath;
	}
}
