/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.entity.pojo.dirtiness.building.impl;

import org.hibernate.search.v6poc.entity.pojo.dirtiness.ReindexOnUpdate;
import org.hibernate.search.v6poc.entity.pojo.extractor.ContainerValueExtractorPath;
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

	private final PojoIndexingDependencyCollectorTypeNode<T> parentNode;
	/**
	 * The path to this node from the parent node, i.e. from the node representing the type holding this property.
	 */
	private final BoundPojoModelPathPropertyNode<T, P> modelPathFromParentNode;
	/**
	 * The last entity node among the ancestor nodes,
	 * i.e. the closest type node representing an entity type.
	 */
	private final PojoIndexingDependencyCollectorTypeNode<?> lastEntityNode;
	private final BoundPojoModelPathPropertyNode<T, P> modelPathFromLastEntityNode;
	/**
	 * The path to this node from the root node,
	 * i.e. from the node representing the type for which dependencies are being collected.
	 */
	private final BoundPojoModelPathPropertyNode<T, P> modelPathFromRootEntityNode;

	PojoIndexingDependencyCollectorPropertyNode(PojoIndexingDependencyCollectorTypeNode<T> parentNode,
			BoundPojoModelPathPropertyNode<T, P> modelPathFromParentNode,
			PojoIndexingDependencyCollectorTypeNode<?> lastEntityNode,
			BoundPojoModelPathPropertyNode<T, P> modelPathFromLastEntityNode,
			BoundPojoModelPathPropertyNode<T, P> modelPathFromRootEntityNode,
			PojoImplicitReindexingResolverBuildingHelper buildingHelper) {
		super( buildingHelper );
		this.parentNode = parentNode;
		this.modelPathFromParentNode = modelPathFromParentNode;
		this.lastEntityNode = lastEntityNode;
		this.modelPathFromLastEntityNode = modelPathFromLastEntityNode;
		this.modelPathFromRootEntityNode = modelPathFromRootEntityNode;
	}

	public <V> PojoIndexingDependencyCollectorValueNode<P, V> value(
			BoundContainerValueExtractorPath<P, V> boundExtractorPath) {
		return new PojoIndexingDependencyCollectorValueNode<>(
				this,
				modelPathFromParentNode.value( boundExtractorPath ),
				lastEntityNode,
				modelPathFromLastEntityNode.value( boundExtractorPath ),
				modelPathFromRootEntityNode.value( boundExtractorPath ),
				buildingHelper
		);
	}

	PojoIndexingDependencyCollectorValueNode<P, ?> value(
			ContainerValueExtractorPath extractorPath) {
		BoundContainerValueExtractorPath<P, ?> boundExtractorPath =
				buildingHelper.bindExtractorPath(
						modelPathFromRootEntityNode.getPropertyModel().getTypeModel(),
						extractorPath
				);
		return value( boundExtractorPath );
	}

	@Override
	ReindexOnUpdate getReindexOnUpdate() {
		return parentNode.getReindexOnUpdate();
	}

	PojoIndexingDependencyCollectorTypeNode<T> getParentNode() {
		return parentNode;
	}
}
