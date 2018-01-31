/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.backend.elasticsearch.document.model.impl;

import org.hibernate.search.v6poc.backend.elasticsearch.logging.impl.Log;
import org.hibernate.search.v6poc.util.spi.LoggerFactory;

/**
 * @author Yoann Rodiere
 */
public class ElasticsearchIndexSchemaFieldNode implements ElasticsearchIndexSchemaNode {

	private static final Log log = LoggerFactory.make( Log.class );

	private final ElasticsearchIndexSchemaObjectNode parent;

	private final ElasticsearchFieldFormatter formatter;

	public ElasticsearchIndexSchemaFieldNode(ElasticsearchIndexSchemaObjectNode parent, ElasticsearchFieldFormatter formatter) {
		this.parent = parent;
		this.formatter = formatter;
	}

	public ElasticsearchIndexSchemaObjectNode getParent() {
		return parent;
	}

	@Override
	public ElasticsearchFieldFormatter getFormatter() {
		return formatter;
	}

	@Override
	public void checkSuitableForNestedQuery() {
		throw log.nonObjectFieldForNestedPredicate();
	}
}
