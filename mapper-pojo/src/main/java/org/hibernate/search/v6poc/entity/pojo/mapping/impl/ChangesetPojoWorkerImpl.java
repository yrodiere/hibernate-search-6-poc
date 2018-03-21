/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.entity.pojo.mapping.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.hibernate.search.v6poc.entity.pojo.mapping.ChangesetPojoWorker;
import org.hibernate.search.v6poc.entity.pojo.mapping.spi.PojoSessionContext;

/**
 * @author Yoann Rodiere
 */
class ChangesetPojoWorkerImpl extends PojoWorkerImpl implements ChangesetPojoWorker {

	private final PojoSessionContext sessionContext;
	private final Map<Class<?>, ChangesetPojoTypeWorker<?, ?>> delegates = new HashMap<>();

	ChangesetPojoWorkerImpl(PojoTypeManagerContainer typeManagers, PojoSessionContext sessionContext) {
		super( typeManagers, sessionContext.getRuntimeIntrospector() );
		this.sessionContext = sessionContext;
	}

	@Override
	public void prepare() {
		for ( ChangesetPojoTypeWorker<?, ?> delegate : delegates.values() ) {
			delegate.resolveDirty( this::updateBecauseOfContained );
		}
		for ( ChangesetPojoTypeWorker<?, ?> delegate : delegates.values() ) {
			delegate.prepare();
		}
	}

	@Override
	public CompletableFuture<?> execute() {
		try {
			prepare();
			List<CompletableFuture<?>> futures = new ArrayList<>();
			for ( ChangesetPojoTypeWorker<?, ?> delegate : delegates.values() ) {
				futures.add( delegate.execute() );
			}
			return CompletableFuture.allOf( futures.toArray( new CompletableFuture[futures.size()] ) );
		}
		finally {
			delegates.clear();
		}
	}

	@Override
	ChangesetPojoTypeWorker<?, ?> getDelegate(Class<?> clazz) {
		return delegates.computeIfAbsent(
				clazz,
				c -> getTypeManager( c ).createWorker( sessionContext )
		);
	}

	private void updateBecauseOfContained(Object containingEntity) {
		// TODO ignore the event when containingEntity has provided IDs
		Class<?> clazz = getIntrospector().getClass( containingEntity );
		ChangesetPojoTypeWorker<?, ?> delegate = getDelegate( clazz );
		delegate.updateBecauseOfContained( containingEntity );
	}

}
