/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.entity.pojo.mapping.impl;

import org.hibernate.search.v6poc.entity.pojo.mapping.StreamPojoWorker;

class StreamPojoWorkerImpl implements StreamPojoWorker {

	private final PojoTypeWorkerContainer<
			StreamPojoTypeWorker,
			StreamPojoIndexedTypeWorker<?, ?, ?>,
			StreamPojoContainedTypeWorker<?>
			> typeWorkerContainer;

	StreamPojoWorkerImpl(PojoTypeWorkerContainer<
			StreamPojoTypeWorker,
			StreamPojoIndexedTypeWorker<?, ?, ?>,
			StreamPojoContainedTypeWorker<?>
			> typeWorkerContainer) {
		this.typeWorkerContainer = typeWorkerContainer;
	}

	@Override
	public void add(Object entity) {
		add( null, entity );
	}

	@Override
	public void add(Object id, Object entity) {
		typeWorkerContainer.getIndexed( entity ).add( id, entity );
	}

	@Override
	public void update(Object entity) {
		update( null, entity );
	}

	@Override
	public void update(Object id, Object entity) {
		typeWorkerContainer.getIndexed( entity ).update( id, entity );
	}

	@Override
	public void delete(Object entity) {
		delete( null, entity );
	}

	@Override
	public void delete(Object id, Object entity) {
		typeWorkerContainer.getIndexed( entity ).delete( id, entity );
	}

	@Override
	public void flush() {
		for ( StreamPojoIndexedTypeWorker<?, ?, ?> delegate : typeWorkerContainer.getAllIndexed() ) {
			delegate.flush();
		}
	}

	@Override
	public void flush(Class<?> clazz) {
		typeWorkerContainer.getIndexed( clazz ).flush();
	}

	@Override
	public void optimize() {
		for ( StreamPojoIndexedTypeWorker<?, ?, ?> delegate : typeWorkerContainer.getAllIndexed() ) {
			delegate.optimize();
		}
	}

	@Override
	public void optimize(Class<?> clazz) {
		typeWorkerContainer.getIndexed( clazz ).optimize();
	}

}
