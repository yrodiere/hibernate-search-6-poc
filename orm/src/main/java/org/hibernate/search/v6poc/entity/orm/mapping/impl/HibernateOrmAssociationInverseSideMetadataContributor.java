/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.entity.orm.mapping.impl;

import org.hibernate.search.v6poc.entity.pojo.extractor.ContainerValueExtractorPath;
import org.hibernate.search.v6poc.entity.pojo.mapping.building.spi.PojoMappingCollectorTypeNode;
import org.hibernate.search.v6poc.entity.pojo.mapping.building.spi.PojoTypeMetadataContributor;
import org.hibernate.search.v6poc.entity.pojo.model.augmented.building.spi.PojoAugmentedModelCollectorTypeNode;
import org.hibernate.search.v6poc.entity.pojo.model.path.PojoModelPathValueNode;

final class HibernateOrmAssociationInverseSideMetadataContributor implements PojoTypeMetadataContributor {
	private final String propertyName;
	private final ContainerValueExtractorPath extractorPath;
	private final PojoModelPathValueNode inverseSideValuePath;

	HibernateOrmAssociationInverseSideMetadataContributor(String propertyName,
			ContainerValueExtractorPath extractorPath, PojoModelPathValueNode inverseSideValuePath) {
		this.propertyName = propertyName;
		this.extractorPath = extractorPath;
		this.inverseSideValuePath = inverseSideValuePath;
	}

	@Override
	public void contributeModel(PojoAugmentedModelCollectorTypeNode collector) {
		collector.property( propertyName ).value( extractorPath ).associationInverseSide( inverseSideValuePath );
	}

	@Override
	public void contributeMapping(PojoMappingCollectorTypeNode collector) {
		// Nothing to do
	}
}
