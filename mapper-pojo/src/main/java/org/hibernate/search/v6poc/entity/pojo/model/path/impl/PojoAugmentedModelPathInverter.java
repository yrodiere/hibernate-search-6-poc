/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.entity.pojo.model.path.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.hibernate.search.v6poc.entity.pojo.extractor.impl.ContainerValueExtractorPath;
import org.hibernate.search.v6poc.entity.pojo.extractor.impl.BoundContainerValueExtractorPath;
import org.hibernate.search.v6poc.entity.pojo.mapping.building.impl.PojoIndexModelBinder;
import org.hibernate.search.v6poc.entity.pojo.model.augmented.building.impl.PojoAugmentedTypeModelProvider;
import org.hibernate.search.v6poc.entity.pojo.model.augmented.impl.PojoAssociationPath;
import org.hibernate.search.v6poc.entity.pojo.model.augmented.impl.PojoAugmentedPropertyModel;
import org.hibernate.search.v6poc.entity.pojo.model.augmented.impl.PojoAugmentedTypeModel;
import org.hibernate.search.v6poc.entity.pojo.model.augmented.impl.PojoAugmentedValueModel;
import org.hibernate.search.v6poc.entity.pojo.model.spi.PojoPropertyModel;
import org.hibernate.search.v6poc.entity.pojo.model.spi.PojoRawTypeModel;
import org.hibernate.search.v6poc.entity.pojo.model.spi.PojoTypeModel;

public class PojoAugmentedModelPathInverter {
	private final PojoAugmentedTypeModelProvider augmentedTypeModelProvider;
	private final PojoIndexModelBinder indexModelBinder;

	public PojoAugmentedModelPathInverter(PojoAugmentedTypeModelProvider augmentedTypeModelProvider,
			PojoIndexModelBinder indexModelBinder) {
		this.augmentedTypeModelProvider = augmentedTypeModelProvider;
		this.indexModelBinder = indexModelBinder;
	}

	public <T> Optional<T> invertPropertyAndExtractors(PojoModelPathValueNodeSelector<T> valueNodeSelector,
			PojoTypeModel<?> propertyToReverseHolderType,
			PojoPropertyModel<?> propertyToReverse, ContainerValueExtractorPath extractorsToReversePath) {
		PojoTypeModel<?> inverseSideHolderTypeModel = valueNodeSelector.getType();
		PojoRawTypeModel<?> inverseSideHolderRawTypeModel = inverseSideHolderTypeModel.getRawType();

		/*
		 * One might refer to an association in multiple ways:
		 * - By intension, e.g. ContainerValueExtractorPath.default()
		 * - By extension, e.g. ContainerValueExtractorPath.noExtractors()
		 *   or ContainerValueExtractorPath.explicitExtractors( ... )
		 * We want to match whatever reference is used, so we have to determine whether this association
		 * uses the default extractor path.
		 */
		List<PojoAssociationPath> associationPathsToMatch = new ArrayList<>();
		associationPathsToMatch.add( new PojoAssociationPath(
				propertyToReverse.getName(), extractorsToReversePath
		) );
		if ( isBoundDefaultExtractorPath( propertyToReverse, extractorsToReversePath ) ) {
			associationPathsToMatch.add( new PojoAssociationPath(
					propertyToReverse.getName(),
					ContainerValueExtractorPath.defaultExtractors()
			) );
		}

		// Try to find inverse side information hosted on the side to inverse
		Optional<PojoAssociationPath> inverseSidePathOptional =
				findInverseSidePathFromSideToInverseModel(
						propertyToReverseHolderType, associationPathsToMatch
				);

		if ( !inverseSidePathOptional.isPresent() ) {
			// Try to find inverse side information hosted on the other side
			inverseSidePathOptional = findInverseSidePathFromInverseSideModel(
					inverseSideHolderRawTypeModel, associationPathsToMatch
			);
		}

		if ( inverseSidePathOptional.isPresent() ) {
			PojoAssociationPath inverseSidePath = inverseSidePathOptional.get();
			String inversePropertyName = inverseSidePath.getPropertyName();
			ContainerValueExtractorPath inverseExtractorPath = inverseSidePath.getExtractorPath();
			return Optional.of( valueNodeSelector.property(
					inverseSideHolderTypeModel.getProperty( inversePropertyName ).getHandle(),
					inverseExtractorPath
			) );
		}

		return Optional.empty();
	}

	private boolean isBoundDefaultExtractorPath(PojoPropertyModel<?> propertyToReverse,
			ContainerValueExtractorPath extractorsToReversePath) {
		Optional<? extends BoundContainerValueExtractorPath<?, ?>> resolvedPathOptional = indexModelBinder
				.tryBindExtractorPath(
						propertyToReverse.getTypeModel(), ContainerValueExtractorPath.defaultExtractors()
				);
		return resolvedPathOptional.isPresent()
				&& extractorsToReversePath.equals( resolvedPathOptional.get().getExtractorPath() );
	}

	private Optional<PojoAssociationPath> findInverseSidePathFromSideToInverseModel(
			PojoTypeModel<?> propertyToReverseHolderType,
			List<PojoAssociationPath> associationPathsToMatch) {
		PojoAugmentedTypeModel augmentedTypeModel =
				augmentedTypeModelProvider.get( propertyToReverseHolderType.getRawType() );
		for ( PojoAssociationPath pathToMatch : associationPathsToMatch ) {
			Optional<PojoAssociationPath> result = augmentedTypeModel.getProperty( pathToMatch.getPropertyName() )
					.getValue( pathToMatch.getExtractorPath() )
					.getInverseSidePath();
			if ( result.isPresent() ) {
				return result;
			}
		}
		return Optional.empty();
	}

	private Optional<PojoAssociationPath> findInverseSidePathFromInverseSideModel(PojoRawTypeModel<?> inverseSideHolderTypeModel,
			List<PojoAssociationPath> associationPathsToMatch) {
		PojoAugmentedTypeModel augmentedInverseSideTypeModel = augmentedTypeModelProvider.get( inverseSideHolderTypeModel );
		for ( PojoAugmentedPropertyModel augmentedPropertyModel :
				augmentedInverseSideTypeModel.getAugmentedProperties() ) {
			for ( PojoAugmentedValueModel augmentedValueModel : augmentedPropertyModel.getAugmentedValues() ) {
				Optional<PojoAssociationPath> candidatePathOptional =
						augmentedValueModel.getInverseSidePath();
				if ( candidatePathOptional.isPresent() ) {
					PojoAssociationPath candidatePath = candidatePathOptional.get();
					if ( associationPathsToMatch.contains( candidatePath ) ) {
						return Optional.of( augmentedValueModel.getPath() );
					}
				}
			}
		}
		return Optional.empty();
	}
}
