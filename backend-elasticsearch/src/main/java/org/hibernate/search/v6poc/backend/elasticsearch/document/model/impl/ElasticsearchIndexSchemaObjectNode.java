/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.backend.elasticsearch.document.model.impl;

import org.hibernate.search.v6poc.backend.document.model.ObjectFieldStorage;
import org.hibernate.search.v6poc.backend.elasticsearch.logging.impl.Log;
import org.hibernate.search.v6poc.util.spi.LoggerFactory;

/**
 * @author Yoann Rodiere
 */
public class ElasticsearchIndexSchemaObjectNode implements ElasticsearchIndexSchemaNode {

	private static final Log log = LoggerFactory.make( Log.class );

	private static final ElasticsearchIndexSchemaObjectNode ROOT =
			new ElasticsearchIndexSchemaObjectNode( null, null, null );

	public static ElasticsearchIndexSchemaObjectNode root() {
		return ROOT;
	}

	private final ElasticsearchIndexSchemaObjectNode parent;

	private final String absolutePath;

	private final ObjectFieldStorage storage;

	public ElasticsearchIndexSchemaObjectNode(ElasticsearchIndexSchemaObjectNode parent, String absolutePath,
			ObjectFieldStorage storage) {
		this.parent = parent;
		this.absolutePath = absolutePath;
		this.storage = storage;
	}

	public ElasticsearchIndexSchemaObjectNode getParent() {
		return parent;
	}

	public String getAbsolutePath() {
		return absolutePath;
	}

	public String getAbsolutePath(String relativeName) {
		return absolutePath == null ? relativeName : absolutePath + "." + relativeName;
	}

	@Override
	public ElasticsearchFieldFormatter getFormatter() {
		throw log.objectFieldForSearchFormatting();
	}

	@Override
	public void checkSuitableForNestedQuery() {
		if ( !ObjectFieldStorage.NESTED.equals( storage ) ) {
			throw log.nonNestedFieldForNestedPredicate();
		}
	}
}
