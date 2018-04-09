/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.entity.pojo.model.augmented.building.impl;

import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Map;

import org.hibernate.search.v6poc.entity.pojo.logging.impl.Log;
import org.hibernate.search.v6poc.entity.pojo.model.augmented.building.spi.PojoAugmentedModelCollectorPropertyNode;
import org.hibernate.search.v6poc.entity.pojo.model.augmented.building.spi.PojoAugmentedModelCollectorTypeNode;
import org.hibernate.search.v6poc.entity.pojo.model.augmented.impl.PojoAugmentedPropertyModel;
import org.hibernate.search.v6poc.entity.pojo.model.augmented.impl.PojoAugmentedTypeModel;
import org.hibernate.search.v6poc.entity.pojo.model.path.PojoModelPathValueNode;
import org.hibernate.search.v6poc.entity.pojo.model.spi.PojoRawTypeModel;
import org.hibernate.search.v6poc.util.impl.common.LoggerFactory;

class PojoAugmentedTypeModelBuilder implements PojoAugmentedModelCollectorTypeNode {
	private static final Log log = LoggerFactory.make( Log.class, MethodHandles.lookup() );

	private final PojoRawTypeModel<?> rawTypeModel;
	private final Map<String, PojoAugmentedPropertyModelBuilder> propertyBuilders = new HashMap<>();
	private boolean entity;
	private final Map<PojoModelPathValueNode, PojoModelPathValueNode> associationOriginalSideToInverseSide = new HashMap<>();

	PojoAugmentedTypeModelBuilder(PojoRawTypeModel<?> rawTypeModel) {
		this.rawTypeModel = rawTypeModel;
	}

	@Override
	public PojoAugmentedModelCollectorPropertyNode property(String propertyName) {
		return propertyBuilders.computeIfAbsent( propertyName, ignored -> new PojoAugmentedPropertyModelBuilder() );
	}

	@Override
	public void markAsEntity() {
		this.entity = true;
	}

	@Override
	public void association(PojoModelPathValueNode originalSidePath, PojoModelPathValueNode inverseSidePath) {
		if ( !entity ) {
			throw log.cannotDefineAssociationOnNonEntityType( rawTypeModel, originalSidePath, inverseSidePath );
		}
		associationOriginalSideToInverseSide.put( originalSidePath, inverseSidePath );
	}

	public PojoAugmentedTypeModel build() {
		Map<String, PojoAugmentedPropertyModel> properties = new HashMap<>();
		for ( Map.Entry<String, PojoAugmentedPropertyModelBuilder> entry : propertyBuilders.entrySet() ) {
			properties.put( entry.getKey(), entry.getValue().build() );

		}
		return new PojoAugmentedTypeModel( entity, properties, associationOriginalSideToInverseSide );
	}
}
