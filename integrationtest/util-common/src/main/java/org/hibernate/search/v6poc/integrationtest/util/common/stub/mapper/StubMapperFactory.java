/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.integrationtest.util.common.stub.mapper;

import org.hibernate.search.v6poc.cfg.ConfigurationPropertySource;
import org.hibernate.search.v6poc.engine.spi.BuildContext;
import org.hibernate.search.v6poc.entity.mapping.building.spi.Mapper;
import org.hibernate.search.v6poc.entity.mapping.building.spi.MapperFactory;
import org.hibernate.search.v6poc.entity.model.spi.IndexableTypeOrdering;

class StubMapperFactory implements MapperFactory<StubMappingContributor, StubMapping> {

	private final StubTypeOrdering typeOrdering;

	StubMapperFactory(StubTypeOrdering typeOrdering) {
		this.typeOrdering = typeOrdering;
	}

	@Override
	public IndexableTypeOrdering getTypeOrdering() {
		return typeOrdering;
	}

	@Override
	public Mapper<StubMappingContributor, StubMapping> createMapper(
			BuildContext buildContext, ConfigurationPropertySource propertySource) {
		return new StubMapper();
	}
}
