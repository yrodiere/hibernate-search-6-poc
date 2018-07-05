/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.backend.lucene.impl;

import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.nio.file.Path;

import org.hibernate.search.v6poc.backend.Backend;
import org.hibernate.search.v6poc.backend.index.spi.IndexManagerBuilder;
import org.hibernate.search.v6poc.backend.lucene.LuceneBackend;
import org.hibernate.search.v6poc.backend.lucene.document.impl.LuceneRootDocumentBuilder;
import org.hibernate.search.v6poc.backend.lucene.index.impl.IndexingBackendContext;
import org.hibernate.search.v6poc.backend.lucene.index.impl.LuceneDirectoryIndexManagerBuilder;
import org.hibernate.search.v6poc.backend.lucene.logging.impl.Log;
import org.hibernate.search.v6poc.backend.lucene.multitenancy.impl.MultiTenancyStrategy;
import org.hibernate.search.v6poc.backend.lucene.orchestration.impl.LuceneQueryWorkOrchestrator;
import org.hibernate.search.v6poc.backend.lucene.orchestration.impl.StubLuceneQueryWorkOrchestrator;
import org.hibernate.search.v6poc.backend.lucene.search.query.impl.SearchBackendContext;
import org.hibernate.search.v6poc.backend.lucene.work.impl.LuceneWorkFactory;
import org.hibernate.search.v6poc.backend.spi.BackendImplementor;
import org.hibernate.search.v6poc.cfg.ConfigurationPropertySource;
import org.hibernate.search.v6poc.backend.spi.BackendBuildContext;
import org.hibernate.search.v6poc.util.impl.common.Closer;
import org.hibernate.search.v6poc.util.impl.common.LoggerFactory;

/**
 * @author Guillaume Smet
 */
public class LuceneLocalDirectoryBackend implements BackendImplementor<LuceneRootDocumentBuilder>, Backend {

	private static final Log log = LoggerFactory.make( Log.class, MethodHandles.lookup() );

	private final String name;

	private final Path rootDirectory;

	private final LuceneQueryWorkOrchestrator queryOrchestrator;
	private final MultiTenancyStrategy multiTenancyStrategy;

	private final IndexingBackendContext indexingContext;
	private final SearchBackendContext searchContext;

	LuceneLocalDirectoryBackend(String name, Path rootDirectory, LuceneWorkFactory workFactory,
			MultiTenancyStrategy multiTenancyStrategy) {
		this.name = name;
		this.rootDirectory = rootDirectory;

		this.queryOrchestrator = new StubLuceneQueryWorkOrchestrator();
		this.multiTenancyStrategy = multiTenancyStrategy;

		this.indexingContext = new IndexingBackendContext(
				this, new MMapDirectoryProvider( this, rootDirectory ),
				workFactory, multiTenancyStrategy
		);
		this.searchContext = new SearchBackendContext(
				this, workFactory, multiTenancyStrategy, queryOrchestrator
		);

		initializeRootDirectory( name, rootDirectory );
	}

	@Override
	public <T> T unwrap(Class<T> clazz) {
		if ( LuceneBackend.class.isAssignableFrom( clazz ) ) {
			return (T) this;
		}
		throw log.backendUnwrappingWithUnknownType( clazz, LuceneBackend.class );
	}

	@Override
	public Backend toAPI() {
		return this;
	}

	@Override
	public IndexManagerBuilder<LuceneRootDocumentBuilder> createIndexManagerBuilder(
			String indexName, boolean multiTenancyEnabled, BackendBuildContext context, ConfigurationPropertySource propertySource) {
		if ( multiTenancyEnabled && !multiTenancyStrategy.isMultiTenancySupported() ) {
			throw log.multiTenancyRequiredButNotSupportedByBackend( this, indexName );
		}

		/*
		 * We do not normalize index names: directory providers are expected to use the exact given index name,
		 * or a reversible conversion of that name, as an internal key (file names, ...),
		 * and therefore the internal key should stay unique.
		 */
		return new LuceneDirectoryIndexManagerBuilder(
				indexingContext, searchContext,
				indexName, context, propertySource
		);
	}

	@Override
	public void close() {
		try ( Closer<RuntimeException> closer = new Closer<>() ) {
			closer.push( LuceneQueryWorkOrchestrator::close, queryOrchestrator );
		}
	}

	@Override
	public String toString() {
		return new StringBuilder( getClass().getSimpleName() )
				.append( "[" )
				.append( "name=" ).append( name ).append( ", " )
				.append( "rootDirectory=" ).append( rootDirectory )
				.append( "]" )
				.toString();
	}

	private static void initializeRootDirectory(String name, Path rootDirectory) {
		if ( Files.exists( rootDirectory ) ) {
			if ( !Files.isDirectory( rootDirectory ) || !Files.isWritable( rootDirectory ) ) {
				throw log.localDirectoryBackendRootDirectoryNotWritableDirectory( name, rootDirectory );
			}
		}
		else {
			try {
				Files.createDirectories( rootDirectory );
			}
			catch (Exception e) {
				throw log.unableToCreateRootDirectoryForLocalDirectoryBackend( name, rootDirectory, e );
			}
		}
	}
}
