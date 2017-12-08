/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */

package org.hibernate.search.v6poc.entity.orm.impl;

import java.util.Map;
import javax.transaction.Synchronization;

import org.hibernate.search.v6poc.entity.orm.logging.impl.Log;
import org.hibernate.search.v6poc.entity.pojo.mapping.ChangesetPojoWorker;
import org.hibernate.search.v6poc.util.spi.LoggerFactory;

/**
 * Execute final work inside a transaction.
 *
 * @author Emmanuel Bernard
 */
class InTransactionWorkQueueSynchronization implements Synchronization {

	private static final Log log = LoggerFactory.make( Log.class );

	private final ChangesetPojoWorker worker;
	private final Map<?, ?> workerPerTransaction;
	private final Object transactionIdentifier;

	InTransactionWorkQueueSynchronization(ChangesetPojoWorker worker,
			Map<?, ?> workerPerTransaction, Object transactionIdentifier) {
		this.worker = worker;
		this.workerPerTransaction = workerPerTransaction;
		this.transactionIdentifier = transactionIdentifier;
	}

	@Override
	public void beforeCompletion() {
		// we are doing all the work in the before completion phase so that it is part of the transaction
		try {
			log.tracef(
					"Processing Transaction's beforeCompletion() phase for %s. Performing work.", this
			);
			worker.execute();
		}
		finally {
			//clean the Synchronization per Transaction
			//not strictly required but a cleaner approach and faster than the GC
			workerPerTransaction.remove( transactionIdentifier );
		}
	}

	@Override
	public void afterCompletion(int status) {
		// nothing to do, everything was done in beforeCompletion
	}

}