/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.entity.pojo.dirtiness.building.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.hibernate.search.v6poc.entity.pojo.extractor.ContainerValueExtractor;
import org.hibernate.search.v6poc.entity.pojo.extractor.impl.ContainerValueExtractorPath;
import org.hibernate.search.v6poc.entity.pojo.mapping.building.impl.PojoIndexModelBinder;
import org.hibernate.search.v6poc.entity.pojo.model.path.impl.PojoAugmentedModelPathInverter;
import org.hibernate.search.v6poc.entity.pojo.model.path.impl.PojoModelPath;
import org.hibernate.search.v6poc.entity.pojo.model.path.impl.PojoModelPathContainerElementNode;
import org.hibernate.search.v6poc.entity.pojo.model.path.impl.PojoModelPathPropertyNode;
import org.hibernate.search.v6poc.entity.pojo.model.path.impl.PojoModelPathTypeNode;
import org.hibernate.search.v6poc.entity.pojo.model.path.impl.PojoModelPathValueNode;
import org.hibernate.search.v6poc.entity.pojo.model.spi.PojoPropertyModel;
import org.hibernate.search.v6poc.entity.pojo.model.spi.PojoRawTypeModel;
import org.hibernate.search.v6poc.entity.pojo.model.spi.PojoTypeModel;
import org.hibernate.search.v6poc.util.AssertionFailure;

public class PojoImplicitReindexingResolverBuilder implements PojoDirtyDependencyCollector {

	private final PojoAugmentedModelPathInverter pathReverser;
	private final PojoIndexModelBinder indexModelBinder;
	private final Map<PojoRawTypeModel<?>, PojoImplicitReindexingResolverTypeNodeBuilder<?>> builderByType = new HashMap<>();

	public PojoImplicitReindexingResolverBuilder(
			PojoAugmentedModelPathInverter pathReverser, PojoIndexModelBinder indexModelBinder) {
		this.pathReverser = pathReverser;
		this.indexModelBinder = indexModelBinder;
	}

	@Override
	public void collect(PojoModelPathValueNode<?, ?> dependingEntityToDirtyValuePath) {


		// TODO backtrack to the last entity type (multiple times, until we reach the root
		// gives two paths:
		// - from the depending entity (path root) to the contained/dirtiness-generating entity (last entity type)
		// - from the contained/dirtiness-generating entity to the contained/dirtiness-generating value
		// Let's ignore the second one for now.
		for each entity in the path {
			PojoModelPathValueNode<?, ?> dependingEntityToDirtyEntityPath;
			doCollect( dependingEntityToDirtyEntityPath );
		}
	}

	/**
	 * @param dependingEntityToDirtyEntityPath The path from the depending entity to the dirty entity type
	 * @param <T> The (potentially) dirty entity type
	 */
	private <T> void doCollect(PojoModelPathValueNode<?, T> dependingEntityToDirtyEntityPath) {
		PojoTypeModel<T> dirtyingTypeModel = dependingEntityToDirtyEntityPath.type().getTypeModel();
		PojoRawTypeModel<? super T> dirtyingRawTypeModel = dirtyingTypeModel.getRawType();

		@SuppressWarnings("unchecked") // We know values have this type, by construction
				PojoImplicitReindexingResolverTypeNodeBuilder<? super T> dirtyingTypeDirtyResolverBuilder =
				(PojoImplicitReindexingResolverTypeNodeBuilder<? super T>)
						builderByType.computeIfAbsent(
								dirtyingRawTypeModel,
								t -> new PojoImplicitReindexingResolverTypeNodeBuilder(
										PojoModelPath.root( t ), indexModelBinder )
						);

		PojoImplicitReindexingResolverValueNodeBuilderDelegate<?> valueBuilder = applyReversePath(
				dirtyingTypeDirtyResolverBuilder, dependingEntityToDirtyEntityPath
		);

		valueBuilder.markForReindexing();
	}

	private <T> PojoImplicitReindexingResolverValueNodeBuilderDelegate<?> applyReversePath(
			PojoImplicitReindexingResolverTypeNodeBuilder<? super T> rootBuilder,
			PojoModelPathValueNode<?, T> pathToReverse) {
		PojoModelPath remainingPathToConsume = pathToReverse;
		PojoImplicitReindexingResolverValueNodeBuilderDelegate<?> currentValueBuilder = null;

		while ( remainingPathToConsume != null ) {
			if ( remainingPathToConsume instanceof PojoModelPathTypeNode ) {
				// Ignore type nodes
				remainingPathToConsume = remainingPathToConsume.parent();
			}
			else if ( remainingPathToConsume instanceof PojoModelPathPropertyNode ) {
				PojoModelPathPropertyNode<?, ?> propertyNodeToReverse =
						(PojoModelPathPropertyNode<?, ?>) remainingPathToConsume;
				remainingPathToConsume = propertyNodeToReverse.parent();

				PojoImplicitReindexingResolverTypeNodeBuilder<?> currentTypeBuilder =
						currentValueBuilder == null ? rootBuilder : currentValueBuilder.type();

				Optional<? extends PojoImplicitReindexingResolverValueNodeBuilderDelegate<?>> newValueBuilder =
						applyReverseProperty( currentTypeBuilder, propertyNodeToReverse );
				if ( newValueBuilder.isPresent() ) {
					currentValueBuilder = newValueBuilder.get();
					checkReverseValueNode( propertyNodeToReverse, currentValueBuilder );
				}
				else {
					// TODO log something? Throw an error?
					throw new IllegalStateException();
				}
			}
			else if ( remainingPathToConsume instanceof PojoModelPathContainerElementNode ) {
				PojoModelPathContainerElementNode<?, ?, ?> containerElementNode =
						(PojoModelPathContainerElementNode<?, ?, ?>) remainingPathToConsume;
				PojoModelPathPropertyNode<?, ?> propertyNodeToReverse = containerElementNode.parent();
				remainingPathToConsume = propertyNodeToReverse.parent();

				PojoImplicitReindexingResolverTypeNodeBuilder<?> currentTypeBuilder =
						currentValueBuilder == null ? rootBuilder : currentValueBuilder.type();

				Optional<? extends PojoImplicitReindexingResolverValueNodeBuilderDelegate<?>> newValueBuilder =
						applyReversePropertyAndExtractors(
								currentTypeBuilder, propertyNodeToReverse, containerElementNode
						);
				if ( newValueBuilder.isPresent() ) {
					currentValueBuilder = newValueBuilder.get();
					checkReverseValueNode( propertyNodeToReverse, currentValueBuilder );
				}
				else {
					// TODO log something? Throw an error?
					throw new IllegalStateException();
				}
			}
		}

		return currentValueBuilder;
	}

	private Optional<? extends PojoImplicitReindexingResolverValueNodeBuilderDelegate<?>> applyReverseProperty(
			PojoImplicitReindexingResolverTypeNodeBuilder<?> builder,
			PojoModelPathPropertyNode<?, ?> propertyNodeToReverse) {
		PojoTypeModel<?> propertyToReverseHolderType = propertyNodeToReverse.parentType().getTypeModel();
		PojoPropertyModel<?> propertyToReverse = propertyNodeToReverse.getPropertyModel();
		return pathReverser.invertPropertyAndExtractors(
				builder, propertyToReverseHolderType, propertyToReverse, ContainerValueExtractorPath.noExtractors()
		);
	}

	private Optional<? extends PojoImplicitReindexingResolverValueNodeBuilderDelegate<?>> applyReversePropertyAndExtractors(
			PojoImplicitReindexingResolverTypeNodeBuilder<?> builder,
			PojoModelPathPropertyNode<?, ?> propertyNodeToReverse,
			PojoModelPathContainerElementNode<?, ?, ?> containerElementNode) {
		PojoTypeModel<?> propertyToReverseHolderType = propertyNodeToReverse.parentType().getTypeModel();
		PojoPropertyModel<?> propertyToReverse = propertyNodeToReverse.getPropertyModel();

		ContainerValueExtractor<?, ?> extractor = containerElementNode.getExtractor();

		return pathReverser.invertPropertyAndExtractors(
				builder, propertyToReverseHolderType, propertyToReverse,
				ContainerValueExtractorPath.explicitExtractors( extractor )
		);
	}

	private void checkReverseValueNode(PojoModelPathPropertyNode<?, ?> propertyNodeToReverse,
			PojoImplicitReindexingResolverValueNodeBuilderDelegate<?> reverseValueBuilder) {
		PojoTypeModel<?> expectedType = propertyNodeToReverse.parent().getTypeModel();
		PojoTypeModel<?> actualType = reverseValueBuilder.type().getType();
		if ( ! expectedType.getRawType().isSubTypeOf( actualType.getRawType() ) ) {
			throw new AssertionFailure(
					"Error while computing the reverse path for " + propertyNodeToReverse +
							"; the result was of type " + actualType
							+ ", but a supertype of " + expectedType + " was expected."
							+ "This is very probably a bug in Hibernate Search, please report it."
			);
		}
	}

}
