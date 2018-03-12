/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.entity.pojo.mapping.impl;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import org.hibernate.search.v6poc.backend.document.DocumentElement;
import org.hibernate.search.v6poc.backend.index.spi.ChangesetIndexWorker;
import org.hibernate.search.v6poc.backend.index.spi.DocumentReferenceProvider;
import org.hibernate.search.v6poc.entity.pojo.mapping.spi.PojoSessionContext;

/**
 * @author Yoann Rodiere
 */
class ChangesetPojoTypeWorker<D extends DocumentElement, E> implements PojoTypeWorker {

	private final PojoTypeManager<?, E, D> typeManager;
	private final PojoSessionContext sessionContext;
	private final ChangesetIndexWorker<D> delegate;

	/*
	 * We use a LinkedHashMap mainly to ensure the order will be stable from one run to another.
	 * This changes everything when debugging...
	 */
	private final Map<String, WorkPerDocument> worksPerDocument = new LinkedHashMap<>();

	ChangesetPojoTypeWorker(PojoTypeManager<?, E, D> typeManager, PojoSessionContext sessionContext,
			ChangesetIndexWorker<D> delegate) {
		this.typeManager = typeManager;
		this.sessionContext = sessionContext;
		this.delegate = delegate;
	}

	@Override
	public void add(Object entity) {
		add( null, entity );
	}

	@Override
	public void add(Object id, Object entity) {
		Supplier<E> entitySupplier = typeManager.toEntitySupplier( sessionContext, entity );
		DocumentReferenceProvider documentReferenceProvider = typeManager.toDocumentReferenceProvider(
				sessionContext, id, entitySupplier
		);
		getWork( documentReferenceProvider ).add( entitySupplier );
	}

	@Override
	public void update(Object entity) {
		update( null, entity );
	}

	@Override
	public void update(Object id, Object entity) {
		Supplier<E> entitySupplier = typeManager.toEntitySupplier( sessionContext, entity );
		DocumentReferenceProvider documentReferenceProvider = typeManager.toDocumentReferenceProvider(
				sessionContext, id, entitySupplier
		);
		getWork( documentReferenceProvider ).update( entitySupplier );
	}

	@Override
	public void delete(Object entity) {
		delete( null, entity );
	}

	@Override
	public void delete(Object id, Object entity) {
		Supplier<E> entitySupplier = typeManager.toEntitySupplier( sessionContext, entity );
		DocumentReferenceProvider documentReferenceProvider = typeManager.toDocumentReferenceProvider(
				sessionContext, id, entitySupplier
		);
		getWork( documentReferenceProvider ).delete();
	}

	public void prepare() {
		sendWorksToDelegate();
		delegate.prepare();
	}

	public CompletableFuture<?> execute() {
		sendWorksToDelegate();
		/*
		 * No need to call prepare() here:
		 * delegates are supposed to handle execute() even without a prior call to prepare().
		 */
		return delegate.execute();
	}

	private void sendWorksToDelegate() {
		try {
			worksPerDocument.values().forEach( WorkPerDocument::sendWorkToDelegate );
		}
		finally {
			worksPerDocument.clear();
		}
	}

	private WorkPerDocument getWork(DocumentReferenceProvider documentReferenceProvider) {
		String identifier = documentReferenceProvider.getIdentifier();
		WorkPerDocument work = worksPerDocument.get( identifier );
		if ( work == null ) {
			work = new WorkPerDocument( documentReferenceProvider );
			worksPerDocument.put( identifier, work );
		}
		return work;
	}

	private class WorkPerDocument {

		private final DocumentReferenceProvider documentReferenceProvider;
		private Supplier<E> entitySupplier;

		private boolean delete;
		private boolean add;

		private WorkPerDocument(DocumentReferenceProvider documentReferenceProvider) {
			this.documentReferenceProvider = documentReferenceProvider;
		}

		void add(Supplier<E> entitySupplier) {
			add = true;
			this.entitySupplier = entitySupplier;
		}

		void update(Supplier<E> entitySupplier) {
			/*
			 * If add is true, either this is already an update (in which case we don't need to change the flags)
			 * or we called add() in the same changeset (in which case we don't expect the document to be in the index).
			 */
			if ( !add ) {
				delete = true;
				add = true;
			}
			this.entitySupplier = entitySupplier;
		}

		void delete() {
			if ( add && !delete ) {
				/*
				 * We called add() in the same changeset, so we don't expect the document to be in the index.
				 * Don't delete, just cancel the addition.
				 */
				add = false;
				delete = false;
			}
			else {
				add = false;
				delete = true;
			}
			this.entitySupplier = null;
		}

		void sendWorkToDelegate() {
			if ( add ) {
				if ( delete ) {
					delegate.update( documentReferenceProvider, typeManager.toDocumentContributor( entitySupplier ) );
				}
				else {
					delegate.add( documentReferenceProvider, typeManager.toDocumentContributor( entitySupplier ) );
				}
			}
			else if ( delete ) {
				delegate.delete( documentReferenceProvider );
			}
		}

	}
}
