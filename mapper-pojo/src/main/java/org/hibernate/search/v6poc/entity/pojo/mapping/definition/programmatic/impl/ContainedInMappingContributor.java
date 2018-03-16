/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.entity.pojo.mapping.definition.programmatic.impl;

import org.hibernate.search.v6poc.entity.pojo.mapping.building.impl.PojoMappingCollectorPropertyNode;
import org.hibernate.search.v6poc.entity.pojo.mapping.building.impl.PojoMetadataContributor;
import org.hibernate.search.v6poc.entity.pojo.model.augmented.building.impl.PojoAugmentedModelCollectorPropertyNode;


/**
 * @author Yoann Rodiere
 */
public class ContainedInMappingContributor
		implements PojoMetadataContributor<PojoAugmentedModelCollectorPropertyNode, PojoMappingCollectorPropertyNode> {

	@Override
	public void contributeModel(PojoAugmentedModelCollectorPropertyNode collector) {
		// Nothing to do
	}

	@Override
	public void contributeMapping(PojoMappingCollectorPropertyNode collector) {
		collector.containedIn();
	}

}
