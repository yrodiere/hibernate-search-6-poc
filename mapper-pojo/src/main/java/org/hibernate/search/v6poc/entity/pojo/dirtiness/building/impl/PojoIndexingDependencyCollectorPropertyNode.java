/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.entity.pojo.dirtiness.building.impl;

import org.hibernate.search.v6poc.entity.pojo.extractor.impl.BoundContainerValueExtractorPath;
import org.hibernate.search.v6poc.entity.pojo.model.path.impl.BoundPojoModelPathPropertyNode;

/**
 * A node representing a property in a dependency collector.
 *
 * @see PojoIndexingDependencyCollectorValueNode
 *
 * @param <P> The property type
 */
public class PojoIndexingDependencyCollectorPropertyNode<T, P> extends AbstractPojoIndexingDependencyCollectorNode {

	private final BoundPojoModelPathPropertyNode<T, P> modelPath;
	private final PojoIndexingDependencyCollectorTypeNode<?> entityAncestor;
	private final BoundPojoModelPathPropertyNode<T, P> modelPathFromEntity;

	PojoIndexingDependencyCollectorPropertyNode(BoundPojoModelPathPropertyNode<T, P> modelPath,
			PojoIndexingDependencyCollectorTypeNode<?> entityAncestor,
			BoundPojoModelPathPropertyNode<T, P> modelPathFromEntity,
			PojoImplicitReindexingResolverBuildingHelper buildingHelper) {
		super( buildingHelper );
		this.modelPath = modelPath;
		this.entityAncestor = entityAncestor;
		this.modelPathFromEntity = modelPathFromEntity;
	}

	public <V> PojoIndexingDependencyCollectorValueNode<P, V> value(
			BoundContainerValueExtractorPath<P, V> boundExtractorPath) {
		return new PojoIndexingDependencyCollectorValueNode<>(
				modelPath.value( boundExtractorPath ),
				entityAncestor,
				modelPathFromEntity.value( boundExtractorPath ),
				buildingHelper
		);
	}

}
