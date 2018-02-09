/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.backend.elasticsearch.impl;

import java.io.IOException;

import org.hibernate.search.v6poc.backend.elasticsearch.client.impl.ElasticsearchClient;
import org.hibernate.search.v6poc.backend.elasticsearch.document.impl.ElasticsearchDocumentObjectBuilder;
import org.hibernate.search.v6poc.backend.elasticsearch.index.impl.ElasticsearchIndexManagerBuilder;
import org.hibernate.search.v6poc.backend.elasticsearch.orchestration.impl.ElasticsearchWorkOrchestrator;
import org.hibernate.search.v6poc.backend.elasticsearch.orchestration.impl.StubElasticsearchWorkOrchestrator;
import org.hibernate.search.v6poc.backend.elasticsearch.work.impl.ElasticsearchWorkFactory;
import org.hibernate.search.v6poc.backend.index.spi.IndexManagerBuilder;
import org.hibernate.search.v6poc.backend.spi.Backend;
import org.hibernate.search.v6poc.cfg.ConfigurationPropertySource;
import org.hibernate.search.v6poc.engine.spi.BuildContext;
import org.hibernate.search.v6poc.util.SearchException;
import org.hibernate.search.v6poc.util.spi.Closer;


/**
 * @author Yoann Rodiere
 */
public class ElasticsearchBackend implements Backend<ElasticsearchDocumentObjectBuilder> {

	private final ElasticsearchClient client;

	private final String name;

	private final ElasticsearchWorkFactory workFactory;

	private final ElasticsearchWorkOrchestrator streamOrchestrator;

	private final ElasticsearchWorkOrchestrator queryOrchestrator;

	public ElasticsearchBackend(ElasticsearchClient client, String name, ElasticsearchWorkFactory workFactory) {
		this.client = client;
		this.name = name;
		this.workFactory = workFactory;
		this.streamOrchestrator = new StubElasticsearchWorkOrchestrator( client );
		this.queryOrchestrator = new StubElasticsearchWorkOrchestrator( client );
	}

	@Override
	public IndexManagerBuilder<ElasticsearchDocumentObjectBuilder> createIndexManagerBuilder(
			String name, BuildContext context, ConfigurationPropertySource propertySource) {
		return new ElasticsearchIndexManagerBuilder( this, name, context, propertySource );
	}

	public ElasticsearchWorkFactory getWorkFactory() {
		return workFactory;
	}

	public ElasticsearchClient getClient() {
		return client;
	}

	public ElasticsearchWorkOrchestrator createChangesetOrchestrator() {
		return new StubElasticsearchWorkOrchestrator( client );
	}

	public ElasticsearchWorkOrchestrator getStreamOrchestrator() {
		return streamOrchestrator;
	}

	public ElasticsearchWorkOrchestrator getQueryOrchestrator() {
		return queryOrchestrator;
	}

	@Override
	public void close() {
		// TODO use a Closer
		try (Closer<IOException> closer = new Closer<>()) {
			closer.push( client::close );
			closer.push( streamOrchestrator::close );
		}
		catch (IOException | RuntimeException e) {
			throw new SearchException( "Failed to shut down the Elasticsearch backend", e );
		}
	}

	@Override
	public String toString() {
		return new StringBuilder( getClass().getSimpleName() )
				.append( "[" )
				.append( "name=" ).append( name )
				.append( "]")
				.toString();
	}

}
