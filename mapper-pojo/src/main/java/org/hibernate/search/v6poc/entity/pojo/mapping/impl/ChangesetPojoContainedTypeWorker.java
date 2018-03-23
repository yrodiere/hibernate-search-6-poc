/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.entity.pojo.mapping.impl;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.hibernate.search.v6poc.entity.pojo.dirtiness.impl.PojoReindexingCollector;
import org.hibernate.search.v6poc.entity.pojo.mapping.spi.PojoSessionContext;

class ChangesetPojoContainedTypeWorker<E> extends PojoTypeWorker {

	private final PojoContainedTypeManager<E> typeManager;

	// Use a LinkedHashMap for stable ordering across JVMs
	private final Map<Object, WorkPlanPerDocument> workPlansPerId = new LinkedHashMap<>();

	ChangesetPojoContainedTypeWorker(PojoContainedTypeManager<E> typeManager, PojoSessionContext sessionContext) {
		super( sessionContext );
		this.typeManager = typeManager;
	}

	@Override
	public void add(Object providedId, Object entity) {
		Supplier<E> entitySupplier = typeManager.toEntitySupplier( sessionContext, entity );
		getWork( providedId ).add( entitySupplier );
	}

	@Override
	public void update(Object providedId, Object entity) {
		Supplier<E> entitySupplier = typeManager.toEntitySupplier( sessionContext, entity );
		getWork( providedId ).update( entitySupplier );
	}

	@Override
	public void delete(Object providedId, Object entity) {
		Supplier<E> entitySupplier = typeManager.toEntitySupplier( sessionContext, entity );
		getWork( providedId ).delete( entitySupplier );
	}

	void resolveDirty(PojoReindexingCollector containingEntityCollector) {
		for ( WorkPlanPerDocument workPerDocument : workPlansPerId.values() ) {
			workPerDocument.resolveDirty( containingEntityCollector );
		}
	}

	private WorkPlanPerDocument getWork(Object identifier) {
		WorkPlanPerDocument work = workPlansPerId.get( identifier );
		if ( work == null ) {
			work = new WorkPlanPerDocument( identifier );
			workPlansPerId.put( identifier, work );
		}
		return work;
	}

	private class WorkPlanPerDocument {
		private final Object identifier;
		private Supplier<E> entitySupplier;

		private boolean delete;
		private boolean add;

		private boolean shouldResolveDirty;

		private WorkPlanPerDocument(Object identifier) {
			this.identifier = identifier;
		}

		void add(Supplier<E> entitySupplier) {
			this.entitySupplier = entitySupplier;
			shouldResolveDirty = true;
			add = true;
		}

		void update(Supplier<E> entitySupplier) {
			this.entitySupplier = entitySupplier;
			/*
			 * Make sure that containing entities that haven't been modified will not trigger an update of their
			 * containing entities, unless those containing entities embed other entities in their index,
			 * and those entities have been modified.
			 */
			if ( !add ) {
				delete = true;
				add = true;
			}
			this.shouldResolveDirty = true;
		}

		void delete(Supplier<E> entitySupplier) {
			this.entitySupplier = entitySupplier;
			if ( add && !delete ) {
				/*
				 * We called add() in the same changeset, so we don't expect the document to be in the index.
				 * Don't delete, just cancel the addition.
				 */
				shouldResolveDirty = false;
				add = false;
				delete = false;
			}
			else {
				add = false;
				delete = true;
			}
		}

		void resolveDirty(PojoReindexingCollector containingEntityCollector) {
			if ( shouldResolveDirty ) {
				shouldResolveDirty = false; // Avoid infinite looping
				typeManager.resolveEntitiesToReindex( containingEntityCollector, entitySupplier );
			}
		}
	}

}
