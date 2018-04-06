/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.entity.pojo.model.impl;

import java.util.stream.Stream;

import org.hibernate.search.v6poc.entity.pojo.dirtiness.building.impl.PojoIndexingDependencyCollectorPropertyNode;
import org.hibernate.search.v6poc.entity.pojo.dirtiness.building.impl.PojoIndexingDependencyCollectorTypeNode;
import org.hibernate.search.v6poc.entity.pojo.dirtiness.building.impl.PojoIndexingDependencyCollectorValueNode;
import org.hibernate.search.v6poc.entity.pojo.model.PojoModelElementAccessor;
import org.hibernate.search.v6poc.entity.pojo.model.PojoModelProperty;
import org.hibernate.search.v6poc.entity.pojo.model.augmented.building.impl.PojoAugmentedTypeModelProvider;
import org.hibernate.search.v6poc.entity.pojo.model.augmented.impl.PojoAugmentedPropertyModel;
import org.hibernate.search.v6poc.entity.pojo.model.path.impl.BoundPojoModelPathPropertyNode;
import org.hibernate.search.v6poc.entity.pojo.model.path.impl.BoundPojoModelPathTypeNode;
import org.hibernate.search.v6poc.entity.pojo.model.path.impl.BoundPojoModelPathValueNode;
import org.hibernate.search.v6poc.entity.pojo.model.spi.PropertyHandle;


class PojoModelNestedElement<T, P> extends AbstractPojoModelElement<P> implements PojoModelProperty {

	private final AbstractPojoModelElement<T> parent;
	private final BoundPojoModelPathValueNode<T, P, P> modelPath;
	private final PojoAugmentedPropertyModel augmentedPropertyModel;

	PojoModelNestedElement(AbstractPojoModelElement<T> parent, BoundPojoModelPathPropertyNode<T, P> modelPath,
			PojoAugmentedPropertyModel augmentedPropertyModel,
			PojoAugmentedTypeModelProvider augmentedTypeModelProvider) {
		super( augmentedTypeModelProvider );
		this.parent = parent;
		this.modelPath = modelPath.valueWithoutExtractors();
		this.augmentedPropertyModel = augmentedPropertyModel;
	}

	@Override
	public <M> Stream<M> markers(Class<M> markerType) {
		return augmentedPropertyModel.getMarkers( markerType );
	}

	@Override
	public String getName() {
		return modelPath.parent().getPropertyModel().getName();
	}

	public void contributeDependencies(PojoIndexingDependencyCollectorTypeNode<T> dependencyCollector) {
		if ( hasAccessor() ) {
			@SuppressWarnings( "unchecked" ) // We used the same handle as in modelPath, on the same type. The result must have the same type.
					PojoIndexingDependencyCollectorPropertyNode<T, P> collectorPropertyNode =
					(PojoIndexingDependencyCollectorPropertyNode<T, P>) dependencyCollector.property( getHandle() );
			PojoIndexingDependencyCollectorValueNode<P, P> collectorValueNode =
					collectorPropertyNode.value( modelPath.getBoundExtractorPath() );
			collectorValueNode.collectDependency();
			contributePropertyDependencies( collectorValueNode.type() );
		}
	}

	@Override
	PojoModelElementAccessor<P> doCreateAccessor() {
		return new PojoModelPropertyElementAccessor<>( parent.createAccessor(), getHandle() );
	}

	@Override
	BoundPojoModelPathTypeNode<P> getModelPathTypeNode() {
		return modelPath.type();
	}

	PropertyHandle getHandle() {
		return modelPath.parent().getPropertyHandle();
	}
}
