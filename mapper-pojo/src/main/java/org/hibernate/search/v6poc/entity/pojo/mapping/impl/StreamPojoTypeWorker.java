/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.entity.pojo.mapping.impl;

import java.util.function.Supplier;

import org.hibernate.search.v6poc.backend.document.DocumentElement;
import org.hibernate.search.v6poc.backend.index.spi.StreamIndexWorker;
import org.hibernate.search.v6poc.entity.pojo.mapping.spi.PojoSessionContext;

class StreamPojoTypeWorker<I, E, D extends DocumentElement> extends PojoTypeWorker {

	private final PojoTypeManager<I, E, D> typeManager;
	private final StreamIndexWorker<D> delegate;

	StreamPojoTypeWorker(PojoTypeManager<I, E, D> typeManager, PojoSessionContext sessionContext,
			StreamIndexWorker<D> delegate) {
		super( sessionContext );
		this.typeManager = typeManager;
		this.delegate = delegate;
	}

	@Override
	public void add(Object providedId, Object entity) {
		Supplier<E> entitySupplier = typeManager.toEntitySupplier( sessionContext, entity );
		I identifier = typeManager.getIdentifierMapping().getIdentifier( providedId, entitySupplier );
		getDelegate().add(
				typeManager.toDocumentReferenceProvider( sessionContext, identifier, entitySupplier ),
				typeManager.toDocumentContributor( entitySupplier )
		);
	}

	@Override
	public void update(Object providedId, Object entity) {
		Supplier<E> entitySupplier = typeManager.toEntitySupplier( sessionContext, entity );
		I identifier = typeManager.getIdentifierMapping().getIdentifier( providedId, entitySupplier );
		getDelegate().update(
				typeManager.toDocumentReferenceProvider( sessionContext, identifier, entitySupplier ),
				typeManager.toDocumentContributor( entitySupplier )
		);
	}

	@Override
	public void delete(Object providedId, Object entity) {
		Supplier<E> entitySupplier = typeManager.toEntitySupplier( sessionContext, entity );
		I identifier = typeManager.getIdentifierMapping().getIdentifier( providedId, entitySupplier );
		getDelegate().delete( typeManager.toDocumentReferenceProvider( sessionContext, identifier, entitySupplier ) );
	}

	public void flush() {
		getDelegate().flush();
	}

	public void optimize() {
		getDelegate().optimize();
	}

	protected StreamIndexWorker<D> getDelegate() {
		return delegate;
	}
}
