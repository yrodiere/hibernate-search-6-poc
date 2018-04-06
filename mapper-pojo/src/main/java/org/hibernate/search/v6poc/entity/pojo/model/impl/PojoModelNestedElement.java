/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.entity.pojo.model.impl;

import java.util.stream.Stream;

import org.hibernate.search.v6poc.entity.pojo.model.PojoModelElementAccessor;
import org.hibernate.search.v6poc.entity.pojo.model.PojoModelProperty;
import org.hibernate.search.v6poc.entity.pojo.model.augmented.building.impl.PojoAugmentedTypeModelProvider;
import org.hibernate.search.v6poc.entity.pojo.model.augmented.impl.PojoAugmentedPropertyModel;
import org.hibernate.search.v6poc.entity.pojo.model.spi.PojoPropertyModel;
import org.hibernate.search.v6poc.entity.pojo.model.spi.PojoTypeModel;
import org.hibernate.search.v6poc.entity.pojo.model.spi.PropertyHandle;


public class PojoModelNestedElement extends AbstractPojoModelElement implements PojoModelProperty {

	private final AbstractPojoModelElement parent;
	private final PojoPropertyModel<?> propertyModel;
	private final PojoAugmentedPropertyModel augmentedPropertyModel;

	PojoModelNestedElement(AbstractPojoModelElement parent, PojoPropertyModel<?> propertyModel,
			PojoAugmentedPropertyModel augmentedPropertyModel,
			PojoAugmentedTypeModelProvider augmentedTypeModelProvider) {
		super( augmentedTypeModelProvider );
		this.parent = parent;
		this.augmentedPropertyModel = augmentedPropertyModel;
		this.propertyModel = propertyModel;
	}

	@Override
	public PojoModelElementAccessor<?> createAccessor() {
		return new PojoModelPropertyElementAccessor<>( parent.createAccessor(), getHandle() );
	}

	@Override
	public <M> Stream<M> markers(Class<M> markerType) {
		return augmentedPropertyModel.getMarkers( markerType );
	}

	public PropertyHandle getHandle() {
		return propertyModel.getHandle();
	}

	@Override
	public PojoTypeModel<?> getTypeModel() {
		return propertyModel.getTypeModel();
	}

	@Override
	public String getName() {
		return propertyModel.getName();
	}
}
