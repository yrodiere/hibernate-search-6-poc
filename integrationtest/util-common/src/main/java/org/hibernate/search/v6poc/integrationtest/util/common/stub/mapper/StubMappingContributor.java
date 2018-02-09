/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.integrationtest.util.common.stub.mapper;

import java.util.function.Consumer;

import org.hibernate.search.v6poc.entity.mapping.building.spi.IndexManagerBuildingState;
import org.hibernate.search.v6poc.entity.mapping.building.spi.IndexModelBindingContext;
import org.hibernate.search.v6poc.entity.mapping.building.spi.TypeMetadataCollector;

class StubMappingContributor {

	private final StubTypeIdentifier typeIdentifier;
	private final String indexName;
	private final Consumer<IndexModelBindingContext> delegate;

	public StubMappingContributor(StubTypeIdentifier typeIdentifier, String indexName, Consumer<IndexModelBindingContext> delegate) {
		this.typeIdentifier = typeIdentifier;
		this.indexName = indexName;
		this.delegate = delegate;
	}

	final void contribute(StubMapperFactory factory, TypeMetadataCollector collector) {
		collector.collect( factory, typeIdentifier, indexName, this );
	}

	public void contribute(IndexManagerBuildingState<?> indexManagerBuildingState) {
		delegate.accept( indexManagerBuildingState.getRootBindingContext() );
	}

}
