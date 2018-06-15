/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.entity.pojo.mapping.impl;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import org.hibernate.search.v6poc.entity.pojo.mapping.spi.PojoSessionContext;
import org.hibernate.search.v6poc.entity.pojo.model.spi.PojoRuntimeIntrospector;
import org.hibernate.search.v6poc.util.SearchException;

final class PojoTypeWorkerContainerImpl<W, I extends W, C extends W>
		implements PojoTypeWorkerContainer<W, I, C> {

	private final PojoIndexedTypeManagerContainer indexedTypeManagers;
	private final PojoContainedTypeManagerContainer containedTypeManagers;
	private final PojoWorkMode<I, C> workMode;

	private final PojoSessionContext sessionContext;
	private final PojoRuntimeIntrospector introspector;

	// Use a LinkedHashMap for deterministic iteration
	private final Map<Class<?>, I> indexedTypeWorkers = new LinkedHashMap<>();
	private final Map<Class<?>, C> containedTypeWorkers = new LinkedHashMap<>();

	private boolean addedAllIndexedTypes = false;

	PojoTypeWorkerContainerImpl(
			PojoIndexedTypeManagerContainer indexedTypeManagers,
			PojoContainedTypeManagerContainer containedTypeManagers,
			PojoWorkMode<I, C> workMode,
			PojoSessionContext sessionContext) {
		this.indexedTypeManagers = indexedTypeManagers;
		this.containedTypeManagers = containedTypeManagers;
		this.workMode = workMode;
		this.sessionContext = sessionContext;
		this.introspector = sessionContext.getRuntimeIntrospector();
	}

	@Override
	public W get(Object entity) {
		Class<?> clazz = introspector.getClass( entity );
		return get( clazz );
	}

	@Override
	public I getIndexed(Object entity) {
		Class<?> clazz = introspector.getClass( entity );
		return getIndexed( clazz );
	}

	@Override
	public W get(Class<?> clazz) {
		W worker = indexedTypeWorkers.get( clazz );
		if ( worker == null ) {
			worker = containedTypeWorkers.get( clazz );
			if ( worker == null ) {
				worker = createTypeWorker( clazz );
			}
		}
		return worker;
	}

	@Override
	public I getIndexed(Class<?> clazz) {
		I worker = indexedTypeWorkers.get( clazz );
		if ( worker == null ) {
			worker = createIndexedTypeWorker( clazz );
		}
		return worker;
	}

	@Override
	public Collection<I> getAllIndexed() {
		if ( !addedAllIndexedTypes ) {
			indexedTypeManagers.getAll().forEach( manager -> getIndexed( manager.getClass() ) );
			addedAllIndexedTypes = true;
		}
		return indexedTypeWorkers.values();
	}

	@Override
	public Collection<I> getCachedIndexed() {
		return indexedTypeWorkers.values();
	}

	@Override
	public Collection<C> getCachedContained() {
		return containedTypeWorkers.values();
	}

	@Override
	public void clear() {
		indexedTypeWorkers.clear();
		containedTypeWorkers.clear();
	}

	private W createTypeWorker(Class<?> clazz) {
		Optional<? extends PojoIndexedTypeManager<?, ?, ?>> indexedTypeManagerOptional =
				indexedTypeManagers.getByExactClass( clazz );
		if ( indexedTypeManagerOptional.isPresent() ) {
			I worker = workMode.createIndexedTypeWorker( indexedTypeManagerOptional.get(), sessionContext );
			indexedTypeWorkers.put( clazz, worker );
			return worker;
		}
		else {
			Optional<? extends PojoContainedTypeManager<?>> containedTypeManagerOptional =
					containedTypeManagers.getByExactClass( clazz );
			if ( containedTypeManagerOptional.isPresent() ) {
				C worker = workMode.createContainedTypeWorker( containedTypeManagerOptional.get(), sessionContext );
				containedTypeWorkers.put( clazz, worker );
				return worker;
			}
		}
		throw new SearchException(
				"Illegal operation on type " + clazz + ": this type is not indexed,"
				+ " neither directly nor as a contained entity in another type."
		);
	}

	private I createIndexedTypeWorker(Class<?> clazz) {
		Optional<? extends PojoIndexedTypeManager<?, ?, ?>> indexedTypeManagerOptional =
				indexedTypeManagers.getByExactClass( clazz );
		if ( indexedTypeManagerOptional.isPresent() ) {
			I worker = workMode.createIndexedTypeWorker( indexedTypeManagerOptional.get(), sessionContext );
			indexedTypeWorkers.put( clazz, worker );
			return worker;
		}

		throw new SearchException(
				"Illegal operation on type " + clazz + ": this type is not indexed directly."
		);
	}
}
