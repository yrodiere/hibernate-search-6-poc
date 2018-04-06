/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.entity.pojo.mapping.definition.programmatic.impl;

import org.hibernate.search.v6poc.entity.pojo.extractor.ContainerValueExtractorPath;
import org.hibernate.search.v6poc.entity.pojo.mapping.building.spi.PojoMappingCollector;
import org.hibernate.search.v6poc.entity.pojo.mapping.building.spi.PojoMetadataContributor;
import org.hibernate.search.v6poc.entity.pojo.mapping.definition.programmatic.AssociationInverseSideMappingContext;
import org.hibernate.search.v6poc.entity.pojo.mapping.definition.programmatic.PropertyMappingContext;
import org.hibernate.search.v6poc.entity.pojo.model.augmented.building.spi.PojoAugmentedModelCollectorPropertyNode;
import org.hibernate.search.v6poc.entity.pojo.model.path.PojoModelPathValueNode;


/**
 * @author Yoann Rodiere
 */
public class AssociationInverseSideMappingContextImpl
		extends DelegatingPropertyMappingContext
		implements AssociationInverseSideMappingContext,
		PojoMetadataContributor<PojoAugmentedModelCollectorPropertyNode, PojoMappingCollector> {

	private final PojoModelPathValueNode inversePath;
	private ContainerValueExtractorPath extractorPath = ContainerValueExtractorPath.defaultExtractors();

	AssociationInverseSideMappingContextImpl(PropertyMappingContext delegate, PojoModelPathValueNode inversePath) {
		super( delegate );
		this.inversePath = inversePath;
	}

	@Override
	public void contributeModel(PojoAugmentedModelCollectorPropertyNode collector) {
		collector.value( extractorPath ).associationInverseSide( inversePath );
	}

	@Override
	public void contributeMapping(PojoMappingCollector collector) {
		// Nothing to do
	}

	@Override
	public AssociationInverseSideMappingContext withExtractors(ContainerValueExtractorPath extractorPath) {
		this.extractorPath = extractorPath;
		return this;
	}
}
