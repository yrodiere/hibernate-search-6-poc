/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.entity.mapping.spi;

import java.util.function.Function;

import org.hibernate.search.v6poc.engine.spi.SessionContext;
import org.hibernate.search.v6poc.search.DocumentReference;
import org.hibernate.search.v6poc.search.ObjectLoader;
import org.hibernate.search.v6poc.search.SearchPredicate;
import org.hibernate.search.v6poc.search.SearchSort;
import org.hibernate.search.v6poc.search.dsl.predicate.SearchPredicateContainerContext;
import org.hibernate.search.v6poc.search.dsl.predicate.impl.SearchTargetPredicateRootContext;
import org.hibernate.search.v6poc.search.dsl.query.SearchQueryResultDefinitionContext;
import org.hibernate.search.v6poc.search.dsl.query.spi.SearchQueryResultDefinitionContextImpl;
import org.hibernate.search.v6poc.search.dsl.sort.SearchSortContainerContext;
import org.hibernate.search.v6poc.search.dsl.sort.impl.SearchTargetSortRootContext;
import org.hibernate.search.v6poc.search.dsl.spi.SearchTargetContext;

public interface MappedIndexSearchTarget {

	SearchQueryResultDefinitionContext<DocumentReference, DocumentReference> query(SessionContext context);

	<R, O> SearchQueryResultDefinitionContext<R, O> query(SessionContext sessionContext,
			Function<DocumentReference, R> documentReferenceTransformer,
			ObjectLoader<R, O> objectLoader);

	SearchPredicateContainerContext<SearchPredicate> predicate();

	SearchSortContainerContext<SearchSort> sort();
}
