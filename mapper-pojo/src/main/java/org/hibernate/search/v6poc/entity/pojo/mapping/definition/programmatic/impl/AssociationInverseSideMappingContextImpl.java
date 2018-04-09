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
import org.hibernate.search.v6poc.entity.pojo.model.augmented.building.spi.PojoAugmentedModelCollectorTypeNode;
import org.hibernate.search.v6poc.entity.pojo.model.path.PojoModelPath;
import org.hibernate.search.v6poc.entity.pojo.model.path.PojoModelPathValueNode;


/**
 * @author Yoann Rodiere
 */
public class AssociationInverseSideMappingContextImpl
		extends DelegatingPropertyMappingContext
		implements AssociationInverseSideMappingContext,
		PojoMetadataContributor<PojoAugmentedModelCollectorTypeNode, PojoMappingCollector> {

	private final String propertyName;
	private ContainerValueExtractorPath extractorPath = ContainerValueExtractorPath.defaultExtractors();
	private PojoModelPathValueNode embeddedPath;
	private final PojoModelPathValueNode inverseSidePath;

	AssociationInverseSideMappingContextImpl(PropertyMappingContext delegate, String propertyName,
			PojoModelPathValueNode inverseSidePath) {
		super( delegate );
		this.propertyName = propertyName;
		this.inverseSidePath = inverseSidePath;
	}

	@Override
	public void contributeModel(PojoAugmentedModelCollectorTypeNode collector) {
		PojoModelPathValueNode originalSidePath = PojoModelPath.fromRoot( propertyName ).value( extractorPath );
		if ( embeddedPath != null ) {
			originalSidePath = originalSidePath.append( embeddedPath );
		}
		collector.association( originalSidePath, inverseSidePath );
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

	@Override
	public AssociationInverseSideMappingContext withEmbeddedPath(PojoModelPathValueNode embeddedPath) {
		this.embeddedPath = embeddedPath;
		return this;
	}
}
