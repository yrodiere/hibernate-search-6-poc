/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.entity.pojo.mapping.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.hibernate.search.v6poc.entity.pojo.mapping.ChangesetPojoWorker;
import org.hibernate.search.v6poc.entity.pojo.mapping.spi.PojoSessionContext;
import org.hibernate.search.v6poc.util.AssertionFailure;
import org.hibernate.search.v6poc.util.SearchException;

class ChangesetPojoWorkerImpl implements ChangesetPojoWorker {

	private final PojoTypeWorkerContainer<
			ChangesetPojoTypeWorker,
			ChangesetPojoIndexedTypeWorker<?, ?, ?>,
			ChangesetPojoContainedTypeWorker<?>
			> typeWorkerContainer;

	ChangesetPojoWorkerImpl(PojoTypeWorkerContainer<
			ChangesetPojoTypeWorker,
			ChangesetPojoIndexedTypeWorker<?, ?, ?>,
			ChangesetPojoContainedTypeWorker<?>
			> typeWorkerContainer) {
		this.typeWorkerContainer = typeWorkerContainer;
	}

	@Override
	public void add(Object entity) {
		add( null, entity );
	}

	@Override
	public void add(Object id, Object entity) {
		typeWorkerContainer.get( entity ).add( id, entity );
	}

	@Override
	public void update(Object entity) {
		update( null, entity );
	}

	@Override
	public void update(Object id, Object entity) {
		typeWorkerContainer.get( entity ).update( id, entity );
	}

	@Override
	public void delete(Object entity) {
		delete( null, entity );
	}

	@Override
	public void delete(Object id, Object entity) {
		typeWorkerContainer.get( entity ).delete( id, entity );
	}

	@Override
	public void update(Object entity, String... dirtyPaths) {
		update( null, entity, dirtyPaths );
	}

	@Override
	public void update(Object id, Object entity, String... dirtyPaths) {
		typeWorkerContainer.get( entity ).update( id, entity, dirtyPaths );
	}

	@Override
	public void prepare() {
		for ( ChangesetPojoTypeWorker worker : typeWorkerContainer.getCachedContained() ) {
			worker.resolveDirty( this::updateBecauseOfContained );
		}
		for ( ChangesetPojoIndexedTypeWorker<?, ?, ?> worker : typeWorkerContainer.getCachedIndexed() ) {
			worker.resolveDirty( this::updateBecauseOfContained );
		}
		for ( ChangesetPojoIndexedTypeWorker<?, ?, ?> worker : typeWorkerContainer.getCachedIndexed() ) {
			worker.prepare();
		}
	}

	@Override
	public CompletableFuture<?> execute() {
		try {
			prepare();
			List<CompletableFuture<?>> futures = new ArrayList<>();
			for ( ChangesetPojoIndexedTypeWorker<?, ?, ?> worker : typeWorkerContainer.getCachedIndexed() ) {
				futures.add( worker.execute() );
			}
			return CompletableFuture.allOf( futures.toArray( new CompletableFuture[futures.size()] ) );
		}
		finally {
			typeWorkerContainer.clear();
		}
	}

	private void updateBecauseOfContained(Object containingEntity) {
		// TODO ignore the event when containingEntity has provided IDs
		typeWorkerContainer.getIndexed( containingEntity ).updateBecauseOfContained( containingEntity );
	}

}
