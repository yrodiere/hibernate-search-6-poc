/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.backend.document.model.dsl;

/**
 * @param <A> The type of accessor created by this context.
 */
public interface IndexSchemaFieldTypedContext<A> extends IndexSchemaFieldTerminalContext<A> {

	// TODO add common options: stored, sortable, ...

	IndexSchemaFieldTypedContext<A> analyzer(String analyzerName);

	IndexSchemaFieldTypedContext<A> normalizer(String normalizerName);

	IndexSchemaFieldTypedContext<A> store(Store store);

	IndexSchemaFieldTypedContext<A> sortable(Sortable sortable);

}
