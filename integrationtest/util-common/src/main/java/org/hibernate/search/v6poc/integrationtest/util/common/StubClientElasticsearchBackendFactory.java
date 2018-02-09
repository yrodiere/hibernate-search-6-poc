/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.integrationtest.util.common;

import org.hibernate.search.v6poc.backend.elasticsearch.client.impl.ElasticsearchClient;
import org.hibernate.search.v6poc.backend.elasticsearch.client.impl.ElasticsearchClientFactory;
import org.hibernate.search.v6poc.backend.elasticsearch.impl.ElasticsearchBackend;
import org.hibernate.search.v6poc.backend.elasticsearch.work.impl.ElasticsearchWorkFactory;
import org.hibernate.search.v6poc.backend.elasticsearch.work.impl.StubElasticsearchWorkFactory;
import org.hibernate.search.v6poc.backend.spi.Backend;
import org.hibernate.search.v6poc.backend.spi.BackendFactory;
import org.hibernate.search.v6poc.cfg.ConfigurationPropertySource;
import org.hibernate.search.v6poc.engine.spi.BuildContext;

/**
 * @author Yoann Rodiere
 */
public class StubClientElasticsearchBackendFactory implements BackendFactory {

	@Override
	public Backend<?> create(String name, BuildContext context, ConfigurationPropertySource propertySource) {
		// TODO implement and detect dialects
		ElasticsearchClientFactory clientFactory = new StubElasticsearchClientFactory();
		ElasticsearchClient client = clientFactory.create( propertySource );
		ElasticsearchWorkFactory workFactory = new StubElasticsearchWorkFactory();
		return new ElasticsearchBackend( client, name, workFactory );
	}

}
