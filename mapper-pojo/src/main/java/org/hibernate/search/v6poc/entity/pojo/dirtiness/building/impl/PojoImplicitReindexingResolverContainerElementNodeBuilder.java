/*
 * Hibernate Search, full-text search for your domain model
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.search.v6poc.entity.pojo.dirtiness.building.impl;

import java.util.Optional;

import org.hibernate.search.v6poc.entity.pojo.dirtiness.impl.PojoImplicitReindexingResolverContainerElementNode;
import org.hibernate.search.v6poc.entity.pojo.dirtiness.impl.PojoImplicitReindexingResolverTypeNode;
import org.hibernate.search.v6poc.entity.pojo.mapping.building.impl.PojoIndexModelBinder;
import org.hibernate.search.v6poc.entity.pojo.model.path.impl.PojoModelPathContainerElementNode;

class PojoImplicitReindexingResolverContainerElementNodeBuilder<P, T> extends
		AbstractPojoImplicitReindexingResolverNodeBuilder {

	private final PojoModelPathContainerElementNode<?, P, T> modelPath;
	private final PojoImplicitReindexingResolverValueNodeBuilderDelegate<T> valueBuilderDelegate;

	PojoImplicitReindexingResolverContainerElementNodeBuilder(PojoModelPathContainerElementNode<?, P, T> modelPath,
			PojoIndexModelBinder indexModelBinder) {
		super( indexModelBinder );
		this.modelPath = modelPath;
		this.valueBuilderDelegate =
				new PojoImplicitReindexingResolverValueNodeBuilderDelegate<>( modelPath, indexModelBinder );
	}

	@Override
	PojoModelPathContainerElementNode<?, P, T> getModelPath() {
		return modelPath;
	}

	PojoImplicitReindexingResolverValueNodeBuilderDelegate<?> value() {
		return valueBuilderDelegate;
	}

	Optional<PojoImplicitReindexingResolverContainerElementNode<P, T>> build() {
		boolean markForReindexing = valueBuilderDelegate.isMarkForReindexing();
		Optional<? extends PojoImplicitReindexingResolverTypeNode<T>> valueTypeNode =
				valueBuilderDelegate.buildTypeNode();

		if ( !markForReindexing && !valueTypeNode.isPresent() ) {
			/*
			 * If this resolver doesn't mark the value for reindexing and doesn't have any nested node,
			 * it is useless and we don't need to build it.
			 */
			return Optional.empty();
		}
		else {
			return Optional.of( new PojoImplicitReindexingResolverContainerElementNode<>(
					modelPath.getExtractor(), markForReindexing, valueTypeNode.orElse( null )
			) );
		}
	}
}
