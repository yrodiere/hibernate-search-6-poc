/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.entity.pojo.model.augmented.impl;

import java.util.Optional;

public class PojoAugmentedValueModel {

	static final PojoAugmentedValueModel EMPTY = new PojoAugmentedValueModel( null );

	private final PojoAssociationInverseSidePath inverseSidePath;

	public PojoAugmentedValueModel(PojoAssociationInverseSidePath inverseSidePath) {
		this.inverseSidePath = inverseSidePath;
	}

	public Optional<PojoAssociationInverseSidePath> getInverseSidePath() {
		return Optional.ofNullable( inverseSidePath );
	}

}
