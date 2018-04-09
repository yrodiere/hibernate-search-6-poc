/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.entity.pojo.dirtiness.building.impl;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;
import java.util.Set;

import org.hibernate.search.v6poc.entity.pojo.extractor.ContainerValueExtractorPath;
import org.hibernate.search.v6poc.entity.pojo.extractor.impl.BoundContainerValueExtractorPath;
import org.hibernate.search.v6poc.entity.pojo.extractor.impl.ContainerValueExtractorBinder;
import org.hibernate.search.v6poc.entity.pojo.model.augmented.building.impl.PojoAugmentedTypeModelProvider;
import org.hibernate.search.v6poc.entity.pojo.model.augmented.impl.PojoAugmentedTypeModel;
import org.hibernate.search.v6poc.entity.pojo.model.path.PojoModelPath;
import org.hibernate.search.v6poc.entity.pojo.model.path.PojoModelPathPropertyNode;
import org.hibernate.search.v6poc.entity.pojo.model.path.PojoModelPathValueNode;
import org.hibernate.search.v6poc.entity.pojo.model.path.impl.BoundPojoModelPathPropertyNode;
import org.hibernate.search.v6poc.entity.pojo.model.path.impl.BoundPojoModelPathTypeNode;
import org.hibernate.search.v6poc.entity.pojo.model.path.impl.BoundPojoModelPathValueNode;
import org.hibernate.search.v6poc.entity.pojo.model.spi.PojoBootstrapIntrospector;
import org.hibernate.search.v6poc.entity.pojo.model.spi.PojoPropertyModel;
import org.hibernate.search.v6poc.entity.pojo.model.spi.PojoRawTypeModel;
import org.hibernate.search.v6poc.entity.pojo.model.spi.PojoTypeModel;

/**
 * An object responsible for inverting an association path,
 * i.e. a chain of properties and container value extractors going from one entity to another.
 */
public final class PojoAssociationPathInverter {
	private final PojoAugmentedTypeModelProvider augmentedTypeModelProvider;
	private final PojoBootstrapIntrospector introspector;
	private final ContainerValueExtractorBinder extractorBinder;

	public PojoAssociationPathInverter(PojoAugmentedTypeModelProvider augmentedTypeModelProvider,
			PojoBootstrapIntrospector introspector,
			ContainerValueExtractorBinder extractorBinder) {
		this.augmentedTypeModelProvider = augmentedTypeModelProvider;
		this.introspector = introspector;
		this.extractorBinder = extractorBinder;
	}

	public Optional<PojoModelPathValueNode> invertPath(PojoTypeModel<?> inverseSideEntityType,
			BoundPojoModelPathValueNode<?, ?, ?> pathToInvert) {
		PojoRawTypeModel<?> originalSideEntityType = pathToInvert.rootType().getRawType();

		List<PojoModelPathValueNode> associationPathsToMatch = computeAssociationPathsToMatch( pathToInvert );

		// Try to find inverse side information hosted on the side to inverse
		Optional<PojoModelPathValueNode> inverseSidePathOptional =
				findInverseSidePathFromOriginalSide( originalSideEntityType, associationPathsToMatch );

		if ( !inverseSidePathOptional.isPresent() ) {
			// Try to find inverse side information hosted on the other side
			inverseSidePathOptional = findInverseSidePathFromInverseSide(
					inverseSideEntityType, originalSideEntityType, associationPathsToMatch
			);
		}

		return inverseSidePathOptional;
	}

	/*
	 * One might refer to the extractor path of an association in multiple ways:
	 * - By intension, e.g. ContainerValueExtractorPath.default()
	 * - By extension, e.g. ContainerValueExtractorPath.noExtractors()
	 *   or ContainerValueExtractorPath.explicitExtractors( ... )
	 * We want to match any type of reference, so we have to determine whether this association
	 * uses the default extractor path, and if so, add it to the list of paths to match.
	 */
	private List<PojoModelPathValueNode> computeAssociationPathsToMatch(
			BoundPojoModelPathValueNode<?, ?, ?> boundPathToInvert) {
		// We're potentially performing lots of insertions, so let's use a LinkedList
		List<PojoModelPathValueNode> associationPathsToMatch = new LinkedList<>();
		collectAssociationPathsToMatch( associationPathsToMatch, boundPathToInvert );
		return associationPathsToMatch;
	}

	private void collectAssociationPathsToMatch(
			List<PojoModelPathValueNode> associationPathsToMatch,
			BoundPojoModelPathValueNode<?, ?, ?> boundPathToInvert) {
		BoundPojoModelPathPropertyNode<?, ?> parentPath = boundPathToInvert.parent();
		BoundPojoModelPathValueNode<?, ?, ?> parentValuePath = parentPath.parent().parent();
		String propertyName = parentPath.getPropertyHandle().getName();
		ContainerValueExtractorPath extractorPath = boundPathToInvert.getExtractorPath();
		boolean isDefaultExtractorPath = isDefaultExtractorPath(
				parentPath.getPropertyModel(), boundPathToInvert.getBoundExtractorPath()
		);
		if ( parentValuePath != null ) {
			collectAssociationPathsToMatch( associationPathsToMatch, parentValuePath );
			ListIterator<PojoModelPathValueNode> iterator = associationPathsToMatch.listIterator();
			while ( iterator.hasNext() ) {
				PojoModelPathValueNode baseValuePath = iterator.next();
				PojoModelPathPropertyNode basePropertyPath = baseValuePath.property( propertyName );
				// Append the property and extractor path to the already-collected paths
				iterator.set( basePropertyPath.value( extractorPath ) );
				if ( isDefaultExtractorPath ) {
					/*
					 * If the current extractor path (which is explicit) represents the default path,
					 * then for each already collected path, add one version using the explicit representation,
					 * and one version using the implicit representation.
					 */
					iterator.add( basePropertyPath.value( ContainerValueExtractorPath.defaultExtractors() ) );
				}
			}
		}
		else {
			// We reached the root: collect the first paths
			PojoModelPathPropertyNode basePropertyPath = PojoModelPath.fromRoot( propertyName );
			associationPathsToMatch.add( basePropertyPath.value( extractorPath ) );
			if ( isDefaultExtractorPath ) {
				// The may be two versions of this path, similarly to what we do above
				associationPathsToMatch.add( basePropertyPath.value( ContainerValueExtractorPath.defaultExtractors() ) );
			}
		}
	}

	private boolean isDefaultExtractorPath(PojoPropertyModel<?> propertyModel,
			BoundContainerValueExtractorPath<?, ?> originalSideBoundExtractorPath) {
		Optional<? extends BoundContainerValueExtractorPath<?, ?>> boundDefaultExtractorPathOptional = extractorBinder
				.tryBindPath(
						introspector, propertyModel.getTypeModel(),
						ContainerValueExtractorPath.defaultExtractors()
				);
		return boundDefaultExtractorPathOptional.isPresent() && originalSideBoundExtractorPath.getExtractorPath().equals(
				boundDefaultExtractorPathOptional.get().getExtractorPath()
		);
	}

	private Optional<PojoModelPathValueNode> findInverseSidePathFromOriginalSide(
			PojoRawTypeModel<?> originalSideEntityType, List<PojoModelPathValueNode> associationPathsToMatch) {
		PojoAugmentedTypeModel augmentedOriginalSideTypeModel =
				augmentedTypeModelProvider.get( originalSideEntityType );

		for ( PojoModelPathValueNode associationPathToMatch : associationPathsToMatch ) {
			Optional<PojoModelPathValueNode> result =
					augmentedOriginalSideTypeModel.getAssociationInverseSidePath( associationPathToMatch );
			if ( result.isPresent() ) {
				return result;
			}
		}

		return Optional.empty();
	}

	private Optional<PojoModelPathValueNode> findInverseSidePathFromInverseSide(
			PojoTypeModel<?> inverseSideTypeModel,
			PojoRawTypeModel<?> originalSideEntityType,
			List<PojoModelPathValueNode> associationPathsToMatch) {
		BoundPojoModelPathTypeNode<?> inverseSidePathRootTypeNode = BoundPojoModelPathTypeNode.root( inverseSideTypeModel );
		PojoAugmentedTypeModel augmentedInverseSideTypeModel =
				augmentedTypeModelProvider.get( inverseSideTypeModel.getRawType() );

		for ( PojoModelPathValueNode associationPathToMatch : associationPathsToMatch ) {
			Set<PojoModelPathValueNode> candidateInverseSidePaths =
					augmentedInverseSideTypeModel.getAssociationOriginalSidePaths( associationPathToMatch );
			for ( PojoModelPathValueNode candidateInverseSidePath : candidateInverseSidePaths ) {
				/*
				 * In order to match, the inverse path, when applied to the inverse entity type,
				 * must also result in a supertype of the entity type holding the association to invert.
				 * This is to handle cases where an entity holds inverse associations of multiple associations
				 * from multiple different entities: in that case, the "original" associations may have
				 * the same name and extractors.
				 */
				BoundPojoModelPathValueNode<?, ?, ?> boundInverseSidePath =
						applyPath( inverseSidePathRootTypeNode, candidateInverseSidePath );
				PojoRawTypeModel<?> rawExtractedTypeModel = boundInverseSidePath.type().getTypeModel().getRawType();
				if ( originalSideEntityType.isSubTypeOf( rawExtractedTypeModel ) ) {
					return Optional.of( candidateInverseSidePath );
				}
			}
		}

		return Optional.empty();
	}

	private BoundPojoModelPathValueNode<?, ?, ?> applyPath(
			BoundPojoModelPathTypeNode<?> root, PojoModelPathValueNode source) {
		BoundPojoModelPathPropertyNode<?, ?> propertyNode = applyPath( root, source.parent() );
		ContainerValueExtractorPath inverseExtractorPath = source.getExtractorPath();
		return bindExtractors( propertyNode, inverseExtractorPath );
	}

	private BoundPojoModelPathPropertyNode<?, ?> applyPath(
			BoundPojoModelPathTypeNode<?> root, PojoModelPathPropertyNode source) {
		PojoModelPathValueNode sourceParent = source.parent();
		BoundPojoModelPathTypeNode<?> targetParent;
		if ( sourceParent != null ) {
			targetParent = applyPath( root, sourceParent ).type();
		}
		else {
			targetParent = root;
		}
		PojoPropertyModel<?> propertyModel = targetParent.getTypeModel().getProperty( source.getPropertyName() );
		return targetParent.property( propertyModel.getHandle() );
	}

	private <P> BoundPojoModelPathValueNode<?, P, ?> bindExtractors(
			BoundPojoModelPathPropertyNode<?, P> propertyNode,
			ContainerValueExtractorPath extractorPath) {
		BoundContainerValueExtractorPath<P, ?> resolvedExtractorPath =
				extractorBinder.bindPath(
						introspector, propertyNode.getPropertyModel().getTypeModel(),
						extractorPath
				);
		return propertyNode.value( resolvedExtractorPath );
	}
}
