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

/**
 * @param <I> The identifier type for the mapped entity type.
 * @param <E> The entity type mapped to the index.
 * @param <D> The document type for the index.
 */
class StreamPojoIndexedTypeWorker<I, E, D extends DocumentElement> extends StreamPojoTypeWorker {

	private final PojoIndexedTypeManager<I, E, D> typeManager;
	private final StreamIndexWorker<D> delegate;

	StreamPojoIndexedTypeWorker(PojoIndexedTypeManager<I, E, D> typeManager, PojoSessionContext sessionContext,
			StreamIndexWorker<D> delegate) {
		super( sessionContext );
		this.typeManager = typeManager;
		this.delegate = delegate;
	}

	void add(Object providedId, Object entity) {
		Supplier<E> entitySupplier = typeManager.toEntitySupplier( sessionContext, entity );
		I identifier = typeManager.getIdentifierMapping().getIdentifier( providedId, entitySupplier );
		getDelegate().add(
				typeManager.toDocumentReferenceProvider( sessionContext, identifier, entitySupplier ),
				typeManager.toDocumentContributor( entitySupplier )
		);
	}

	void update(Object providedId, Object entity) {
		Supplier<E> entitySupplier = typeManager.toEntitySupplier( sessionContext, entity );
		I identifier = typeManager.getIdentifierMapping().getIdentifier( providedId, entitySupplier );
		getDelegate().update(
				typeManager.toDocumentReferenceProvider( sessionContext, identifier, entitySupplier ),
				typeManager.toDocumentContributor( entitySupplier )
		);
	}

	void delete(Object providedId, Object entity) {
		Supplier<E> entitySupplier = typeManager.toEntitySupplier( sessionContext, entity );
		I identifier = typeManager.getIdentifierMapping().getIdentifier( providedId, entitySupplier );
		getDelegate().delete( typeManager.toDocumentReferenceProvider( sessionContext, identifier, entitySupplier ) );
	}

	void flush() {
		getDelegate().flush();
	}

	void optimize() {
		getDelegate().optimize();
	}

	private StreamIndexWorker<D> getDelegate() {
		return delegate;
	}
}
