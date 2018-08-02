/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.backend.lucene.index.impl;

import java.lang.invoke.MethodHandles;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.hibernate.search.v6poc.entity.mapping.spi.MappedIndexSearchTarget;
import org.hibernate.search.v6poc.entity.mapping.spi.MappedIndexSearchTargetBuilder;
import org.hibernate.search.v6poc.backend.lucene.document.model.impl.LuceneIndexModel;
import org.hibernate.search.v6poc.backend.lucene.index.spi.ReaderProvider;
import org.hibernate.search.v6poc.backend.lucene.logging.impl.Log;
import org.hibernate.search.v6poc.backend.lucene.search.impl.LuceneSearchTargetModel;
import org.hibernate.search.v6poc.backend.lucene.search.query.impl.LuceneSearchTargetContext;
import org.hibernate.search.v6poc.backend.lucene.search.query.impl.SearchBackendContext;
import org.hibernate.search.v6poc.util.impl.common.LoggerFactory;


/**
 * @author Yoann Rodiere
 * @author Guillaume Smet
 */
class LuceneIndexSearchTargetBuilder implements MappedIndexSearchTargetBuilder {

	private static final Log log = LoggerFactory.make( Log.class, MethodHandles.lookup() );

	private final SearchBackendContext searchBackendContext;

	// Use LinkedHashSet to ensure stable order when generating requests
	private final Set<LuceneIndexManager> indexManagers = new LinkedHashSet<>();

	LuceneIndexSearchTargetBuilder(SearchBackendContext searchBackendContext, LuceneIndexManager indexManager) {
		this.searchBackendContext = searchBackendContext;
		this.indexManagers.add( indexManager );
	}

	void add(SearchBackendContext searchBackendContext, LuceneIndexManager indexManager) {
		if ( ! this.searchBackendContext.equals( searchBackendContext ) ) {
			throw log.cannotMixLuceneSearchTargetWithOtherBackend(
					this, indexManager, searchBackendContext.getEventContext()
			);
		}
		indexManagers.add( indexManager );
	}

	@Override
	public MappedIndexSearchTarget build() {
		// Use LinkedHashSet to ensure stable order when generating requests
		Set<LuceneIndexModel> indexModels = indexManagers.stream().map( LuceneIndexManager::getModel )
				.collect( Collectors.toCollection( LinkedHashSet::new ) );

		// TODO obviously, this will have to be changed once we have the full storage complexity from Search 5
		Set<ReaderProvider> readerProviders = indexManagers.stream().map( LuceneIndexManager::getReaderProvider )
				.collect( Collectors.toCollection( LinkedHashSet::new ) );

		LuceneSearchTargetModel searchTargetModel = new LuceneSearchTargetModel( indexModels, readerProviders );

		return new MappedIndexSearchTarget(
				new LuceneSearchTargetContext( searchBackendContext, searchTargetModel )
		);
	}

	@Override
	public String toString() {
		return new StringBuilder( getClass().getSimpleName() )
				.append( "[" )
				.append( "searchBackendContext=" ).append( searchBackendContext )
				.append( ", indexManagers=" ).append( indexManagers )
				.append( "]" )
				.toString();
	}

}
