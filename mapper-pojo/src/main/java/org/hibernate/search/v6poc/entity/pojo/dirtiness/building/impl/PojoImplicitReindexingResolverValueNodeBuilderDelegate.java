/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.entity.pojo.dirtiness.building.impl;

import java.util.Optional;

import org.hibernate.search.v6poc.entity.pojo.dirtiness.impl.PojoImplicitReindexingResolverTypeNode;
import org.hibernate.search.v6poc.entity.pojo.mapping.building.impl.PojoIndexModelBinder;
import org.hibernate.search.v6poc.entity.pojo.model.path.impl.PojoModelPathValueNode;

class PojoImplicitReindexingResolverValueNodeBuilderDelegate<T> {

	private final PojoModelPathValueNode<?, T> modelPath;
	private final PojoIndexModelBinder indexModelBinder;
	private PojoImplicitReindexingResolverTypeNodeBuilder<T> typeBuilder;
	private boolean markForReindexing = false;

	PojoImplicitReindexingResolverValueNodeBuilderDelegate(PojoModelPathValueNode<?, T> modelPath,
			PojoIndexModelBinder indexModelBinder) {
		this.modelPath = modelPath;
		this.indexModelBinder = indexModelBinder;
	}

	PojoImplicitReindexingResolverTypeNodeBuilder<T> type() {
		if ( typeBuilder == null ) {
			typeBuilder = new PojoImplicitReindexingResolverTypeNodeBuilder<>( modelPath.type(), indexModelBinder );
		}
		return typeBuilder;
	}

	void markForReindexing() {
		markForReindexing = true;
	}

	boolean isMarkForReindexing() {
		return markForReindexing;
	}

	Optional<PojoImplicitReindexingResolverTypeNode<T>> buildTypeNode() {
		if ( typeBuilder == null ) {
			return Optional.empty();
		}
		else {
			return typeBuilder.build();
		}
	}
}
