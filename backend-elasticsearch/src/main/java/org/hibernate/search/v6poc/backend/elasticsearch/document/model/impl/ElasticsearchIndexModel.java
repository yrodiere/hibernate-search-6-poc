/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.backend.elasticsearch.document.model.impl;

import java.util.HashMap;
import java.util.Map;

import org.hibernate.search.v6poc.backend.elasticsearch.document.model.impl.esnative.TypeMapping;

/**
 * @author Yoann Rodiere
 */
public class ElasticsearchIndexModel {

	private final String indexName;
	private final TypeMapping mapping;
	private final Map<String, ElasticsearchIndexSchemaNode> schemaNodes = new HashMap<>();

	public ElasticsearchIndexModel(String indexName, ElasticsearchRootIndexSchemaCollectorImpl collector) {
		this.indexName = indexName;
		this.mapping = collector.contribute( schemaNodes::put );
	}

	public String getIndexName() {
		return indexName;
	}

	public TypeMapping getMapping() {
		return mapping;
	}

	public ElasticsearchIndexSchemaNode getSchemaNode(String absolutePath) {
		return schemaNodes.get( absolutePath );
	}

	@Override
	public String toString() {
		return new StringBuilder( getClass().getSimpleName() )
				.append( "[" )
				.append( "indexName=" ).append( indexName )
				.append( ", mapping=" ).append( mapping )
				.append( "]" )
				.toString();
	}

}
