/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.backend.elasticsearch.document.model.impl;

public interface ElasticsearchIndexSchemaNode {

	/**
	 * @return The formatter to use when writing a value for this field in a JSON object.
	 * @throws org.hibernate.search.v6poc.util.SearchException if this node does not allow formatting
	 * (in particular, this is true for {@link ElasticsearchIndexSchemaObjectNode}.
	 */
	ElasticsearchFieldFormatter getFormatter();

	/**
	 * @throws org.hibernate.search.v6poc.util.SearchException if this node cannot be used as the object field
	 * for nested queries.
	 */
	void checkSuitableForNestedQuery();

}
