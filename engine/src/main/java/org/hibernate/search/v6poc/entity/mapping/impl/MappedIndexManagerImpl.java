/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.entity.mapping.impl;

import org.hibernate.search.v6poc.backend.document.DocumentElement;
import org.hibernate.search.v6poc.backend.index.spi.ChangesetIndexWorker;
import org.hibernate.search.v6poc.backend.index.spi.IndexManager;
import org.hibernate.search.v6poc.backend.index.spi.StreamIndexWorker;
import org.hibernate.search.v6poc.engine.spi.SessionContext;
import org.hibernate.search.v6poc.entity.mapping.spi.MappedIndexManager;
import org.hibernate.search.v6poc.entity.mapping.spi.MappedIndexSearchTargetBuilder;

public final class MappedIndexManagerImpl<D extends DocumentElement> implements MappedIndexManager<D> {
	final IndexManager<D> indexManager;

	public MappedIndexManagerImpl(IndexManager<D> indexManager) {
		this.indexManager = indexManager;
	}

	@Override
	public ChangesetIndexWorker<D> createWorker(SessionContext sessionContext) {
		return indexManager.createWorker( sessionContext );
	}

	@Override
	public StreamIndexWorker<D> createStreamWorker(SessionContext sessionContext) {
		return indexManager.createStreamWorker( sessionContext );
	}

	@Override
	public MappedIndexSearchTargetBuilder createSearchTarget() {
		return new MappedIndexSearchTargetBuilderImpl( indexManager.createSearchTargetContext() );
	}

}
