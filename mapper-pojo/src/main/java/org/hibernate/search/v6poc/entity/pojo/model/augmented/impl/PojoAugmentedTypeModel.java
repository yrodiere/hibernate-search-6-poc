/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.entity.pojo.model.augmented.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.hibernate.search.v6poc.entity.pojo.model.path.PojoModelPathValueNode;

public class PojoAugmentedTypeModel {
	private final boolean entity;
	private final Map<String, PojoAugmentedPropertyModel> properties;
	private final Map<PojoModelPathValueNode, PojoModelPathValueNode> associationOriginalSidePathToInverseSidePath;
	private final Map<PojoModelPathValueNode, Set<PojoModelPathValueNode>> associationInverseSidePathToOriginalSidePaths;

	public PojoAugmentedTypeModel(boolean entity, Map<String, PojoAugmentedPropertyModel> properties,
			Map<PojoModelPathValueNode, PojoModelPathValueNode> associationOriginalSidePathToInverseSidePath) {
		this.entity = entity;
		this.properties = properties;
		this.associationOriginalSidePathToInverseSidePath = associationOriginalSidePathToInverseSidePath;
		associationInverseSidePathToOriginalSidePaths = new HashMap<>();
		for ( Map.Entry<PojoModelPathValueNode, PojoModelPathValueNode> entry :
				associationOriginalSidePathToInverseSidePath.entrySet() ) {
			associationInverseSidePathToOriginalSidePaths
					.computeIfAbsent( entry.getValue(), ignored -> new HashSet<>() )
					.add( entry.getKey() );
		}
		// Make sure every Set is unmodifiable
		for ( Map.Entry<PojoModelPathValueNode, Set<PojoModelPathValueNode>> entry :
				associationInverseSidePathToOriginalSidePaths.entrySet() ) {
			entry.setValue( Collections.unmodifiableSet( entry.getValue() ) );
		}
	}

	public PojoAugmentedPropertyModel getProperty(String name) {
		return properties.getOrDefault( name, PojoAugmentedPropertyModel.EMPTY );
	}

	/**
	 * Determine whether the given type is an entity type.
	 * <p>
	 * Types marked as entity types are guaranteed by the augmented model contributors
	 * to be the only types that can be the target of an association.
	 * All other types are assumed to only be able to be embedded in other objects,
	 * with their lifecycle completely tied to their embedding object.
	 * As a result, entity types are the only types whose lifecycle events are expected to be sent
	 * to the POJO workers.
	 *
	 * @return {@code true} if this type is an entity type, {@code false} otherwise.
	 */
	public boolean isEntity() {
		return entity;
	}

	/**
	 * @param originalSidePath The path of an association on the original side (from this type).
	 * @return The association inverse side path that has been registered the given original side path,
	 * or an empty optional if there isn't any.
	 */
	public Optional<PojoModelPathValueNode> getAssociationInverseSidePath(PojoModelPathValueNode originalSidePath) {
		return Optional.ofNullable( associationOriginalSidePathToInverseSidePath.get( originalSidePath ) );
	}

	/**
	 * @param inverseSidePath The path of an association on the inverse side (from the target type).
	 * @return The set of association original side paths (paths from this type)
	 * that have been registered the given inverse side path.
	 * A single inverse side path may match multiple original side paths,
	 * if the original sides point to distinct entity types.
	 * This is why we return a Set here instead of a single path.
	 */
	public Set<PojoModelPathValueNode> getAssociationOriginalSidePaths(PojoModelPathValueNode inverseSidePath) {
		return associationInverseSidePathToOriginalSidePaths.getOrDefault( inverseSidePath, Collections.emptySet() );
	}

}
