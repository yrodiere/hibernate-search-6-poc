/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.entity.mapping.spi;

import org.hibernate.search.v6poc.backend.index.spi.ChangesetIndexWorker;
import org.hibernate.search.v6poc.backend.index.spi.StreamIndexWorker;
import org.hibernate.search.v6poc.engine.spi.SessionContext;

/**
 * The object responsible for applying works and searches to a full-text index.
 * <p>
 * This is the interface provided to mappers to access the index manager, not to be confused
 * with {@link org.hibernate.search.v6poc.backend.index.spi.IndexManager}
 */
public interface MappedIndexManager<D> {

	ChangesetIndexWorker<D> createWorker(SessionContext sessionContext);

	StreamIndexWorker<D> createStreamWorker(SessionContext sessionContext);

	MappedIndexSearchTargetBuilder createSearchTarget();

}
