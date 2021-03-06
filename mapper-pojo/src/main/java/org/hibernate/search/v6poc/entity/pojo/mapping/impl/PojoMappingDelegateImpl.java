/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.entity.pojo.mapping.impl;

import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import org.hibernate.search.v6poc.engine.spi.SessionContext;
import org.hibernate.search.v6poc.entity.pojo.logging.impl.Log;
import org.hibernate.search.v6poc.entity.pojo.mapping.ChangesetPojoWorker;
import org.hibernate.search.v6poc.entity.pojo.mapping.StreamPojoWorker;
import org.hibernate.search.v6poc.entity.pojo.mapping.spi.PojoMappingDelegate;
import org.hibernate.search.v6poc.entity.pojo.mapping.spi.PojoSearchTargetDelegate;
import org.hibernate.search.v6poc.entity.pojo.mapping.spi.PojoSessionContext;
import org.hibernate.search.v6poc.util.SearchException;
import org.hibernate.search.v6poc.util.impl.common.Closer;
import org.hibernate.search.v6poc.util.impl.common.LoggerFactory;


/**
 * @author Yoann Rodiere
 */
public class PojoMappingDelegateImpl implements PojoMappingDelegate {

	private static final Log log = LoggerFactory.make( Log.class, MethodHandles.lookup() );

	private final PojoIndexedTypeManagerContainer indexedTypeManagers;
	private final PojoContainedTypeManagerContainer containedTypeManagers;

	public PojoMappingDelegateImpl(PojoIndexedTypeManagerContainer indexedTypeManagers,
			PojoContainedTypeManagerContainer containedTypeManagers) {
		this.indexedTypeManagers = indexedTypeManagers;
		this.containedTypeManagers = containedTypeManagers;
	}

	@Override
	public void close() {
		try ( Closer<RuntimeException> closer = new Closer<>() ) {
			closer.pushAll( PojoIndexedTypeManager::close, indexedTypeManagers.getAll() );
			closer.pushAll( PojoContainedTypeManager::close, containedTypeManagers.getAll() );
		}
	}

	@Override
	public ChangesetPojoWorker createWorker(PojoSessionContext sessionContext) {
		return new ChangesetPojoWorkerImpl( indexedTypeManagers, containedTypeManagers, sessionContext );
	}

	@Override
	public StreamPojoWorker createStreamWorker(PojoSessionContext sessionContext) {
		return new StreamPojoWorkerImpl( indexedTypeManagers, sessionContext );
	}

	@Override
	public <T> PojoSearchTargetDelegate<T> createPojoSearchTarget(Collection<? extends Class<? extends T>> targetedTypes,
			SessionContext sessionContext) {
		if ( targetedTypes.isEmpty() ) {
			throw log.cannotSearchOnEmptyTarget();
		}

		Set<PojoIndexedTypeManager<?, ? extends T, ?>> targetedTypeManagers = new LinkedHashSet<>();
		for ( Class<? extends T> targetedType : targetedTypes ) {
			targetedTypeManagers.addAll(
					indexedTypeManagers.getAllBySuperClass( targetedType )
							.orElseThrow( () -> new SearchException( "Type " + targetedType + " is not indexed and hasn't any indexed supertype." ) )
			);
		}

		return new PojoSearchTargetDelegateImpl<>( indexedTypeManagers, targetedTypeManagers, sessionContext );
	}

	@Override
	public boolean isWorkable(Class<?> type) {
		return indexedTypeManagers.getByExactClass( type ).isPresent()
				|| containedTypeManagers.getByExactClass( type ).isPresent();
	}

	@Override
	public boolean isIndexable(Class<?> type) {
		return indexedTypeManagers.getByExactClass( type ).isPresent();
	}

	@Override
	public boolean isSearchable(Class<?> type) {
		return indexedTypeManagers.getAllBySuperClass( type ).isPresent();
	}
}
