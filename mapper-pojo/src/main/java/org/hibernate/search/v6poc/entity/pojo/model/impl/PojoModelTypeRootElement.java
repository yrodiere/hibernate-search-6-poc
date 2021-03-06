/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.entity.pojo.model.impl;

import org.hibernate.search.v6poc.entity.pojo.dirtiness.building.impl.PojoIndexingDependencyCollectorTypeNode;
import org.hibernate.search.v6poc.entity.pojo.model.PojoModelElementAccessor;
import org.hibernate.search.v6poc.entity.pojo.model.PojoModelType;
import org.hibernate.search.v6poc.entity.pojo.model.additionalmetadata.building.impl.PojoTypeAdditionalMetadataProvider;
import org.hibernate.search.v6poc.entity.pojo.model.path.impl.BoundPojoModelPathTypeNode;

/**
 * @param <T> The type used as a root element.
 */
public class PojoModelTypeRootElement<T> extends AbstractPojoModelCompositeElement<T> implements PojoModelType {

	private final BoundPojoModelPathTypeNode<T> modelPath;

	public PojoModelTypeRootElement(BoundPojoModelPathTypeNode<T> modelPath,
			PojoTypeAdditionalMetadataProvider typeAdditionalMetadataProvider) {
		super( typeAdditionalMetadataProvider );
		this.modelPath = modelPath;
	}

	@Override
	public String toString() {
		return modelPath.getTypeModel().toString();
	}

	public void contributeDependencies(PojoIndexingDependencyCollectorTypeNode<T> dependencyCollector) {
		contributePropertyDependencies( dependencyCollector );
	}

	@Override
	PojoModelElementAccessor<T> doCreateAccessor() {
		return new PojoModelRootElementAccessor<>();
	}

	@Override
	BoundPojoModelPathTypeNode<T> getModelPathTypeNode() {
		return modelPath;
	}
}
